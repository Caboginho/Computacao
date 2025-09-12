# Dubom - Projeto MVP (Doces Artesanais)

Arquivos gerados: app.py, models.py, templates/, Dockerfile, requirements.txt, docker-compose.yml

## Como rodar localmente (sem Docker)
1. Crie e ative virtualenv:
   ```bash
   python -m venv venv
   source venv/bin/activate   # Linux/Mac
   venv\Scripts\activate    # Windows
   ```
2. Instale dependências:
   ```bash
   pip install -r requirements.txt
   ```
3. Copie `.env.example` para `.env` e ajuste as variáveis (opcional).
4. Rode:
   ```bash
   python app.py
   ```
5. Abra `http://127.0.0.1:5000`

## Com Docker
```bash
docker compose up --build
```

## Expor com ngrok
```bash
ngrok http 5000
```

## Integração com Google Sheets (opcional)
- Crie uma service account no Google Cloud, baixe o JSON e aponte `GOOGLE_SA_FILE` para o caminho deste arquivo.
- Crie/compartilhe uma planilha com o email da service account e configure `GOOGLE_SHEET_NAME`.
