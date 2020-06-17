package com.example.myapplication.video;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaExtractor;
import android.net.Uri;
import android.os.Bundle;

import com.example.myapplication.R;
import com.example.myapplication.bean.MediaFileDescrtpter;

import java.io.IOException;

public class VideoActivity extends AppCompatActivity {


    MediaFileDescrtpter  mDescrtpter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    void refreshView(){
        mDescrtpter = getIntent().getParcelableExtra("data");
        Uri uri = getIntent().getData();
        if(uri!= null) {
            MediaExtractor extractor = new MediaExtractor();
            try {
                extractor.setDataSource(this,uri,null);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
