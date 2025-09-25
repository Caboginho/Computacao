# src/controller/game_controller.py

import pygame
import random
import time
import sys

from model.particula import Particula
from model.snake import Snake
from model.food import Food
from model.score_manager import ScoreManager
from view.game_view import GameView
from view.sound_manager import SoundManager
from utils.constants import *

class GameController:
    def __init__(self, tela,sound_manager):
        self.screen = tela
        self.sound_manager = sound_manager
        self.clock = pygame.time.Clock()
        self.score_manager = ScoreManager()
        self.sound_manager = SoundManager()
        self.snake = Snake(LARGURA//TAMANHO_BLOCO//2, AREA_JOGO_ALTURA//TAMANHO_BLOCO//2)
        self.food = Food(self.snake.body)
        self.vida_extra = None
        self.score = 0
        self.vidas = 3
        self.score_max = (LARGURA//TAMANHO_BLOCO) * (AREA_JOGO_ALTURA//TAMANHO_BLOCO) - 1
        self.direcao = UP
        self.tempo_inicio = time.time()
        self.view = GameView(tela, pygame.font.SysFont(None, 30), controller=self)
        self.particulas = []
        self.game_over = False
        self.vitoria = False

    def reset_snake(self):
        self.snake.reset(LARGURA//TAMANHO_BLOCO//2, AREA_JOGO_ALTURA//TAMANHO_BLOCO//2)
        self.direcao = UP

    def get_tempo_decorrido(self):
        return time.time() - self.tempo_inicio

    def gerar_vida_extra(self):
        while True:
            vida = [random.randint(0, LARGURA//TAMANHO_BLOCO - 1),
                    random.randint(0, AREA_JOGO_ALTURA//TAMANHO_BLOCO - 1)]
            if vida not in self.snake.body:
                return vida

    def tela_inicial(self):
        scores = self.score_manager.best_scores()
        while True:
            self.screen.fill(PRETO)
            fonte_titulo = pygame.font.SysFont(None, 60)
            titulo = fonte_titulo.render("Snake Cabogi", True, VERDE)
            self.screen.blit(titulo, (LARGURA//2 - titulo.get_width()//2, 50))

            fonte_scores = pygame.font.SysFont(None, 36)
            subtitulo = fonte_scores.render("Melhores Scores:", True, DOURADO)
            self.screen.blit(subtitulo, (LARGURA//2 - subtitulo.get_width()//2, 130))

            fonte_score = pygame.font.SysFont(None, 30)
            for i in range(3):
                if i < len(scores):
                    score_data = scores[i]
                    minutos = int(score_data['tempo'] // 60)
                    segundos = int(score_data['tempo'] % 60)
                    texto_score = fonte_score.render(
                        f"{i+1}. Score: {score_data['score']} - Tempo: {minutos:02d}:{segundos:02d}", True, BRANCO)
                else:
                    texto_score = fonte_score.render(f"{i+1}. ---", True, CINZA)
                self.screen.blit(texto_score, (LARGURA//2 - texto_score.get_width()//2, 180 + i*40))

            fonte_inicio = pygame.font.SysFont(None, 30)
            texto_inicio = fonte_inicio.render("Pressione ENTER para começar", True, BRANCO)
            self.screen.blit(texto_inicio, (LARGURA//2 - texto_inicio.get_width()//2, ALTURA - 100))
            fonte_instrucao = pygame.font.SysFont(None, 20)
            texto_instrucao = fonte_instrucao.render(
                "Use WASD ou SETAS para mover, ESPAÇO para acelerar", True, CINZA)
            self.screen.blit(texto_instrucao, (LARGURA//2 - texto_instrucao.get_width()//2, ALTURA - 50))

            pygame.display.flip()
            for evento in pygame.event.get():
                if evento.type == pygame.QUIT:
                    pygame.quit()
                    sys.exit()
                if evento.type == pygame.KEYDOWN:
                    if evento.key == pygame.K_RETURN:
                        return
                    if evento.key == pygame.K_ESCAPE:
                        pygame.quit()
                        sys.exit()

    def tela_final(self):
        self.score_manager.save_score(self.score, self.get_tempo_decorrido())
        som = self.sound_manager.play('vitoria') if self.vitoria else self.sound_manager.play('game_over')
        fundo = FUNDO_VITORIA if self.vitoria else FUNDO_GAMEOVER
        msg = "Parabéns! Vitória!" if self.vitoria else "Game Over!"
        cor_msg = DOURADO if self.vitoria else VERMELHO

        esperando = True
        while esperando:
            self.screen.fill(fundo)
            fonte = pygame.font.SysFont(None, 50)
            texto = fonte.render(msg, True, cor_msg)
            self.screen.blit(texto, (LARGURA//2 - texto.get_width()//2, ALTURA//2 - 60))
            fonte_score = pygame.font.SysFont(None, 30)
            texto_score = fonte_score.render(f"Score final: {self.score}", True, BRANCO)
            self.screen.blit(texto_score, (LARGURA//2 - texto_score.get_width()//2, ALTURA//2))
            minutos = int(self.get_tempo_decorrido() // 60)
            segundos = int(self.get_tempo_decorrido() % 60)
            texto_tempo = fonte_score.render(f"Tempo: {minutos:02d}:{segundos:02d}", True, AMARELO)
            self.screen.blit(texto_tempo, (LARGURA//2 - texto_tempo.get_width()//2, ALTURA//2 + 30))
            fonte_continuar = pygame.font.SysFont(None, 25)
            texto_continuar = fonte_continuar.render("Pressione ENTER para voltar ao menu", True, BRANCO)
            self.screen.blit(texto_continuar, (LARGURA//2 - texto_continuar.get_width()//2, ALTURA//2 + 80))
            pygame.display.flip()
            for evento in pygame.event.get():
                if evento.type == pygame.QUIT:
                    pygame.quit()
                    sys.exit()
                if evento.type == pygame.KEYDOWN:
                    if evento.key == pygame.K_RETURN:
                        esperando = False
                    if evento.key == pygame.K_ESCAPE:
                        pygame.quit()
                        sys.exit()

    def run(self):
        while True:
            self.tela_inicial()
            self.iniciar_jogo()

    def iniciar_jogo(self):
        colunas = LARGURA // TAMANHO_BLOCO
        linhas = AREA_JOGO_ALTURA // TAMANHO_BLOCO
        offset_y = ALTURA - AREA_JOGO_ALTURA

        self.snake.reset(colunas//2, linhas//2)
        self.direcao = UP
        self.score = 0
        self.vidas = 3
        self.vida_extra = None
        self.particulas = []
        self.tempo_inicio = time.time()
        self.game_over = False
        self.vitoria = False
        self.food.gerar(self.snake.body)

        while not self.game_over and not self.vitoria:
            tempo_decorrido = self.get_tempo_decorrido()
            multiplicador_velocidade = 1.0 + (self.score // 50) * 0.1
            fps_atual = FPS_NORMAL * multiplicador_velocidade
            keys = pygame.key.get_pressed()
            if keys[pygame.K_SPACE]:
                fps_atual = FPS_RAPIDO * multiplicador_velocidade
            self.clock.tick(fps_atual)

            for evento in pygame.event.get():
                if evento.type == pygame.QUIT:
                    pygame.quit()
                    sys.exit()
                if evento.type == pygame.KEYDOWN:
                    if evento.key in [pygame.K_w, pygame.K_UP] and self.direcao != DOWN:
                        self.direcao = UP
                        self.sound_manager.play('mover')
                    elif evento.key in [pygame.K_s, pygame.K_DOWN] and self.direcao != UP:
                        self.direcao = DOWN
                        self.sound_manager.play('mover')
                    elif evento.key in [pygame.K_a, pygame.K_LEFT] and self.direcao != RIGHT:
                        self.direcao = LEFT
                        self.sound_manager.play('mover')
                    elif evento.key in [pygame.K_d, pygame.K_RIGHT] and self.direcao != LEFT:
                        self.direcao = RIGHT
                        self.sound_manager.play('mover')

            self.snake.mover(self.direcao)

            # Colisão com parede ou consigo mesmo
            if self.snake.colisao_parede(colunas, linhas) or self.snake.colisao_corpo():
                self.vidas -= 1
                self.sound_manager.play('game_over')
                if self.vidas <= 0:
                    self.game_over = True
                    break
                else:
                    self.reset_snake()
                    self.food.gerar(self.snake.body)
                    self.vida_extra = None
                    continue

            # Comer comida
            if self.snake.body[0] == self.food.pos:
                self.score += 1
                self.sound_manager.play('comida')
                self.snake.crescer()
                
                # Partículas de comida
                x = self.food.pos[0] * TAMANHO_BLOCO + TAMANHO_BLOCO//2
                y = self.food.pos[1] * TAMANHO_BLOCO + (ALTURA - AREA_JOGO_ALTURA) + TAMANHO_BLOCO//2
                for _ in range(15):
                    self.particulas.append(Particula(x, y, BRILHO_COMIDA))

                self.food.gerar(self.snake.body)
                if self.score % 100 == 0 and self.vidas < 3:
                    self.vida_extra = self.gerar_vida_extra()

            # Coletar vida extra
            # Coletar vida extra com partículas
            if self.vida_extra and self.snake.body[0] == self.vida_extra:
                self.vidas = min(3, self.vidas + 1)
                self.sound_manager.play('vida')

                x = self.vida_extra[0] * TAMANHO_BLOCO + TAMANHO_BLOCO//2
                y = self.vida_extra[1] * TAMANHO_BLOCO + (ALTURA - AREA_JOGO_ALTURA) + TAMANHO_BLOCO//2
                for _ in range(15):
                    self.particulas.append(Particula(x, y, BRILHO_VIDA))

                self.vida_extra = None

            # Atualizar renderização
            self.view.render(self.snake.body, self.food.pos, self.vida_extra,
                             self.score, self.score_max, self.vidas, tempo_decorrido, multiplicador_velocidade)

            # Vitória
            if self.score >= self.score_max:
                self.vitoria = True
                break

        self.tela_final()
