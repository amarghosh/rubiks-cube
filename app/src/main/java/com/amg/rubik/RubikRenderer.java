package com.amg.rubik;

import android.opengl.GLES20;

/**
 * Created by amar on 30/11/15.
 */
public class RubikRenderer extends GLRenderer {

    RubiksCube mCube;

    RubikRenderer(RubiksCube cube) {
        mCube = cube;
    }

    @Override
    public void onCreate(int width, int height, boolean contextLost) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
    }

    @Override
    public void onDrawFrame(boolean firstDraw) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
                | GLES20.GL_DEPTH_BUFFER_BIT);
        mCube.draw(mMVPMatrix);
    }
}
