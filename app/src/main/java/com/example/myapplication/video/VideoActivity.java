package com.example.myapplication.video;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myapplication.R;
import com.example.myapplication.bean.MediaFileDescrtpter;

import java.io.IOException;

public class VideoActivity extends AppCompatActivity {


    MediaFileDescrtpter  mDescrtpter;
    RecyclerView rv_list;
    Button bt_pick,bt_done;

    SourceAdapter mAdapter;

    SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        rv_list = findViewById(R.id.rv_source_list);
        bt_done = findViewById(R.id.bt_done);
        bt_pick = findViewById(R.id.bt_pick);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_list.setLayoutManager(linearLayoutManager);
        mAdapter = new SourceAdapter();
        rv_list.setAdapter(mAdapter);


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
            //MediaMuxer mediaMuxer = new MediaMuxer();
            try {
                extractor.setDataSource(this,uri,null);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        MediaExtractor extractor = new MediaExtractor();

        MediaMuxer mediaMuxer = new MediaMuxer("",MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mediaMuxer.addTrack(MediaFormat.createAudioFormat());
        mediaMuxer.start();
        mediaMuxer.writeSampleData();

        MediaFormat mediaFormat = extractor.getTrackFormat(0);
        mediaFormat.getByteBuffer()


    }

    class SourceViewHolder extends  RecyclerView.ViewHolder {

        public SourceViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class SourceAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

    }
}
