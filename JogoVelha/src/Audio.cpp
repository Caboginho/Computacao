#include <windows.h>
#include <mmsystem.h>
#include "../include/Audio.h"

void playSound(const char* file) {
    PlaySound(file, NULL, SND_FILENAME | SND_ASYNC);
}
