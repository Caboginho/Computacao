#ifndef GAME_H
#define GAME_H

extern const int width, height;
extern int board[3][3];
extern int currentPlayer;
extern bool gameOver;
extern int winner;
extern int winLine[2][2];
extern int animationStep[3][3];
extern int animationMax;
extern int winLineStep;

void resetGame();
void checkWinner();

#endif
