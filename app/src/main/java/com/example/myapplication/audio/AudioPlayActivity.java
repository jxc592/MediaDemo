package com.example.myapplication.audio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.IAudioPlayService;
import com.example.myapplication.R;
import com.example.myapplication.bean.MediaFileDescrtpter;
import com.example.myapplication.util.NativeLib;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AudioPlayActivity extends AppCompatActivity implements View.OnClickListener,AudioAction {


    MediaFileDescrtpter mDescriptor;
    ImageView iv;
    TextView tv_title;
    TextView tv_author;
    TextView tv_album;
    TextView tv_duration;

    ImageButton bt_pre,bt_play,bt_next;


    AudioActionReciver mreciver;
    ServiceConnection mServiceConnection;
    IAudioPlayService iAudioPlayService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_play);
        tv_title = findViewById(R.id.tv_title);
        tv_author = findViewById(R.id.tv_author);
        tv_album = findViewById(R.id.tv_album);
        tv_duration = findViewById(R.id.tv_duration);
        iv = findViewById(R.id.iv_thumbuil);

        bt_play = findViewById(R.id.bt_play);
        bt_next = findViewById(R.id.bt_next);
        bt_pre = findViewById(R.id.bt_pre);

        bt_play.setOnClickListener(this);
        bt_next.setOnClickListener(this);
        bt_pre.setOnClickListener(this);

        mreciver = new AudioActionReciver();
        mreciver.setActionListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioAction.ACTION_COMPLETE);
        intentFilter.addAction(AudioAction.ACTION_PLAY_STATE);
        intentFilter.addAction(AudioAction.ACTION_PAUSE_STATE);
        registerReceiver(mreciver,intentFilter);


        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                iAudioPlayService = IAudioPlayService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                iAudioPlayService = null;
            }
        };
        Intent intent = new Intent(this, AudioPlayService.class);
        bindService(intent,mServiceConnection, Activity.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mreciver);
        if(mServiceConnection != null && iAudioPlayService != null) {
            unbindService(mServiceConnection);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        MediaFileDescrtpter descrtpter = getIntent().getParcelableExtra("data");

        if(descrtpter != null && descrtpter.getData()!=null ) {
            File file = new File(descrtpter.getData());
            Uri uri = FileProvider.getUriForFile(getBaseContext(), BuildConfig.APPLICATION_ID.concat(".provider"),file);
            grantUriPermission(BuildConfig.APPLICATION_ID,uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        mDescriptor = descrtpter;

        refrehView();

    }

    void refrehView() {
        NativeLib nativeLib = new NativeLib();

        int length =0 ;
        byte[] data = nativeLib.parserAlbumArt(mDescriptor.getData());

        if(data != null && data.length >0) {
            Bitmap source = BitmapFactory.decodeByteArray(data,0,data.length);
            Log.d("jxc","bitmap == null ? " + (source == null ? "true" :"false"));
            if(source != null)
                iv.setImageBitmap(source);
        }

        tv_title.setText(mDescriptor.getTitle());
        tv_author.setText(mDescriptor.getArtist());
        tv_album.setText(mDescriptor.getAlbum());
        tv_duration.setText(mDescriptor.getDuration() /1000 /60 +"");

        bt_play.setBackground(getDrawable(R.drawable.player_btn_pause_normal));
        bt_play.setTag(1);
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.bt_next:
                intent.setAction(AudioAction.ACTION_NEXT);
                break;
            case R.id.bt_pre:
                intent.setAction(AudioAction.ACTION_PRE);
                break;
            case R.id.bt_play:
                Object a = bt_play.getTag();
                Log.d("jxc","play button state" + (int)a);
                if((int)bt_play.getTag() == 0)
                    intent.setAction(AudioAction.ACTION_PLAY);
                else
                    intent.setAction(AudioAction.ACTION_PAUSE);
                break;
        }
        sendBroadcast(intent);
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onPlayStart() {

    }

    @Override
    public void onPlayStarted() {
        try {
            if(iAudioPlayService.getCurrentSond() != null) {
                mDescriptor = MediaFileDescrtpter.unmarshall(iAudioPlayService.getCurrentSond());
                Log.d("jxc","onPlayStarted" + mDescriptor.getTitle());
                refrehView();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPlayPause() {

    }

    @Override
    public void onPlayPaused() {
        bt_play.setBackground(getDrawable(R.drawable.player_btn_play_normal));
        bt_play.setTag(0);
    }


    @Override
    public void onPre() {

    }

    @Override
    public void onNext() {

    }
}
