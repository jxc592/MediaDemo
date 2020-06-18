package com.example.myapplication.media;
 

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import java.io.IOException;
import java.nio.ByteBuffer;


public class MediaUtils {
 

    public void deMP3(String path){
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int audioIndex = -1;//音频通道
        int videoIndex = -1;//视频通道
        //获取多媒体文件信息
        MediaFormat audioFormat=null;
        MediaFormat videoFormat=null;
        MediaFormat trackFormat;
        for (int i = 0; i < extractor.getTrackCount()/*轨道数*/; i++) {
             trackFormat = extractor.getTrackFormat(i);
            if (trackFormat.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                audioIndex = i;
                audioFormat=trackFormat;
            }
            if (trackFormat.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                videoIndex = i;
                videoFormat=trackFormat;
            }
        }
        extractor.selectTrack(audioIndex);//切换到音频通道

        doMp3(audioFormat,extractor,audioIndex);
        //释放音频的轨道
        extractor.unselectTrack(audioIndex);
        extractor.selectTrack(videoIndex);//切换到视频通道
        doMP4(videoFormat,extractor,videoIndex);
        extractor.release();
        compositing();
    }
 
    /**
     * 提取MP3
     * @param trackFormat
     * @param extractor
     * @param audioIndex
     */
    public void doMp3(MediaFormat trackFormat,MediaExtractor extractor,int audioIndex){
        MediaMuxer   mediaMuxer = null;
        try {
 
            mediaMuxer = new MediaMuxer( MP3PATH, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            Log.e("IOException",""+e.toString());
            e.printStackTrace();
        }
 
        //添加轨道 得到轨道所在的index
        int writeAudioIndex = mediaMuxer.addTrack(trackFormat);
        mediaMuxer.start();
        //声明缓冲去，用于读取一帧的数据存放
        ByteBuffer byteBuffer = ByteBuffer.allocate(trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        long stampTime = 0;
        //获取相邻帧之间的间隔时间
        {
            //读取一帧
            extractor.readSampleData(byteBuffer, 0);
            if (extractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                extractor.advance();
            }
            extractor.readSampleData(byteBuffer, 0);
            long secondTime = extractor.getSampleTime();
            //跳到下一帧
            extractor.advance();
            extractor.readSampleData(byteBuffer, 0);
            //得到这一帧的时间
            long thirdTime = extractor.getSampleTime();
            stampTime = Math.abs(thirdTime - secondTime);
        }
        //重新切换此信道，不然上面跳过了3帧,造成前面的帧数模糊
        extractor.unselectTrack(audioIndex);
        extractor.selectTrack(audioIndex);
        while (true) {
            int readSampleSize = extractor.readSampleData(byteBuffer, 0);
            if (readSampleSize < 0) {
                break;
            }
            extractor.advance();//移动到下一帧
            bufferInfo.size = readSampleSize;
            bufferInfo.flags = extractor.getSampleFlags();
            bufferInfo.offset = 0;
            bufferInfo.presentationTimeUs += stampTime;
 
            mediaMuxer.writeSampleData(writeAudioIndex, byteBuffer, bufferInfo);
        }
        mediaMuxer.stop();
        mediaMuxer.release();
    }
 
    /**
     * 提取MP4
     * @param trackFormat
     * @param extractor
     * @param videoIndex
     */
    public void doMP4(MediaFormat trackFormat,MediaExtractor extractor,int videoIndex){
        MediaMuxer   mediaMuxer = null;
        try {
            mediaMuxer = new MediaMuxer(MP4PATH, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            Log.e("IOException",""+e.toString());
            e.printStackTrace();
        }
 
        int writevideoIndex = mediaMuxer.addTrack(trackFormat);
        mediaMuxer.start();
        ByteBuffer byteBuffer = ByteBuffer.allocate(trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        long stampTime = 0;
        //获取相邻帧之间的间隔时间
        {
            extractor.readSampleData(byteBuffer, 0);
            if (extractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                extractor.advance();
            }
            extractor.readSampleData(byteBuffer, 0);
            long secondTime = extractor.getSampleTime();
            extractor.advance();
            extractor.readSampleData(byteBuffer, 0);
            long thirdTime = extractor.getSampleTime();
            stampTime = Math.abs(thirdTime - secondTime);
        }
        //重新切换此信道，不然上面跳过了3帧,造成前面的帧数模糊
        extractor.unselectTrack(videoIndex);
        extractor.selectTrack(videoIndex);
        while (true) {
            int readSampleSize = extractor.readSampleData(byteBuffer, 0);
            if (readSampleSize < 0) {
                break;
            }
            extractor.advance();//移动到下一帧
            bufferInfo.size = readSampleSize;
            bufferInfo.flags = extractor.getSampleFlags();
            bufferInfo.offset = 0;
            bufferInfo.presentationTimeUs += stampTime;
 
            //往往对应的轨道填写数据
            mediaMuxer.writeSampleData(writevideoIndex, byteBuffer, bufferInfo);
        }
        mediaMuxer.stop();
        mediaMuxer.release();
    }
 
    /**
     * 合成视频和音频
     */
    public void compositing(){
        MediaExtractor videoExtractor = new MediaExtractor();
        try {
            videoExtractor.setDataSource(MP4PATH);
 
        MediaFormat videoFormat = null;
        int videoTrackIndex = -1;
        int videoTrackCount = videoExtractor.getTrackCount();
        for (int i = 0; i < videoTrackCount; i++) {
            videoFormat = videoExtractor.getTrackFormat(i);
            String mimeType = videoFormat.getString(MediaFormat.KEY_MIME);
            if (mimeType.startsWith("video/")) {
                videoTrackIndex = i;
                break;
            }
        }
            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(MP3PATH);
            MediaFormat audioFormat = null;
            int audioTrackIndex = -1;
            int audioTrackCount = audioExtractor.getTrackCount();
            for (int i = 0; i < audioTrackCount; i++) {
                audioFormat = audioExtractor.getTrackFormat(i);
                String mimeType = audioFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("audio/")) {
                    audioTrackIndex = i;
                    break;
                }
            }
            videoExtractor.selectTrack(videoTrackIndex);
            audioExtractor.selectTrack(audioTrackIndex);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
 
            MediaMuxer mediaMuxer = new MediaMuxer(COMPOSITINGPATH, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeVideoTrackIndex = mediaMuxer.addTrack(videoFormat);
            int writeAudioTrackIndex = mediaMuxer.addTrack(audioFormat);
            mediaMuxer.start();
 
            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            long sampleTime = 0;
            {
                videoExtractor.readSampleData(byteBuffer, 0);
                if (videoExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    videoExtractor.advance();
                }
                videoExtractor.readSampleData(byteBuffer, 0);
                long secondTime = videoExtractor.getSampleTime();
                videoExtractor.advance();
                long thirdTime = videoExtractor.getSampleTime();
                sampleTime = Math.abs(thirdTime - secondTime);
            }
            videoExtractor.unselectTrack(videoTrackIndex);
            videoExtractor.selectTrack(videoTrackIndex);
 
            while (true) {
                int readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0);
                if (readVideoSampleSize < 0) {
                    break;
                }
                videoBufferInfo.size = readVideoSampleSize;
                videoBufferInfo.presentationTimeUs += sampleTime;
                videoBufferInfo.offset = 0;
                videoBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo);
                videoExtractor.advance();
            }
 
            while (true) {
                int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
                if (readAudioSampleSize < 0) {
                    break;
                }
 
                audioBufferInfo.size = readAudioSampleSize;
                audioBufferInfo.presentationTimeUs += sampleTime;
                audioBufferInfo.offset = 0;
                audioBufferInfo.flags = videoExtractor.getSampleFlags();

                mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
                audioExtractor.advance();
            }
 
            mediaMuxer.stop();
            mediaMuxer.release();
            videoExtractor.release();
            audioExtractor.release();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
 
    }
}
