package com.example.tenny.myapplication1;

import android.util.Log;

/**
 * Created by tenny on 17-4-16.
 */

public class GLJniLib {
    public  interface RefreshListener{
        void refresh();
    }
    RefreshListener refreshListener;
    static {
        System.loadLibrary("native-lib");
    }

    public void setRefresh(RefreshListener listener){
        refreshListener = listener;
    }
    public void refresh(){
        refreshListener.refresh();
    }
    public  native void init(int width, int height);

    public  native void create();

    public  native void step();
}
