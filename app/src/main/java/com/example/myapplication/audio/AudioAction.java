package com.example.myapplication.audio;

public interface AudioAction {

    public static String ACTION_COMPLETE = "music_action_completed";
    public static String ACTION_PLAY = "music_action_play";
    public static String ACTION_PLAY_STATE = "music_action_play_state";
    public static String ACTION_PAUSE = "music_action_pause";
    public static String ACTION_PAUSE_STATE = "music_action_pause_state";
    public static String ACTION_PRE ="music_action_pre";
    public static String ACTION_NEXT ="music_action_next";

    void onCompleted () ;
    void onPlayStart();
    void onPlayStarted();
    void onPlayPause();
    void onPlayPaused();
    void onPre();
    void onNext();
}
