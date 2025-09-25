# src/view/sound_manager.py

import pygame
import os

class SoundManager:
    def __init__(self):
        pygame.mixer.init()
        self.sounds = {
            'mover': self._load_sound('som_mover.wav', 0.3),
            'comida': self._load_sound('som_comida.wav', 0.5),
            'vida': self._load_sound('som_vida.wav', 0.5),
            'game_over': self._load_sound('som_game_over.wav', 0.7),
            'vitoria': self._load_sound('som_vitoria.wav', 0.7)
        }

    def _load_sound(self, filename, volume=0.5):
        caminho = os.path.join('src', 'sounds', filename)
        if os.path.exists(caminho):
            s = pygame.mixer.Sound(caminho)
            s.set_volume(volume)
            return s
        else:
            class SilentSound:
                def play(self): pass
            return SilentSound()

    def play(self, key):
        self.sounds.get(key).play()
