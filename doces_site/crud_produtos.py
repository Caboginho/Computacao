from db import get_connection

def criar_produto(nome, descricao, preco, estoque, imagem=None):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("""
        INSERT INTO produtos (nome, descricao, preco, estoque, imagem)
        VALUES (?, ?, ?, ?, ?)
    """, (nome, descricao, preco, estoque, imagem))
    conn.commit()
    conn.close()

def listar_produtos():
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("SELECT * FROM produtos")
    produtos = cur.fetchall()
    conn.close()
    return produtos

def buscar_produto_por_id(produto_id):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("SELECT * FROM produtos WHERE id = ?", (produto_id,))
    produto = cur.fetchone()
    conn.close()
    return produto

def atualizar_produto(produto_id, nome=None, descricao=None, preco=None, estoque=None, imagem=None):
    conn = get_connection()
    cur = conn.cursor()
    campos = []
    valores = []
    if nome: 
        campos.append("nome = ?")
        valores.append(nome)
    if descricao: 
        campos.append("descricao = ?")
        valores.append(descricao)
    if preco is not None: 
        campos.append("preco = ?")
        valores.append(preco)
    if estoque is not None: 
        campos.append("estoque = ?")
        valores.append(estoque)
    if imagem: 
        campos.append("imagem = ?")
        valores.append(imagem)

    valores.append(produto_id)
    sql = f"UPDATE produtos SET {', '.join(campos)} WHERE id = ?"
    cur.execute(sql, valores)
    conn.commit()
    conn.close()

def deletar_produto(produto_id):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("DELETE FROM produtos WHERE id = ?", (produto_id,))
    conn.commit()
    conn.close()
