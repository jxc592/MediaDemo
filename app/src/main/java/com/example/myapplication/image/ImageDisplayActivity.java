package com.example.myapplication.image;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.myapplication.R;
import com.example.myapplication.bean.MediaFileDescrtpter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ImageDisplayActivity extends AppCompatActivity  {

    ImageView mImageView;

    MediaFileDescrtpter mDescrtpter;

    FrameLayout mVideoParent;

    Handler mHander;

    Bitmap mBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image);
        mImageView = findViewById(R.id.iv_preview);
        mVideoParent = findViewById(R.id.videoView_parent);
        getSupportActionBar().hide();
        mHander = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                ImageDisplayActivity.this.handleMessage(msg);
            }
        };
        //getActionBar().hide();
        refresh();

    }

    void handleMessage(@NonNull Message msg){

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    void refresh(){
        mDescrtpter = getIntent().getParcelableExtra("data");
        Uri uri = getIntent().getData();
        if(uri != null ) {
            InputStream inputStream ;
            try {
                inputStream = getContentResolver().openInputStream(uri);

                if(mBitmap != null) {
                    mBitmap.recycle();
                }
                mBitmap = BitmapFactory.decodeStream(inputStream);
                //mVideoView.start();
                inputStream.close();
                mImageView.setImageBitmap(mBitmap);
                mHander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        layoutImageView();
                    }
                },200);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        layoutImageView();
    }


    

    void layoutImageView(){
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
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mImageView.getLayoutParams();
            lp.width = (int)calWidth;
            lp.height = (int)calHeight;
            mImageView.setLayoutParams(lp);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBitmap != null) {
            mBitmap.recycle();
        }
    }
}
