# src/main.py

import pygame
from controller.game_controller import GameController
from utils.constants import LARGURA, ALTURA

def main():
    pygame.init()
    screen = pygame.display.set_mode((LARGURA, ALTURA))
    pygame.display.set_caption("Snake Cabogi")
    game = GameController(screen)
    game.run()

if __name__ == "__main__":
    main()
