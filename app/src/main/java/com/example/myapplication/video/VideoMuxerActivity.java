package com.example.myapplication.video;

import androidx.annotation.Nullable;

import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.base.BaseHandlerActivity;
import com.example.myapplication.bean.MediaFileDescrtpter;
import com.example.myapplication.media.MediaParser;
import com.example.myapplication.media.SimpleMediaMuxer;

import java.io.IOException;
import java.util.ArrayList;

import static android.os.Environment.DIRECTORY_MOVIES;

public class VideoMuxerActivity extends BaseHandlerActivity implements View.OnClickListener {

    Button bt_pick1,bt_pick2;
    TextView tv_path1,tv_path2,tv_result;
    String path1,path2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_muxer);
        bt_pick1 = findViewById(R.id.bt_pick);
        bt_pick2 = findViewById(R.id.bt_pick_2);
        tv_path1 = findViewById(R.id.tv_path1);
        tv_path2 = findViewById(R.id.tv_path2);
        tv_result = findViewById(R.id.tv_result);

        bt_pick1.setOnClickListener(this);
        bt_pick2 .setOnClickListener(this);
        findViewById(R.id.bt_muxer).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.bt_muxer) {


            new Thread(){
                @Override
                public void run() {
                    super.run();

                    String path = VideoMuxerActivity.this.getExternalFilesDir(DIRECTORY_MOVIES) + "/test.mp4";
                    Log.d("jxc",path);
                    try {



                    final SimpleMediaMuxer mediaMuxer = new SimpleMediaMuxer(path);

                    MediaFormat mediaFormat = SimpleMediaMuxer.createSimpleAudioFormat(44100,2);
                    mediaMuxer.addTrack(mediaFormat);
                    mediaFormat = SimpleMediaMuxer.createSimpleVideoFormat(1080,2440);
                    mediaMuxer.addTrack(mediaFormat);

                    mediaMuxer.start();

                    MediaParser mediaParser1= null;
                    MediaParser mediaParser2 = null;



                    try {
                        mediaParser1 = new MediaParser(path1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    MediaParser.CodecCallBack callBack = new MediaParser.CodecCallBack() {
                        @Override
                        public void onAudioDecoderedBufferAvailable(byte[] data, MediaCodec.BufferInfo info) {
                            mediaMuxer.writeAudioData(data,info);

                        }

                        @Override
                        public void onVideoDecoderedBufferAvailAble(byte[] data, MediaCodec.BufferInfo info) {
                            mediaMuxer.writeVideoData(data,info);
                        }
                    };

                    mediaParser1.setCodecCallBack(callBack);
                    mediaParser1.decodeAudio();
                    mediaParser1.decodeVideo();

                    /*try {
                        mediaParser2 = new MediaParser(path2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mediaParser2.setCodecCallBack(callBack);
                    mediaParser2.decodeAudio();
                    mediaParser2.decodeVideo();*/

                    Thread.sleep(100);
                    mediaMuxer.stop();
                    }
                    catch (Exception e) {
                        Log.d("jxc","error" + e.getMessage());
                        e.printStackTrace();

                    }
                }
            }.start();
            return;
        }
        Intent intent = new Intent(this, MediaPickerActivity.class);
        startActivityForResult(intent,v.getId() == R.id.bt_pick ? 1:2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;
        ArrayList<String> list = data.getStringArrayListExtra("result");
        MediaFileDescrtpter descrtpter =null;
        if(list != null && list.size() >0 ) {
            descrtpter = MediaFileDescrtpter.unmarshall(list.get(0));
        }
        if(descrtpter == null) return;
        if(requestCode == 1) {
            tv_path1.setText(descrtpter.getData());
            path1 = descrtpter.getData();
        } else {
            tv_path2.setText(descrtpter.getData());
            path2 = descrtpter.getData();
        }
    }


}
