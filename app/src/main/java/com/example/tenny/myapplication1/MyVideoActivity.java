package com.example.tenny.myapplication1;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by tenny on 17-4-13.
 */

public class MyVideoActivity extends Activity {
    GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);
        addContentView(glSurfaceView, new ViewGroup.LayoutParams(-1, -1));
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new MyRender());

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    class MyRender implements GLSurfaceView.Renderer {
        private String verticalShaderStr = "attribute vec4 vPosition;\n"
                + "attribute vec2 a_texCoord;"
                + "varying vec2 v_texCoord;"
                + "void main() {\n"
                + "  gl_Position = vPosition;\n"
                + "  v_texCoord = a_texCoord;"
                + "}";
        ;
        private String fragmentShaderStr = "precision mediump float;" +
                "varying vec2 v_texCoord;" +
                "uniform sampler2D s_texture;" +
                "void main() {" +
                "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
                "}";

        private final float[] VERTEX = {   // in counterclockwise order:
                1, 1, 0,   // top right
                -1, 1, 0,  // top left
                -1, -1, 0, // bottom left
                1, -1, 0,  // bottom right
        };
        private final short[] VERTEX_INDEX = {0, 1, 2, 2, 0, 3};
        private final float[] UV_TEX_VERTEX = {   // in clockwise order:
                1, 0,  // bottom right
                0, 0,  // bottom left
                0, 1,  // top left
                1, 1,  // top right
        };

        private FloatBuffer mVertexBuffer;
        private final ShortBuffer mVertexIndexBuffer;
        private FloatBuffer mUvTexVertexBuffer;

        private int mProgram;
        private int mPositionHandle;
        private int mTexCoordHandle;
        private int mTexSamplerHandle;
        int[] mTexNames;

        public MyRender() {
            mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(VERTEX);
            mVertexBuffer.position(0);
            mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * 2)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer()
                    .put(VERTEX_INDEX);
            mVertexIndexBuffer.position(0);

            mUvTexVertexBuffer = ByteBuffer.allocateDirect(UV_TEX_VERTEX.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(UV_TEX_VERTEX);
            mUvTexVertexBuffer.position(0);
        }

        int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mProgram = GLES20.glCreateProgram();
            int verticalShader = loadShader(GLES20.GL_VERTEX_SHADER, verticalShaderStr);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderStr);
            GLES20.glAttachShader(mProgram, verticalShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
            mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

            mTexNames = new int[1];
            GLES20.glGenTextures(1, mTexNames, 0);


            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexNames[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_REPEAT);

        }

        @Override
        public void onDrawFrame(GL10 gl) {

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();


            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUseProgram(mProgram);

            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                    0, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(mTexCoordHandle);

            GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0,
                    mUvTexVertexBuffer);
            GLES20.glUniform1i(mTexSamplerHandle, 0);


            GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.length,
                    GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);


            GLES20.glDisableVertexAttribArray(mTexCoordHandle);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
        //Matrix.frustumM();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

}
