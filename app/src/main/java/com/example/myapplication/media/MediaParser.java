package com.example.myapplication.media;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class MediaParser {
    private static final String TAG = "jxc_MediaParser";

    String mVideoPath;
    MediaExtractor mExtractor;
    MediaFormat mVideoFormat;
    MediaFormat mAudioFormat;
    byte mTrackCount = -1;
    byte mAudioTrackIdx = -1;
    byte mVideoTrackIdx = -1;
    boolean hasAudio = false;
    boolean hasVideo = false;
    boolean isAACAudioFormat = false;

    CodecCallBack mCodecCallBack;
    public interface CodecCallBack {
        void onAudioDecoderedBufferAvailable(byte[] data, MediaCodec.BufferInfo info);
        void onVideoDecoderedBufferAvailAble(byte[] data,MediaCodec.BufferInfo info);
    }


    public MediaFormat getVideoFormat() {
        return mVideoFormat;
    }

    public MediaFormat getAudioFormat() {
        return mAudioFormat;
    }

    public byte getTrackCount() {
        return mTrackCount;
    }

    public byte getAudioTrackIdx() {
        return mAudioTrackIdx;
    }

    public byte getVideoTrackIdx() {
        return mVideoTrackIdx;
    }

    public boolean isHasAudio() {
        return hasAudio;
    }

    public boolean HasVideo() {
        return hasVideo;
    }

    public boolean isAACAudioFormat() {
        return isAACAudioFormat;
    }


    public void setCodecCallBack(MediaParser.CodecCallBack callBack) {
        this.mCodecCallBack = callBack;
    }


    // ui block
    public MediaParser(String mVideoPath) throws IOException {
        this.mVideoPath = mVideoPath;
        mExtractor = new MediaExtractor();
        mExtractor.setDataSource(mVideoPath);
        mTrackCount = (byte) mExtractor.getTrackCount();
        for (byte i = 0; i<mTrackCount;i++) {
            MediaFormat tmft =  mExtractor.getTrackFormat(i);
            String mime = tmft.getString(MediaFormat.KEY_MIME,"");
            if(mime.startsWith("audio")) {
                mAudioFormat = tmft;
                hasAudio = true;
                mAudioTrackIdx = i;
                if(mime.equals(MediaFormat.MIMETYPE_AUDIO_AAC)) {
                    isAACAudioFormat = true;
                }
            } else if(mime.startsWith("video")){
                mVideoFormat = tmft;
                hasVideo = true;
                mVideoTrackIdx = i;
            }
            //TODO add subtitle format.
        }
    }

    public void  extractorAudio(String tempSavedPath){
        if(!hasAudio) {
            Log.d(TAG,"no audio track");
            return;
        }
        boolean isErrOccured = false;
        mExtractor.selectTrack(mAudioTrackIdx);
        FileOutputStream fo = null;
        try {
            fo  = new FileOutputStream(tempSavedPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG,"extractorAudio: file not found:" + tempSavedPath);
            isErrOccured = true;
        }
        if(isErrOccured) return;

        ByteBuffer byteBuffer = ByteBuffer.allocate(mAudioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
        while (true) {
            int sampleSize = mExtractor.readSampleData(byteBuffer,0);
            if (sampleSize < 0) {
                Log.d(TAG,"extractorAudio: read completed.");
                break;
            }
            byte[] data = new byte[sampleSize];
            byteBuffer.get(data);
            byteBuffer.clear();
            if(isAACAudioFormat)  {
                int adtsPktLength;
                adtsPktLength = sampleSize + 7;
                byte[] aacData = new byte[adtsPktLength];
                MediaUtils.addADTStoPacket(aacData,adtsPktLength);
                System.arraycopy(data,0,aacData,7,sampleSize);
                try {
                    fo.write(aacData);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG,"extractorAudio: aac write to file error.");
                    isErrOccured = true;
                }
            } else  {
                try {
                    fo.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG,"extractorAudio: write to file error.");
                    isErrOccured = true;
                }
            }
            if(isErrOccured) {
                break;
            }
            mExtractor.advance();
        }
        mExtractor.unselectTrack(mAudioTrackIdx);
        try {
            fo.flush();
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"extractorAudio: close  file error.");
        }
    }

    public  void extractorVideo(String tempSavedPath) {
        if(!hasVideo) {
            Log.d(TAG,"no audio track");
            return;
        }
        boolean isErrOccured = false;
        mExtractor.selectTrack(mVideoTrackIdx);
        FileOutputStream fo = null;
        try {
            fo  = new FileOutputStream(tempSavedPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG,"extractorAudio: file not found:" + tempSavedPath);
            isErrOccured = true;
        }
        if(isErrOccured) return;

        ByteBuffer byteBuffer = ByteBuffer.allocate(mAudioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
        while (true) {
            int sampleSize = mExtractor.readSampleData(byteBuffer,0);
            if (sampleSize < 0) {
                Log.d(TAG,"extractorAudio: read completed.");
                break;
            }
            byte[] data = new byte[sampleSize];
            byteBuffer.get(data);
            byteBuffer.clear();

            try {
                fo.write(data);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "extractorAudio: write to file error.");
                isErrOccured = true;
            }
            if(isErrOccured) {
                break;
            }
            mExtractor.advance();
        }
        mExtractor.unselectTrack(mAudioTrackIdx);
        try {
            fo.flush();
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"extractorVideo: close  file error.");
        }
    }

    MediaCodec initAudioDecoder() {
        MediaCodec codec = null;
        boolean isErrOccured =false;
        try {
            codec = MediaCodec.createDecoderByType(mAudioFormat.getString(MediaFormat.KEY_MIME));
            codec.configure(mAudioFormat,null,null,0);
            codec.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"create codec error");
            isErrOccured = true;
        }
        if(!isErrOccured)
            return codec;
        else
            return null;
    }

    MediaCodec initVideoDecoder() {
        return initVideoDecoder(null);
    }
    MediaCodec initVideoDecoder(Surface surface) {
        MediaCodec codec = null;
        boolean isErrOccured =false;
        try {
            codec = MediaCodec.createDecoderByType(mVideoFormat.getString(MediaFormat.KEY_MIME));
            codec.configure(mVideoFormat,surface,null,0);
            codec.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"create codec error");
            isErrOccured = true;
        }
        if(!isErrOccured)
            return codec;
        else
            return null;
    }


    MediaCodec initEncoder(MediaFormat format) {
        MediaCodec codec = null;
        boolean isErrOccured =false;
        try {
            codec = MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME));
            codec.configure(format,null,null,0);
            codec.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"create codec error");
            isErrOccured = true;
        }
        if(!isErrOccured)
            return codec;
        else
            return null;
    }


    public void decodeAudio() {
        if(!hasAudio) {
            Log.d(TAG,"no audio track");
            return;
        }
        boolean isErrOccured = false;
        mExtractor.selectTrack(mAudioTrackIdx);

        if(isErrOccured) return;

        MediaCodec codec = initAudioDecoder();
        if(codec == null)
            return;


        while (true) {
            ByteBuffer byteBuffer = null;
            int inputIdx = -1;
            try {
                inputIdx = codec.dequeueInputBuffer(0);
            } catch (Exception e) {
                Log.d(TAG,"decodeAudio  dequeueInputBuffer error" +e.getMessage());
            }

            if(inputIdx >= 0 ) {
                byteBuffer = codec.getInputBuffer(inputIdx);
            } else {
                //codec 创建容器失败重新dequeue
                continue;
            }
            int sampleSize = mExtractor.readSampleData(byteBuffer,0);
            if (sampleSize < 0) {
                Log.d(TAG,"extractorAudio: read completed.");
                codec.queueInputBuffer(inputIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                break;
            }
            byte[] data = new byte[sampleSize];

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            bufferInfo.flags = mExtractor.getSampleFlags();
            bufferInfo.presentationTimeUs = mExtractor.getSampleTime();
            bufferInfo.size = sampleSize;
            bufferInfo.offset=0;
            if(isAACAudioFormat)  {
                int adtsPktLength;
                adtsPktLength = sampleSize + 7;
                byte[] aacData = new byte[adtsPktLength];
                MediaUtils.addADTStoPacket(aacData,adtsPktLength);
                System.arraycopy(data,0,aacData,7,sampleSize);
                byteBuffer.clear();
                byteBuffer.put(aacData);

                codec.queueInputBuffer(inputIdx,0,adtsPktLength,mExtractor.getSampleTime(),mExtractor.getSampleFlags());
                bufferInfo.size = adtsPktLength;


            } else {
                codec.queueInputBuffer(inputIdx, 0, sampleSize, mExtractor.getSampleTime(), mExtractor.getSampleFlags());
            }

            int outputIdx = codec.dequeueOutputBuffer(bufferInfo,100);
            if (outputIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat format = codec.getOutputFormat();
                Log.d(TAG, "New format " + format);
            }else if (outputIdx == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.d(TAG, "dequeueOutputBuffer timed out!");
                //continue;
            } else {
                    ByteBuffer outBuffer = codec.getOutputBuffer(outputIdx);
                    Log.v(TAG, "decoded buffer available" + outBuffer);
                    final byte[] chunk = new byte[bufferInfo.size];
                    outBuffer.get(chunk); // Read the buffer all at once
                    outBuffer.clear(); // clear and release.
                    MediaFormat format = codec.getOutputFormat();
                    if(mCodecCallBack != null) {
                        mCodecCallBack.onAudioDecoderedBufferAvailable(chunk,bufferInfo);
                    }
                    codec.releaseOutputBuffer(outputIdx, false);
            }

            if(isErrOccured) {
                break;
            }
            mExtractor.advance();
        }
        codec.stop();
        codec.release();
        mExtractor.unselectTrack(mAudioTrackIdx);
    }

    public void encodeTrack(MediaFormat format){

    }

    public void decodeVideoAndRender(Surface surface) {
        decodeVideo(surface);
    }

    public void decodeVideo(){
        decodeVideo(null);
    }

    protected void decodeVideo(Surface surface){
        if(!hasVideo) {
            Log.d(TAG,"decodeVideo no video track ,ignore decode action.");
            return;
        }
        mExtractor.selectTrack(mVideoTrackIdx);
        MediaCodec codec = initVideoDecoder(surface);
        if(codec == null) return;

        boolean isErrOccur = false;
        int sampleSize = -1;

        while (true) {
            ByteBuffer byteBuffer = null;
            int inputIdx = -1;
            try {
                inputIdx = codec.dequeueInputBuffer(1000);
            } catch (Exception e) {
                isErrOccur = true;
            }

            if(isErrOccur) {
                Log.d(TAG,"decodeVideo: dequeueInputBuffer enter error.");
                break;
            }

            if(inputIdx >= 0) {
                byteBuffer = codec.getInputBuffer(inputIdx);
            } else {
                Log.d(TAG,"decodeVideo: dequeueInputBuffer failed,retry it.");
                continue;
            }
            // read encodecd avpacket.
            sampleSize = mExtractor.readSampleData(byteBuffer,0);
            if(sampleSize < 0) {
                Log.d(TAG,"decodeVideo: read completed.");
                codec.queueInputBuffer(inputIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                break;
            }
            // add decode action to queue
            codec.queueInputBuffer(inputIdx,0,sampleSize,mExtractor.getSampleTime(),mExtractor.getSampleFlags());

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            bufferInfo.flags = mExtractor.getSampleFlags();
            bufferInfo.presentationTimeUs = mExtractor.getSampleTime();
            bufferInfo.offset=0;
            bufferInfo.size = sampleSize;

            int outputIdx = codec.dequeueOutputBuffer(bufferInfo,1000);
            if (outputIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat format = codec.getOutputFormat();
                Log.d(TAG, "New format " + format);
            }else if (outputIdx == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.d(TAG, "dequeueOutputBuffer timed out!");
                //continue;
            } else {
                ByteBuffer outBuffer = codec.getOutputBuffer(outputIdx);
                Log.v(TAG, "decoded buffer available" + outBuffer);
                final byte[] chunk = new byte[bufferInfo.size];
                outBuffer.get(chunk); // Read the buffer all at once
                outBuffer.clear(); // clear and release.
                MediaFormat format = codec.getOutputFormat();
                if(mCodecCallBack != null) {
                    mCodecCallBack.onVideoDecoderedBufferAvailAble(chunk,bufferInfo);
                }
                codec.releaseOutputBuffer(outputIdx, false);
            }

            mExtractor.advance();
        }
        codec.stop();
        codec.release();


    }
    public void dumpVideoFormat() {
        if(!hasVideo) {
            Log.d(TAG,"no video format to dump");
            return;
        }
        Iterator<String> iterable = mVideoFormat.getKeys().iterator();
        while (iterable.hasNext()) {
            String key = iterable.next();
            int valueType = mVideoFormat.getValueTypeForKey(key);
            Object value = null;
            switch (valueType) {
                case MediaFormat.TYPE_FLOAT:
                    value = mVideoFormat.getFloat(key, -1f);
                    break;
                case MediaFormat.TYPE_INTEGER:
                    value = mVideoFormat.getInteger(key, -1);
                    break;
                case MediaFormat.TYPE_LONG:
                    value = mVideoFormat.getLong(key, -1l);
                    break;
                case MediaFormat.TYPE_STRING:
                    value = mVideoFormat.getString(key, "-1");
                    break;
                default:
                    break;
            }
            Log.d(TAG,"dump videoformat : key:" + key + "valuetype :" + valueType +",value:"+value);
        }
    }

    public void dumpAudioFormat() {
        if(!hasAudio) {
            Log.d(TAG,"no audio format to dump");
            return;
        }
        Iterator<String> iterable = mAudioFormat.getKeys().iterator();
        while (iterable.hasNext()) {
            String key = iterable.next();
            int valueType = mAudioFormat.getValueTypeForKey(key);
            Object value = null;
            switch (valueType) {
                case MediaFormat.TYPE_FLOAT:
                    value = mAudioFormat.getFloat(key, -1f);
                    break;
                case MediaFormat.TYPE_INTEGER:
                    value = mAudioFormat.getInteger(key, -1);
                    break;
                case MediaFormat.TYPE_LONG:
                    value = mAudioFormat.getLong(key, -1l);
                    break;
                case MediaFormat.TYPE_STRING:
                    value = mAudioFormat.getString(key, "-1");
                    break;
                default:
                    break;
            }
            Log.d(TAG,"dump videoformat : key:" + key + "valuetype :" + valueType +",value:"+value);
        }
    }

}
