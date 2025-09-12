from db import get_connection

def adicionar_item(pedido_id, produto_id, quantidade, preco_unitario):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("""
        INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, preco_unitario)
        VALUES (?, ?, ?, ?)
    """, (pedido_id, produto_id, quantidade, preco_unitario))
    conn.commit()
    conn.close()

def listar_itens(pedido_id):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("SELECT * FROM itens_pedido WHERE pedido_id = ?", (pedido_id,))
    itens = cur.fetchall()
    conn.close()
    return itens

def atualizar_item(item_id, quantidade=None, preco_unitario=None):
    conn = get_connection()
    cur = conn.cursor()
    campos = []
    valores = []
    if quantidade is not None: 
        campos.append("quantidade = ?")
        valores.append(quantidade)
    if preco_unitario is not None: 
        campos.append("preco_unitario = ?")
        valores.append(preco_unitario)

    valores.append(item_id)
    sql = f"UPDATE itens_pedido SET {', '.join(campos)} WHERE id = ?"
    cur.execute(sql, valores)
    conn.commit()
    conn.close()

def deletar_item(item_id):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("DELETE FROM itens_pedido WHERE id = ?", (item_id,))
    conn.commit()
    conn.close()
