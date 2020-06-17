package com.example.myapplication.util;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ImageLoader extends Service {

    private static ImageLoader instance;
    Context context = getApplicationContext();
    public static ImageLoader getInstance(Context context) {
         if(instance == null) {
             instance = new ImageLoader(context);
         }
        return new ImageLoader(context);
    }

    private ImageLoader(Context context){

    }

    Handler mHander = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    public void loadImage(ImageView imageView, final Uri uri) {
        new Thread(){
            @Override
            public void run() {
                super.run();

            }
        }.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
