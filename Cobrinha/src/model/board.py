# src/model/board.py

import pygame
from utils.constants import LARGURA, ALTURA, TAMANHO_BLOCO, PRETO

class Board:
    def __init__(self):
        self.width_blocks = LARGURA // TAMANHO_BLOCO
        self.height_blocks = ALTURA // TAMANHO_BLOCO

    def check_wall_collision(self, snake_head):
        """Verifica se a cobra colidiu com as paredes do tabuleiro."""
        x, y = snake_head
        return x < 0 or x >= self.width_blocks or y < 0 or y >= self.height_blocks

    def draw_grid(self, surface):
        """Desenha o fundo quadriculado do cenário."""
        surface.fill(PRETO)
        for x in range(0, LARGURA, TAMANHO_BLOCO):
            pygame.draw.line(surface, (40, 40, 40), (x, 0), (x, ALTURA))
        for y in range(0, ALTURA, TAMANHO_BLOCO):
            pygame.draw.line(surface, (40, 40, 40), (0, y), (LARGURA, y))
