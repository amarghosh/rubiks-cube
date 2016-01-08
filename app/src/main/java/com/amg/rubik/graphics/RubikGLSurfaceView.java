package com.amg.rubik.graphics;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.amg.rubik.cube.Rotation;
import com.amg.rubik.cube.RubiksCube;

/**
 * Created by amar on 9/12/15.
 */
public class RubikGLSurfaceView extends GLSurfaceView {

    private static final String tag = "rubik-view";
    private RubikRenderer mRenderer;
    private RubiksCube mCube;

    private boolean mRotateWholeCube = false;

    private static final int TAP_SENSITIVITY_FACTOR = 10;

    private PointF mStartPoint;
    private PointF mEndPoint;

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
        mStartPoint = new PointF();
        mEndPoint = new PointF();
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

        return true;
    }

    private TouchInfo getTouchedSquare(float originX ,float originY) {
        TouchInfo result = new TouchInfo();
        Point3D eye = mRenderer.getEye();
        int cubeSize = mCube.size();
        float squareSize = (mCube.getRightFaceX() - mCube.getLeftFaceX()) / cubeSize;
        int row, col;

        /**
         * Figure out the touched point by translating 2D coordinates to the near plane.
         * Keep in mind that opengl uses regular cartesian coordinates, whereas android uses
         * computer coordinates where origin is at top left corner.
         * */
        float touchX = mRenderer.getFrustrumLeft() +
                (originX * mRenderer.getFrustrumWidth()) / getWidth();

        float touchY = mRenderer.getFrustrumBottom() +
                (originY * mRenderer.getFrustrumHeight()) / getHeight();
        touchY *= -1;

        /**
         * TODO: adding a correction to the z value here
         * Reached this value by trial and error on a nexus-4 phone in portrait mode.
         * This seems to adjust the calcuated z value closer to the real point.
         * We gotta figure out the proper way later.
         * */

        float touchZ = eye.getZ() + 0.015f;

        // translate touched point to the eye vector (z value remains same)
        touchX += eye.getX();
        touchY += eye.getY();

        /**
         * Imagine a line through this point and parallel to the eye vector.
         * Check if this line touches either of the three visible faces.
         * Parametric equation for a line is
         * x = x1 + t * a;
         * y = y1 + t * b;
         * z = z1 + t * c;
         * where x1, y1, z1 is a known point and a, b, c is the directional vector.
         *
         * As we know the value of one of x, y, z for each plane, we can find t from there
         * */

        float a = eye.getX();
        float b = eye.getY();
        float c = eye.getZ();
        float x, y, z, t;

        // check for front face
        z = mCube.getFrontFaceZ();
        t = (z - touchZ) / c;
        x = touchX + t * a;
        y = touchY + t * b;

        if (x > mCube.getLeftFaceX() && x < mCube.getRightFaceX() &&
                y > mCube.getBottomFaceY() && y < mCube.getTopFaceY()) {
            row = (int) Math.floor((y - mCube.getBottomFaceY()) / squareSize);
            col = (int) Math.floor((x - mCube.getLeftFaceX()) / squareSize);
            result.update(RubiksCube.FACE_FRONT, row, col);
            result.setPoint(x, y, z);
            return result;
        }

        // check top face
        y = mCube.getTopFaceY();
        t = (y - touchY) / b;
        x = touchX + t * a;
        z = touchZ + t * c;

        if (x > mCube.getLeftFaceX() && x < mCube.getRightFaceX() &&
                z > mCube.getBackFaceZ() && z < mCube.getFrontFaceZ()) {
            row = (int) Math.floor((z - mCube.getBackFaceZ()) / squareSize);
            col = (int) Math.floor((x - mCube.getLeftFaceX()) / squareSize);
            result.update(RubiksCube.FACE_TOP, row, col);
            result.setPoint(x, y, z);
            return result;
        }

        // check right face
        x = mCube.getRightFaceX();
        t = (x - touchX) / a;
        y = touchY + t * b;
        z = touchZ + t * c;

        if (y > mCube.getBottomFaceY() && y < mCube.getTopFaceY() &&
                z > mCube.getBackFaceZ() && z < mCube.getFrontFaceZ()) {
            row = (int) Math.floor((y - mCube.getBottomFaceY()) / squareSize);
            col = (int) Math.floor((z - mCube.getBackFaceZ()) / squareSize);
            result.update(RubiksCube.FACE_RIGHT, row, col);
            result.setPoint(x, y, z);
            return result;
        }

        return result;
    }

    private void handleMovementStart(MotionEvent e) {
        mStartPoint.x = e.getX();
        mStartPoint.y = e.getY();
    }

    private static final float SLOPE_CUTOFF_MIN_X = -50.0f;
    private static final float SLOPE_CUTOFF_MAX_X = -1.0f;
    private static final float SLOPE_CUTOFF_MIN_Y = -0.65f;
    private static final float SLOPE_CUTOFF_MAX_Y = 0.65f;
    private static final float SLOPE_CUTOFF_MIN_Z = 0.75f;
    private static final float SLOPE_CUTOFF_MAX_Z = 10.0f;

    private void handleMovementEnd(MotionEvent e) {
        int cubeSize = mCube.size();
        TouchInfo touch = getTouchedSquare(mStartPoint.x, mStartPoint.y);
        Axis axis;
        if (!touch.status) {
            mRenderer.clearHighlight();
            return;
        }

        mRenderer.setHighlightPoint(touch.point, mCube.face2axis(touch.face));

        Rotation rot = null;
        Direction direction;

        mEndPoint.x = e.getX();
        mEndPoint.y = e.getY();
        int dx = (int) (mEndPoint.x - mStartPoint.x);
        int dy = (int) (mEndPoint.y - mStartPoint.y);

        if (Math.abs(dx) + Math.abs(dy) < TAP_SENSITIVITY_FACTOR) {
            return;
        }
        if (dx == 0) dx = 1;

        float slope = ((float)dy) / dx;

        /*
        Log.w(tag, String.format("From %f,%f to %f,%f: slope %f",
                mStartPoint.x, mStartPoint.y,
                mEndPoint.x, mEndPoint.y, slope));
        */

        int face;

        if (slope > SLOPE_CUTOFF_MIN_X && slope < SLOPE_CUTOFF_MAX_X) {
            axis = Axis.X_AXIS;
            direction = dy > 0 ? Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
            face = touch.face == RubiksCube.FACE_RIGHT ? cubeSize - 1 : touch.col;
            rot = new Rotation(axis, direction, face);
        } else if (slope > SLOPE_CUTOFF_MIN_Y && slope < SLOPE_CUTOFF_MAX_Y) {
            axis = Axis.Y_AXIS;
            direction = dx > 0 ? Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
            face = touch.face == RubiksCube.FACE_TOP ? cubeSize - 1 : touch.row;
            rot = new Rotation(axis, direction, face);
        } else if (slope > SLOPE_CUTOFF_MIN_Z && slope < SLOPE_CUTOFF_MAX_Z) {
            axis = Axis.Z_AXIS;
            direction = dy > 0 ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
            if (touch.face == RubiksCube.FACE_FRONT) {
                face = cubeSize - 1;
            } else if (touch.face == RubiksCube.FACE_TOP) {
                face = touch.row;
            } else {
                face = touch.col;
            }
            rot = new Rotation(axis, direction, face);
        }

        if (rot != null) {
            if (mRotateWholeCube) {
                rot.setStartFace(0);
                rot.setFaceCount(cubeSize);
            }
            mCube.rotate(rot);
        }
    }

    public void printDebugInfo() {
        Log.w(tag, "FPS " + mRenderer.getFps());
    }

    public void setCube(RubiksCube cube) {
        mCube = cube;
        mRenderer.setCube(cube);
    }

    public void setWholeCubeRotation(boolean value) {
        mRotateWholeCube = value;
    }

    private static class TouchInfo {
        int face;
        int row;
        int col;
        Point3D point;
        boolean status;
        TouchInfo() {
            status = false;
        }

        void update(int face, int row, int col) {
            Log.d(tag, String.format("touched %s face at %d, %d",
                    RubiksCube.faceName(face), row, col));
            if (!(col >= 0 && row >= 0)) throw new AssertionError(row + ", " + col);
            status = true;
            this.face = face;
            this.row = row;
            this.col = col;
        }

        void setPoint(float x, float y, float z) {
            point = new Point3D(x, y, z);
        }
    }
}
