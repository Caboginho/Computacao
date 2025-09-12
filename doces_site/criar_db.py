import sqlite3

def criar_db():
    conn = sqlite3.connect("dubom.db")
    cursor = conn.cursor()

    # =========================
    # Tabelas
    # =========================

    # Tabela de usuários
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS usuarios (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nome TEXT NOT NULL,
        email TEXT UNIQUE NOT NULL,
        senha TEXT NOT NULL,
        perfil TEXT NOT NULL CHECK(perfil IN ('cliente', 'admin')),
        criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    """)

    # Tabela de produtos
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS produtos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nome TEXT NOT NULL,
        descricao TEXT,
        preco REAL NOT NULL,
        imagem TEXT,
        estoque INTEGER NOT NULL DEFAULT 0,
        criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    """)

    # Tabela de pedidos
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS pedidos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        usuario_id INTEGER NOT NULL,
        status TEXT NOT NULL CHECK(status IN ('orcado', 'aguardando_pagamento', 'pago', 'enviado', 'concluido')),
        total REAL NOT NULL,
        criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    );
    """)

    # Itens do pedido
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS itens_pedido (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        pedido_id INTEGER NOT NULL,
        produto_id INTEGER NOT NULL,
        quantidade INTEGER NOT NULL,
        preco_unitario REAL NOT NULL,
        FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
        FOREIGN KEY (produto_id) REFERENCES produtos(id)
    );
    """)

    # Controle financeiro
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS financeiro (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        pedido_id INTEGER,
        tipo TEXT NOT NULL CHECK(tipo IN ('receita', 'despesa')),
        valor REAL NOT NULL,
        descricao TEXT,
        criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
    );
    """)

    # =========================
    # Inserção de dados iniciais
    # =========================

    # Usuários
    cursor.execute("""
    INSERT OR IGNORE INTO usuarios (id, nome, email, senha, perfil)
    VALUES (1, 'Admin DuBom', 'admin@dubom.com', 'admin123', 'admin')
    """)
    cursor.execute("""
    INSERT OR IGNORE INTO usuarios (id, nome, email, senha, perfil)
    VALUES (2, 'João Cliente', 'joao@email.com', '1234', 'cliente')
    """)

    # Produtos
    produtos_exemplo = [
        ("Brigadeiro Gourmet", "Brigadeiro de chocolate belga com granulado artesanal.", 3.50, 100, "img/brigadeiro.jpg"),
        ("Beijinho", "Docinho de coco com leite condensado, finalizado com cravo.", 3.00, 80, "img/beijinho.jpg"),
        ("Trufa de Maracujá", "Trufa recheada com ganache de maracujá.", 5.00, 50, "img/trufa.jpg"),
        ("Caixa Surpresa", "Caixa com seleção de 12 doces variados.", 30.00, 20, "img/caixa.jpg")
    ]
    cursor.executemany(
        "INSERT INTO produtos (nome, descricao, preco, estoque, imagem) VALUES (?, ?, ?, ?, ?)", 
        produtos_exemplo
    )

    # Pedido de exemplo (João)
    cursor.execute("INSERT INTO pedidos (usuario_id, status, total) VALUES (2, 'orcado', 11.50)")
    pedido_id = cursor.lastrowid

    # Itens do pedido
    itens = [
        (pedido_id, 1, 2, 3.50),  # 2 brigadeiros
        (pedido_id, 2, 1, 3.00),  # 1 beijinho
        (pedido_id, 3, 1, 5.00)   # 1 trufa
    ]
    cursor.executemany(
        "INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)", 
        itens
    )

    # Financeiro (receita do pedido)
    cursor.execute(
        "INSERT INTO financeiro (pedido_id, tipo, valor, descricao) VALUES (?, 'receita', ?, 'Venda inicial de doces')",
        (pedido_id, 11.50)
    )

    conn.commit()
    conn.close()
    print("Banco de dados criado e populado com dados de exemplo!")

if __name__ == "__main__":
    criar_db()
