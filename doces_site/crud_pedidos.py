from db import get_connection

def criar_pedido(usuario_id, status, total):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("INSERT INTO pedidos (usuario_id, status, total) VALUES (?, ?, ?)", (usuario_id, status, total))
    conn.commit()
    pedido_id = cur.lastrowid
    conn.close()
    return pedido_id

def listar_pedidos():
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("SELECT * FROM pedidos")
    pedidos = cur.fetchall()
    conn.close()
    return pedidos

def buscar_pedido_por_id(pedido_id):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("SELECT * FROM pedidos WHERE id = ?", (pedido_id,))
    pedido = cur.fetchone()
    conn.close()
    return pedido

def atualizar_pedido(pedido_id, status=None, total=None):
    conn = get_connection()
    cur = conn.cursor()
    campos = []
    valores = []
    if status: 
        campos.append("status = ?")
        valores.append(status)
    if total is not None: 
        campos.append("total = ?")
        valores.append(total)

    valores.append(pedido_id)
    sql = f"UPDATE pedidos SET {', '.join(campos)} WHERE id = ?"
    cur.execute(sql, valores)
    conn.commit()
    conn.close()

def deletar_pedido(pedido_id):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("DELETE FROM pedidos WHERE id = ?", (pedido_id,))
    conn.commit()
    conn.close()
