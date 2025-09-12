from flask import Flask, render_template, request, redirect, url_for, session
import sqlite3

app = Flask(__name__)
app.secret_key = "sua_chave_secreta"

DB_PATH = "dubom.db"

# -----------------------
# Função para conectar ao banco
# -----------------------
def get_db_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

# -----------------------
# ROTAS PÚBLICAS
# -----------------------
@app.route("/")
def index():
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute("SELECT id, nome, descricao, preco, imagem FROM produtos WHERE estoque > 0")
    produtos = cur.fetchall()
    conn.close()
    return render_template("produtos.html", produtos=produtos, usuario_nome=session.get("usuario_nome"), mostrar_carrinho=False)

@app.route("/produto/<int:produto_id>")
def produto(produto_id):
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute("SELECT * FROM produtos WHERE id = ?", (produto_id,))
    produto = cur.fetchone()
    conn.close()
    if produto:
        return render_template("produto.html", produto=produto)
    return redirect(url_for("index"))

@app.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        usuario = request.form["usuario"]
        senha = request.form["senha"]

        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT * FROM usuarios WHERE usuario = ? AND senha = ?", (usuario, senha))
        user = cur.fetchone()
        conn.close()

        if user:
            session["usuario_nome"] = user["usuario"]
            session["perfil"] = user["perfil"]
            if user["perfil"] == "admin":
                return redirect(url_for("admin_dashboard"))
            else:
                return redirect(url_for("cliente_dashboard"))
    return render_template("login.html")

@app.route("/registrar", methods=["GET", "POST"])
def registrar():
    if request.method == "POST":
        usuario = request.form["usuario"]
        senha = request.form["senha"]
        perfil = "cliente"

        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("INSERT INTO usuarios (usuario, senha, perfil) VALUES (?, ?, ?)", (usuario, senha, perfil))
        conn.commit()
        conn.close()

        return redirect(url_for("login"))
    return render_template("registrar.html")

# -----------------------
# ROTAS ADMIN
# -----------------------
@app.route("/admin")
def admin_dashboard():
    if "perfil" in session and session["perfil"] == "admin":
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT COUNT(*) FROM produtos")
        total_produtos = cur.fetchone()[0]
        cur.execute("SELECT COUNT(*) FROM pedidos")
        total_pedidos = cur.fetchone()[0]
        conn.close()
        return render_template("admin/dashboard.html",
                               nome=session["usuario_nome"],
                               total_produtos=total_produtos,
                               total_pedidos=total_pedidos)
    return redirect(url_for("login"))

@app.route("/admin/produtos")
def gerenciar_produtos():
    if "perfil" in session and session["perfil"] == "admin":
        conn = get_db_connection()
        produtos = conn.execute("SELECT * FROM produtos").fetchall()
        conn.close()
        return render_template("admin/gerenciar_produtos.html", produtos=produtos)
    return redirect(url_for("login"))

@app.route("/admin/vendas")
def gerenciar_vendas():
    if "perfil" in session and session["perfil"] == "admin":
        # Placeholder: buscar vendas/pedidos no banco
        pedidos = []
        return render_template("admin/gerenciar_vendas.html", pedidos=pedidos)
    return redirect(url_for("login"))

@app.route("/admin/historico")
def historico():
    if "perfil" in session and session["perfil"] == "admin":
        # Placeholder para histórico de vendas
        return render_template("admin/historico.html")
    return redirect(url_for("login"))

# -----------------------
# ROTAS CLIENTE
# -----------------------
@app.route("/cliente")
def cliente_dashboard():
    if "perfil" in session and session["perfil"] == "cliente":
        conn = get_db_connection()
        produtos = conn.execute("SELECT * FROM produtos WHERE estoque > 0").fetchall()
        conn.close()
        return render_template("produtos.html", produtos=produtos,
                               usuario_nome=session["usuario_nome"],
                               mostrar_carrinho=True)
    return redirect(url_for("login"))

@app.route("/carrinho")
def ver_carrinho():
    if "perfil" in session and session["perfil"] == "cliente":
        carrinho = session.get("carrinho", [])
        return render_template("carrinho.html", carrinho=carrinho)
    return redirect(url_for("login"))

@app.route("/carrinho/adicionar/<int:produto_id>", methods=["POST"])
def adicionar_carrinho(produto_id):
    if "perfil" in session and session["perfil"] == "cliente":
        carrinho = session.get("carrinho", [])
        carrinho.append(produto_id)
        session["carrinho"] = carrinho
        return redirect(url_for("cliente_dashboard"))
    return redirect(url_for("login"))

# -----------------------
# LOGOUT
# -----------------------
@app.route("/logout")
def logout():
    session.clear()
    return redirect(url_for("login"))

# -----------------------
# EXECUÇÃO
# -----------------------
if __name__ == "__main__":
    app.run()
