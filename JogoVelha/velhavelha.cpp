#include <GL/glut.h>
#include <iostream>
#include <string>
#include <sstream>
#include <cmath>

using namespace std;

// Constantes do jogo
const int BOARD_SIZE = 3;
const int WINDOW_WIDTH = 600;
const int WINDOW_HEIGHT = 700;

// Estados do jogo
enum GameState {
    PLAYER1_TURN,
    PLAYER2_TURN,
    GAME_OVER
};

// Estrutura para armazenar o placar
struct Score {
    int player1;
    int player2;
    int draws;
};

// Variáveis globais
char board[BOARD_SIZE][BOARD_SIZE];
GameState currentState;
Score score;
bool inputMode; // true para mouse, false para teclado
int inputRow, inputCol;
string inputMessage;

// Inicialização do jogo
void initializeGame() {
    for (int i = 0; i < BOARD_SIZE; i++) {
        for (int j = 0; j < BOARD_SIZE; j++) {
            board[i][j] = ' ';
        }
    }
    currentState = PLAYER1_TURN;
    inputMode = true; // Começa com modo mouse
    inputMessage = "Player 1's Turn (X) - Use Mouse or Type Coordinates";
    inputRow = inputCol = -1;
}

// Verifica se há um vencedor
char checkWinner() {
    // Verifica linhas
    for (int i = 0; i < BOARD_SIZE; i++) {
        if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
            return board[i][0];
        }
    }
    
    // Verifica colunas
    for (int j = 0; j < BOARD_SIZE; j++) {
        if (board[0][j] != ' ' && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
            return board[0][j];
        }
    }
    
    // Verifica diagonais
    if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
        return board[0][0];
    }
    if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
        return board[0][2];
    }
    
    return ' '; // Sem vencedor
}

// Verifica se o tabuleiro está cheio
bool isBoardFull() {
    for (int i = 0; i < BOARD_SIZE; i++) {
        for (int j = 0; j < BOARD_SIZE; j++) {
            if (board[i][j] == ' ') {
                return false;
            }
        }
    }
    return true;
}

// Processa uma jogada
void makeMove(int row, int col) {
    if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE || board[row][col] != ' ') {
        inputMessage = "Invalid move! Try again.";
        return;
    }
    
    if (currentState == PLAYER1_TURN) {
        board[row][col] = 'X';
        currentState = PLAYER2_TURN;
        inputMessage = "Player 2's Turn (O) - Use Mouse or Type Coordinates";
    } else {
        board[row][col] = 'O';
        currentState = PLAYER1_TURN;
        inputMessage = "Player 1's Turn (X) - Use Mouse or Type Coordinates";
    }
    
    // Verifica se o jogo terminou
    char winner = checkWinner();
    if (winner != ' ') {
        currentState = GAME_OVER;
        if (winner == 'X') {
            score.player1++;
            inputMessage = "Player 1 (X) Wins! Click or Press Enter to play again.";
        } else {
            score.player2++;
            inputMessage = "Player 2 (O) Wins! Click or Press Enter to play again.";
        }
    } else if (isBoardFull()) {
        currentState = GAME_OVER;
        score.draws++;
        inputMessage = "It's a Draw! Click or Press Enter to play again.";
    }
}

// Reinicia o jogo
void restartGame() {
    initializeGame();
}

// Função para desenhar texto na tela
void drawText(float x, float y, const string& text) {
    glRasterPos2f(x, y);
    for (char c : text) {
        glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18, c);
    }
}

// Função para desenhar o tabuleiro
void drawBoard() {
    glLineWidth(3.0);
    glColor3f(1.0, 1.0, 1.0);
    
    // Linhas verticais
    for (int i = 1; i < BOARD_SIZE; i++) {
        glBegin(GL_LINES);
        glVertex2f(i * 200.0, 50.0);
        glVertex2f(i * 200.0, 650.0);
        glEnd();
    }
    
    // Linhas horizontais
    for (int i = 1; i < BOARD_SIZE; i++) {
        glBegin(GL_LINES);
        glVertex2f(0.0, 50.0 + i * 200.0);
        glVertex2f(600.0, 50.0 + i * 200.0);
        glEnd();
    }
    
    // Desenha X e O
    for (int i = 0; i < BOARD_SIZE; i++) {
        for (int j = 0; j < BOARD_SIZE; j++) {
            float centerX = j * 200.0 + 100.0;
            float centerY = i * 200.0 + 150.0;
            
            if (board[i][j] == 'X') {
                glColor3f(1.0, 0.0, 0.0); // Vermelho para X
                glLineWidth(4.0);
                glBegin(GL_LINES);
                glVertex2f(centerX - 50, centerY - 50);
                glVertex2f(centerX + 50, centerY + 50);
                glVertex2f(centerX + 50, centerY - 50);
                glVertex2f(centerX - 50, centerY + 50);
                glEnd();
            } else if (board[i][j] == 'O') {
                glColor3f(0.0, 0.0, 1.0); // Azul para O
                glLineWidth(4.0);
                glBegin(GL_LINE_LOOP);
                for (int k = 0; k < 360; k += 10) {
                    float angle = k * 3.14159 / 180;
                    glVertex2f(centerX + 50 * cos(angle), centerY + 50 * sin(angle));
                }
                glEnd();
            }
        }
    }
}

// Função de display principal
void display() {
    glClear(GL_COLOR_BUFFER_BIT);
    
    // Desenha o placar
    glColor3f(1.0, 1.0, 1.0);
    stringstream scoreText;
    scoreText << "Player 1 (X): " << score.player1 << "  Player 2 (O): " << score.player2 << "  Draws: " << score.draws;
    drawText(10, 20, scoreText.str());
    
    // Desenha mensagem de status
    glColor3f(0.0, 1.0, 0.0);
    drawText(10, 680, inputMessage);
    
    // Desenha instruções de entrada se no modo teclado
    if (!inputMode && currentState != GAME_OVER) {
        glColor3f(1.0, 1.0, 0.0);
        string coordText = "Enter coordinates (0-2,0-2): " + to_string(inputRow) + "," + to_string(inputCol);
        drawText(10, 650, coordText);
    }
    
    // Desenha o tabuleiro
    drawBoard();
    
    glutSwapBuffers();
}

// Função para processar cliques do mouse
void mouseClick(int button, int state, int x, int y) {
    if (button == GLUT_LEFT_BUTTON && state == GLUT_DOWN && inputMode) {
        if (currentState == GAME_OVER) {
            restartGame();
        } else {
            // Converte coordenadas do mouse para posição no tabuleiro
            int col = x / (WINDOW_WIDTH / BOARD_SIZE);
            int row = (WINDOW_HEIGHT - y - 50) / (600 / BOARD_SIZE);
            
            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                makeMove(row, col);
            }
        }
        glutPostRedisplay();
    }
}

// Função para processar entrada do teclado
void keyboard(unsigned char key, int x, int y) {
    if (key == 13) { // Enter
        if (currentState == GAME_OVER) {
            restartGame();
        } else if (!inputMode && inputRow != -1 && inputCol != -1) {
            makeMove(inputRow, inputCol);
            inputRow = inputCol = -1;
        }
    } else if (key == 'm' || key == 'M') {
        inputMode = !inputMode;
        inputMessage = inputMode ? "Mouse mode activated" : "Keyboard mode activated";
    } else if (key >= '0' && key <= '2' && !inputMode && currentState != GAME_OVER) {
        if (inputRow == -1) {
            inputRow = key - '0';
        } else if (inputCol == -1) {
            inputCol = key - '0';
        }
    } else if (key == 'r' || key == 'R') {
        restartGame();
    }
    
    glutPostRedisplay();
}

// Função de reshape
void reshape(int w, int h) {
    glViewport(0, 0, w, h);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluOrtho2D(0, WINDOW_WIDTH, 0, WINDOW_HEIGHT);
    glMatrixMode(GL_MODELVIEW);
}

// Função de inicialização do OpenGL
void init() {
    glClearColor(0.0, 0.0, 0.0, 1.0);
    initializeGame();
    score.player1 = score.player2 = score.draws = 0;
}

// Função principal
int main(int argc, char** argv) {
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB);
    glutInitWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    glutInitWindowPosition(100, 100);
    glutCreateWindow("Tic-Tac-Toe Game");
    
    init();
    
    glutDisplayFunc(display);
    glutReshapeFunc(reshape);
    glutMouseFunc(mouseClick);
    glutKeyboardFunc(keyboard);
    
    cout << "=== Tic-Tac-Toe Game ===" << endl;
    cout << "Controls:" << endl;
    cout << "- Mouse: Click on the board to make moves" << endl;
    cout << "- Keyboard: Press 'M' to toggle input mode" << endl;
    cout << "- In keyboard mode: Enter coordinates (0-2,0-2) followed by Enter" << endl;
    cout << "- Press 'R' to restart the game at any time" << endl;
    cout << "- Press Enter when game is over to play again" << endl;
    
    glutMainLoop();
    
    return 0;
}