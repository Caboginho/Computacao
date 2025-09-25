# src/model/food.py

import random

class Food:
    def __init__(self, cobra_body):
        self.pos = [0, 0]
        self.gerar(cobra_body)

    def gerar(self, cobra_body):
        colunas = 30  # padrão LARGURA/TAMANHO_BLOCO=600/20
        linhas = 20   # padrão AREA_JOGO_ALTURA/TAMANHO_BLOCO=400/20
        while True:
            comida = [random.randint(0, colunas-1), random.randint(0, linhas-1)]
            if comida not in cobra_body:
                self.pos = comida
                break
