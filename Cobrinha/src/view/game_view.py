# src/view/game_view.py

import pygame
from utils.constants import *

class GameView:
    def __init__(self, tela, fonte, controller):
        self.tela = tela
        self.fonte = fonte
        self.controller = controller

    def render(self, snake_body, comida_pos, vida_extra_pos, score, score_max, vidas, tempo_decorrido, velocidade):
        # Fundo
        self.tela.fill(PRETO)
        
        # Barra superior
        pygame.draw.rect(self.tela, CINZA, (0,0,LARGURA, ALTURA-AREA_JOGO_ALTURA))
        pygame.draw.line(self.tela, BRANCO, (0, ALTURA-AREA_JOGO_ALTURA), (LARGURA, ALTURA-AREA_JOGO_ALTURA), 2)
        
        # Score
        texto_score = self.fonte.render(f"Score: {score}/{score_max}", True, BRANCO)
        self.tela.blit(texto_score, (10,10))
        
        # Vidas
        texto_vidas = self.fonte.render(f"Vidas: {vidas}", True, VERMELHO)
        self.tela.blit(texto_vidas, (180,10))
        
        # Tempo
        minutos = int(tempo_decorrido // 60)
        segundos = int(tempo_decorrido % 60)
        texto_tempo = self.fonte.render(f"Tempo: {minutos:02d}:{segundos:02d}", True, AMARELO)
        self.tela.blit(texto_tempo, (280,10))
        
        # Velocidade
        texto_vel = self.fonte.render(f"Velo: +{int((velocidade-1.0)*100)}%", True, VERDE)
        self.tela.blit(texto_vel, (450,10))
        
        # Grade
        offset_y = ALTURA - AREA_JOGO_ALTURA
        #for x in range(0, LARGURA, TAMANHO_BLOCO):
        #    pygame.draw.line(self.tela, (50,50,50), (x, offset_y), (x, ALTURA))
        #for y in range(offset_y, ALTURA, TAMANHO_BLOCO):
        #    pygame.draw.line(self.tela, (50,50,50), (0, y), (LARGURA, y))
        
        # Cobra
        for i, parte in enumerate(snake_body):
            cor = VERDE_ESCURO if i==0 else VERDE_GRADIENTE[i % len(VERDE_GRADIENTE)]
            pygame.draw.rect(self.tela, cor, (parte[0]*TAMANHO_BLOCO, parte[1]*TAMANHO_BLOCO + offset_y, TAMANHO_BLOCO, TAMANHO_BLOCO))
            pygame.draw.rect(self.tela, (0,50,0), (parte[0]*TAMANHO_BLOCO, parte[1]*TAMANHO_BLOCO + offset_y, TAMANHO_BLOCO, TAMANHO_BLOCO), 1)
        
        # Comida
        pygame.draw.circle(self.tela, BRILHO_COMIDA,
                           (comida_pos[0]*TAMANHO_BLOCO + TAMANHO_BLOCO//2,
                            comida_pos[1]*TAMANHO_BLOCO + offset_y + TAMANHO_BLOCO//2),
                           TAMANHO_BLOCO//2)
        
        # Vida extra
        if vida_extra_pos:
            pygame.draw.circle(self.tela, BRILHO_VIDA,
                               (vida_extra_pos[0]*TAMANHO_BLOCO + TAMANHO_BLOCO//2,
                                vida_extra_pos[1]*TAMANHO_BLOCO + offset_y + TAMANHO_BLOCO//2),
                               TAMANHO_BLOCO//2)
            fonte_vida = pygame.font.SysFont(None, 15)
            texto_vida = fonte_vida.render("+", True, BRANCO)
            self.tela.blit(texto_vida, (vida_extra_pos[0]*TAMANHO_BLOCO + 7, vida_extra_pos[1]*TAMANHO_BLOCO + offset_y + 5))
        
        # Partículas
        for p in self.controller.particulas[:]:
            p.atualizar()
            if p.vida <= 0:
                self.controller.particulas.remove(p)
            else:
                p.desenhar(self.tela)
        
        pygame.display.flip()
