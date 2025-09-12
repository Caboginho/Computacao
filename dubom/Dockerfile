# Usar imagem oficial Python
FROM python:3.13-slim

# Setar diretório de trabalho dentro do container
WORKDIR /app

# Copiar requirements.txt e instalar dependências
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiar o restante do projeto
COPY . .

# Expor porta que o Flask usará
EXPOSE 5000

# Variáveis de ambiente padrão
ENV FLASK_APP=app.py
ENV FLASK_RUN_HOST=0.0.0.0
ENV FLASK_ENV=development

# Comando para rodar a aplicação
CMD ["flask", "run"]
