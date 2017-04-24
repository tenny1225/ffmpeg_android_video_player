package com.example.tenny.myapplication1;

import android.util.Log;

/**
 * Created by tenny on 17-4-4.
 */

public class DecoderJniLib {
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avutil-55");
        System.loadLibrary("swresample-2");
        System.loadLibrary("swscale-4");
    }

    public native static String stringFromJNI();

    public native static void decode(String input, String output, int w, int h, byte[] data);
}
