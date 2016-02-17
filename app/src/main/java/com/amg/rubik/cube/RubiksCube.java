package com.amg.rubik.cube;

import java.util.ArrayList;
import java.util.Random;

import android.util.Log;

import com.amg.rubik.graphics.Axis;
import com.amg.rubik.graphics.CubeRenderer;
import com.amg.rubik.graphics.Direction;

/**
 *
 *
      ___ ___ ___
    /___/___/___/|
   /_T_/_o_/_p_/||
  /___/___/__ /|/|
 |   |   |   | /||
 |___|___|___|/|/|
 |   |   |   |R/||
 |_F_|r_n|_t_|/|/
 |   |   |   | /
 |___|___|___|/

     Bottom

 *
 * This class handles the rotation and drawing for all cubes.
 *
 * TODO: Some state variables are accessed from the renderer thread and UI thread.
 * This might corrupt the cube due to race condition.
 * The solve, cancel, randomize etc need to be synchronized with the draw function.
 * */

public class RubiksCube extends Cube {

    private static final String tag = "rubik-cube";

    // Default value for incrementing angle during rotation
    static final float ANGLE_DELTA_SLOW = 2f;
    static final float ANGLE_DELTA_NORMAL = 4f;
    static final float ANGLE_DELTA_FAST = 10f;

    private static final int MAX_UNDO_COUNT = 40;

    public enum CubeState {
        IDLE,
        RANDOMIZE,
        SOLVING,
        TESTING
    }

    protected CubeListener mListener = null;
    protected CubeState mState = CubeState.IDLE;
    private Rotation mRotation;

    enum RotateMode {
        NONE,
        MANUAL,
        RANDOM,
        ALGORITHM,
        REPEAT
    }

    private RotateMode rotateMode = RotateMode.NONE;

    private Algorithm mCurrentAlgo;

    /**
     * The value can be used to grade the solving speed during manual and automated modes.
     * It doesn't mean much during manual solving right now as the user can invoke machine
     * solving and cancel at the last moment to reset the counter. Once we add
     * support for autodetecting solved cube during manual rotation, we should
     * find a way to use this value in a more meaningful way.
     * */
    int mMoveCount;

    /**
     * Stores past @MAX_UNDO_COUNT moves to perform undo
     * */
    private ArrayList<Rotation> mUndoStack;
    private boolean mUndoingFlag = false;

    private CubeRenderer mRenderer;

    private void init() {
        mCurrentAlgo = null;
        mRotation = new Rotation();
        mUndoStack = new ArrayList<>();
        mMoveCount = 0;
    }

    public RubiksCube(int x, int y, int z) {
        super(x, y, z);
        init();
    }

    public RubiksCube(int size) {
        super(size, size, size);
        init();
    }

    public void setRenderer(CubeRenderer renderer) {
        mRenderer = renderer;
    }

    public void restoreColors(String colors) {
        // TODO:
//        int expectedLength = FACE_COUNT * mCubeSize * mCubeSize;
//        if (colors.length() != expectedLength) {
//            throw new InvalidParameterException(
//                    String.format("Squares: Expected %d for size %d, got %d",
//                            expectedLength, mCubeSize, colors.length()));
//        }
    }

    /**
     * TODO: Serialize and deserialize the cube state
     * Implement these two functions
     * */
    public String getColorString() {
        return  null;
    }

    public CubeState getState() {
        return mState;
    }

    /**
     * Rotate randomly for @count moves. This function just updates the state instantaneously
     * without animating the rotations
     *
     * @see public void randomize()
     * */
    public void randomize(int count) {
        Random random = new Random();
        Axis[] axes = new Axis[] {Axis.X_AXIS, Axis.Y_AXIS, Axis.Z_AXIS};

        for (int i = 0; i < count; i++) {
            Axis axis = axes[Math.abs(random.nextInt(3))];
            Direction direction = random.nextBoolean() ?
                    Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
            int size = getAxisSize(axis);
            int startFace = Math.abs(random.nextInt(size));
            rotate(axis, direction, startFace);
        }

        mMoveCount = 0;
        clearUndoStack();
    }

    /**
     * Start scrambling the cube. Random faces will be rotated until stopRandomize is called. This
     * function animates individual rotations.
     *
     * @see public void randomize(int count)
     * */
    public void randomize() {
        if (mState != CubeState.IDLE) {
            Log.e(tag, "invalid state for randomize " + mState);
            return;
        }
        clearUndoStack();
        rotateMode = RotateMode.RANDOM;
        mState = CubeState.RANDOMIZE;
        mRotation.start();
    }

    public void stopRandomize() {
        if (mState != CubeState.RANDOMIZE) {
            Log.e(tag, "No randomize in progress " + mState);
            return;
        }
        rotateMode = RotateMode.NONE;
        finishRotation();
        mRotation.reset();
        mState = CubeState.IDLE;
        mMoveCount = 0;
    }

    protected void sendMessage(String str) {
        try {
            if (mListener != null) {
                mListener.handleCubeMessage(str);
            }
        } catch (Exception e) {
            Log.e(tag, e.toString());
        }
        Log.w(tag, str);
    }

    public int solve() {
        sendMessage("Robots can solve only 3x3 cubes right now");
        return -1;
    }

    public void setListener(CubeListener listener) {
        mListener = listener;
    }

    /**
     * So far we changed only the orientation of the pieces. This function updates
     * the colors of squares according to the Rotation in progress.
     * */
    private void finishRotation() {
        /**
         * If 90' rotation of a single face is not possible along the given axis, and we are
         * rotating all layers along that axis, just reorient the cube.
         * */
        boolean symmetryFlag = isSymmetricAroundAxis(mRotation.axis);
        if (symmetryFlag == false && mRotation.faceCount == getAxisSize(mRotation.axis)) {
            rotate(mRotation.axis, mRotation.direction);
        } else {
            for (int face = mRotation.startFace;
                 face < mRotation.startFace + mRotation.faceCount;
                 face++) {
                rotate(mRotation.axis, mRotation.direction, face);
            }
        }

        /**
         * Exclude whole cube rotations from the count
         * */
        if (mUndoingFlag == false && mRotation.faceCount != getAxisSize(mRotation.axis)) mMoveCount++;

        if (mUndoingFlag) {
            mUndoingFlag = false;
            if (mRotation.faceCount != getAxisSize(mRotation.axis)) mMoveCount--;
        }

        switch (rotateMode) {
            case ALGORITHM:
                if (mCurrentAlgo.isDone()) {
                    mRotation.reset();
                    updateAlgo();
                } else {
                    mRotation = mCurrentAlgo.getNextStep();
                    mRotation.start();
                }
                break;

            case REPEAT:
                repeatRotation();
                break;

            case RANDOM:
                rotateRandom();
                break;

            default:
                mRotation.reset();
                rotateMode = RotateMode.NONE;
                mState = CubeState.IDLE;
                break;
        }

        if (mListener != null) {
            mListener.handleRotationCompleted();
        }

        if (mState == CubeState.IDLE && isSolved() && mListener != null) {
            mListener.handleCubeSolved();
        }
    }

    protected void updateAlgo() {
        rotateMode = RotateMode.NONE;
        mRotation.reset();
        mCurrentAlgo = null;
        if (mState == CubeState.TESTING) {
            mState = CubeState.IDLE;
        }
    }

    private void repeatRotation() {
        mRotation.angle = 0;
        mRotation.start();
    }

    /**
     * @see public void randomize(int count);
     * */
    private void rotateRandom() {
        mRotation.reset();
        Random random = new Random();
        Axis[] axes = new Axis[] {Axis.X_AXIS,
                Axis.Y_AXIS, Axis.Z_AXIS};
        mRotation.setAxis(axes[Math.abs(random.nextInt(3))]);
        mRotation.direction = random.nextBoolean() ?
                Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
        int size = getAxisSize(mRotation.axis);
        mRotation.setStartFace(Math.abs(random.nextInt(size)));
        mRotation.start();
    }

    private void drawCube() {
        mRenderer.setRotation(0, 0, 0, 0);
        for (Square sq: mAllSquares) {
            mRenderer.drawSquare(sq);
        }
    }

    public void draw() {

        if (rotateMode == RotateMode.NONE ||
                mRotation.getStatus() == false) {
            drawCube();
            return;
        }

        ArrayList<ArrayList<Piece>> faceList;
        int axisSize = getAxisSize(mRotation.axis);

        float angle = mRotation.angle;
        float angleX = 0;
        float angleY = 0;
        float angleZ = 0;

        switch (mRotation.axis) {
            case X_AXIS:
                angleX = 1;
                faceList = mXaxisFaceList;
                break;
            case Y_AXIS:
                angleY = 1;
                faceList = mYaxisFaceList;
                break;
            case Z_AXIS:
                angleZ = 1;
                faceList = mZaxisFaceList;
                break;
            default:
                throw new RuntimeException("What is " + mRotation.axis);
        }

        mRenderer.setRotation(0, 0, 0, 0);
        for (int i = 0; i < mRotation.startFace; i++) {
            ArrayList<Piece> pieces = faceList.get(i);
            for (Piece piece : pieces) {
                for (Square square : piece.mSquares) {
                    mRenderer.drawSquare(square);
                }
            }
        }

        mRenderer.setRotation(angle, angleX, angleY, angleZ);
        for (int i = 0; i < mRotation.faceCount; i++) {
            ArrayList<Piece> pieces = faceList.get(mRotation.startFace + i);
            for (Piece piece : pieces) {
                for (Square square : piece.mSquares) {
                    mRenderer.drawSquare(square);
                }
            }
        }

        mRenderer.setRotation(0, 0, 0, 0);
        for (int i = mRotation.startFace + mRotation.faceCount; i < axisSize; i++) {
            ArrayList<Piece> pieces = faceList.get(i);
            for (Piece piece : pieces) {
                for (Square square : piece.mSquares) {
                    mRenderer.drawSquare(square);
                }
            }
        }

        boolean symmetric = isSymmetricAroundAxis(mRotation.axis);
        float max_angle = symmetric ? 90f : 180f;
        if (mRotation.faceCount == axisSize) {
            /**
             * Even if it isn't symmetric, we can do half rotations if
             * we are rotating the whole cube. @finishRotation takes care of this.
             * */
            max_angle = 90f;
        }

        if (Math.abs(mRotation.angle) > max_angle - 0.01f) {
            finishRotation();
        } else {
            mRotation.increment(mAngleDelta, max_angle);
        }
    }

    private boolean checkFace(ArrayList<Square> squares) {
        int centerColor = squares.get(squares.size()/2).getColor();
        for (int i = 0; i < squares.size(); i++) {
            if (squares.get(i).getColor() != centerColor)
                return false;
        }
        return true;
    }

    protected boolean isSolved() {
        return checkFace(mTopSquares) &&
                checkFace(mLeftSquares) &&
                checkFace(mFrontSquares) &&
                checkFace(mRightSquares) &&
                checkFace(mBackSquares) &&
                checkFace(mBottomSquares);
    }

    protected void setAlgo(Algorithm algo) {
        if (mCurrentAlgo != null &&
                mCurrentAlgo.isDone() == false) {
            throw new IllegalStateException("There is already an algorithm running");
        }
        if (mState != CubeState.SOLVING && mState != CubeState.TESTING) {
            throw new IllegalStateException("Invalid state for algos: " + mState);
        }
        mCurrentAlgo = algo;
        mRotation = algo.getNextStep();
        rotateMode = RotateMode.ALGORITHM;
        mRotation.start();
    }

    public void rotate(Rotation rotation) {
        if (mState != CubeState.IDLE) {
            Log.w(tag, "Cannot rotate in state " + mState);
            return;
        }
        if (rotateMode != RotateMode.NONE) {
            Log.w(tag, "Cannot rotate in mode " + rotateMode);
            return;
        }
        int size = getAxisSize(rotation.axis);
        if (rotation.startFace + rotation.faceCount > size) {
            /**
             * TODO: Throw exception instead of correcting the value
             * */
            rotation.faceCount = size - rotation.startFace;
//            throw new InvalidParameterException(
//                   String.format("size %d, rotation %s", size, rotation.toString()));
        }
        if (rotation.startFace >= size) {
            return;
        }
        rotateMode = RotateMode.MANUAL;
        mRotation = rotation.duplicate();
        if (mUndoStack.size() == MAX_UNDO_COUNT) {
            mUndoStack.remove(0);
        }
        mUndoStack.add(rotation.getReverse());
        mRotation.start();
    }

    public void undo() {
        if (mState != CubeState.IDLE) {
            Log.w(tag, "Cannot undo in state " + mState);
            return;
        }
        if (rotateMode != RotateMode.NONE) {
            Log.w(tag, "Cannot undo in mode " + rotateMode);
            return;
        }

        if (mUndoStack.size() == 0) {
            Log.d(tag, "nothing to undo");
            return;
        }
        rotateMode = RotateMode.MANUAL;
        mUndoingFlag = true;
        int index = mUndoStack.size() - 1;
        Rotation rotation = mUndoStack.get(index);
        mUndoStack.remove(index);
        mRotation = rotation;
        mRotation.start();
    }

    protected void clearUndoStack() {
        mUndoStack.clear();
    }

    protected void startSolving() {
        mMoveCount = 0;
    }

    public int cancelSolving() {
        if (mState == CubeState.SOLVING) {
            rotateMode = RotateMode.MANUAL;
            mCurrentAlgo = null;
            // State will be set to idle in finishRotation called in the next frame
        }
        return 0;
    }

    private final int SLOW = 0;
    private final int MEDIUM = 1;
    private final int FAST = 2;

    private int mSpeed = MEDIUM;
    private float mAngleDelta = ANGLE_DELTA_NORMAL;
    public void setSpeed(int speed) {
        mSpeed = speed;
        switch (speed) {
            case FAST:
                mAngleDelta = ANGLE_DELTA_FAST; break;
            case MEDIUM:
                mAngleDelta = ANGLE_DELTA_NORMAL; break;
            case SLOW:
                mAngleDelta = ANGLE_DELTA_SLOW; break;
        }
    }

    /**
     * Sets the color of the whole cube
     * */
    public void setColor(int color) {
        for (Square sq: mAllSquares) {
            sq.setColor(color);
        }
    }

    /**
     * Sets the color of squares on the given face
     * @face One of the FACE_* values
     * */
    public void setColor(int face, int color) {
        if (!(face >= 0 && face < FACE_COUNT)) throw new AssertionError("Face " + face);
        for (Square sq: mAllFaces[face]) {
            sq.setColor(color);
        }
    }

    /**
     * TODO: Fix these functions for skewed cubes
     * */

    /**
     * Sets the color of all pieces on given side (face squares + side squares)
     * */
    public void setColor(Axis axis, int layer, int color) {
        // if (!(layer >= 0 && layer < mSize)) throw new AssertionError();
        ArrayList<Piece> pieces;
        switch (axis) {
            case X_AXIS: pieces = mXaxisFaceList.get(layer); break;
            case Y_AXIS: pieces = mYaxisFaceList.get(layer); break;
            case Z_AXIS: pieces = mZaxisFaceList.get(layer); break;
            default:throw new AssertionError();
        }
        for (Piece p: pieces) {
            for (Square sq: p.mSquares) {
                sq.setColor(color);
            }
        }
    }

    public void setColor(int face, int row, int column, int color) {
//        if (!(face < FACE_COUNT && row < mSize && column < mSize))
//            throw new AssertionError(String.format("%d %d %d", face, row, column));
//        mAllFaces[face].get(row * mSize + column).setColor(color);
    }

    public void setRowColor(int face, int row, int color) {
//        if (!(face < FACE_COUNT && row < mSize))
//            throw new AssertionError(String.format("%d %d", face, row));
//        ArrayList<Square> squares = mAllFaces[face];
//        for (int i = row; i < row + mSize; i++) {
//            squares.get(i).setColor(color);
//        }
    }

    public void setColumnColor(int face, int column, int color) {
//        if (!(face < FACE_COUNT && column < mSize))
//            throw new AssertionError(String.format("%d %d", face, column));
//        ArrayList<Square> squares = mAllFaces[face];
//        for (int i = 0; i < mSize; i++) {
//            squares.get(i * mSize + column).setColor(color);
//        }
    }

    public void reset() {
        if (mState != CubeState.IDLE) {
            sendMessage("cube is in state " + mState);
            return;
        }
        setColor(FACE_FRONT, COLOR_FRONT);
        setColor(FACE_BACK, COLOR_BACK);
        setColor(FACE_BOTTOM, COLOR_BOTTOM);
        setColor(FACE_TOP, COLOR_TOP);
        setColor(FACE_LEFT, COLOR_LEFT);
        setColor(FACE_RIGHT, COLOR_RIGHT);
        clearUndoStack();
        mMoveCount = 0;
    }

    public int getMoveCount() {
        return mMoveCount;
    }
}
