package com.amg.rubik;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by amar on 9/12/15.
 */
public class RubikGLSurfaceView extends GLSurfaceView {

    private static final String tag = "rubik-view";
    private RubikRenderer mRenderer;

    private static final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;


    RubikGLSurfaceView(Context context) {
        super(context);
        Log.w(tag, "creating glsurfaceview");
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        mRenderer = new RubikRenderer();
        setRenderer(mRenderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }

                mRenderer.setAngle(
                        mRenderer.getAngle() +
                                ((dx + dy) * TOUCH_SCALE_FACTOR));
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    public void printDebugInfo() {
        Log.w(tag, "FPS " + mRenderer.getFps());
    }

    public void setCube(RubiksCube cube) {
        mRenderer.setCube(cube);
    }
}
