//
// Created by kevin on 19-8-20.
//

#ifndef MYAPPLICATION2_FFMPEGHELPER_H
#define MYAPPLICATION2_FFMPEGHELPER_H


#include <string>
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include "log.h"
#include <unistd.h>
class ffmpegsource {



private:
    const char *datasource;
    bool hasAudio;
    bool hasVideo;
    AVFormatContext *mMediaFormatContext;
    AVCodecContext *mAudioCodecContext;
    AVCodecContext *mVideoCodecContext;
    AVCodec *mAudioCodec;
    AVCodec *mVideoCodec;

public:
    ffmpegsource(const char *datasource) : datasource(datasource) {
        init();
    }

    ~ffmpegsource(){
        //deInit();
    };
//    void setDataSource(std::string source) {
//        datasource = source;
//    };


    void init(){
        av_register_all();
        mMediaFormatContext = avformat_alloc_context();
        int formatOpenErr = avformat_open_input(&mMediaFormatContext,datasource,NULL,NULL);
        if(formatOpenErr<0) {
            LOGE("avformat_open_input error %d",formatOpenErr);
            return;
        }

        for(int i=0;i<mMediaFormatContext->nb_streams;i++) {
            if(mMediaFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
                mAudioCodecContext = mMediaFormatContext->streams[i]->codec;
                hasAudio = true;
                mAudioCodec = avcodec_find_decoder(mAudioCodecContext->codec_id);
                int res = avcodec_open2(mAudioCodecContext,mAudioCodec,NULL);
                LOGE("open audio decodec %d",res);
            }
            if(mMediaFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
                mVideoCodecContext = mMediaFormatContext->streams[i]->codec;

                hasVideo = true;
                mVideoCodec = avcodec_find_decoder(mVideoCodecContext->codec_id);
                int res = avcodec_open2(mVideoCodecContext,mVideoCodec,NULL);

                LOGE("open video decodec %d",res);
            }
        }
    };

    int readDecodedAudioData(){
        AVPacket *avPacket = av_packet_alloc();

        av_init_packet(avPacket);

        AVFrame *avFrame = av_frame_alloc();

        while (av_read_frame(mMediaFormatContext,avPacket) >= 0) {
            avcodec_send_packet(mAudioCodecContext,avPacket);
            avcodec_receive_frame(mAudioCodecContext,avFrame);
            LOGE("avframe info \n音频桢数:%d\n 音频音轨数目：%d\n音频包大小：%d\nframe linesince:%d",avFrame->nb_samples,avFrame->channels,avFrame->pkt_size,avFrame->linesize);
            //av_free_packet(avPacket);
            usleep(1000*1000 * 1);
        }


        av_frame_free(&avFrame);
        //av_packet_unref(avPacket);
        av_packet_free(&avPacket);
    };

    int decodeVideo(){
        AVPacket *avPacket = av_packet_alloc();

        av_init_packet(avPacket);

        AVFrame *avFrame = av_frame_alloc();

        while (av_read_frame(mMediaFormatContext,avPacket) >=0) {
            avcodec_send_packet(mVideoCodecContext,avPacket);
            avcodec_receive_frame(mVideoCodecContext,avFrame);
            LOGE("avframe info \n音频桢数:%d\n 音频音轨数目：%d\n音频包大小：%d\nframe linesince:%d",avFrame->nb_samples,avFrame->channels,avFrame->pkt_size,avFrame->linesize);
        }

        //av_frame_free(&avFrame);
        //av_packet_unref(avPacket);
        //av_packet_free(&avPacket);
    };


    void deInit(){
        avcodec_close(mAudioCodecContext);
        avcodec_close(mVideoCodecContext);
        avcodec_free_context(&mAudioCodecContext);
        avcodec_free_context(&mVideoCodecContext);
        avformat_close_input(&mMediaFormatContext);
    }


};


#endif //MYAPPLICATION2_FFMPEGHELPER_H
