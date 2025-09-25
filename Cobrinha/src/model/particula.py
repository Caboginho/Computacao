import random
import pygame

class Particula:
    def __init__(self, x, y, cor):
        self.x = x
        self.y = y
        self.cor = cor
        self.velocidade = [random.uniform(-2,2), random.uniform(-2,2)]
        self.vida = random.randint(10,20)
        self.tamanho = random.randint(2,4)

    def atualizar(self):
        self.x += self.velocidade[0]
        self.y += self.velocidade[1]
        self.vida -= 1

    def desenhar(self, tela):
        if self.vida > 0:
            pygame.draw.circle(tela, self.cor, (int(self.x), int(self.y)), self.tamanho)
