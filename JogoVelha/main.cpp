#include <GL/glut.h>
#include "include/Game.h"
#include "include/Renderer.h"
#include "include/Input.h"
#include "include/Utils.h"

int main(int argc,char**argv){
    glutInit(&argc,argv);
    glutInitDisplayMode(GLUT_DOUBLE|GLUT_RGB);
    glutInitWindowSize(width,height);
    glutCreateWindow("Tic Tac Toe - OpenGL");
    init();
    glutDisplayFunc(display);
    glutIdleFunc(display);
    glutMouseFunc(mouse);
    glutKeyboardFunc(keyboard);
    glutMainLoop();
    return 0;
}
