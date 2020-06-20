package com.example.myapplication.media;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class SimpleMediaMuxer {

    public static final String TAG= "jxc_SimpleMediaMuxer";

    ArrayList<MediaFormat> formats;
    MediaFormat mAudioFormat;
    MediaFormat mVidioFormat;
    String mMuxterPath;
    boolean hasAudio = false;
    boolean hasVideo = false;
    byte audioIdx = -1;
    byte videoIdx = -1;


    MediaMuxer mediaMuxer

    // create aac audio format
    public static MediaFormat createSimpleAudioFormat (int sampleRate,int channel) {
        MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,sampleRate,channel);
        return format;
    }

    // create aac audio format
    public static MediaFormat createSimpleVideoFormat (int width,int height) {
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,width,width);
        return format;
    }


    public SimpleMediaMuxer(String savedPath) throws IOException {
        mediaMuxer = new MediaMuxer(savedPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        formats = new ArrayList<>();
    }

    public void addTrack(MediaFormat format) {
        String mime = format.getString(MediaFormat.KEY_MIME);
        formats.add(format);
        if(mime.startsWith("audio")) {
            mAudioFormat = format;
            audioIdx = (byte) formats.indexOf(format);
        } else if(mime.startsWith("video")) {
            mVidioFormat = format;
            videoIdx = (byte) formats.indexOf(format);
        }
    }

    void start(){
        if(mVidioFormat != null) hasVideo = true;
        if(mAudioFormat != null) hasAudio = true;
        mediaMuxer.start();
    }

    MediaCodec initEncoder(MediaFormat format) throws IOException {
        MediaCodec codec = MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME));
        codec.configure(format,null,null,0);
        codec.start();
        return codec;
    }

    public ByteBuffer encodeAudio(byte[] data, MediaCodec.BufferInfo info)  {
        //encode(data,)
    }

    public ByteBuffer encode(byte[] data, MediaCodec codec,MediaCodec.BufferInfo info)  {
        int inputIdx = codec.dequeueInputBuffer(10);
        if(inputIdx >= 0 ) {
            Log.d(TAG,"dequeueInputBuffer idx = " + inputIdx);
        }

        ByteBuffer byteBuffer = codec.getInputBuffer(inputIdx);

        byteBuffer.clear();
        byteBuffer.put(data);


        ByteBuffer outBuffer;
        int outputIdx = codec.dequeueOutputBuffer(info,10);
        while (true) {
            if (outputIdx == MediaCodec.INFO_TRY_AGAIN_LATER) {
                continue;
            }
            if(outputIdx >=0) {
                outBuffer = codec.getOutputBuffer(outputIdx);
                break;
            }
        }
        return outBuffer;
    }

    /**
     * @param data the decoded byte array
     * @param info bufferinfo
     */
    public void writeAudioData(byte[] data, MediaCodec.BufferInfo info){
        ByteBuffer out = encodeAudio(data,info);
        mediaMuxer.writeSampleData(audioIdx,out,info);
    }

    public void writeVideoData(byte[] data, MediaCodec.BufferInfo info){
        ByteBuffer out = encodeAudio(data,info);
        mediaMuxer.writeSampleData(videoIdx,out,info);
    }

    /**
     * @param buffer the packet which was from extractor
     * @param info bufferinfo
     */
    public void writeAudioData(ByteBuffer buffer, MediaCodec.BufferInfo info){
        mediaMuxer.writeSampleData(audioIdx,buffer,info);
    }
    public void writeVideoData(ByteBuffer buffer, MediaCodec.BufferInfo info){
        mediaMuxer.writeSampleData(videoIdx,buffer,info);
    }


}
