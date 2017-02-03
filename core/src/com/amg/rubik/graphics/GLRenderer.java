
package com.amg.rubik.graphics;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

public abstract class GLRenderer implements Renderer {

    private static final String tag = "rubik-glrenderer";

    private static final float DEFAULT_EYE_X = 2;
    private static final float DEFAULT_EYE_Y = 2;
    private static final float DEFAULT_EYE_Z = 3;

    private Point3D mEye;

    private boolean mFirstDraw;
    private boolean mIsNewSurface;
    private int mWidth;
    private int mHeight;
    private long mStartTime;
    private int mFrameCount;

    private float frustrumLeft;
    private float frustrumRight;
    private float frustrumTop;
    private float frustrumBottom;
    private float frustrumNear;
    private float frustrumFar;


    final float[] mMVPMatrix = new float[16];
    protected final float[] mProjectionMatrix = new float[16];
    protected final float[] mViewMatrix = new float[16];


    public GLRenderer() {
        mFirstDraw = true;
        mIsNewSurface = false;
        mWidth = -1;
        mHeight = -1;
        mStartTime = System.currentTimeMillis();
        mFrameCount = 0;
        mEye = new Point3D(DEFAULT_EYE_X, DEFAULT_EYE_Y, DEFAULT_EYE_Z);

        frustrumLeft = -1;
        frustrumRight = 1;
        frustrumBottom = -1;
        frustrumTop = 1;
        frustrumNear = 3;
        frustrumFar = 7;
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        if (!mIsNewSurface && mWidth == width && mHeight == height) {
            return;
        }

        GLES20.glViewport(0, 0, width, height);
        float ratio = ((float) width) / height;
        frustrumLeft = -ratio;
        frustrumRight = ratio;

        Log.w(tag, String.format("width %d, height %d, ratio %f", width, height, ratio));

        Matrix.frustumM(mProjectionMatrix, 0,
                frustrumLeft, frustrumRight,
                frustrumBottom, frustrumTop,
                frustrumNear, frustrumFar);
        mWidth = width;
        mHeight = height;

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                mEye.getX(), mEye.getY(), mEye.getZ(),
                0f, 0f, 0f, // center
                0f, 1.0f, 0f // up
        );

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

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
        onDrawFrame(mFirstDraw);
        mFrameCount++;
        mFirstDraw = false;
    }

    /**
     * Get the FPS (frames per second). This function resets the timer and frame count.
     * currentTimeMillis sometime returns old value resulting in divide-by-zero error.
     * Avoid that by hardcoding it to 100 milliseconds in such cases.
     * */
    public int getFps() {
        long delta = System.currentTimeMillis() - mStartTime;
        delta = delta == 0 ? 100 : delta;
        int fps = (int) ((mFrameCount * 1000) / delta);
        mStartTime = System.currentTimeMillis();
        mFrameCount = 0;
        return fps;
    }

    public Point3D getEye() {
        return new Point3D(mEye);
    }

    public float getFrustrumTop() {
        return frustrumTop;
    }

    public float getFrustrumBottom() {
        return frustrumBottom;
    }

    public float getFrustrumLeft() {
        return frustrumLeft;
    }

    public float getFrustrumRight() {
        return frustrumRight;
    }

    public float getFrustrumFar() {
        return frustrumFar;
    }

    public float getFrustrumNear() {
        return frustrumNear;
    }

    public float getFrustrumWidth() {
        return frustrumRight - frustrumLeft;
    }

    public float getFrustrumHeight() {
        return frustrumTop - frustrumBottom;
    }

    protected abstract void onCreate(int width, int height, boolean contextLost);
    protected abstract void onDrawFrame(boolean firstDraw);
}
