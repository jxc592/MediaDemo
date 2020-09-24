//
// Created by kevin on 20-6-14.
//

#include "ffmpeg.h"

void getAlbumArt(const char *path, uint8_t **data, int *size) {
    AVFormatContext *avFormatContext = avformat_alloc_context();
    int err = avformat_open_input(&avFormatContext, path, NULL, NULL);

    if (err < 0) {
        LOGE("avformat_open_input error %d", err);
        return;
    }
    for (int i = 0; i < avFormatContext->nb_streams; i++) {
        AVStream *avStream = avFormatContext->streams[i];
        if (avStream && avStream->disposition & AV_DISPOSITION_ATTACHED_PIC) {
            AVPacket pkt = avStream->attached_pic;
            *data = pkt.data;
            *size = pkt.size;
            break;
        }
    }
    avformat_close_input(&avFormatContext);
}

void extractAudio(const char *input,char *output) {
    AVFormatContext *avFormatContext = avformat_alloc_context();
    int err = avformat_open_input(&avFormatContext, input, NULL, NULL);

    if (err < 0) {
        LOGE("avformat_open_input error %d", err);
        return;
    }
    int streamIdx =av_find_best_stream(avFormatContext,AVMEDIA_TYPE_AUDIO, -1,-1,NULL,-1);

    AVStream *avStream;
    if(streamIdx != AVERROR_STREAM_NOT_FOUND && streamIdx != AVERROR_DECODER_NOT_FOUND) {
        avStream = avFormatContext->streams[streamIdx];
    }




    //AVIOContext *avioContext = avio_alloc_context()
}