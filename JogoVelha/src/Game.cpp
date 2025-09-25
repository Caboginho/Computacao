#include <GL/glut.h>
#include "../include/Game.h"
#include "../include/Audio.h"

const int width = 600, height = 600;
int board[3][3];
int currentPlayer = 1;
bool gameOver = false;
int winner = 0;
int winLine[2][2];
int animationStep[3][3];
int animationMax = 60;
int winLineStep = 0;

void resetGame() {
    for (int i=0;i<3;i++) 
        for(int j=0;j<3;j++) {
            board[i][j]=0;
            animationStep[i][j]=0;
        }
    currentPlayer=1; 
    gameOver=false; 
    winner=0;
    winLine[0][0]=winLine[0][1]=winLine[1][0]=winLine[1][1]=-1;
    winLineStep = 0;
    playSound("assets/start.wav");
    glutPostRedisplay();
}

void checkWinner() {
    for(int i=0;i<3;i++){
        if(board[i][0] && board[i][0]==board[i][1] && board[i][1]==board[i][2]){
            winner=board[i][0]; gameOver=true; playSound("assets/win.wav");
            winLine[0][0]=i; winLine[0][1]=0; winLine[1][0]=i; winLine[1][1]=2; return;
        }
        if(board[0][i] && board[0][i]==board[1][i] && board[1][i]==board[2][i]){
            winner=board[0][i]; gameOver=true; playSound("assets/win.wav");
            winLine[0][0]=0; winLine[0][1]=i; winLine[1][0]=2; winLine[1][1]=i; return;
        }
    }
    if(board[0][0] && board[0][0]==board[1][1] && board[1][1]==board[2][2]){
        winner=board[0][0]; gameOver=true; playSound("assets/win.wav");
        winLine[0][0]=0; winLine[0][1]=0; winLine[1][0]=2; winLine[1][1]=2; return;
    }
    if(board[0][2] && board[0][2]==board[1][1] && board[1][1]==board[2][0]){
        winner=board[0][2]; gameOver=true; playSound("assets/win.wav");
        winLine[0][0]=0; winLine[0][1]=2; winLine[1][0]=2; winLine[1][1]=0; return;
    }
    bool full=true;
    for(int i=0;i<3;i++) for(int j=0;j<3;j++) if(board[i][j]==0) full=false;
    if(full){ winner=3; gameOver=true; playSound("assets/draw.wav"); }
}
