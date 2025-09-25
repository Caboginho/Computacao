#include <GL/glut.h>
#include "../include/Utils.h"
#include "../include/Audio.h"

void init(){
    glClearColor(1,1,1,1);
    glMatrixMode(GL_PROJECTION); gluOrtho2D(-300,300,-300,300);
    playSound("assets/start.wav");
}
