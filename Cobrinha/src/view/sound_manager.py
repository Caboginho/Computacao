# src/view/sound_manager.py

import pygame
import sys
import os

# Cores para referência de partículas, se necessário
BRILHO_COMIDA = (255, 50, 50)
BRILHO_VIDA = (50, 50, 255)

import sys, os

import sys, os

def resource_path(relative_path):
    """Retorna caminho absoluto compatível com Python e PyInstaller"""
    try:
        base_path = sys._MEIPASS  # executável
    except AttributeError:
        base_path = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))  # volta para src/

    return os.path.join(base_path, relative_path)

class SoundManager:
    def __init__(self):
        pygame.mixer.pre_init(44100, -16, 2, 512)
        pygame.init()
        self.sons = {
            "mover": self.carregar_som("sounds/som_mover.wav", 0.3),
            "comida": self.carregar_som("sounds/som_comida.wav", 0.5),
            "vida": self.carregar_som("sounds/som_vida.wav", 0.5),
            "game_over": self.carregar_som("sounds/som_game_over.wav", 0.7),
            "vitoria": self.carregar_som("sounds/som_vitoria.wav", 0.7)
        }

    def carregar_som(self, nome_arquivo, volume=0.5):
        caminho = resource_path(nome_arquivo)
        try:
            som = pygame.mixer.Sound(caminho)
            som.set_volume(volume)
            return som
        except Exception as e:
            print(f"Erro ao carregar {nome_arquivo}: {e}")
            class SilentSound:
                def play(self): pass
            return SilentSound()

    def play(self, nome):
        if nome in self.sons:
            self.sons[nome].play()
