package com.example.myapplication;

import android.app.Application;

import com.example.myapplication.base.BaseComponentManager;

public class VideoApplication  extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BaseComponentManager.getInstance().addComponent(this);
    }
}
