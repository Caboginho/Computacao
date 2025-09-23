import pygame
import random
import sys

# Configurações
LARGURA, ALTURA = 600, 400
TAMANHO_BLOCO = 20
FPS_NORMAL = 8
FPS_RAPIDO = 15

# Cores
PRETO = (0, 0, 0)
VERDE = (0, 200, 0)
VERMELHO = (200, 0, 0)
BRANCO = (255, 255, 255)

def gerar_comida(cobra, colunas, linhas):
    while True:
        comida = [random.randint(0, colunas - 1), random.randint(0, linhas - 1)]
        if comida not in cobra:  # garante que não aparece na cobra
            return comida

def main():
    pygame.init()
    tela = pygame.display.set_mode((LARGURA, ALTURA))
    pygame.display.set_caption("Snake - Cabogi Edition")
    clock = pygame.time.Clock()

    colunas = LARGURA // TAMANHO_BLOCO
    linhas = ALTURA // TAMANHO_BLOCO

    cobra = [[colunas // 2, linhas // 2]]
    direcao = [0, -1]  # Começa indo pra cima
    comida = gerar_comida(cobra, colunas, linhas)

    score = 0
    score_maximo = colunas * linhas - 1  # quando ocupa o tabuleiro todo

    rodando = True
    while rodando:
        clock.tick(FPS_RAPIDO if pygame.key.get_pressed()[pygame.K_SPACE] else FPS_NORMAL)

        for evento in pygame.event.get():
            if evento.type == pygame.QUIT:
                rodando = False

        # Controle com W, A, S, D
        teclas = pygame.key.get_pressed()
        if teclas[pygame.K_w] and direcao != [0, 1]:
            direcao = [0, -1]
        elif teclas[pygame.K_s] and direcao != [0, -1]:
            direcao = [0, 1]
        elif teclas[pygame.K_a] and direcao != [1, 0]:
            direcao = [-1, 0]
        elif teclas[pygame.K_d] and direcao != [-1, 0]:
            direcao = [1, 0]

        # Nova cabeça
        cabeca = [cobra[0][0] + direcao[0], cobra[0][1] + direcao[1]]

        # Colisão com borda ou com o corpo
        if (cabeca in cobra) or not (0 <= cabeca[0] < colunas) or not (0 <= cabeca[1] < linhas):
            rodando = False
            continue

        cobra.insert(0, cabeca)

        # Comer comida
        if cabeca == comida:
            score += 1
            if score == score_maximo:
                rodando = False  # End Game, cobra ocupou tudo
            else:
                comida = gerar_comida(cobra, colunas, linhas)
        else:
            cobra.pop()  # Move sem crescer

        # Desenho
        tela.fill(PRETO)
        for parte in cobra:
            pygame.draw.rect(tela, VERDE, (parte[0]*TAMANHO_BLOCO, parte[1]*TAMANHO_BLOCO, TAMANHO_BLOCO, TAMANHO_BLOCO))
        pygame.draw.rect(tela, VERMELHO, (comida[0]*TAMANHO_BLOCO, comida[1]*TAMANHO_BLOCO, TAMANHO_BLOCO, TAMANHO_BLOCO))

        fonte = pygame.font.SysFont(None, 30)
        texto = fonte.render(f"Score: {score}/{score_maximo}", True, BRANCO)
        tela.blit(texto, (10, 10))

        pygame.display.flip()

    # Tela de Game Over
    tela.fill(PRETO)
    fonte = pygame.font.SysFont(None, 50)
    if score == score_maximo:
        msg = "Parabéns! Você ocupou todo o tabuleiro!"
    else:
        msg = "Game Over!"
    texto = fonte.render(msg, True, VERMELHO)
    tela.blit(texto, (LARGURA//2 - texto.get_width()//2, ALTURA//2 - texto.get_height()//2))

    pygame.display.flip()
    pygame.time.wait(3000)
    pygame.quit()
    sys.exit()

if __name__ == "__main__":
    main()
