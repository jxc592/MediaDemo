package com.example.myapplication.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseHandlerActivity extends AppCompatActivity {

    Handler mHander = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            handleMessage(msg);
        }
    };

    protected void sendMessage(Message message){
        mHander.sendMessage(message);
    }

    protected void sendEmptyMessage(int i){
        mHander.sendEmptyMessage(i);
    }

    protected void sendEmptyMessageAtTime(int i,long uptimeMillis){
        mHander.sendEmptyMessageAtTime(i,uptimeMillis);
    }

    protected void postRunable(Runnable runnable){
        mHander.post(runnable);
    }

    protected void postDelayedRunable(Runnable runnable,long delay) {
        mHander.postDelayed(runnable,delay);
    }

    protected void handleMessage(@NonNull Message msg) {

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseComponentManager.getInstance().addComponent(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //BaseComponentManager.getInstance().re
    }
}
