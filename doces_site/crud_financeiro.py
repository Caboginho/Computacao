from db import get_connection

def registrar_lancamento(pedido_id, tipo, valor, descricao):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("""
        INSERT INTO financeiro (pedido_id, tipo, valor, descricao)
        VALUES (?, ?, ?, ?)
    """, (pedido_id, tipo, valor, descricao))
    conn.commit()
    conn.close()

def listar_lancamentos():
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("SELECT * FROM financeiro")
    lancamentos = cur.fetchall()
    conn.close()
    return lancamentos

def buscar_lancamento_por_id(lancamento_id):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("SELECT * FROM financeiro WHERE id = ?", (lancamento_id,))
    lancamento = cur.fetchone()
    conn.close()
    return lancamento

def atualizar_lancamento(lancamento_id, tipo=None, valor=None, descricao=None):
    conn = get_connection()
    cur = conn.cursor()
    campos = []
    valores = []
    if tipo: 
        campos.append("tipo = ?")
        valores.append(tipo)
    if valor is not None: 
        campos.append("valor = ?")
        valores.append(valor)
    if descricao: 
        campos.append("descricao = ?")
        valores.append(descricao)

    valores.append(lancamento_id)
    sql = f"UPDATE financeiro SET {', '.join(campos)} WHERE id = ?"
    cur.execute(sql, valores)
    conn.commit()
    conn.close()

def deletar_lancamento(lancamento_id):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("DELETE FROM financeiro WHERE id = ?", (lancamento_id,))
    conn.commit()
    conn.close()
