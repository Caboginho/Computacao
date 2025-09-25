#ifndef RENDERER_H
#define RENDERER_H

void drawText(const char* text, float x, float y);
void drawBackground();
void drawBoard();
void drawX(float x, float y, int step);
void drawO(float x, float y, int step);
void drawWinLine();
void display();

#endif
