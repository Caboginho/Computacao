import pygame
import random
import sys
import time
import json
import os

# ------------------- Configurações -------------------
LARGURA, ALTURA = 600, 500
TAMANHO_BLOCO = 20
FPS_NORMAL = 8
FPS_RAPIDO = 15
AREA_JOGO_ALTURA = 400

# Cores
PRETO = (0, 0, 0)
VERDE = (0, 200, 0)
VERDE_ESCURO = (0, 150, 0)
VERDE_GRADIENTE = [(0, 255, 0), (0, 220, 0), (0, 190, 0)]
VERMELHO = (200, 0, 0)
BRANCO = (255, 255, 255)
CINZA = (50, 50, 50)
AZUL = (0, 100, 255)
AMARELO = (255, 255, 0)
DOURADO = (255, 215, 0)
FUNDO_VITORIA = (50, 0, 50)
FUNDO_GAMEOVER = (50, 0, 0)
BRILHO_COMIDA = (255, 50, 50)
BRILHO_VIDA = (50, 50, 255)

SCORES_FILE = "snake_scores.json"

# ------------------- Inicialização do pygame -------------------
pygame.mixer.pre_init(44100, -16, 2, 512)
pygame.init()

# ------------------- Carregar sons -------------------
def carregar_som(nome_arquivo, volume=0.5):
    caminho = os.path.join(os.path.dirname(__file__), nome_arquivo)
    try:
        som = pygame.mixer.Sound(caminho)
        som.set_volume(volume)
        return som
    except Exception as e:
        print(f"Erro ao carregar {nome_arquivo}: {e}")
        class SilentSound:
            def play(self): pass
        return SilentSound()

som_mover = carregar_som("som_mover.wav", 0.3)
som_comida = carregar_som("som_comida.wav", 0.5)
som_vida = carregar_som("som_vida.wav", 0.5)
som_game_over = carregar_som("som_game_over.wav", 0.7)
som_vitoria = carregar_som("som_vitoria.wav", 0.7)

# ------------------- Funções do jogo -------------------
def gerar_comida(cobra, colunas, linhas):
    while True:
        comida = [random.randint(0, colunas-1), random.randint(0, linhas-1)]
        if comida not in cobra:
            return comida

def gerar_vida_extra(cobra, colunas, linhas):
    while True:
        vida = [random.randint(0, colunas-1), random.randint(0, linhas-1)]
        if vida not in cobra:
            return vida

def desenhar_cobra(tela, cobra, tamanho_bloco, offset_y):
    for i, parte in enumerate(cobra):
        # Cabeça mais escura e corpo gradiente
        if i == 0:
            cor = VERDE_ESCURO
        else:
            cor = VERDE_GRADIENTE[i % len(VERDE_GRADIENTE)]
        pygame.draw.rect(tela, cor, (parte[0]*tamanho_bloco, parte[1]*tamanho_bloco + offset_y, tamanho_bloco, tamanho_bloco))
        pygame.draw.rect(tela, (0, 50, 0), (parte[0]*tamanho_bloco, parte[1]*tamanho_bloco + offset_y, tamanho_bloco, tamanho_bloco), 1)

def desenhar_barra_superior(tela, largura, altura_barra, score, score_maximo, vidas, tempo_decorrido, velocidade):
    pygame.draw.rect(tela, CINZA, (0, 0, largura, altura_barra))
    pygame.draw.line(tela, BRANCO, (0, altura_barra), (largura, altura_barra), 2)
    
    fonte_pequena = pygame.font.SysFont(None, 24)
    fonte_media = pygame.font.SysFont(None, 30)
    
    texto_score = fonte_media.render(f"Score: {score}/{score_maximo}", True, BRANCO)
    tela.blit(texto_score, (10, 10))
    
    texto_vidas = fonte_media.render(f"Vidas: {vidas}", True, VERMELHO)
    tela.blit(texto_vidas, (200, 10))
    
    minutos = int(tempo_decorrido // 60)
    segundos = int(tempo_decorrido % 60)
    texto_timer = fonte_media.render(f"Tempo: {minutos:02d}:{segundos:02d}", True, AMARELO)
    tela.blit(texto_timer, (350, 10))
    
    texto_velocidade = fonte_pequena.render(f"Velocidade: +{int((velocidade - 1.0) * 100)}%", True, VERDE)
    tela.blit(texto_velocidade, (500, 15))

# ------------------- Telas -------------------
def tela_inicial(tela, largura, altura):
    """Exibe a tela inicial com título e melhores scores"""
    scores = carregar_scores()
    
    while True:
        tela.fill(PRETO)
        fonte_titulo = pygame.font.SysFont(None, 60)
        titulo = fonte_titulo.render("Snake Cabogi", True, VERDE)
        tela.blit(titulo, (largura//2 - titulo.get_width()//2, 50))
        
        fonte_scores = pygame.font.SysFont(None, 36)
        subtitulo = fonte_scores.render("Melhores Scores:", True, DOURADO)
        tela.blit(subtitulo, (largura//2 - subtitulo.get_width()//2, 130))
        
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
            tela.blit(texto_score, (largura//2 - texto_score.get_width()//2, 180 + i*40))
        
        # Instruções
        fonte_inicio = pygame.font.SysFont(None, 30)
        texto_inicio = fonte_inicio.render("Pressione ENTER para começar", True, BRANCO)
        tela.blit(texto_inicio, (largura//2 - texto_inicio.get_width()//2, altura - 100))
        
        fonte_instrucao = pygame.font.SysFont(None, 20)
        texto_instrucao = fonte_instrucao.render(
            "Use WASD ou SETAS para mover, ESPAÇO para acelerar", True, CINZA)
        tela.blit(texto_instrucao, (largura//2 - texto_instrucao.get_width()//2, altura - 50))
        
        pygame.display.flip()
        
        for evento in pygame.event.get():
            if evento.type == pygame.QUIT:
                pygame.quit()
                sys.exit()
            if evento.type == pygame.KEYDOWN:
                if evento.key == pygame.K_RETURN:
                    return True
                if evento.key == pygame.K_ESCAPE:
                    pygame.quit()
                    sys.exit()

def tela_final(tela, largura, altura, vitoria, score, tempo_decorrido):
    if vitoria:
        salvar_score(score, tempo_decorrido)
        som_vitoria.play()
        fundo = FUNDO_VITORIA
        msg = "Parabéns! Vitória!"
        cor_msg = DOURADO
    else:
        som_game_over.play()
        fundo = FUNDO_GAMEOVER
        msg = "Game Over!"
        cor_msg = VERMELHO
    esperando = True
    while esperando:
        tela.fill(fundo)
        fonte = pygame.font.SysFont(None, 50)
        texto = fonte.render(msg, True, cor_msg)
        tela.blit(texto, (largura//2 - texto.get_width()//2, altura//2 - 60))
        fonte_score = pygame.font.SysFont(None, 30)
        texto_score = fonte_score.render(f"Score final: {score}", True, BRANCO)
        tela.blit(texto_score, (largura//2 - texto_score.get_width()//2, altura//2))
        minutos = int(tempo_decorrido // 60)
        segundos = int(tempo_decorrido % 60)
        texto_tempo = fonte_score.render(f"Tempo: {minutos:02d}:{segundos:02d}", True, AMARELO)
        tela.blit(texto_tempo, (largura//2 - texto_tempo.get_width()//2, altura//2 + 30))
        fonte_continuar = pygame.font.SysFont(None, 25)
        texto_continuar = fonte_continuar.render("Pressione ENTER para voltar ao menu", True, BRANCO)
        tela.blit(texto_continuar, (largura//2 - texto_continuar.get_width()//2, altura//2 + 80))
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

# ------------------- Partículas -------------------
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

# ------------------- Jogo -------------------
def jogar():
    tela = pygame.display.set_mode((LARGURA, ALTURA))
    pygame.display.set_caption("Snake - Cabogi Edition")
    clock = pygame.time.Clock()

    colunas = LARGURA // TAMANHO_BLOCO
    linhas = AREA_JOGO_ALTURA // TAMANHO_BLOCO
    offset_y = ALTURA - AREA_JOGO_ALTURA

    vidas = 3
    score = 0
    score_maximo = colunas * linhas - 1
    multiplicador_velocidade = 1.0
    vida_extra = None
    tempo_inicio = time.time()

    cobra = [[colunas // 2, linhas // 2]]
    direcao = [0, -1]
    comida = gerar_comida(cobra, colunas, linhas)

    particulas = []

    rodando = True
    while rodando:
        tempo_decorrido = time.time() - tempo_inicio
        multiplicador_velocidade = 1.0 + (score // 50) * 0.1
        fps_atual = FPS_NORMAL * multiplicador_velocidade
        if pygame.key.get_pressed()[pygame.K_SPACE]:
            fps_atual = FPS_RAPIDO * multiplicador_velocidade

        clock.tick(fps_atual)

        for evento in pygame.event.get():
            if evento.type == pygame.QUIT:
                pygame.quit()
                sys.exit()
            if evento.type == pygame.KEYDOWN:
                if evento.key in [pygame.K_w, pygame.K_UP] and direcao != [0,1]:
                    direcao = [0, -1]
                    som_mover.play()
                elif evento.key in [pygame.K_s, pygame.K_DOWN] and direcao != [0,-1]:
                    direcao = [0, 1]
                    som_mover.play()
                elif evento.key in [pygame.K_a, pygame.K_LEFT] and direcao != [1,0]:
                    direcao = [-1, 0]
                    som_mover.play()
                elif evento.key in [pygame.K_d, pygame.K_RIGHT] and direcao != [-1,0]:
                    direcao = [1, 0]
                    som_mover.play()

        cabeca = [cobra[0][0] + direcao[0], cobra[0][1] + direcao[1]]

        if (cabeca in cobra) or not (0 <= cabeca[0] < colunas) or not (0 <= cabeca[1] < linhas):
            vidas -= 1
            som_game_over.play()
            if vidas <= 0:
                tela_final(tela, LARGURA, ALTURA, False, score, tempo_decorrido)
                return
            else:
                cobra = [[colunas // 2, linhas // 2]]
                direcao = [0, -1]
                comida = gerar_comida(cobra, colunas, linhas)
                vida_extra = None
                continue

        cobra.insert(0, cabeca)

        # Comer comida
        if cabeca == comida:
            score += 1
            som_comida.play()
            # Gerar partículas de comida
            for _ in range(15):
                particulas.append(Particula(cabeca[0]*TAMANHO_BLOCO + TAMANHO_BLOCO//2,
                                            cabeca[1]*TAMANHO_BLOCO + offset_y + TAMANHO_BLOCO//2,
                                            BRILHO_COMIDA))
            if score == score_maximo:
                tela_final(tela, LARGURA, ALTURA, True, score, tempo_decorrido)
                return
            comida = gerar_comida(cobra, colunas, linhas)
            if score % 100 == 0 and vidas < 3:
                vida_extra = gerar_vida_extra(cobra, colunas, linhas)
        else:
            cobra.pop()

        # Coletar vida extra
        if vida_extra and cabeca == vida_extra:
            vidas = min(3, vidas + 1)
            som_vida.play()
            for _ in range(15):
                particulas.append(Particula(cabeca[0]*TAMANHO_BLOCO + TAMANHO_BLOCO//2,
                                            cabeca[1]*TAMANHO_BLOCO + offset_y + TAMANHO_BLOCO//2,
                                            BRILHO_VIDA))
            vida_extra = None

        # ------------------- Desenho -------------------
        tela.fill(PRETO)
        desenhar_barra_superior(tela, LARGURA, ALTURA - AREA_JOGO_ALTURA,
                                score, score_maximo, vidas, tempo_decorrido, multiplicador_velocidade)
        pygame.draw.rect(tela, (30, 30, 30), (0, offset_y, LARGURA, AREA_JOGO_ALTURA))

        # Grade
        for x in range(0, LARGURA, TAMANHO_BLOCO):
            pygame.draw.line(tela, (50,50,50), (x, offset_y), (x, ALTURA))
        for y in range(offset_y, ALTURA, TAMANHO_BLOCO):
            pygame.draw.line(tela, (50,50,50), (0, y), (LARGURA, y))

        desenhar_cobra(tela, cobra, TAMANHO_BLOCO, offset_y)

        # Desenhar comida
        pygame.draw.circle(tela, BRILHO_COMIDA,
                           (comida[0]*TAMANHO_BLOCO + TAMANHO_BLOCO//2,
                            comida[1]*TAMANHO_BLOCO + offset_y + TAMANHO_BLOCO//2),
                           TAMANHO_BLOCO//2)
        # Desenhar vida extra
        if vida_extra:
            pygame.draw.circle(tela, BRILHO_VIDA,
                               (vida_extra[0]*TAMANHO_BLOCO + TAMANHO_BLOCO//2,
                                vida_extra[1]*TAMANHO_BLOCO + offset_y + TAMANHO_BLOCO//2),
                               TAMANHO_BLOCO//2)
            fonte_vida = pygame.font.SysFont(None, 15)
            texto_vida = fonte_vida.render("+", True, BRANCO)
            tela.blit(texto_vida, (vida_extra[0]*TAMANHO_BLOCO + 7, vida_extra[1]*TAMANHO_BLOCO + offset_y + 5))

        # Atualizar partículas
        for p in particulas[:]:
            p.atualizar()
            if p.vida <= 0:
                particulas.remove(p)
            else:
                p.desenhar(tela)

        pygame.display.flip()

# ------------------- Funções de score -------------------
def carregar_scores():
    """Carrega os scores salvos do arquivo, retorna lista de dicionários"""
    if os.path.exists(SCORES_FILE):
        try:
            with open(SCORES_FILE, 'r') as f:
                scores = json.load(f)
                # Ordena por score desc e tempo asc
                scores.sort(key=lambda x: (-x['score'], x['tempo']))
                return scores[:3]  # Mantém apenas os 3 melhores
        except:
            return []
    return []

def salvar_score(score, tempo):
    """Salva um novo score e mantém top 3"""
    scores = carregar_scores()
    novo_score = {
        'score': score,
        'tempo': tempo,
        'data': time.strftime("%d/%m/%Y %H:%M")
    }
    scores.append(novo_score)
    scores.sort(key=lambda x: (-x['score'], x['tempo']))
    scores = scores[:3]
    try:
        with open(SCORES_FILE, 'w') as f:
            json.dump(scores, f, indent=4)
    except Exception as e:
        print(f"Erro ao salvar scores: {e}")
    return scores

# ------------------- Loop principal -------------------
def main():
    while True:
        tela = pygame.display.set_mode((LARGURA, ALTURA))
        if not tela_inicial(tela, LARGURA, ALTURA):
            break
        jogar()

if __name__ == "__main__":
    main()
