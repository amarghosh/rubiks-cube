package com.amg.rubik.graphics;

import android.opengl.GLES20;
import android.util.Log;

import com.amg.rubik.cube.Rotation;
import com.amg.rubik.cube.RubiksCube;
import com.amg.rubik.cube.Square;

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

    Square highlightPoint;
    float mSize = 0.03f;

    public void setHighlightPoint(Point3D point, Rotation.Axis axis) {
        // TODO: only front face is implemented now
        if (axis != Rotation.Axis.Z_AXIS) throw new AssertionError();
        Point3D[] corners = new Point3D[4];

        corners[0] = new Point3D(point.getX() - mSize, point.getY() + mSize, point.getZ());
        corners[1] = new Point3D(point.getX() - mSize, point.getY() - mSize, point.getZ());
        corners[2] = new Point3D(point.getX() + mSize, point.getY() - mSize, point.getZ());
        corners[3] = new Point3D(point.getX() + mSize, point.getY() + mSize, point.getZ());

        this.highlightPoint = new Square(corners, Square.HIGHLIGHT_COLOR);
    }

    @Override
    public void onDrawFrame(boolean firstDraw) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
                | GLES20.GL_DEPTH_BUFFER_BIT);
        if (mCube == null) {
            Log.w(tag, "no cube set");
            return;
        }

        Square.startDrawing();
        mCube.draw(mMVPMatrix);
        if (highlightPoint != null) {
            highlightPoint.draw(mMVPMatrix);
        }
        Square.finishDrawing();
    }
}
