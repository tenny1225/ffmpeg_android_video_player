package com.example.tenny.myapplication1;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.Serializable;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by tenny on 17-4-4.
 */

public class OpenGLActivity extends Activity implements GLJniLib.RefreshListener {
    GLSurfaceView surfaceView;
    GLJniLib glJniLib;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opengl_activity);
        surfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
        surfaceView.setEGLContextClientVersion(2);
        glJniLib = new GLJniLib();
        glJniLib.setRefresh(this);
        surfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                glJniLib.create();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                glJniLib.init(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                glJniLib.step();
            }
        });
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void refresh() {
        if (surfaceView != null)
            surfaceView.requestRender();
    }

    @Override
    protected void onResume() {
        super.onResume();
        surfaceView.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
        surfaceView.onPause();
    }
}
