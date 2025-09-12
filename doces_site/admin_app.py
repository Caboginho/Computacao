from flask import Flask, render_template, request, redirect, url_for
import crud_usuarios
import crud_produtos
import crud_pedidos
import crud_financeiro

app = Flask(__name__)

# Página inicial do admin
@app.route("/")
def dashboard():
    usuarios = crud_usuarios.listar_usuarios()
    produtos = crud_produtos.listar_produtos()
    pedidos = crud_pedidos.listar_pedidos()
    lancamentos = crud_financeiro.listar_lancamentos()

    total_receitas = sum(l[3] for l in lancamentos if l[2] == "receita")
    total_despesas = sum(l[3] for l in lancamentos if l[2] == "despesa")

    return render_template("admin/dashboard.html",
                           usuarios=usuarios,
                           produtos=produtos,
                           pedidos=pedidos,
                           receitas=total_receitas,
                           despesas=total_despesas)

# ======================
# Usuários
# ======================
@app.route("/usuarios")
def listar_usuarios():
    usuarios = crud_usuarios.listar_usuarios()
    return render_template("admin/usuarios.html", usuarios=usuarios)

# ======================
# Produtos
# ======================
@app.route("/produtos")
def listar_produtos():
    produtos = crud_produtos.listar_produtos()
    return render_template("admin/produtos.html", produtos=produtos)

@app.route("/produtos/adicionar", methods=["POST"])
def adicionar_produto():
    nome = request.form["nome"]
    descricao = request.form["descricao"]
    preco = float(request.form["preco"])
    estoque = int(request.form["estoque"])
    crud_produtos.criar_produto(nome, descricao, preco, estoque)
    return redirect(url_for("listar_produtos"))

# ======================
# Pedidos
# ======================
@app.route("/pedidos")
def listar_pedidos():
    pedidos = crud_pedidos.listar_pedidos()
    return render_template("admin/pedidos.html", pedidos=pedidos)

@app.route("/pedidos/<int:pedido_id>/status", methods=["POST"])
def atualizar_status_pedido(pedido_id):
    novo_status = request.form["status"]
    crud_pedidos.atualizar_pedido(pedido_id, status=novo_status)
    return redirect(url_for("listar_pedidos"))

# ======================
# Financeiro
# ======================
@app.route("/financeiro")
def listar_financeiro():
    lancamentos = crud_financeiro.listar_lancamentos()
    return render_template("admin/financeiro.html", lancamentos=lancamentos)

if __name__ == "__main__":
    app.run(debug=True, port=5001)
