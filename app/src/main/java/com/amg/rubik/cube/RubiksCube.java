package com.amg.rubik.cube;

import java.security.InvalidParameterException;
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

public class RubiksCube extends AbstractCube {

    protected static final String tag = "rubik-cube";

    // Default value for incrementing angle during rotation
    protected static final float ANGLE_DELTA_NORMAL = 4f;
    protected static final float ANGLE_DELTA_FAST = 10f;

    private static final int MAX_UNDO_COUNT = 20;

    public enum CubeState {
        IDLE,
        RANDOMIZE,
        SOLVING,
        TESTING
    }

    protected CubeListener mListener = null;
    protected CubeState mState = CubeState.IDLE;
    protected Rotation mRotation;

    enum RotateMode {
        NONE,
        MANUAL,
        RANDOM,
        ALGORITHM,
        REPEAT
    }

    private RotateMode rotateMode = RotateMode.NONE;

    private Algorithm mCurrentAlgo;
    protected int mMoveCount;
    private ArrayList<Rotation> mUndoStack;

    protected CubeRenderer mRenderer;

    public RubiksCube(int size) {
        super(size);
        mCurrentAlgo = null;
        mRotation = new Rotation();
        mUndoStack = new ArrayList<>();
        mMoveCount = 0;
    }

    public void setRenderer(CubeRenderer renderer) {
        mRenderer = renderer;
    }

    public void restoreColors(String colors) {
        int expectedLength = FACE_COUNT * mSize * mSize;
        if (colors.length() != expectedLength) {
            throw new InvalidParameterException(
                    String.format("Squares: Expected %d for size %d, got %d",
                            expectedLength, mSize, colors.length()));
        }
    }

    // TODO: Implement these two functions.
    public String getColorString() {
        return  null;
    }

    public CubeState getState() {
        return mState;
    }

    public void randomize() {
        if (mState != CubeState.IDLE) {
            Log.e(tag, "invalid state for randomize " + mState);
            return;
        }
        clearUndoStack();
        rotateMode = RotateMode.RANDOM;
        mState = CubeState.RANDOMIZE;
        mRotation.setAngleDelta(ANGLE_DELTA_FAST);
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
        mRotation.setAngleDelta(ANGLE_DELTA_NORMAL);
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
        if (mSize == 1) {
            sendMessage(":-)");
        } else {
            sendMessage("Robots can solve only 3x3 cubes right now");
        }
        return -1;
    }

    public void setListener(CubeListener listener) {
        mListener = listener;
    }

    private void rotateColors(ArrayList<ArrayList<Square>> squareList, Direction dir) {
        ArrayList<ArrayList<Square>> workingCopy;
        ArrayList<Integer> tempColors = new ArrayList<>(mSize);
        ArrayList<Square> dst;
        ArrayList<Square> src;

        if (dir == Direction.COUNTER_CLOCKWISE) {
            // input is in clockwise order
            workingCopy = squareList;
        } else {
            // reverse and rotate
            workingCopy = new ArrayList<>(mSize);
            for (int i = 0; i < CUBE_SIDES; i++) {
                workingCopy.add(squareList.get(CUBE_SIDES - 1 - i));
            }
        }

        src = workingCopy.get(0);
        for (int i = 0; i < mSize; i++) {
            tempColors.add(src.get(i).mColor);
        }

        for (int i = 0; i < CUBE_SIDES - 1; i++) {
            dst = workingCopy.get(i);
            src = workingCopy.get(i + 1);
            for (int j = 0; j < mSize; j++) {
                dst.get(j).mColor = src.get(j).mColor;
            }
        }

        dst = workingCopy.get(CUBE_SIDES-1);
        for (int i = 0; i < mSize; i++) {
            dst.get(i).mColor = tempColors.get(i);
        }
    }

    /**
     * So far we changed only the orientation of the pieces. This function updates
     * the colors of squares according to the Rotation in progress.
     * TODO: Add proper comments.
     * This function handles rotating the colors and deciding the next move. The logic should be
     * separated. Pure rotation can be moved to AbstractCube.
     * */
    protected void finishRotation() {
        for (int face = mRotation.startFace;
                 face < mRotation.startFace + mRotation.faceCount;
                 face++) {

            ArrayList<Square> faceSquares = null;
            ArrayList<ArrayList<Square>> squareList = new ArrayList<>(CUBE_SIDES);
            for (int i = 0; i < CUBE_SIDES; i++) {
                squareList.add(new ArrayList<Square>(mSize));
            }
            switch (mRotation.axis) {
                case X_AXIS:
                    for (int i = 0; i < mSize; i++) {
                        squareList.get(0).add(mFrontSquares.get(mSize * i + face));
                        squareList.get(1).add(mTopSquares.get(mSize * i + face));
                        squareList.get(2).add(mBackSquares.get((mSize - 1 - i) * mSize +
                                (mSize - 1 - face)));
                        squareList.get(3).add(mBottomSquares.get(mSize * i + face));
                    }
                    if (face == 0) {
                        faceSquares = mLeftSquares;
                    } else if (face == mSize - 1) {
                        faceSquares = mRightSquares;
                    }
                    break;

                case Y_AXIS:
                    for (int i = 0; i < mSize; i++) {
                        squareList.get(0).add(
                                mFrontSquares.get((mSize - 1 - face) * mSize + i));
                        squareList.get(1).add(
                                mLeftSquares.get((mSize - 1 - face) * mSize + i));
                        squareList.get(2).add(
                                mBackSquares.get((mSize - 1 - face) * mSize + i));
                        squareList.get(3).add(
                                mRightSquares.get((mSize - 1 - face) * mSize + i));
                    }
                    if (face == 0) {
                        faceSquares = mBottomSquares;
                    } else if (face == mSize - 1) {
                        faceSquares = mTopSquares;
                    }
                    break;

                case Z_AXIS:
                    for (int i = 0; i < mSize; i++) {
                        squareList.get(0).add(mTopSquares.get(mSize * face + i));
                        squareList.get(1).add(
                                mRightSquares.get(mSize * i + mSize - 1 - face));
                        squareList.get(2).add(mBottomSquares.get(
                                mSize * (mSize - 1 - face) + mSize - 1 - i));
                        squareList.get(3).add(
                                mLeftSquares.get(mSize * (mSize - 1 - i) + face));
                    }
                    if (face == 0) {
                        faceSquares = mBackSquares;
                    } else if (face == mSize - 1) {
                        faceSquares = mFrontSquares;
                    }
                    break;
            }
            rotateColors(squareList, mRotation.direction);

            if (face == mSize - 1) {
                // Rotate a face that is on the positive edge of the
                // corresponding axis (front, top or right).
                // As squares are stored in clockwise order, rotation is straightforward.
                rotateFaceColors(faceSquares, mRotation.direction, mSize);
            } else if (face == 0) {
                rotateFaceColors(faceSquares,
                        mRotation.direction == Direction.CLOCKWISE ?
                                Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE, mSize);
            }
        }

        updateSquareFaces();

        /**
         * Exclude whole cube rotations from the count
         * */
        if (mRotation.faceCount != mSize) mMoveCount++;

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
    }

    void updateAlgo() {
        rotateMode = RotateMode.NONE;
        mRotation.reset();
        mCurrentAlgo = null;
        if (mState == CubeState.TESTING) {
            mState = CubeState.IDLE;
        }
    }

    void rotateFaceColors(ArrayList<Square> squares, Direction direction, int size) {
        ArrayList<Integer> tempColors = new ArrayList<>(size);
        if (direction == Direction.COUNTER_CLOCKWISE) {
            for (int i = 0; i < size - 1; i++) {
                tempColors.add(squares.get(i).mColor);
                squares.get(i).mColor = squares.get(i * size + size - 1).mColor;
            }

            for (int i = 0; i < size - 1; i++) {
                squares.get(i * size + size - 1).mColor =
                        squares.get(size * size - 1 - i).mColor;
            }

            for (int i = 0; i < size - 1; i++) {
                squares.get(size * size - 1 - i).mColor =
                        squares.get(size * (size - 1 - i)).mColor;
            }

            for (int i = 0; i < size - 1; i++) {
                squares.get(size * (size - 1 - i)).mColor =
                        tempColors.get(i);
            }
        } else {
            for (int i = 0; i < size - 1; i++) {
                tempColors.add(squares.get(i).mColor);
                squares.get(i).mColor = squares.get(size * (size - 1 - i)).mColor;
            }
            for (int i = 0; i < size - 1; i++) {
                squares.get(size * (size - 1 - i)).mColor =
                        squares.get(size * size - 1 - i).mColor;
            }
            for (int i = 0; i < size - 1; i++) {
                squares.get(size * size - 1 - i).mColor =
                        squares.get(i * size + size - 1).mColor;
            }
            for (int i = 0; i < size - 1; i++) {
                squares.get(i * size + size - 1).mColor =
                        tempColors.get(i);
            }
        }

        if (size > 3) {
            ArrayList<Square> subset = new ArrayList<>(size - 2);
            for (int i = 1; i < size - 1; i++) {
                for (int j = 1; j < size - 1; j++) {
                    subset.add(squares.get(i * size + j));
                }
            }
            rotateFaceColors(subset, direction, size - 2);
        }
    }

    void repeatRotation() {
        mRotation.angle = 0;
        mRotation.start();
    }

    void rotateRandom() {
        mRotation.reset();
        Random random = new Random();
        Axis[] axes = new Axis[] {Axis.X_AXIS,
                Axis.Y_AXIS, Axis.Z_AXIS};
        mRotation.setAxis(axes[Math.abs(random.nextInt(3))]);
        mRotation.direction = random.nextBoolean() ?
                Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;

        // Do not rotate the center piece in case of odd cubes
        if (mSize != 1 && mSize % 2 == 1) {
            int startFace = Math.abs(random.nextInt(mSize - 1));
            if (startFace >= mSize / 2) {
                startFace++;
            }
            mRotation.setStartFace(startFace);
        } else {
            mRotation.setStartFace(Math.abs(random.nextInt(mSize)));
        }
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

        ArrayList<ArrayList<Piece>> faceList = null;

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
            for (Piece piece: pieces) {
                for (Square square : piece.mSquares) {
                    mRenderer.drawSquare(square);
                }
            }
        }

        mRenderer.setRotation(angle, angleX, angleY, angleZ);
        for (int i = 0; i < mRotation.faceCount; i++) {
            ArrayList<Piece> pieces = faceList.get(mRotation.startFace + i);
            for (Piece piece: pieces) {
                for (Square square: piece.mSquares) {
                    mRenderer.drawSquare(square);
                }
            }
        }

        mRenderer.setRotation(0, 0, 0, 0);
        for (int i = mRotation.startFace + mRotation.faceCount; i < mSize; i++) {
            ArrayList<Piece> pieces = faceList.get(i);
            for (Piece piece: pieces) {
                for (Square square: piece.mSquares) {
                    mRenderer.drawSquare(square);
                }
            }
        }

        if (Math.abs(mRotation.angle) > 89.9f) {
            finishRotation();
        } else {
            mRotation.increment();
        }
    }

    protected boolean checkFace(ArrayList<Square> squares) {
        int centerColor = squares.get(squares.size()/2).mColor;
        for (int i = 0; i < squares.size(); i++) {
            if (squares.get(i).mColor != centerColor)
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
        if (rotation.startFace + rotation.faceCount > mSize) {
            throw new InvalidParameterException(
                    String.format("size %d, rotation %s", mSize, rotation.toString()));
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

}
