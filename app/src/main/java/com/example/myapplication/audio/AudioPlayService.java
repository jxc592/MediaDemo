package com.example.myapplication.audio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.collection.LruCache;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.IAudioPlayService;
import com.example.myapplication.R;
import com.example.myapplication.bean.MediaFileDescrtpter;
import com.example.myapplication.util.BitmapUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class AudioPlayService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener ,
        AudioManager.OnAudioFocusChangeListener,AudioAction {
    public AudioPlayService() {
    }


    NotificationManager mNotificationManager;
    MediaPlayer mediaPlayer;

    MediaFileDescrtpter mCurrtentmediaFileDescrtpter;

    String notiId = BuildConfig.APPLICATION_ID;//唯一就可以

    AudioManager mAudioManager;
    AudioFocusRequest mRequest;
    AudioActionReciver mreciver;
    IAudioPlayService.Stub iAudioPlayService;

    List<MediaFileDescrtpter> mDataList = new ArrayList<>();


    boolean isForegroundState = false;


    //TODO need to import cache solution.
    //LruCache<Integer,Bitmap> lruCacheBitmap = new LruCache<Integer, Bitmap>(){};


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return  iAudioPlayService.asBinder();
    }


    @Override
    public void onCreate() {
        super.onCreate();

        iAudioPlayService = new IAudioPlayService.Stub() {
            @Override
            public List<String> getAudioList() throws RemoteException {

                return getEncoderList();
            }

            @Override
            public String getCurrentSond() throws RemoteException {
                if(mCurrtentmediaFileDescrtpter != null)
                    return mCurrtentmediaFileDescrtpter.object2String();
                else
                    return null;
            }
        };

        Cursor audioCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,"_ID desc");

        if(audioCursor != null ) {
            while (audioCursor.moveToNext()) {
                int id = audioCursor.getInt(audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
                String data = audioCursor.getString(audioCursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
                String title = audioCursor.getString(audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
                String album = audioCursor.getString(audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
                long albumid = audioCursor.getLong(audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
                String artist = audioCursor.getString(audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
                String bookmart = audioCursor.getString(audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.BOOKMARK));
                String year = audioCursor.getString(audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.YEAR));
                long size = audioCursor.getInt(audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE));


                String albumKey = audioCursor.getString(audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_KEY));

                MediaFileDescrtpter mediaFileDescrtpter = new MediaFileDescrtpter(id, data, title, album, artist, bookmart, year, size);

                mediaFileDescrtpter.setAlbumid(albumid);

                mDataList.add(mediaFileDescrtpter);
            }
            audioCursor.close();

        }


        NotificationChannel channel = new NotificationChannel(notiId,"AudioPlayer", NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationManager  = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(channel);



        mediaPlayer = new MediaPlayer();
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        mreciver = new AudioActionReciver();
        mreciver.setActionListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioAction.ACTION_COMPLETE);
        intentFilter.addAction(AudioAction.ACTION_NEXT);
        intentFilter.addAction(AudioAction.ACTION_PAUSE);
        intentFilter.addAction(AudioAction.ACTION_PLAY);
        intentFilter.addAction(AudioAction.ACTION_PRE);
        intentFilter.addAction(AudioAction.ACTION_PLAY_STATE);
        intentFilter.addAction(AudioAction.ACTION_PAUSE_STATE);
        registerReceiver(mreciver,intentFilter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mreciver);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        MediaFileDescrtpter mediaFileDescrtpter = intent.getParcelableExtra("data");
        if(mediaFileDescrtpter != null && mediaFileDescrtpter.getData().length() > 0) {
            if(mCurrtentmediaFileDescrtpter != null
             && mediaFileDescrtpter.getData().equals(mCurrtentmediaFileDescrtpter.getData())) {

            } else {
                play(mediaFileDescrtpter);
            }
        }
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        Intent intent = new Intent(AudioAction.ACTION_PLAY_STATE);
        sendBroadcast(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playNextSound();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    private void playNextSound(){
        if(mCurrtentmediaFileDescrtpter == null) {
            if(mDataList.size() > 0) {
                MediaFileDescrtpter descrtpter = mDataList.get(0);
            }
        } else {
            int i = mDataList.indexOf(mCurrtentmediaFileDescrtpter);
            Log.d("jxc","first check current music index += " + i);

            if(i < 0) {
                mCurrtentmediaFileDescrtpter.getId();
                for (int j =0 ;j<mDataList.size();j ++) {
                    if(mCurrtentmediaFileDescrtpter.getId() == mDataList.get(j).getId()) {
                        i = j;
                        break;
                    }
                }
                Log.d("jxc","playNextSound double check current music index += " + i);
            }

            if(i>=0 && i<mDataList.size()-1) {
                play(mDataList.get(i+1));
            } else {
                play(mDataList.get(0));
            }
        }
    }


    private void playPreSound(){
        if(mCurrtentmediaFileDescrtpter == null) {
            if(mDataList.size() > 0) {
                MediaFileDescrtpter descrtpter = mDataList.get(0);
            }
        } else {
            int i = mDataList.indexOf(mCurrtentmediaFileDescrtpter);
            if(i < 0) {
                mCurrtentmediaFileDescrtpter.getId();
                for (int j =0 ;j<mDataList.size();j ++) {
                    if(mCurrtentmediaFileDescrtpter.getId() == mDataList.get(j).getId()) {
                        i = j;
                        break;
                    }
                }
                Log.d("jxc","playPreSound double check current music index += " + i);
            }

            if(i>1) {
                play(mDataList.get(i-1));
            } else {
                play(mDataList.get(mDataList.size() -1));
            }
        }
    }


    void play(MediaFileDescrtpter mediaFileDescrtpter) {
        mRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setOnAudioFocusChangeListener(this).setWillPauseWhenDucked(true).build();
        mAudioManager.requestAudioFocus(mRequest);

        boolean successSet = false;
        if (mCurrtentmediaFileDescrtpter == null) {
            mCurrtentmediaFileDescrtpter = mediaFileDescrtpter;
            try {
                mediaPlayer.setDataSource(mCurrtentmediaFileDescrtpter.getData());
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setOnErrorListener(this);
                successSet = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mCurrtentmediaFileDescrtpter =mediaFileDescrtpter;
            if(mediaPlayer != null ){
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mCurrtentmediaFileDescrtpter.getData());
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setOnErrorListener(this);
                successSet = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(successSet) {
            if(isForegroundState) {
                startForeground(1,createNoti(mediaFileDescrtpter));
                //mNotificationManager.notify(1,createNoti(mediaFileDescrtpter));
            } else {
                startForeground(1,createNoti(null));
                isForegroundState = true;
            }
        } else {
            mAudioManager.abandonAudioFocusRequest(mRequest);
        }
    }


    /**
     *
     * @param mediaFileDescrtpter
     * @param state pause:false   play:true
     * @return
     */
    Notification createNoti(MediaFileDescrtpter mediaFileDescrtpter,boolean state) {
        RemoteViews remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID,R.layout.layout_audio_noti);
        Intent intentNext = new Intent(AudioAction.ACTION_NEXT);
        PendingIntent piNext = PendingIntent.getBroadcast(getBaseContext(),10,intentNext,FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.bt_next,piNext);

        Intent intentPre = new Intent(AudioAction.ACTION_PRE);
        PendingIntent piPre = PendingIntent.getBroadcast(getBaseContext(),11,intentPre,FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.bt_pre,piPre);

        Intent intentStateChange = new Intent(AudioAction.ACTION_PLAY);
        PendingIntent piStateChange = PendingIntent.getBroadcast(getBaseContext(),12,intentStateChange,FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.bt_play,piStateChange);

        if(state){
            remoteViews.setInt(R.id.bt_play,"setBackgroundResource",R.drawable.ic_noti_player_pause);
        } else{
            remoteViews.setInt(R.id.bt_play,"setBackgroundResource",R.drawable.ic_noti_player_play);
        }

        if(mediaFileDescrtpter !=null) {
            remoteViews.setCharSequence(R.id.tv_title, "setText", mediaFileDescrtpter.getTitle());
            remoteViews.setCharSequence(R.id.tv_author, "setText", mediaFileDescrtpter.getArtist());
            Bitmap source = BitmapUtils.getAlbumArt(this,mediaFileDescrtpter.getData());
            remoteViews.setImageViewBitmap(R.id.iv_thumbuil,source);
        }

        Intent intent = new Intent(this,AudioPlayActivity.class);
        PendingIntent pi  = PendingIntent.getActivity(this,0,intent,0);
        Notification notification = new NotificationCompat.Builder(this,notiId) //发送通道
                .setContent(remoteViews)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.icon))
                .setContentIntent(pi)
                .build();
        return notification;
    }

    Notification createNoti(MediaFileDescrtpter mediaFileDescrtpter) {
        return createNoti(mediaFileDescrtpter,true);
    }

    @Override
    public void onAudioFocusChange(int i) {
        switch (i) {
            case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer!= null &&mediaPlayer.isPlaying()) {
                    onPlayPause();
                }
                break;
            case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                if (mediaPlayer!= null &&!mediaPlayer.isPlaying() ) {
                    onPlayStart();
                }
                break;
            default:
        }
        Log.d("jxc","onAudioFocusChange focus state:" + AudioUtils.AudioStateToStr(i));
    }


    @Override
    public void onCompleted() {

    }

    @Override
    public void onPlayStart() {
        if(mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                Intent intent = new Intent(AudioAction.ACTION_PLAY_STATE);
                sendBroadcast(intent);
            } else {
                mediaPlayer.pause();
                Intent intent = new Intent(AudioAction.ACTION_PAUSE_STATE);
                sendBroadcast(intent);
            }
        }
    }

    @Override
    public void onPlayStarted() {
        mNotificationManager.notify(1,createNoti(mCurrtentmediaFileDescrtpter,true));
    }

    @Override
    public void onPlayPause() {
        if(mediaPlayer != null) {
            mediaPlayer.pause();
            Intent intent = new Intent(AudioAction.ACTION_PAUSE_STATE);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onPlayPaused() {
        mNotificationManager.notify(1,createNoti(mCurrtentmediaFileDescrtpter,false));
    }

    @Override
    public void onPre() {
        playPreSound();
    }

    @Override
    public void onNext() {
        playNextSound();
    }

    List<String> getEncoderList() {
        List<String> list = new ArrayList<>();
        if(mDataList != null &&mDataList.size()>0) {
            for (int i=0;i<mDataList.size();i++) {
                MediaFileDescrtpter descrtpter = mDataList.get(i);
                list.add(descrtpter.object2String());
            }
        }
        return list;
    }
}
