#include <GL/glut.h>
#include <windows.h>
#include <mmsystem.h>
#include <iostream>
#include <cmath>
using namespace std;

const int width = 600, height = 600;
int board[3][3]; // 0 = vazio, 1 = X, 2 = O
int currentPlayer = 1;
bool gameOver = false;
int winner = 0; // 0 = ninguém, 1 = X, 2 = O, 3 = empate
int winLine[2][2]; // linha vencedora
int animationStep[3][3]; // passos da animação de cada célula
int animationMax = 60;   // frames para animar X/O e linha vencedora
int winLineStep = 0;     // passo da animação da linha vencedora

void playSound(const char* file) { PlaySound(file, NULL, SND_FILENAME | SND_ASYNC); }

void resetGame() {
    for (int i=0;i<3;i++) for(int j=0;j<3;j++) board[i][j]=0, animationStep[i][j]=0;
    currentPlayer=1; gameOver=false; winner=0;
    winLine[0][0]=winLine[0][1]=winLine[1][0]=winLine[1][1]=-1;
    winLineStep = 0;
    playSound("start.wav");
    glutPostRedisplay();
}

void drawText(const char* text,float x,float y){
    glRasterPos2f(x,y);
    while(*text) { glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18,*text); text++; }
}

void drawBackground(){
    if(gameOver){
        if(winner==3) glColor3f(1.0f,1.0f,0.6f); // empate = amarelo
        else glColor3f(0.6f,1.0f,0.6f); // vitória = verde
        glBegin(GL_QUADS);
        glVertex2f(-300,-300); glVertex2f(300,-300);
        glVertex2f(300,300); glVertex2f(-300,300);
        glEnd();
    } else {
        glBegin(GL_QUADS);
        glColor3f(0.8f,0.9f,1.0f); glVertex2f(-300,-300);
        glColor3f(0.8f,0.9f,1.0f); glVertex2f(300,-300);
        glColor3f(1.0f,1.0f,1.0f); glVertex2f(300,300);
        glColor3f(1.0f,1.0f,1.0f); glVertex2f(-300,300);
        glEnd();
    }
}

void drawBoard(){
    glColor3f(0.1f,0.1f,0.1f); glLineWidth(8);
    glBegin(GL_LINES);
    glVertex2f(-100,300); glVertex2f(-100,-300);
    glVertex2f(100,300); glVertex2f(100,-300);
    glVertex2f(-300,100); glVertex2f(300,100);
    glVertex2f(-300,-100); glVertex2f(300,-100);
    glEnd();
}

void drawX(float x,float y,int step){
    float progress = (float)step/animationMax;
    glColor3f(0.9,0.2,0.2); glLineWidth(10);
    glBegin(GL_LINES);
    glVertex2f(x-60, y-60); glVertex2f(x-60 + 120*progress, y-60 + 120*progress);
    glVertex2f(x-60, y+60); glVertex2f(x-60 + 120*progress, y+60 - 120*progress);
    glEnd();
}

void drawO(float x,float y,int step){
    float progress = (float)step/animationMax;
    glColor3f(0.2,0.4,0.9); glLineWidth(5); glBegin(GL_LINE_LOOP);
    for(int i=0;i<100;i++){
        float theta = 2.0f*3.1415926f*i/100.0f*progress;
        float dx=60*cosf(theta), dy=60*sinf(theta);
        glVertex2f(x+dx,y+dy);
    }
    glEnd();
}

void drawWinLine(){
    if(winLine[0][0]==-1) return;
    float progress = (float)winLineStep/animationMax;
    float x1 = -200+winLine[0][1]*200;
    float y1 = 200-winLine[0][0]*200;
    float x2 = -200+winLine[1][1]*200;
    float y2 = 200-winLine[1][0]*200;
    float dx = x2 - x1;
    float dy = y2 - y1;

    glColor3f(1,0.8,0); glLineWidth(12);
    glBegin(GL_LINES);
    glVertex2f(x1, y1);
    glVertex2f(x1 + dx*progress, y1 + dy*progress);
    glEnd();

    if(winLineStep < animationMax) winLineStep++;
}

void checkWinner(){
    for(int i=0;i<3;i++){
        if(board[i][0] && board[i][0]==board[i][1] && board[i][1]==board[i][2]){
            winner=board[i][0]; gameOver=true; playSound("win.wav");
            winLine[0][0]=i; winLine[0][1]=0; winLine[1][0]=i; winLine[1][1]=2; return;
        }
        if(board[0][i] && board[0][i]==board[1][i] && board[1][i]==board[2][i]){
            winner=board[0][i]; gameOver=true; playSound("win.wav");
            winLine[0][0]=0; winLine[0][1]=i; winLine[1][0]=2; winLine[1][1]=i; return;
        }
    }
    if(board[0][0] && board[0][0]==board[1][1] && board[1][1]==board[2][2]){
        winner=board[0][0]; gameOver=true; playSound("win.wav");
        winLine[0][0]=0; winLine[0][1]=0; winLine[1][0]=2; winLine[1][1]=2; return;
    }
    if(board[0][2] && board[0][2]==board[1][1] && board[1][1]==board[2][0]){
        winner=board[0][2]; gameOver=true; playSound("win.wav");
        winLine[0][0]=0; winLine[0][1]=2; winLine[1][0]=2; winLine[1][1]=0; return;
    }
    bool full=true;
    for(int i=0;i<3;i++) for(int j=0;j<3;j++) if(board[i][j]==0) full=false;
    if(full){ winner=3; gameOver=true; playSound("draw.wav"); }
}

void display(){
    glClear(GL_COLOR_BUFFER_BIT);
    drawBackground();
    drawBoard();

    for(int i=0;i<3;i++){
        for(int j=0;j<3;j++){
            float cx=-200+j*200; float cy=200-i*200;
            if(board[i][j]==1) drawX(cx,cy,animationStep[i][j]);
            else if(board[i][j]==2) drawO(cx,cy,animationStep[i][j]);
            if(animationStep[i][j]<animationMax) animationStep[i][j]++;
        }
    }

    if(gameOver) drawWinLine();

    if(gameOver){
        glColor3f(0,0,0);
        if(winner==1) drawText("Jogador X venceu!",-80,0);
        else if(winner==2) drawText("Jogador O venceu!",-80,0);
        else if(winner==3) drawText("Empate!",-40,0);
        drawText("Pressione R para reiniciar",-110,-40);
        drawText("Pressione ESC para sair",-100,-70);
    }

    glutSwapBuffers();
}

void mouse(int button,int state,int x,int y){
    if(button==GLUT_LEFT_BUTTON && state==GLUT_DOWN && !gameOver){
        // Convertendo pixels do mouse para coordenadas OpenGL (-300 a 300)
        float fx = (float)x / width * 600 - 300;
        float fy = 300 - (float)y / height * 600; // inverter y

        // Converter coordenadas para índices do tabuleiro
        int col = (int)((fx + 300) / 200);
        int row = (int)((300 - fy) / 200);

        if(row>=0 && row<3 && col>=0 && col<3 && board[row][col]==0){
            board[row][col]=currentPlayer;
            currentPlayer=(currentPlayer==1)?2:1;
            playSound("move.wav");
        }
        checkWinner(); 
        glutPostRedisplay();
    }
}

void keyboard(unsigned char key,int,int){
    if(key=='r'||key=='R') resetGame();
    else if(key==27){ playSound("exit.wav"); Sleep(500); exit(0);}
}

void init(){
    glClearColor(1,1,1,1);
    glMatrixMode(GL_PROJECTION); gluOrtho2D(-300,300,-300,300);
    playSound("start.wav");
}

int main(int argc,char**argv){
    glutInit(&argc,argv);
    glutInitDisplayMode(GLUT_DOUBLE|GLUT_RGB);
    glutInitWindowSize(width,height);
    glutCreateWindow("Tic Tac Toe - OpenGL");
    init();
    glutDisplayFunc(display);
    glutIdleFunc(display); // animação contínua
    glutMouseFunc(mouse);
    glutKeyboardFunc(keyboard);
    glutMainLoop();
    return 0;
}

// Compile no Windows com MinGW:
// g++ jogodavelha.cpp -o jogodavelha -lopengl32 -lglu32 -lfreeglut -lwinmm
