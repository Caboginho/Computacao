#include <GL/glut.h>
#include <windows.h>
#include "../include/Game.h"
#include "../include/Input.h"
#include "../include/Audio.h"

void mouse(int button, int state, int x, int y) {
    if(button==GLUT_LEFT_BUTTON && state==GLUT_DOWN && !gameOver){
        float fx = (float)x / width * 600 - 300;
        float fy = 300 - (float)y / height * 600;

        int col = (int)((fx + 300) / 200);
        int row = (int)((300 - fy) / 200);

        if(row>=0 && row<3 && col>=0 && col<3 && board[row][col]==0){
            board[row][col]=currentPlayer;
            currentPlayer=(currentPlayer==1)?2:1;
            playSound("assets/move.wav");
        }
        checkWinner(); 
        glutPostRedisplay();
    }
}

void keyboard(unsigned char key,int,int){
    if(key=='r'||key=='R') resetGame();
    else if(key==27){ playSound("assets/exit.wav"); Sleep(500); exit(0);}
}
