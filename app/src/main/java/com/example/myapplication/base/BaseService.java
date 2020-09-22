package com.example.myapplication.base;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BaseService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


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
    public void onCreate() {
        super.onCreate();
        BaseComponentManager.getInstance().addComponent(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseComponentManager.getInstance().removeComponent(this);
    }
}
