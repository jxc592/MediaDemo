package com.example.myapplication.video;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaExtractor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myapplication.R;
import com.example.myapplication.base.BaseHandlerActivity;
import com.example.myapplication.bean.MediaFileDescrtpter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoActivity extends BaseHandlerActivity implements View.OnClickListener {


    MediaFileDescrtpter  mDescrtpter;
    RecyclerView rv_list;
    Button bt_pick,bt_done;

    SourceAdapter mAdapter;

    SurfaceView mSurfaceView;

    List<MediaFileDescrtpter> mDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        getSupportActionBar().setTitle("video edit");
        rv_list = findViewById(R.id.rv_source_list);
        bt_done = findViewById(R.id.bt_done);
        bt_pick = findViewById(R.id.bt_pick);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_list.setLayoutManager(linearLayoutManager);
        mAdapter = new SourceAdapter();
        rv_list.setAdapter(mAdapter);

        bt_pick.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            ArrayList<String> list = data.getStringArrayListExtra("result");
            if(list != null && list.size() >0 ) {
                for (String a :list) {
                    MediaFileDescrtpter mediaFileDescrtpter = MediaFileDescrtpter.unmarshall(a);
                    boolean exist =false;
                    for (MediaFileDescrtpter descrtpter : mDataList) {
                        if( mediaFileDescrtpter.getId() == descrtpter.getId()) {
                            exist = true;
                            break;
                        }
                    }
                    if(!exist) {
                        mDataList.add(mediaFileDescrtpter);
                    }
                }

                for (MediaFileDescrtpter descrtpter :mDataList) {

                }
            }
        }
    }

    @Override
    protected void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
    }

    void getContent(){
        Intent intent = new Intent(VideoActivity.this,MediaPickerActivity.class);
        startActivityForResult(intent,1);
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
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.bt_pick) {
            getContent();
        }
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
