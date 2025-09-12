from db import get_connection

# CREATE
def criar_usuario(nome, email, senha, tipo="cliente"):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("""
        INSERT INTO usuarios (nome, email, senha, tipo) VALUES (?, ?, ?, ?)
    """, (nome, email, senha, tipo))
    conn.commit()
    conn.close()

# READ
def listar_usuarios():
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("SELECT id, nome, email, tipo, criado_em FROM usuarios")
    usuarios = cur.fetchall()
    conn.close()
    return usuarios

def buscar_usuario_por_id(usuario_id):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("SELECT * FROM usuarios WHERE id = ?", (usuario_id,))
    usuario = cur.fetchone()
    conn.close()
    return usuario

# UPDATE
def atualizar_usuario(usuario_id, nome=None, email=None, senha=None, tipo=None):
    conn = get_connection()
    cur = conn.cursor()
    campos = []
    valores = []
    if nome: 
        campos.append("nome = ?")
        valores.append(nome)
    if email: 
        campos.append("email = ?")
        valores.append(email)
    if senha: 
        campos.append("senha = ?")
        valores.append(senha)
    if tipo: 
        campos.append("tipo = ?")
        valores.append(tipo)

    valores.append(usuario_id)
    sql = f"UPDATE usuarios SET {', '.join(campos)} WHERE id = ?"
    cur.execute(sql, valores)
    conn.commit()
    conn.close()

# DELETE
def deletar_usuario(usuario_id):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("DELETE FROM usuarios WHERE id = ?", (usuario_id,))
    conn.commit()
    conn.close()
