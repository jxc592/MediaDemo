package com.example.myapplication.video;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.StatusBarManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.R;
import com.example.myapplication.bean.MediaFileDescrtpter;

import java.io.File;

public class VideoPlayActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    VideoView mVideoView;

    MediaFileDescrtpter mDescrtpter;
    MediaController mediaController;

    FrameLayout mVideoParent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_play);
        mVideoView = findViewById(R.id.videoView);
        mVideoParent = findViewById(R.id.videoView_parent);
        getSupportActionBar().hide();
        //getActionBar().hide();



    }

    @Override
    protected void onResume() {
        super.onResume();
        mDescrtpter = getIntent().getParcelableExtra("data");
        Uri uri = getIntent().getData();
        if(uri != null ) {


            mVideoView.setVideoURI(uri);
            mVideoView.setVideoPath(mDescrtpter.getData());
            mVideoView.setOnCompletionListener(this);
            mVideoView.setOnPreparedListener(this);
            mVideoView.setOnErrorListener(this);

            mediaController = new MediaController(this);
            mVideoView.setMediaController(mediaController);
            mediaController.setEnabled(true);
            mediaController.setMediaPlayer(mVideoView);
            //mVideoView.start();

        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        layoutVideoView();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();

        layoutVideoView();

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e("jxc","i" + i);
        return false;
    }


    void layoutVideoView(){
        if(mDescrtpter !=null) {
            float width = mDescrtpter.getWidth();
            float height = mDescrtpter.getHeight();

            float viewHeight = mVideoParent.getMeasuredHeight();
            float viewWidth = mVideoParent.getMeasuredWidth();

            float heightRat= viewHeight/height;
            float widthRat = viewWidth/width;

            Log.d("jxc","wr: " + widthRat + " hr =" +heightRat);

            double calWidth;
            double calHeight;
            if(widthRat > heightRat) {
                calHeight = viewHeight;
                calWidth = width * heightRat;
            } else {
                calWidth = viewWidth;
                calHeight = height * widthRat;
            }
            Log.d("jxc","cal width = " +calWidth + "heigh = " + calHeight);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mVideoView.getLayoutParams();
            lp.width = (int)calWidth;
            lp.height = (int)calHeight;
            mVideoView.setLayoutParams(lp);
        }
    }
}
