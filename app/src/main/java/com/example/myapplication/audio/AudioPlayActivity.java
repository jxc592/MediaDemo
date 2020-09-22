package com.example.myapplication.audio;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.IAudioPlayService;
import com.example.myapplication.R;
import com.example.myapplication.base.BaseHandlerActivity;
import com.example.myapplication.bean.MediaFileDescrtpter;
import com.example.myapplication.util.NativeLib;
import com.example.myapplication.widget.CircleImageDrawable;

public class AudioPlayActivity extends BaseHandlerActivity implements View.OnClickListener,AudioAction {


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

     protected final int MSG_CODE_REFRESH_IMAGEVIEW = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawable(getDrawable(R.drawable.player_background_real));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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

                try {
                    if(iAudioPlayService!=null && iAudioPlayService.getCurrentSond() != null) {
                        refrehView();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        refrehView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    void refrehView() {

        try {
            if(iAudioPlayService!=null && iAudioPlayService.getCurrentSond() != null) {
                mDescriptor = MediaFileDescrtpter.unmarshall(iAudioPlayService.getCurrentSond());
                Log.d("jxc","refrehView dump song:" + mDescriptor.getTitle() +" path " + mDescriptor.getData());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if(mDescriptor == null) return;

        NativeLib nativeLib = new NativeLib();
        int length =0 ;
        byte[] data = nativeLib.parserAlbumArt(mDescriptor.getData());
        if(data != null && data.length >0) {
            Bitmap source = BitmapFactory.decodeByteArray(data,0,data.length);
            Log.d("jxc","bitmap == null ? " + (source == null ? "true" :"false"));
            if(source != null) {
                iv.setImageDrawable(new CircleImageDrawable(source));
            }else {
                iv.setImageDrawable(new CircleImageDrawable(BitmapFactory.
                        decodeResource(getResources(),R.drawable.profile_default_bg_small)));
            }
        } else {
            iv.setImageDrawable(new CircleImageDrawable(BitmapFactory.
                    decodeResource(getResources(),R.drawable.profile_default_bg_small)));
        }


        reLayoutAlbumArt();

        tv_title.setText(mDescriptor.getTitle());
        tv_author.setText(mDescriptor.getArtist());
        tv_album.setText(mDescriptor.getAlbum());
        tv_duration.setText(mDescriptor.getDuration() /1000 /60 +"");

        bt_play.setBackground(getDrawable(R.drawable.player_btn_pause_normal));
        bt_play.setTag(1);

        getSupportActionBar().setTitle(mDescriptor.getTitle());
    }


    void reLayoutAlbumArt(){
        float activityWidth = getWindow().getDecorView().getMeasuredWidth();
        float imageViewWidth;
        if (activityWidth == 0) {
            sendEmptyMessageAtTime(MSG_CODE_REFRESH_IMAGEVIEW, System.currentTimeMillis() + 50);
        } else {
            imageViewWidth = activityWidth / 5 * 3;
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) iv.getLayoutParams();
            lp.width = (int) imageViewWidth;
            lp.width = (int) imageViewWidth;
            iv.setLayoutParams(lp);
        }
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
        //ui do not need take action
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
        //ui do not need take action
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

    @Override
    protected void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        if(isFinishing() || isDestroyed()) {
            return;
        }
        switch (msg.what) {
            case MSG_CODE_REFRESH_IMAGEVIEW:
                reLayoutAlbumArt();
                break;
            default:
                    break;
        }
    }
}
