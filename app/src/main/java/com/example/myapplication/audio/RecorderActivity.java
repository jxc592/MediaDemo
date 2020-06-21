package com.example.myapplication.audio;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.view.View;

import com.example.myapplication.R;
import com.example.myapplication.media.MediaParser;

import java.io.IOException;

import static android.media.AudioTrack.MODE_STREAM;

public class RecorderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        findViewById(R.id.bt_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        MediaParser mediaParser = null;
                        try {
                            mediaParser = new MediaParser("/storage/emulated/0/Music/周杰伦合集/电影原声带/天台爱情电影原声带/黄雨勋 - 水管的友情.mp3");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        mediaParser.dumpAudioFormat();
                        AudioAttributes audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build();
                        int sampleRate = mediaParser.getAudioFormat().getInteger(MediaFormat.KEY_SAMPLE_RATE);
                        //int  format = mediaParser.getAudioFormat().getInteger(MediaFormat.KEY_PCM_ENCODING);
                        //int chanel = mediaParser.getAudioFormat().getInteger(MediaFormat.KEY_CHANNEL_MASK);
                        AudioFormat audioFormat = new AudioFormat.Builder().setSampleRate(sampleRate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build();
                        int minbuffer = AudioTrack.getMinBufferSize(sampleRate,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT);
                        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
                        final AudioTrack track = new AudioTrack(audioAttributes,audioFormat,minbuffer,MODE_STREAM,am.generateAudioSessionId());
                        track.play();
                        mediaParser.setCodecCallBack(new MediaParser.CodecCallBack() {
                           @Override
                           public void onAudioDecoderedBufferAvailable(byte[] data, MediaCodec.BufferInfo info) {
                               track.write(data,0,data.length);
                           }

                            @Override
                            public void onVideoDecoderedBufferAvailAble(byte[] data, MediaCodec.BufferInfo info) {

                            }
                        });
                        mediaParser.decodeAudio();

                    }
                }.start();

            }
        });
    }
}
