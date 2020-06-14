package com.example.myapplication.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class AudioActionReciver extends BroadcastReceiver implements AudioAction{

    public void setActionListener(AudioAction actionListener) {
        this.actionListener = actionListener;
    }

    AudioAction actionListener;
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(AudioAction.ACTION_COMPLETE.equals(action)) {
                onCompleted();
            } else if(AudioAction.ACTION_PLAY.equals(action)) {
                onPlayStart();
            } else if(AudioAction.ACTION_PAUSE.equals(action)) {
                onPlayPause();
            } else if(AudioAction.ACTION_PRE.equals(action)) {
                onPre();
            }  else if(AudioAction.ACTION_NEXT.equals(action)) {
                onNext();
            } else if(AudioAction.ACTION_PAUSE_STATE.equals(action)) {
                onPlayPaused();
            } else if(AudioAction.ACTION_PLAY_STATE.equals(action)) {
                onPlayStarted();
            }
        }


    @Override
    public void onCompleted() {
        if(actionListener!= null) { actionListener.onCompleted();}
    }

    @Override
    public void onPlayStart() {
        if(actionListener!= null) { actionListener.onPlayStart();}
    }

    @Override
    public void onPlayStarted() {
        if(actionListener!= null) { actionListener.onPlayStarted();}
    }

    @Override
    public void onPlayPause() {
        if(actionListener!= null) { actionListener.onPlayPause();}
    }

    @Override
    public void onPlayPaused() {
        if(actionListener!= null) { actionListener.onPlayPaused();}
    }

    @Override
    public void onPre() {
        if(actionListener!= null) { actionListener.onPre();}
    }

    @Override
    public void onNext() {
        if(actionListener!= null) { actionListener.onNext();}
    }
}