//
// Created by kevin on 20-6-14.
//

#ifndef MYAPPLICATION2_FFMPEG_H
#define MYAPPLICATION2_FFMPEG_H


#include <string>
extern "C"
{
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
}
#include "log.h"
#include <unistd.h>
#include <android/bitmap.h>


void getAlbumArt(const char *path, uint8_t **data, int *size);

#endif //MYAPPLICATION2_FFMPEG_H



