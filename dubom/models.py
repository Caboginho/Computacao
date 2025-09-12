from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

db = SQLAlchemy()

class Product(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(200), nullable=False)
    description = db.Column(db.Text, nullable=True)
    price = db.Column(db.Float, nullable=False)
    image = db.Column(db.String(400), nullable=True)
    # campos para marketplaces (cada produto pode ter 0/1 links por plataforma)
    marketplace_shopee = db.Column(db.String(800), nullable=True)
    marketplace_mercadolivre = db.Column(db.String(800), nullable=True)

class Customer(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(200), nullable=False)
    email = db.Column(db.String(200), nullable=True, unique=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

class Order(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    customer_id = db.Column(db.Integer, db.ForeignKey('customer.id'))
    items_json = db.Column(db.Text)  # ex: [{"product_id":1,"qty":2},...]
    total = db.Column(db.Float)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

class AccessLog(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    ip = db.Column(db.String(100))
    path = db.Column(db.String(400))
    method = db.Column(db.String(20))
    location = db.Column(db.String(300), nullable=True)  # opcional (preenchido por header/API)
    order_id = db.Column(db.Integer, nullable=True)      # referência ao pedido, se houver
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
