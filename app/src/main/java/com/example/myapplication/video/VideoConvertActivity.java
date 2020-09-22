package com.example.myapplication.video;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.TextView;

import com.example.myapplication.IVideoInterface;
import com.example.myapplication.R;
import com.example.myapplication.base.BaseHandlerActivity;

import java.lang.reflect.Array;
import java.util.Arrays;

public class VideoConvertActivity extends BaseHandlerActivity {

    TextView tv_codecs_infos,tv_detail,tv_output;
    RecyclerView listView;

    IVideoInterface iVideoInterface;

    ServiceConnection mVideoServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iVideoInterface = IVideoInterface.Stub.asInterface(service);
            try {
                iVideoInterface.videoPrepare(mData.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    Intent mIntent = new Intent(this,VideoService.class);

    Uri mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_convert);
        tv_codecs_infos = findViewById(R.id.tv_codec_infos);
        tv_detail = findViewById(R.id.tv_codec_infos);
        tv_output = findViewById(R.id.tv_output);

        //dump();

        mData = getIntent().getData();
        if(mData == null) {
            finish();
        }
        bindService(mIntent,mVideoServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    void dump(){
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[] infos = mediaCodecList.getCodecInfos();
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0;i<infos.length;i++) {
            MediaCodecInfo info = infos[i];

            stringBuilder.append("  name:").append(info.getName()).append("\n")
                    .append("  canonicalName:").append(info.getCanonicalName()).append("\n")
                    .append("  isEncoder:").append(info.isEncoder()).append("\n")
                    .append("  supportedTypes:").append(Arrays.toString(info.getSupportedTypes())).append("\n")
                    .append("  isAlias：").append(info.isAlias()).append("\n")
                    .append("  isHardwareAccelerated:").append(info.isHardwareAccelerated()).append("\n")
                    .append("  isSoftwareOnly:").append(info.isSoftwareOnly()).append("\n")
                    .append("  isVendor:").append(info.isVendor())
                    .append("  ").append(info.getCapabilitiesForType(info.getSupportedTypes()[0]))
                    .append("\n---------------------------------------------------------\n");

        }
        tv_codecs_infos.setText(stringBuilder.toString());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mVideoServiceConnection);
    }
}
