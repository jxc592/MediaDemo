//
// Created by kevin on 19-8-20.
//

#ifndef MYAPPLICATION2_AUDIOPLAYER_H
#define MYAPPLICATION2_AUDIOPLAYER_H

#include <aaudio/AAudio.h>
#include <string>

class AudioPlayer {

private:
    std::string filename;
    int32_t buffersize;
    int8_t sampleFormat;
    int32_t sampleRate;
    AAudioStream *mAudioStream;
    AAudioStreamBuilder *mAudioStreamBuilder;

public:
    void setDataSource(std::string);
    void setBufferSize(int32_t);
    void setSampleFormat(int8_t);
    void setSampleRate(int32_t);
    AudioPlayer();


};


#endif //MYAPPLICATION2_AUDIOPLAYER_H
