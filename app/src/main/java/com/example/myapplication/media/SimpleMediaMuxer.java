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


    MediaMuxer mediaMuxer;
    MediaCodec mVideoCodec;
    MediaCodec mAudioCodec;

    long mLastAudioPresentationTimeUs = 0;
    long mLastVideopresentationTimeUs = 0;

    long mCachedAudioPresentationTimeUs = 0;
    long mCachedVideopresentationTimeUs = 0;

    boolean audioAppendFlag = false;
    boolean videoAppendFlag = false;
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
        mediaMuxer.addTrack(format);
    }

    public void start(){
        if(mVidioFormat != null) hasVideo = true;
        if(mAudioFormat != null) hasAudio = true;
        mediaMuxer.start();
    }


    public void stop(){
        if(hasAudio && mAudioCodec !=null) {
            mAudioCodec.stop();
            mAudioCodec.release();
        }
        if(hasVideo && mVideoCodec != null) {
            mVideoCodec.stop();
            mVideoCodec.release();
        }
        mediaMuxer.stop();
        mediaMuxer.release();
    }

    MediaCodec initEncoder(MediaFormat format) {
        MediaCodec codec = null;
        try {
            codec = MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"initEncoder error");
            return null;
        }
        codec.configure(format,null,null,0);
        codec.start();
        return codec;
    }

    public ByteBuffer encodeAudio(byte[] data, MediaCodec.BufferInfo info)  {
        if(mAudioCodec == null) {
            mAudioCodec = initEncoder(mAudioFormat);
            if(mAudioCodec == null)
            {
                Log.d(TAG,"encodeAudio return dueto codec not ready");
                return null;
            }
        }
        return encode(data,mAudioCodec,info);
    }

    public ByteBuffer encodeVideo(byte[] data, MediaCodec.BufferInfo info)  {
        if(mVideoCodec == null) {
            mVideoCodec = initEncoder(mAudioFormat);
            if(mVideoCodec == null)
            {
                Log.d(TAG,"encodeVideo return dueto codec not ready");
                return null;
            }
        }
        return encode(data,mVideoCodec,info);
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
        if (audioAppendFlag) {
            long diff = info.presentationTimeUs - mCachedAudioPresentationTimeUs;
            info.presentationTimeUs += diff;
        } else {
            long pts = info.presentationTimeUs;
            if (pts < mLastAudioPresentationTimeUs) {
                audioAppendFlag = true;
                info.presentationTimeUs += mLastAudioPresentationTimeUs;
                mCachedAudioPresentationTimeUs = info.presentationTimeUs;
            }
        }
        ByteBuffer out = encodeAudio(data,info);
        mediaMuxer.writeSampleData(audioIdx,out,info);
        mLastAudioPresentationTimeUs = info.presentationTimeUs;
    }

    public void writeVideoData(byte[] data, MediaCodec.BufferInfo info){

        if (videoAppendFlag) {
            long diff = info.presentationTimeUs - mCachedVideopresentationTimeUs;
            info.presentationTimeUs += diff;
        } else {
            long pts = info.presentationTimeUs;
            if (pts < mLastVideopresentationTimeUs) {
                videoAppendFlag = true;
                info.presentationTimeUs += mLastVideopresentationTimeUs;
                mCachedVideopresentationTimeUs = info.presentationTimeUs;
            }
        }

        ByteBuffer out = encodeAudio(data,info);
        mediaMuxer.writeSampleData(videoIdx,out,info);
    }

    /**
     * @param buffer the packet which was from extractor
     * @param info bufferinfo
     */
    public void writeAudioData(ByteBuffer buffer, MediaCodec.BufferInfo info){
        if (audioAppendFlag) {
            long diff = info.presentationTimeUs - mCachedAudioPresentationTimeUs;
            info.presentationTimeUs += diff;
        } else {
            long pts = info.presentationTimeUs;
            if (pts < mLastAudioPresentationTimeUs) {
                audioAppendFlag = true;
                info.presentationTimeUs += mLastAudioPresentationTimeUs;
                mCachedAudioPresentationTimeUs = info.presentationTimeUs;
            }
        }
        mediaMuxer.writeSampleData(audioIdx,buffer,info);
        mLastAudioPresentationTimeUs = info.presentationTimeUs;
    }
    public void writeVideoData(ByteBuffer buffer, MediaCodec.BufferInfo info){
        mediaMuxer.writeSampleData(videoIdx,buffer,info);
    }


}
