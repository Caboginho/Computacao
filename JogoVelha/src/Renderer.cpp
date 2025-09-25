#include <GL/glut.h>
#include <cmath>
#include "../include/Game.h"
#include "../include/Renderer.h"

void drawText(const char* text,float x,float y){
    glRasterPos2f(x,y);
    while(*text) { glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18,*text); text++; }
}

void drawBackground(){
    if(gameOver){
        if(winner==3) glColor3f(1.0f,1.0f,0.6f);
        else glColor3f(0.6f,1.0f,0.6f);
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
