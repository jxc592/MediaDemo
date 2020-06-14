package com.example.myapplication.util;

import android.graphics.Bitmap;
import android.view.Surface;

public class NativeLib {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    public native byte[] parserAlbumArt(String path);

}
