# src/main.py

import pygame
from controller.game_controller import GameController
from view.sound_manager import SoundManager
from utils.constants import LARGURA, ALTURA, TAMANHO_BLOCO

#   Gerar o EXE
#   pyinstaller --onefile --windowed --add-data "src/sounds;./sounds" --add-data "src/data/snake_scores.json;." src/main.py
#   Executar EXE
#   dist\main.exe
#   Executar projeto
#   python src/main.py

def main():
    pygame.init()
    tela = pygame.display.set_mode((LARGURA, ALTURA))
    pygame.display.set_caption("Snake Cabogi")

    sound_manager = SoundManager()
    game = GameController(tela, sound_manager)
    game.run()

if __name__ == "__main__":
    main()
