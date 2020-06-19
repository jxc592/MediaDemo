package com.example.myapplication.media;
 

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.util.Log;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;


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
        //compositing();
    }

    public static String MP3PATH ="";
    public static String MP4PATH ="";
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
    public void compositing(String audioPath,String videoPath,String out){
        MediaExtractor videoExtractor = new MediaExtractor();
        try {
            videoExtractor.setDataSource(videoPath);
 
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
            audioExtractor.setDataSource(audioPath);
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
 
            MediaMuxer mediaMuxer = new MediaMuxer(out, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
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


    /**
     * 合成视频和音频
     */
    public static void compositVideo(List<String> pathsList, String out){

        try {

            for(String videoPath :pathsList) {

                MediaExtractor videoExtractor = new MediaExtractor();
                videoExtractor.setDataSource(videoPath);

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

                videoExtractor.selectTrack(videoTrackIndex);

                MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();


                MediaMuxer mediaMuxer = new MediaMuxer(out, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                int writeVideoTrackIndex = mediaMuxer.addTrack(videoFormat);
                mediaMuxer.start();

                ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
                long sampleTime = 0l;
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


                mediaMuxer.stop();
                mediaMuxer.release();
                videoExtractor.release();

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void test(){

    }

    void decodeVideo(String path) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(path);
        int trackCount = extractor.getTrackCount();

        for(int i=0;i<trackCount;i++) {
            MediaFormat mediaFormat = extractor.getTrackFormat(i);
            Iterator<String> iterable = mediaFormat.getKeys().iterator();
            int framesize = 500 * 1024;
            String mime= null;

            while (iterable.hasNext()) {
                String key = iterable.next();
                int valueType = mediaFormat.getValueTypeForKey(key);
                Object value = null;
                switch (valueType) {
                    case MediaFormat.TYPE_FLOAT:
                        value = mediaFormat.getFloat(key, -1f);
                        break;
                    case MediaFormat.TYPE_INTEGER:
                        value = mediaFormat.getInteger(key, -1);
                        break;
                    case MediaFormat.TYPE_LONG:
                        value = mediaFormat.getLong(key, -1l);
                        break;
                    case MediaFormat.TYPE_STRING:
                        value = mediaFormat.getString(key, "-1");
                        break;
                    default:
                        break;
                }
                if(MediaFormat.KEY_MIME.equals(key))
                    mime = (String) value;
                if(MediaFormat.KEY_MAX_INPUT_SIZE.equals(key))
                    framesize = (int) value;
                Log.d("jxc","decode: dump mediaformat : key:" + key + "valuetype :" + valueType +",value:"+value);
            }

            if(mime.startsWith("audio/"))  {

                extractor.selectTrack(i);

                MediaCodec decodec = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
                decodec.configure(mediaFormat,null,null,0);
                decodec.start();


                //ByteBuffer byteBuffer = ByteBuffer.allocate(framesize);

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

                while (true) {
                    int inIndex = decodec.dequeueInputBuffer(50);

                    if(inIndex >=0) {
                        ByteBuffer byteBuffer = decodec.getInputBuffer(inIndex);

                        int readAudioSampleSize = extractor.readSampleData(byteBuffer, 0);


                        if (readAudioSampleSize < 0) {
                            Log.d("jxc", "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                            decodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            break;
                        }



                        bufferInfo.size = readAudioSampleSize;
                        bufferInfo.presentationTimeUs = extractor.getSampleTime();
                        bufferInfo.offset = 0;
                        bufferInfo.flags = extractor.getSampleFlags();

                        //TODO if format same ,donot decoder.
                        //mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);

                        if(byteBuffer == null) {
                            continue;
                        }
                        decodec.queueInputBuffer(inIndex,0,bufferInfo.size,bufferInfo.presentationTimeUs,bufferInfo.flags);


                        int outIndex = decodec.dequeueOutputBuffer(bufferInfo,50);

                        switch (outIndex) {

                            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                MediaFormat format = decodec.getOutputFormat();
                                Log.d("jxc", "New format " + format);

                                break;
                            case MediaCodec.INFO_TRY_AGAIN_LATER:
                                Log.d("jxc", "dequeueOutputBuffer timed out!");
                                break;

                            default:
                                ByteBuffer outBuffer = decodec.getOutputBuffer(outIndex);
                                Log.v("jxc", "We can't use this buffer but render it due to the API limit, " + outBuffer);

                                final byte[] chunk = new byte[bufferInfo.size];
                                outBuffer.get(chunk); // Read the buffer all at once
                                outBuffer.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN

                                MediaFormat mFormat = decodec.getOutputFormat();
                                //TODO dear with chunk data. the decoded data.

                                decodec.releaseOutputBuffer(outIndex, false);
                                break;
                        }
                    }
                }
            }
        }
    }


    /** 这里之前遇到一个坑，以为这个packetLen是adts头的长度，也就是7，仔细看了下代码，发现这个不是adts头的长度，而是一帧音频的长度
     * @param packet    一帧数据（包含adts头长度）
     * @param packetLen 一帧数据（包含adts头）的长度
     */
    public static void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = getFreqIdx(44100);
        int chanCfg = 2; // CPE

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }


    private static int getFreqIdx(int sampleRate) {
        int freqIdx;

        switch (sampleRate) {
            case 96000:
                freqIdx = 0;
                break;
            case 88200:
                freqIdx = 1;
                break;
            case 64000:
                freqIdx = 2;
                break;
            case 48000:
                freqIdx = 3;
                break;
            case 44100:
                freqIdx = 4;
                break;
            case 32000:
                freqIdx = 5;
                break;
            case 24000:
                freqIdx = 6;
                break;
            case 22050:
                freqIdx = 7;
                break;
            case 16000:
                freqIdx = 8;
                break;
            case 12000:
                freqIdx = 9;
                break;
            case 11025:
                freqIdx = 10;
                break;
            case 8000:
                freqIdx = 11;
                break;
            case 7350:
                freqIdx = 12;
                break;
            default:
                freqIdx = 8;
                break;
        }

        return freqIdx;
    }
}
