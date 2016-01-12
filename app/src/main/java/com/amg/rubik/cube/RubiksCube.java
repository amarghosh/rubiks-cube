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
 *  TODO: Some state variables are accessed from the renderer thread and UI thread.
 *  This might corrupt the cube due to race condition.
 *  The solve, cancel, randomize etc need to be synchronized with the draw function.
 * */

public class RubiksCube {

    protected static final String tag = "rubik-cube";

    // Default value for incrementing angle during rotation
    protected static final float ANGLE_DELTA_NORMAL = 4f;
    protected static final float ANGLE_DELTA_FAST = 10f;

    // Do not change these values. They are used during solving.
    public static final int FACE_FRONT = 0;
    public static final int FACE_RIGHT = 1;
    public static final int FACE_BACK = 2;
    public static final int FACE_LEFT = 3;
    public static final int FACE_TOP = 4;
    public static final int FACE_BOTTOM = 5;
    private static String[] faceNames = {
            "front", "right", "back", "left", "top", "bottom"
    };

    // We don't support skewed cubes yet.
    protected static final int CUBE_SIDES = 4;
    protected static final int FACE_COUNT = 6;

    /**
     * To calculate the square size:
     * Screen spans from -1f to +1f.
     * OpenGl won't draw things close to the frustrum border, hence we add padding and use
     * 1.2f instead of 2.0f as the total size
     * */
    private static final float TOTAL_SIZE = 2.0f;
    private static final float PADDING = 0.8f;

    private static final float GAP = 0.01f;

    private static final int MAX_UNDO_COUNT = 20;

    private int mSize;

    private float squareSize;

    protected ArrayList<Square> mAllSquares;
    protected ArrayList<Square> mFrontSquares;
    protected ArrayList<Square> mBackSquares;
    protected ArrayList<Square> mTopSquares;
    protected ArrayList<Square> mBottomSquares;
    protected ArrayList<Square> mLeftSquares;
    protected ArrayList<Square> mRightSquares;
    protected ArrayList<Square>[] mAllFaces;

    protected ArrayList<ArrayList<Piece>> mXaxisFaceList;
    protected ArrayList<ArrayList<Piece>> mYaxisFaceList;
    protected ArrayList<ArrayList<Piece>> mZaxisFaceList;

    private CubeRenderer mRenderer;

    public void setRenderer(CubeRenderer renderer) {
        mRenderer = renderer;
    }

    public int cancelSolving() {
        if (mState == CubeState.SOLVING) {
            rotateMode = RotateMode.MANUAL;
            mCurrentAlgo = null;
            // State will be set to idle in finishRotation called in the next frame
        }
        return 0;
    }

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
    private int mAlgoIndex = 0;

    private ArrayList<Rotation> mUndoStack;

    public RubiksCube(int size) {
        if (size <= 0) throw new AssertionError();
        mSize = size;
        squareSize = (TOTAL_SIZE - PADDING - GAP * (mSize + 1)) / mSize;
        cube();
        mCurrentAlgo = null;
        mRotation = new Rotation();
        mUndoStack = new ArrayList<>();
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

    private void cube()
    {
        mAllSquares = new ArrayList<>();
        mFrontSquares = new ArrayList<>();
        mBackSquares = new ArrayList<>();
        mTopSquares = new ArrayList<>();
        mBottomSquares = new ArrayList<>();
        mLeftSquares = new ArrayList<>();
        mRightSquares = new ArrayList<>();
        mAllFaces = new ArrayList[FACE_COUNT];
        mAllFaces[FACE_FRONT] = mFrontSquares;
        mAllFaces[FACE_RIGHT] = mRightSquares;
        mAllFaces[FACE_BACK] = mBackSquares;
        mAllFaces[FACE_LEFT] = mLeftSquares;
        mAllFaces[FACE_TOP] = mTopSquares;
        mAllFaces[FACE_BOTTOM] = mBottomSquares;
        createAllSquares();

        mXaxisFaceList = new ArrayList<>(mSize);
        mYaxisFaceList = new ArrayList<>(mSize);
        mZaxisFaceList = new ArrayList<>(mSize);
        createFaces();
        updateSquareFaces();
    }

    private void updateSquareFaces() {
        for (int i = 0; i < FACE_COUNT; i++) {
            for (Square sq: mAllFaces[i]) {
                sq.setFace(i);
            }
        }
    }

    private static Piece.PieceType getPieceType(int row, int col, int totalRows, int totalCols) {
        if (row == 0 || row == totalRows - 1) {
            return (col == 0 || col == totalCols - 1) ?
                    Piece.PieceType.CORNER : Piece.PieceType.EDGE;
        } else if (col == 0 || col == totalCols - 1) {
            return Piece.PieceType.EDGE;
        } else {
            return Piece.PieceType.CENTER;
        }
    }

    private void createFaces() {
        ArrayList<Piece> frontFace = new ArrayList<>();
        ArrayList<Piece> rightFace = new ArrayList<>();
        ArrayList<Piece> leftFace = new ArrayList<>();
        ArrayList<Piece> topFace = new ArrayList<>();
        ArrayList<Piece> bottomFace = new ArrayList<>();
        ArrayList<Piece> backFace = new ArrayList<>();

        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                Piece.PieceType type = getPieceType(i, j, mSize, mSize);
                Piece piece = new Piece(type);
                piece.addSquare(mFrontSquares.get(i * mSize + j));
                if (i == 0) {
                    piece.addSquare(mTopSquares.get(mSize * (mSize - 1) + j));
                }
                if (i == mSize - 1) {
                    piece.addSquare(mBottomSquares.get(j));
                }
                if (j == 0) {
                    piece.addSquare(mLeftSquares.get(mSize * (i + 1) - 1));
                }
                if (j == mSize - 1) {
                    piece.addSquare(mRightSquares.get(mSize * i));
                }
                frontFace.add(piece);
            }
        }

        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                if (j == 0) {
                    rightFace.add(frontFace.get((i + 1) * mSize - 1));
                    continue;
                }
                Piece.PieceType type = getPieceType(i, j, mSize, mSize);
                Piece piece = new Piece(type);
                piece.addSquare(mRightSquares.get(i * mSize + j));
                if (i == 0) {
                    piece.addSquare(mTopSquares.get((mSize - j) * mSize - 1));
                }
                if (i == mSize - 1) {
                    piece.addSquare(mBottomSquares.get((j + 1) * mSize - 1));
                }
                if (j == mSize - 1) {
                    piece.addSquare(mBackSquares.get(i * mSize));
                }
                rightFace.add(piece);
            }
        }

        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                if (j == mSize - 1) {
                    leftFace.add(frontFace.get(i * mSize));
                    continue;
                }
                Piece.PieceType type = getPieceType(i, j, mSize, mSize);
                Piece piece = new Piece(type);
                piece.addSquare(mLeftSquares.get(i * mSize + j));
                if (i == 0) {
                    piece.addSquare(mTopSquares.get(j * mSize));
                }
                if (i == mSize - 1) {
                    piece.addSquare(mBottomSquares.get(mSize * (mSize - j - 1)));
                }
                if (j == 0) {
                    piece.addSquare(mBackSquares.get(mSize * (i + 1) - 1));
                }
                leftFace.add(piece);
            }
        }

        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                if (j == 0) {
                    topFace.add(leftFace.get(i));
                    continue;
                }
                if (j == mSize - 1) {
                    topFace.add(rightFace.get(mSize - 1 - i));
                    continue;
                }
                if (i == mSize - 1) {
                    topFace.add(frontFace.get(j));
                    continue;
                }
                Piece.PieceType type = getPieceType(i, j, mSize, mSize);
                Piece piece = new Piece(type);
                piece.addSquare(mTopSquares.get(i * mSize + j));
                if (i == 0) {
                    piece.addSquare(mBackSquares.get(mSize - 1 - j));
                }
                topFace.add(piece);
            }
        }

        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                if (i == 0) {
                    bottomFace.add(frontFace.get(mSize * (mSize - 1) + j));
                    continue;
                }
                if (j == 0) {
                    bottomFace.add(leftFace.get(mSize * mSize - 1 - i));
                    continue;
                }
                if (j == mSize - 1) {
                    bottomFace.add(rightFace.get(mSize * (mSize - 1) + i));
                    continue;
                }
                Piece.PieceType type = getPieceType(i, j, mSize, mSize);
                Piece piece = new Piece(type);
                piece.addSquare(mBottomSquares.get(i * mSize + j));
                if (i == mSize - 1) {
                    piece.addSquare(
                            mBackSquares.get(mSize * (mSize - 1) + mSize - 1 - j));
                }
                bottomFace.add(piece);
            }
        }

        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                if (i == 0) {
                    backFace.add(topFace.get(mSize - 1 - j));
                    continue;
                }
                if (i == mSize - 1) {
                    backFace.add(bottomFace.get(mSize * (mSize - 1) + mSize - 1 - j));
                    continue;
                }
                if (j == 0) {
                    backFace.add(rightFace.get(mSize * (i + 1) - 1));
                    continue;
                }
                if (j == mSize - 1) {
                    backFace.add(leftFace.get(i * mSize));
                    continue;
                }
                Piece.PieceType type = getPieceType(i, j, mSize, mSize);
                Piece piece = new Piece(type);
                piece.addSquare(mBackSquares.get(i * mSize + j));
                backFace.add(piece);
            }
        }

        mXaxisFaceList.add(leftFace);
        for (int i = 1; i < mSize - 1; i++) {
            ArrayList<Piece> pieces = new ArrayList<>();
            for (int j = 0; j < mSize - 1; j++) {
                pieces.add(topFace.get(j * mSize + i));
            }
            for (int j = 0; j < mSize - 1; j++) {
                pieces.add(frontFace.get(j * mSize + i));
            }
            for (int j = 0; j < mSize - 1; j++) {
                pieces.add(bottomFace.get(j * mSize + i));
            }
            for (int j = 0; j < mSize - 1; j++) {
                pieces.add(backFace.get(mSize * (mSize - 1 - j) + mSize - 1 - i));
            }
            mXaxisFaceList.add(pieces);
        }
        mXaxisFaceList.add(rightFace);

        mYaxisFaceList.add(bottomFace);
        for (int i = 1; i < mSize - 1; i++) {
            ArrayList<Piece> pieces = new ArrayList<>();
            for (int j = 0; j < mSize - 1; j++) {
                pieces.add(frontFace.get((mSize - 1 - i) * mSize + j));
            }
            for (int j = 0; j < mSize - 1; j++) {
                pieces.add(rightFace.get((mSize - 1 - i) * mSize + j));
            }
            for (int j = 0; j < mSize - 1; j++) {
                pieces.add(backFace.get((mSize - 1 - i) * mSize + j));
            }
            for (int j = 0; j < mSize - 1; j++) {
                pieces.add(leftFace.get((mSize - 1 - i) * mSize + j));
            }
            mYaxisFaceList.add(pieces);
        }
        mYaxisFaceList.add(topFace);

        mZaxisFaceList.add(backFace);
        for (int i = 1; i < mSize - 1; i++) {
            ArrayList<Piece> pieces = new ArrayList<>();
            for (int j = 0; j < mSize - 1; j++) {
                pieces.add(topFace.get(i * mSize + j));
            }
            for (int j = 0; j < mSize - 1; j++) {
                pieces.add(rightFace.get(mSize * j + mSize - 1 - i));
            }
            for (int j = 0; j < mSize - 1; j++) {
                pieces.add(bottomFace.get((mSize - 1 - i) * mSize + mSize - 1 - j));
            }
            for (int j = 0; j < mSize - 1; j++) {
                pieces.add(leftFace.get((mSize - 1 - j) * mSize + i));
            }
            mZaxisFaceList.add(pieces);
        }
        mZaxisFaceList.add(frontFace);
    }

    private void createAllSquares()
    {
        createFrontSquares(Square.BLUE);
        createBackSquares(Square.GREEN);
        createLeftSquares(Square.RED);
        createRightSquares(Square.ORANGE);
        createTopSquares(Square.WHITE);
        createBottomSquares(Square.YELLOW);
    }

    /**
     * From top-far to bottom-near. YZ
     * Y moves down after filling each row on Z axis
     *
     * On negative X plane
     * */
    private void createLeftSquares(int color)
    {
        float startX = getLeftFaceX();
        float startY = (squareSize + GAP) * (mSize / 2.0f);
        float startZ = 0 - (squareSize + GAP) * (mSize / 2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY - squareSize, startZ,
                  startX,  startY - squareSize, startZ + squareSize,
                  startX,  startY, startZ + squareSize
            };

        for (int i = 0; i < mSize; i++) {
            vertices[1] = startY - i * (squareSize + GAP);
            vertices[4] = vertices[1] - squareSize;
            vertices[7] = vertices[1] - squareSize;
            vertices[10] = vertices[1];

            for (int j = 0; j < mSize; j++) {
                vertices[2] = startZ + j * (squareSize + GAP);
                vertices[5] = vertices[2];
                vertices[8] = vertices[2] + squareSize;
                vertices[11] = vertices[2] + squareSize;
                Square sq = new Square(vertices, color);
                mAllSquares.add(sq);
                mLeftSquares.add(sq);
            }
        }
    }

    /**
     * From top-near to bottom-far. YZ
     * Y moves down after filling each row on Z axis
     *
     * On positive X plane
     * */
    private void createRightSquares(int color)
    {
        float startX = getRightFaceX();
        float startY = (squareSize + GAP) * (mSize / 2.0f);
        float startZ = (squareSize + GAP) * (mSize / 2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY - squareSize, startZ,
                  startX,  startY - squareSize, startZ - squareSize,
                  startX,  startY, startZ - squareSize
            };

        for (int i = 0; i < mSize; i++) {
            vertices[1] = startY - i * (squareSize + GAP);
            vertices[4] = vertices[1] - squareSize;
            vertices[7] = vertices[1] - squareSize;
            vertices[10] = vertices[1];

            for (int j = 0; j < mSize; j++) {
                vertices[2] = startZ - j * (squareSize + GAP);
                vertices[5] = vertices[2];
                vertices[8] = vertices[2] - squareSize;
                vertices[11] = vertices[2] - squareSize;
                Square sq = new Square(vertices, color);
                mAllSquares.add(sq);
                mRightSquares.add(sq);
            }
        }
    }

    /**
     * From far-left to near-right. ZX
     * Z moves closer after filling each row on X axis
     *
     * On positive Y plane
     * */
    private void createTopSquares(int color)
    {
        float startX = - (squareSize + GAP) * (mSize / 2.0f);
        float startY = getTopFaceY();
        float startZ = - (squareSize + GAP) * (mSize / 2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY, startZ + squareSize,
                  startX + squareSize,  startY, startZ + squareSize,
                  startX + squareSize,  startY, startZ
            };

        for (int i = 0; i < mSize; i++) {
            vertices[2] = startZ + i * (squareSize + GAP);
            vertices[5] = vertices[2] + squareSize;
            vertices[8] = vertices[2] + squareSize;
            vertices[11] = vertices[2];

            for (int j = 0; j < mSize; j++) {
                vertices[0] = startX + j * (squareSize + GAP);
                vertices[3] = vertices[0];
                vertices[6] = vertices[0] + squareSize;
                vertices[9] = vertices[0] + squareSize;
                Square sq = new Square(vertices, color);
                mAllSquares.add(sq);
                mTopSquares.add(sq);
            }
        }
    }

    /**
     * From near-left to far-right. ZX
     * Z moves further after filling each row on X axis
     *
     * On negative Y plane
     * */
    private void createBottomSquares(int color)
    {
        float startX = - (squareSize + GAP) * (mSize / 2.0f);
        float startY = getBottomFaceY();
        float startZ = (squareSize + GAP) * (mSize / 2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY, startZ - squareSize,
                  startX + squareSize,  startY, startZ - squareSize,
                  startX + squareSize,  startY, startZ
            };

        for (int i = 0; i < mSize; i++) {
            vertices[2] = startZ - i * (squareSize + GAP);
            vertices[5] = vertices[2] - squareSize;
            vertices[8] = vertices[2] - squareSize;
            vertices[11] = vertices[2];

            for (int j = 0; j < mSize; j++) {
                vertices[0] = startX + j * (squareSize + GAP);
                vertices[3] = vertices[0];
                vertices[6] = vertices[0] + squareSize;
                vertices[9] = vertices[0] + squareSize;
                Square sq = new Square(vertices, color);
                mAllSquares.add(sq);
                mBottomSquares.add(sq);
            }
        }
    }

    /**
     * From top-left to bottom-right.
     * Y moves down after filling each row on X axis
     *
     * On positive z (near plane).
     * */
    private void createFrontSquares(int color) {
        float startX = 0 - (squareSize + GAP) * (mSize / 2.0f);
        float startY = (squareSize + GAP) * (mSize / 2.0f);
        float startZ = getFrontFaceZ();

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY - squareSize, startZ,
                  startX + squareSize,  startY - squareSize, startZ,
                  startX + squareSize,  startY, startZ
            };

        for (int i = 0; i < mSize; i++) {
            vertices[1] = startY - i * (squareSize + GAP);
            vertices[4] = vertices[1] - squareSize;
            vertices[7] = vertices[1] - squareSize;
            vertices[10] = vertices[1];

            for (int j = 0; j < mSize; j++) {
                vertices[0] = startX + j * (squareSize + GAP);
                vertices[3] = vertices[0];
                vertices[6] = vertices[0] + squareSize;
                vertices[9] = vertices[0] + squareSize;
                Square sq = new Square(vertices, color);
                mAllSquares.add(sq);
                mFrontSquares.add(sq);
            }
        }
    }

    /**
     * From top-right to bottom-left.
     * Y moves down after filling each row on X axis
     *
     * On negative z (far plane).
     * */
    private void createBackSquares(int color) {
        float startX = (squareSize + GAP) * (mSize / 2.0f);
        float startY = (squareSize + GAP) * (mSize / 2.0f);
        float startZ = getBackFaceZ();

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY - squareSize, startZ,
                  startX - squareSize,  startY - squareSize, startZ,
                  startX - squareSize,  startY, startZ
            };

        for (int i = 0; i < mSize; i++) {
            vertices[1] = startY - i * (squareSize + GAP);
            vertices[4] = vertices[1] - squareSize;
            vertices[7] = vertices[1] - squareSize;
            vertices[10] = vertices[1];

            for (int j = 0; j < mSize; j++) {
                vertices[0] = startX - j * (squareSize + GAP);
                vertices[3] = vertices[0];
                vertices[6] = vertices[0] - squareSize;
                vertices[9] = vertices[0] - squareSize;
                Square sq = new Square(vertices, color);
                mAllSquares.add(sq);
                mBackSquares.add(sq);
            }
        }
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

    public float getFrontFaceZ() {
        return (squareSize + GAP) * (mSize / 2.0f);
    }

    public float getBackFaceZ() {
        return - (squareSize + GAP) * (mSize / 2.0f);
    }

    public float getLeftFaceX() {
        return  - (squareSize + GAP) * (mSize / 2.0f);
    }

    public float getRightFaceX() {
        return (squareSize + GAP) * (mSize / 2.0f);
    }

    public float getTopFaceY() {
        return (squareSize + GAP) * (mSize / 2.0f);
    }

    public float getBottomFaceY() {
        return - (squareSize + GAP) * (mSize / 2.0f);
    }

    public int size() {
        return mSize;
    }

    public static Axis face2axis(int face) {
        switch (face) {
            case FACE_BACK:
            case FACE_FRONT:
                return Axis.Z_AXIS;
            case FACE_BOTTOM:
            case FACE_TOP:
                return Axis.Y_AXIS;
            case FACE_LEFT:
            case FACE_RIGHT:
                return Axis.X_AXIS;
            default:
                throw new InvalidParameterException("Whats on face " + face);
        }
    }

    public static String faceName(int face) {
        return faceNames[face];
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
}
