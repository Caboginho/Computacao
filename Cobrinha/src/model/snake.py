# src/model/snake.py

from utils.constants import TAMANHO_BLOCO

class Snake:
    def __init__(self, x, y):
        self.reset(x, y)

    def reset(self, x, y):
        self.body = [[x, y]]
        self.grow_next = False

    def mover(self, direcao):
        cabeca = [self.body[0][0] + direcao[0], self.body[0][1] + direcao[1]]
        self.body.insert(0, cabeca)
        if not self.grow_next:
            self.body.pop()
        else:
            self.grow_next = False

    def crescer(self):
        self.grow_next = True

    def colisao_parede(self, colunas, linhas):
        cabeca = self.body[0]
        return not (0 <= cabeca[0] < colunas) or not (0 <= cabeca[1] < linhas)

    def colisao_corpo(self):
        return self.body[0] in self.body[1:]
