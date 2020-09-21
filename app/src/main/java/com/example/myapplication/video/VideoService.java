package com.example.myapplication.video;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;


import com.example.myapplication.BuildConfig;
import com.example.myapplication.IVideoInterface;
import com.example.myapplication.media.MP4VideoExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;

import androidx.annotation.NonNull;

public class VideoService extends Service {

    String mOutputDir;

    String TAG = "VideoService";

    NotificationManager mNotificationManager;

    String notiId = BuildConfig.APPLICATION_ID+"video";//唯一就可以
    public VideoService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            //if(Build.VERSION.SDK_INT < 29)
            mOutputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getCanonicalPath();
//            else
//                mOutputDir = MediaStore.Audio.Media.getContentUri();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationChannel channel = new NotificationChannel(notiId,"video", NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationManager  = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(channel);
    }


    IVideoInterface mVideoInterface = new IVideoInterface.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void videoPrepare(String uri) throws RemoteException {

        }

        @Override
        public void videoInfoCallBack(String result) throws RemoteException {

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mVideoInterface.asBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }


    private void extractAudioFromVideo(String oriVideo){

        String filename = mOutputDir + "/" + System.currentTimeMillis()+".aac";
        Log.d("JXC","extractAudioFromVideo input file= " + filename);

        FileOutputStream audioOutputStream;
        try {
            audioOutputStream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG,"open file failed");
            return;
        }

        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int audioTrackIdx=-1;

        for (int i =0 ;i>mediaExtractor.getTrackCount();i++) {
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
            String mineType = trackFormat.getString(MediaFormat.KEY_MIME);
            //音频信道
            if (!mineType.startsWith("audio/")) {
               continue;
            } else {
                audioTrackIdx = i;
            }
        }
        if(audioTrackIdx == -1) {
            Log.d("JXC","extractAudioFromVideo no audio track.");
            return;
        }

        mediaExtractor.selectTrack(audioTrackIdx);
        MediaFormat mediaFormat = mediaExtractor.getTrackFormat(audioTrackIdx);

        String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
        int bitrate =  mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
        long durtion = mediaFormat.getLong(MediaFormat.KEY_DURATION);
        Log.d(TAG,"bitrate :" + bitrate + " duration: " + durtion);

        int maxBufferCount = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        ByteBuffer byteBuffer = ByteBuffer.allocate(maxBufferCount);


        long current = 0;

        while (true) {
            int readSampleCount = mediaExtractor.readSampleData(byteBuffer, 0);
            Log.d(TAG, "audio:readSampleCount:" + readSampleCount);
            if (readSampleCount < 0) {
                break;
            }
            //保存音频信息
            byte[] buffer = new byte[readSampleCount];
            byteBuffer.get(buffer);

            if(mime.startsWith("audio/aac")) {

                /************************* 用来为aac添加adts头**************************/
                byte[] aacaudiobuffer = new byte[readSampleCount + 7];
                MP4VideoExtractor.addADTStoPacket(aacaudiobuffer, readSampleCount + 7);
                System.arraycopy(buffer, 0, aacaudiobuffer, 7, readSampleCount);
                try {
                    audioOutputStream.write(aacaudiobuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("JXC", "write error");
                }
                /***************************************close**************************/
            } else {
                try {
                    audioOutputStream.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("JXC", "write error");
                }
            }
            byteBuffer.clear();
            mediaExtractor.advance();
        }

        mediaExtractor.release();
        try {
            audioOutputStream.flush();
            audioOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("JXC","audio stream operation error");
        }

    }
}
