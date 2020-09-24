package com.example.myapplication.video;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;


import androidx.core.app.NotificationCompat;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.IVideoInterface;
import com.example.myapplication.R;
import com.example.myapplication.audio.AudioPlayActivity;
import com.example.myapplication.base.BaseService;
import com.example.myapplication.media.MP4VideoExtractor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoService extends BaseService {


    private static final byte ERR_FILE_OPEN_FAILED =1;
    private static final byte ERR_NO_OUTPUT_FILE =2;
    private static final byte ERR_NO_AUDIO_TRACK =3;
    private static final byte NO_ERROR = 0;

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
        //mNotificationManager.no
    }


    IVideoInterface mVideoInterface = new IVideoInterface.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void videoPrepare(final String uri) throws RemoteException {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    extractAudioFromVideo(VideoService.this,Uri.parse(uri));
                }
            }.start();
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


    private void videoPrepare(Context context,Uri uri) {
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            mediaExtractor.setDataSource(fileDescriptor.getFileDescriptor());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("JXC","open uri failed.");
            handleExtractorResult(ERR_FILE_OPEN_FAILED,"打开源文件失败",null);
            return;
        }

        int audioTrackIdx=-1;

        for (int i =0 ;i<mediaExtractor.getTrackCount();i++) {
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
            String mineType = trackFormat.getString(MediaFormat.KEY_MIME);
            //音频信道
            if (!mineType.startsWith("audio/")) {
                continue;
            } else {
                Log.d("JXC","mime = " + mineType);
                audioTrackIdx = i;
            }
        }
    }

    private void extractAudioFromVideo(Context context,Uri uri){
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            mediaExtractor.setDataSource(fileDescriptor.getFileDescriptor());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("JXC","open uri failed.");
            handleExtractorResult(ERR_FILE_OPEN_FAILED,"打开源文件失败",null);
            return;
        }

        int audioTrackIdx=-1;

        for (int i =0 ;i<mediaExtractor.getTrackCount();i++) {
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
            String mineType = trackFormat.getString(MediaFormat.KEY_MIME);
            //音频信道
            if (!mineType.startsWith("audio/")) {
                continue;
            } else {
                Log.d("JXC","mime = " + mineType);
                audioTrackIdx = i;
            }
        }
        if(audioTrackIdx == -1) {
            Log.d("JXC","extractAudioFromVideo no audio track.");
            handleExtractorResult(ERR_NO_OUTPUT_FILE,"访问输出文件失败",null);
            return;
        }

        String filename = mOutputDir + "/" + System.currentTimeMillis()+".aac";
        Log.d("JXC","extractAudioFromVideo input file= " + filename);


        FileOutputStream audioOutputStream;
        try {
            audioOutputStream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG,"open file failed");
            handleExtractorResult(ERR_NO_OUTPUT_FILE,"访问输出文件失败",null);
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

        long totalTime = mediaFormat.getLong(MediaFormat.KEY_DURATION);

        while (true) {

            int readSampleCount = mediaExtractor.readSampleData(byteBuffer, 0);

            current += mediaExtractor.getSampleTime();


            if (readSampleCount < 0) {
                break;
            }

            if(current*100/totalTime )
            updateProgress(current*100/totalTime);

            //保存音频信息
            byte[] buffer = new byte[readSampleCount];
            byteBuffer.get(buffer);

            if(mime.startsWith("audio/mp4a-latm")) {

                /************************* 用来为aac添加adts头**************************/
                byte[] aacaudiobuffer = new byte[readSampleCount + 7];
                MP4VideoExtractor.addADTStoPacket(aacaudiobuffer, readSampleCount + 7);
                System.arraycopy(buffer, 0, aacaudiobuffer, 7, readSampleCount);
                try {
                    audioOutputStream.write(aacaudiobuffer);
                    Log.d(TAG, "audio:writeaac buffer:" + readSampleCount);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("JXC", "write error");
                }
                /***************************************close**************************/
            } else {
                try {
                    audioOutputStream.write(buffer);
                    Log.d(TAG, "audio:write common buffer:" + readSampleCount);
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
        handleExtractorResult(NO_ERROR,"抽离音频文件成功：" + filename,filename);
    }



    private void handleExtractorResult(byte errCode,String msg,String data){
        switch (errCode) {
            case ERR_FILE_OPEN_FAILED:
                break;
            case ERR_NO_OUTPUT_FILE:
                break;
            case ERR_NO_AUDIO_TRACK:
                break;
            case NO_ERROR:
                break;
        }
    }

    private void updateProgress(float progress){
        notify("提取",(int)progress);
    }

    void notify(String title, int progress){
        NotificationCompat.Builder builder  = new NotificationCompat.Builder(VideoService.this,notiId);
        builder.setContentTitle(title);
        builder.setProgress(100,progress,false);
        builder.setContentText("正在抽离音乐");
        builder.setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.icon));
        Intent intent = new Intent(this, VideoConvertActivity.class);
        PendingIntent pi  = PendingIntent.getActivity(this,0,intent,0);
        builder.setContentIntent(pi);
        mNotificationManager.notify(1000,builder.build());
    }
}
