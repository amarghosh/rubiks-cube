package com.amg.rubik;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

public abstract class GLRenderer implements Renderer {

    private static final String tag = "rubik-glrenderer";

    private boolean mFirstDraw;
    private boolean mIsNewSurface;
    private int mWidth;
    private int mHeight;
    private long mStartTime;
    private int mFrameCount;

    public GLRenderer() {
        mFirstDraw = true;
        mIsNewSurface = false;
        mWidth = -1;
        mHeight = -1;
        mStartTime = System.currentTimeMillis();
        mFrameCount = 0;
    }

    protected final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    protected float[] mRotationMatrix = new float[16];

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        if (!mIsNewSurface && mWidth == width && mHeight == height) {
            return;
        }

        Log.w(tag, "Width " + width + ", Height " + height);

        GLES20.glViewport(0, 0, width, height);

        float ratio = ((float) width) / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0,
                -ratio, ratio,
                -1, 1,
                3, 7);
        mWidth = width;
        mHeight = height;

        GLES20.glEnable(GLES20.GL_CULL_FACE);

        onCreate(width, height, mIsNewSurface);
        mIsNewSurface = false;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        mIsNewSurface = true;
        mWidth = -1;
        mHeight = -1;
    }

    public void onDrawFrame(GL10 arg0) {
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                2, 2, 3f, // eye
                0f, 0f, 0f, // center
                0f, 1.0f, 0f // up
            );

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        onDrawFrame(mFirstDraw);

        mFrameCount++;
        mFirstDraw = false;
    }

    /**
     * Get the FPS (frames per second). This function resets the timer and frame count.
     * */
    public int getFps() {
        long currentTime = System.currentTimeMillis();
        int fps = (int)((mFrameCount * 1000) / (currentTime - mStartTime));
        mStartTime = System.currentTimeMillis();
        mFrameCount = 0;
        return fps;
    }

    public abstract void onCreate(int width, int height, boolean contextLost);
    public abstract void onDrawFrame(boolean firstDraw);
}
