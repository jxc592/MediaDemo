package com.example.myapplication.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;

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
}
