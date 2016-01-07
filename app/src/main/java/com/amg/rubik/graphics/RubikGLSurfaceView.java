package com.amg.rubik.graphics;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.amg.rubik.cube.Rotation;
import com.amg.rubik.cube.Rotation.Axis;
import com.amg.rubik.cube.Rotation.Direction;
import com.amg.rubik.cube.RubiksCube;
import com.amg.rubik.graphics.RubikRenderer;

import java.security.InvalidParameterException;

/**
 * Created by amar on 9/12/15.
 */
public class RubikGLSurfaceView extends GLSurfaceView {

    private static final String tag = "rubik-view";
    private RubikRenderer mRenderer;
    private RubiksCube mCube;

    private static final int TAP_SENSITIVITY_FACTOR = 10;

    Point mStartPoint;
    Point mEndPoint;
    Point mIntermediatePoint;

    public RubikGLSurfaceView(Context context) {
        super(context);
        Log.w(tag, "creating glsurfaceview");
        init();
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        mRenderer = new RubikRenderer();
        setRenderer(mRenderer);
    }

    void init() {
        mStartPoint = new Point();
        mEndPoint = new Point();
        mIntermediatePoint = new Point();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleMovementStart(e);
                break;

            case MotionEvent.ACTION_UP:
                handleMovementEnd(e);
                break;
        }

        mIntermediatePoint.x = (int)e.getX();
        mIntermediatePoint.y = (int)e.getY();
        return true;
    }

    private void handleMovementStart(MotionEvent e) {
        mStartPoint.x = (int)e.getX();
        mStartPoint.y = (int)e.getY();
    }

    private static final float SLOPE_CUTOFF_MIN_X = -50.0f;
    private static final float SLOPE_CUTOFF_MAX_X = -1.0f;
    private static final float SLOPE_CUTOFF_MIN_Y = -0.65f;
    private static final float SLOPE_CUTOFF_MAX_Y = 0.65f;
    private static final float SLOPE_CUTOFF_MIN_Z = 0.75f;
    private static final float SLOPE_CUTOFF_MAX_Z = 10.0f;

    private static final int FACE_RANGE_X_MIN = 30;
    private static final int FACE_RANGE_X_MAX = 50;
    private static final int FACE_RANGE_Y_MIN = 55;
    private static final int FACE_RANGE_Y_MAX = 60;
    private static final int FACE_RANGE_Z_MIN = 35;
    private static final int FACE_RANGE_Z_MAX = 40;

    private void handleMovementEnd(MotionEvent e) {
        Rotation rot = null;
        Direction direction;
        Axis axis;
        int face = 2;

        mEndPoint.x = (int)e.getX();
        mEndPoint.y = (int)e.getY();
        int dx = mEndPoint.x - mStartPoint.x;
        int dy = mEndPoint.y - mStartPoint.y;

        if (Math.abs(dx) + Math.abs(dy) < TAP_SENSITIVITY_FACTOR) {
            Log.w(tag, String.format("noop: dx %d, dy %d", dx, dy));
            return;
        }
        if (dx == 0) dx = 1;

        float slope = ((float)dy) / dx;

        Log.w(tag, String.format("From %d,%d to %d,%d: slope %f",
                mStartPoint.x, mStartPoint.y,
                mEndPoint.x, mEndPoint.y, slope));

        if (slope > SLOPE_CUTOFF_MIN_X && slope < SLOPE_CUTOFF_MAX_X) {
            axis = Axis.X_AXIS;
            direction = dy > 0 ? Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
            face = estimateFace(mStartPoint.x, getWidth(), mCube.size(),
                    FACE_RANGE_X_MIN, FACE_RANGE_X_MAX);
            rot = new Rotation(axis, direction, face);
        } else if (slope > SLOPE_CUTOFF_MIN_Y && slope < SLOPE_CUTOFF_MAX_Y) {
            axis = Axis.Y_AXIS;
            direction = dx > 0 ? Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
            face = estimateFace(mStartPoint.y, getHeight(), mCube.size(),
                    FACE_RANGE_Y_MIN, FACE_RANGE_Y_MAX);
            face = mCube.size() - 1 - face;
            rot = new Rotation(axis, direction, face);
        } else if (slope > SLOPE_CUTOFF_MIN_Z && slope < SLOPE_CUTOFF_MAX_Z) {
            axis = Axis.Z_AXIS;
            direction = dy > 0 ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
            face = estimateFace(mStartPoint.y, getHeight(), mCube.size(),
                    FACE_RANGE_Z_MIN, FACE_RANGE_Z_MAX);
            rot = new Rotation(axis, direction, face);
        } else {
            Log.w(tag, "hmm...?!");
        }

        if (rot != null) {
            mCube.rotate(rot);
        }
    }

    private int estimateFace(int pos, int max_pos, int cubeSize, int zeroFactor, int maxFactor) {
        if (cubeSize == 2 && zeroFactor != maxFactor) {
            maxFactor = zeroFactor;
        }
        if (pos < 0 || max_pos < 0 || pos > max_pos) {
            throw new InvalidParameterException(
                    String.format("pos %d max_pos %d", pos, max_pos));
        }

        int pt = (pos * 100) / max_pos;
        if (pt < zeroFactor) return 0;
        if (pt >= maxFactor) return cubeSize - 1;
        int result = 1 + ((pt - zeroFactor) / ((maxFactor - zeroFactor) / (cubeSize - 2)));
        Log.w(tag, String.format("getFace: %d/%d, pt %d, result %d (factors %d %d)",
                    pos, max_pos, pt, result, zeroFactor, maxFactor));
        return result;
    }

    public void printDebugInfo() {
        Log.w(tag, "FPS " + mRenderer.getFps());
    }

    public void setCube(RubiksCube cube) {
        mCube = cube;
        mRenderer.setCube(cube);
    }
}
