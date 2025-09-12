import os, json, io
from flask import Flask, render_template, request, redirect, url_for, jsonify, session, flash, send_file
from models import db, Product, Customer, Order, AccessLog
from datetime import datetime
from functools import wraps
from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
# gspread/google optional, mantidos no requirements (ativa com variáveis de ambiente)
import gspread
from google.oauth2.service_account import Credentials

# CONFIG
app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = os.environ.get('DATABASE_URL', 'sqlite:///dubom.db')
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.secret_key = os.environ.get('DUBOM_SECRET_KEY', 'troque_esta_chave_para_producao')
ADMIN_PASS = os.environ.get('DUBOM_ADMIN_PASS','admin123')

db.init_app(app)

# Optional: Google Sheets config (set path to service account json in env GOOGLE_SA_FILE)
GSHEET_ENABLED = bool(os.environ.get('GOOGLE_SA_FILE'))
GSHEET_SPREADSHEET = os.environ.get('GOOGLE_SHEET_NAME', 'dubom-orders')

def log_access(request, order_id=None):
    try:
        ip = request.headers.get('X-Forwarded-For', request.remote_addr)
        # alguns proxies/serviços podem passar headers com localização; você pode preencher via API depois
        location = request.headers.get('X-AppEngine-City') or request.headers.get('X-Geo-Location') or None
        a = AccessLog(ip=ip, path=request.path, method=request.method, location=location, order_id=order_id)
        db.session.add(a); db.session.commit()
    except Exception as e:
        app.logger.debug('log access failed: %s', e)

# admin_required decorator
def admin_required(f):
    @wraps(f)
    def wrap(*args, **kwargs):
        if session.get("admin"):
            return f(*args, **kwargs)
        return redirect(url_for("admin_login"))
    return wrap

# Public pages
@app.route('/')
def index():
    log_access(request)
    products = Product.query.order_by(Product.id.desc()).all()
    return render_template('index.html', products=products)

@app.route('/about')
def about():
    return render_template('about.html')

@app.route('/contact')
def contact():
    return render_template('contact.html')

@app.route('/product/<int:pid>')
def product_view(pid):
    log_access(request)
    p = Product.query.get_or_404(pid)
    return render_template('product.html', product=p)

@app.route('/api/products')
def api_products():
    ps = Product.query.all()
    return jsonify([{
        'id':p.id,'name':p.name,'price':p.price,'image':p.image,
        'marketplace_shopee':p.marketplace_shopee,'marketplace_mercadolivre':p.marketplace_mercadolivre
        } for p in ps])

@app.route('/cart', methods=['POST'])
def cart():
    log_access(request)
    data = request.json
    customer = data.get('customer', {})
    items = data.get('items', [])
    # save or get customer
    c = None
    if customer.get('email'):
        c = Customer.query.filter_by(email=customer.get('email')).first()
    if not c:
        c = Customer(name=customer.get('name') or 'Cliente sem nome', email=customer.get('email'))
        db.session.add(c); db.session.commit()
    # calculate total
    total = 0
    for it in items:
        p = Product.query.get(int(it['product_id']))
        if p:
            total += p.price * int(it.get('qty',1))
    order = Order(customer_id=c.id, items_json=json.dumps(items), total=total)
    db.session.add(order); db.session.commit()
    # log access with order id
    try:
        log_access(request, order_id=order.id)
    except:
        pass
    # send to google sheets if enabled
    if GSHEET_ENABLED:
        try:
            creds = Credentials.from_service_account_file(os.environ.get('GOOGLE_SA_FILE'), scopes=['https://www.googleapis.com/auth/spreadsheets'])
            gc = gspread.authorize(creds)
            sh = gc.open(GSHEET_SPREADSHEET)
            ws = sh.sheet1
            ws.append_row([str(datetime.utcnow()), c.name, c.email or '', str(items), str(total)])
        except Exception as e:
            app.logger.warning('gspread failed: %s', e)
    # build redirects list (per-product marketplace links)
    redirects = []
    for it in items:
        p = Product.query.get(int(it['product_id']))
        redirects.append({
            'product_id': p.id,
            'marketplace_shopee': p.marketplace_shopee,
            'marketplace_mercadolivre': p.marketplace_mercadolivre
        })
    return jsonify({'order_id': order.id, 'total': total, 'redirects': redirects})

@app.route('/quote/pdf/<int:order_id>')
def quote_pdf(order_id):
    # generate simple PDF for the order
    order = Order.query.get_or_404(order_id)
    customer = Customer.query.get(order.customer_id)
    buf = io.BytesIO()
    c = canvas.Canvas(buf, pagesize=A4)
    c.setFont('Helvetica-Bold', 16)
    c.drawString(40, 820, f'Dubom - Orçamento #{order.id}')
    c.setFont('Helvetica', 12)
    c.drawString(40, 800, f'Data: {order.created_at.strftime("%Y-%m-%d %H:%M") }')
    c.drawString(40, 780, f'Cliente: {customer.name} - {customer.email or "-"}')
    c.drawString(40, 760, 'Itens:')
    y = 740
    items = json.loads(order.items_json or '[]')
    for it in items:
        p = Product.query.get(int(it['product_id']))
        line = f'{p.name} x{it.get("qty",1)} - R$ {p.price:.2f} cada'
        c.drawString(60, y, line)
        y -= 18
        if y < 60:
            c.showPage()
            y = 800
    c.drawString(40, y-10, f'Total: R$ {order.total:.2f}')
    c.showPage()
    c.save()
    buf.seek(0)
    return send_file(buf, download_name=f'orcamento_{order.id}.pdf', as_attachment=True)

# Admin
@app.route('/admin/login', methods=['GET','POST'])
def admin_login():
    if request.method == 'POST':
        pw = request.form.get('password')
        if pw == ADMIN_PASS:
            session['admin'] = True
            return redirect(url_for('admin_products'))
        else:
            flash('Senha incorreta', 'danger')
    return render_template('admin/login.html')

@app.route('/admin/logout')
def admin_logout():
    session.pop('admin', None)
    return redirect(url_for('index'))

@app.route('/admin/products')
@admin_required
def admin_products():
    products = Product.query.order_by(Product.id.desc()).all()
    return render_template('admin/products.html', products=products)

@app.route('/admin/products/create', methods=['POST'])
@admin_required
def create_product():
    data = request.form
    p = Product(
        name=data.get('name'), # type: ignore
        price=float(data.get('price') or 0), # type: ignore
        description=data.get('description'), # pyright: ignore[reportCallIssue]
        image=data.get('image'), # type: ignore
        marketplace_shopee=data.get('marketplace_shopee'), # type: ignore
        marketplace_mercadolivre=data.get('marketplace_mercadolivre'), # type: ignore
    )
    db.session.add(p); db.session.commit()
    return redirect(url_for('admin_products'))

@app.route('/admin/products/update/<int:pid>', methods=['POST'])
@admin_required
def update_product(pid):
    p = Product.query.get_or_404(pid)
    data = request.form
    p.name = data.get('name')
    p.price = float(data.get('price') or 0)
    p.description = data.get('description')
    p.image = data.get('image')
    p.marketplace_shopee = data.get('marketplace_shopee')
    p.marketplace_mercadolivre = data.get('marketplace_mercadolivre')
    db.session.commit()
    return redirect(url_for('admin_products'))

@app.route('/admin/customers')
@admin_required
def admin_customers():
    customers = Customer.query.order_by(Customer.created_at.desc()).all()
    return render_template('admin/customers.html', customers=customers)

@app.route('/admin/orders')
@admin_required
def admin_orders():
    orders = Order.query.order_by(Order.created_at.desc()).all()
    return render_template('admin/orders.html', orders=orders)

@app.route('/admin/dashboard')
@admin_required
def admin_dashboard():
    total_orders = Order.query.count()
    total_customers = Customer.query.count()
    revenue = db.session.query(db.func.sum(Order.total)).scalar() or 0
    # top products (by quantity)
    prod_counts = {}
    for o in Order.query.all():
        try:
            items = json.loads(o.items_json or '[]')
            for it in items:
                pid = int(it['product_id'])
                qty = int(it.get('qty',1))
                prod_counts[pid] = prod_counts.get(pid,0) + qty
        except: pass
    top = sorted(prod_counts.items(), key=lambda x: x[1], reverse=True)[:10]
    top_products = [{'product': Product.query.get(pid).name if Product.query.get(pid) else '—', 'qty': qty} for pid, qty in top] # type: ignore
    logs = AccessLog.query.order_by(AccessLog.created_at.desc()).limit(200).all()
    return render_template('admin/dashboard.html', orders=total_orders, customers=total_customers, revenue=revenue, top_products=top_products, logs=logs)

@app.route('/admin/products/delete/<int:pid>', methods=['POST'])
@admin_required
def delete_product(pid):
    p = Product.query.get_or_404(pid)
    try:
        db.session.delete(p)
        db.session.commit()
        flash(f'Produto "{p.name}" deletado com sucesso.', 'success')
    except Exception as e:
        db.session.rollback()
        flash(f'Erro ao deletar produto: {e}', 'danger')
    return redirect(url_for('admin_products'))

if __name__ == '__main__':
    # ensure tables exist on startup (Flask 3 compatibility)
    with app.app_context():
        db.create_all()
        # seed example product only if none exist
        if Product.query.count() == 0:
            p = Product(name="Brigadeiro Gourmet (caixa 12)", price=35.0,
                        description="Brigadeiros artesanais - caixa com 12 unidades",
                        image="https://receitasdebemcasado.com/wp-content/uploads/2025/05/Brigadeiro.png",
                        marketplace_shopee="https://shopee.exemplo/produto123",
                        marketplace_mercadolivre="https://mercadolivre.exemplo/produto123")
            db.session.add(p); db.session.commit()
    app.run(host='0.0.0.0', port=int(os.environ.get('PORT', 5000)), debug=bool(os.environ.get('FLASK_DEBUG', True)))
