# Computação - Projetos e Estrutura

Este repositório reúne projetos de programação desenvolvidos para fins de estudo, prática e demonstração de conceitos em diferentes linguagens e frameworks. Cada pasta representa um projeto independente, com seu próprio contexto e tecnologias utilizadas.

---

## Projetos

### **cobrinha/**
Jogo da cobrinha (Snake) desenvolvido em Python utilizando a biblioteca Pygame.

- **Snake.py**: Código-fonte principal do jogo. Permite jogar com controles via teclado, exibe placar e tela de game over.

---

### **damas/**
Jogo de Damas (Checkers) implementado em Java com interface gráfica Swing.

- **CheckersGame.java**: Código-fonte principal do jogo.
- **Arquivos `.class`**: Versões compiladas do código.
- Permite jogar com mouse ou coordenadas, possui placar e detecção de vitória/empate.

---

### **dubom/**
Aplicação web MVP para venda de doces artesanais, desenvolvida em Python com Flask.

- **app.py**: Aplicação principal Flask.
- **models.py**: Modelos/tabelas do banco de dados (SQLAlchemy).
- **Dockerfile** e **docker-compose.yml**: Suporte a containers Docker.
- **requirements.txt**: Dependências do projeto.
- **ngrok.exe**: Ferramenta para expor a aplicação localmente.
- **.env.example**: Exemplo de variáveis de ambiente.
- **static/**: Arquivos estáticos (CSS, imagens, etc).
- **templates/**: Templates HTML para as páginas do site.

---

### **doces_site_01/**
Projeto web para gestão e venda de doces, desenvolvido em Flask, com arquitetura modular.

- **app.py**: Arquivo principal da aplicação Flask.
- **config.py**: Configurações do projeto (banco de dados SQLite, preparado para MySQL futuramente).
- **models/**: Definição das tabelas do banco de dados (SQLAlchemy).
  - **produto.py, usuario.py, pedido.py**: Modelos das tabelas.
- **services/**: Regras de negócio e operações CRUD.
  - **produto_service.py, usuario_service.py**: Lógica de produtos e usuários.
- **routes/**: Blueprints para organização das rotas.
  - **cliente.py, admin.py**: Rotas para clientes e administração.
- **templates/**: Templates HTML para as páginas do site.
- **static/**: Arquivos estáticos (CSS, imagens, JS, etc).

---

### **JogoVelha/**
Jogo da Velha (Tic-Tac-Toe) implementado em C++ com interface gráfica usando OpenGL/GLUT.

- **velhavelha.cpp**: Código-fonte principal do jogo.
- **velhavelha.exe**: Executável para Windows.
- Permite jogar com mouse ou teclado, exibe placar e permite reiniciar partidas.

---

### **.vscode/**
Configurações do Visual Studio Code para facilitar o desenvolvimento e depuração dos projetos.

- **c_cpp_properties.json, launch.json, settings.json, tasks.json**: Arquivos de configuração do editor.

---

## Outros arquivos

- **.gitignore**: Arquivos e pastas ignorados pelo Git.

---

## Observações

- Cada projeto pode ser executado separadamente conforme instruções em seus próprios arquivos README ou comentários no código.
- Verifique as dependências necessárias (Python, Java, C++, bibliotecas, etc) antes de executar cada projeto.
- Sinta-se à vontade para explorar, modificar e contribuir com os projetos deste repositório!

---