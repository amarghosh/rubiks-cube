package com.amg.rubik.graphics;

import android.opengl.GLES20;
import android.util.Log;

import com.amg.rubik.cube.RubiksCube;

/**
 * Created by amar on 30/11/15.
 */
public class RubikRenderer extends GLRenderer {

    private static final String tag = "rubik-renderer";
    RubiksCube mCube;

    public RubikRenderer() {
        mCube = null;
    }

    public void setCube(RubiksCube cube) {
        this.mCube = cube;
    }

    @Override
    public void onCreate(int width, int height, boolean contextLost) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
    }

    @Override
    public void onDrawFrame(boolean firstDraw) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
                | GLES20.GL_DEPTH_BUFFER_BIT);
        if (mCube == null) {
            Log.w(tag, "no cube set");
            return;
        }
        mCube.draw(mMVPMatrix);
    }
}
