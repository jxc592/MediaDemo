package com.example.myapplication.audio;

import android.media.AudioManager;

import java.util.HashMap;

public class AudioUtils {

    /*
    *
    public static final int AUDIOFOCUS_NONE = 0;
    public static final int AUDIOFOCUS_GAIN = 1;
    public static final int AUDIOFOCUS_GAIN_TRANSIENT = 2;
    public static final int AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK = 3;
    public static final int AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE = 4;
    public static final int AUDIOFOCUS_LOSS = -1 ;
    public static final int AUDIOFOCUS_LOSS_TRANSIENT = -2;
    public static final int AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK =-3;
    * */

    public static int[] AudioFocusArray = {
        AudioManager.AUDIOFOCUS_NONE,
        AudioManager.AUDIOFOCUS_GAIN,
        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE,
        AudioManager.AUDIOFOCUS_LOSS,
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
    };

    public static String[] AudioFocusStrArray = {
        "AUDIOFOCUS_NONE",
        "AUDIOFOCUS_GAIN",
        "AUDIOFOCUS_GAIN_TRANSIENT",
        "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK",
        "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE",
        "AUDIOFOCUS_LOSS",
        "AUDIOFOCUS_LOSS_TRANSIENT" ,
        "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK"
    };

    public static String AudioStateToStr(int focus){
        String result =null;
        for (int i =0;i<AudioFocusArray.length;i++) {
            if(focus == AudioFocusArray[i]) {
                result = AudioFocusStrArray[i];
                break;
            }
        }
        return result;
    }


}
