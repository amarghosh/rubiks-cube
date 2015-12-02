package com.amg.rubik;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

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

 * */

public class RubiksCube {

    // We don't support skewed cubes yet.
    private static final int CUBE_SIDES = 4;

    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;

    public enum Direction {
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }

    private static final String tag = "rubik-cube";

    private static final float SQ_SIZE = 0.15f;
    private static final float GAP = 0.005f;
    private static final float ANGLE_DELTA = 1f;

    private int DIMENSION = 3;
    private GLSurfaceView mGlView = null;

    private ArrayList<Square> mAllSquares;
    private ArrayList<Square> mFrontSquares;
    private ArrayList<Square> mBackSquares;
    private ArrayList<Square> mTopSquares;
    private ArrayList<Square> mBottomSquares;
    private ArrayList<Square> mLeftSquares;
    private ArrayList<Square> mRightSquares;

    private ArrayList<ArrayList<Piece>> mXaxisFaceList;
    private ArrayList<ArrayList<Piece>> mYaxisFaceList;
    private ArrayList<ArrayList<Piece>> mZaxisFaceList;

    private Rotation mRotation;
    private int mCurrentIndex = 0;

    private Rotation[] algo = new Rotation[4];

    void populateAlgo() {
        algo[0] = new Rotation(X_AXIS, Direction.COUNTER_CLOCKWISE, 2);
        algo[1] = new Rotation(Y_AXIS, Direction.CLOCKWISE, 0);
        algo[2] = new Rotation(X_AXIS, Direction.CLOCKWISE, 2);
        algo[3] = new Rotation(Y_AXIS, Direction.COUNTER_CLOCKWISE, 0);
    }

    public RubiksCube(Context context, int size) {
        /*
        TODO: remove this hard coding once generic cubes are implemented.
        * */
        DIMENSION = 3; //size
        cube();
        populateAlgo();
        mRotation = algo[0].duplicate();
        mGlView = new RubikGLSurfaceView(context);
        mRotation.status = true;
    }

    public GLSurfaceView getView() {
        return mGlView;
    }

    public void onResume() {
        mGlView.onResume();
    }

    public void onPause() {
        mGlView.onPause();
    }

    void rotateColors(ArrayList<ArrayList<Square>> squareList, Direction dir) {
        ArrayList<ArrayList<Square>> workingCopy;
        ArrayList<Integer> tempColors = new ArrayList<>(DIMENSION);
        ArrayList<Square> dst;
        ArrayList<Square> src;

        if (dir == Direction.COUNTER_CLOCKWISE) {
            // input is in clockwise order
            workingCopy = squareList;
        } else {
            // reverse and rotate
            workingCopy = new ArrayList<>(DIMENSION);
            for (int i = 0; i < CUBE_SIDES; i++) {
                workingCopy.add(squareList.get(CUBE_SIDES - 1 - i));
            }
        }

        src = workingCopy.get(0);
        for (int i = 0; i < DIMENSION; i++) {
            tempColors.add(src.get(i).mColor);
        }

        for (int i = 0; i < CUBE_SIDES - 1; i++) {
            dst = workingCopy.get(i);
            src = workingCopy.get(i + 1);
            for (int j = 0; j < DIMENSION; j++) {
                dst.get(j).mColor = src.get(j).mColor;
            }
        }

        dst = workingCopy.get(CUBE_SIDES-1);
        for (int i = 0; i < DIMENSION; i++) {
            dst.get(i).mColor = tempColors.get(i);
        }
    }

    /**
     * So far we changed only the orientation of the pieces. This function updates
     * the colors of squares according to the Rotation in progress.
     * TODO: Add proper comments.
     * */
    void finishRotation() {
        int face = mRotation.startFace;
        ArrayList<Square> faceSquares = null;
        ArrayList<ArrayList<Square>> squareList = new ArrayList<>(CUBE_SIDES);
        for (int i = 0; i < CUBE_SIDES; i++) {
            squareList.add(new ArrayList<Square>(DIMENSION));
        }

        switch (mRotation.axis) {
            case X_AXIS:
                for (int i = 0; i < DIMENSION; i++) {
                    squareList.get(0).add(mFrontSquares.get(DIMENSION * i + face));
                    squareList.get(1).add(mTopSquares.get(DIMENSION * i + face));
                    squareList.get(2).add(mBackSquares.get((DIMENSION - 1 - i) * DIMENSION + (DIMENSION - 1 - face)));
                    squareList.get(3).add(mBottomSquares.get(DIMENSION * i + face));
                }
                if (face == 0) {
                    faceSquares = mLeftSquares;
                } else if (face == DIMENSION - 1) {
                    faceSquares = mRightSquares;
                }
                break;
            case Y_AXIS:
                for (int i = 0; i < DIMENSION; i++) {
                    squareList.get(0).add(mFrontSquares.get((DIMENSION - 1 - face) * DIMENSION + i));
                    squareList.get(1).add(mLeftSquares.get((DIMENSION - 1 - face) * DIMENSION + i));
                    squareList.get(2).add(mBackSquares.get((DIMENSION - 1 - face) * DIMENSION + i));
                    squareList.get(3).add(mRightSquares.get((DIMENSION - 1 - face) * DIMENSION + i));
                }
                if (face == 0) {
                    faceSquares = mBottomSquares;
                } else if (face == DIMENSION - 1) {
                    faceSquares = mTopSquares;
                }
                break;
            case Z_AXIS:
                for (int i = 0; i < DIMENSION; i++) {
                    squareList.get(0).add(mTopSquares.get(DIMENSION * face + i));
                    squareList.get(1).add(mRightSquares.get(DIMENSION * i + DIMENSION - 1 - face));
                    squareList.get(2).add(mBottomSquares.get(DIMENSION * (DIMENSION - 1 - face) + DIMENSION - 1 - i));
                    squareList.get(3).add(mLeftSquares.get(DIMENSION * (DIMENSION - 1 - i) + face));
                }
                if (face == 0) {
                    faceSquares = mBackSquares;
                } else if (face == DIMENSION - 1) {
                    faceSquares = mFrontSquares;
                }
                break;
        }
        rotateColors(squareList, mRotation.direction);

        if (face == DIMENSION - 1) {
            // Rotate a face that is on the positive edge of the
            // corresponding axis (front, top or right).
            // As squares are stored in clockwise order, rotation is straightforward.
            rotateFaceColors(faceSquares, mRotation.direction);
        } else if (face == 0) {
            rotateFaceColors(faceSquares,
                    mRotation.direction == Direction.CLOCKWISE ?
                            Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE);
        }

        mRotation.reset();
        mCurrentIndex = (mCurrentIndex + 1) % algo.length;
        mRotation = algo[mCurrentIndex].duplicate();
        mRotation.status = true;
    }

    void rotateFaceColors(ArrayList<Square> squares, Direction direction) {
        ArrayList<Integer> tempColors = new ArrayList<>(DIMENSION);
        if (direction == Direction.COUNTER_CLOCKWISE) {
            for (int i = 0; i < DIMENSION - 1; i++) {
                tempColors.add(squares.get(i).mColor);
                squares.get(i).mColor = squares.get(i * DIMENSION + DIMENSION - 1).mColor;
            }

            for (int i = 0; i < DIMENSION - 1; i++) {
                squares.get(i * DIMENSION + DIMENSION - 1).mColor =
                        squares.get(DIMENSION * DIMENSION - 1 - i).mColor;
            }

            for (int i = 0; i < DIMENSION - 1; i++) {
                squares.get(DIMENSION * DIMENSION - 1 - i).mColor =
                        squares.get(DIMENSION * (DIMENSION - 1 - i)).mColor;
            }

            for (int i = 0; i < DIMENSION - 1; i++) {
                squares.get(DIMENSION * (DIMENSION - 1 - i)).mColor =
                        tempColors.get(i);
            }
        } else {
            for (int i = 0; i < DIMENSION - 1; i++) {
                tempColors.add(squares.get(i).mColor);
                squares.get(i).mColor = squares.get(DIMENSION * (DIMENSION - 1 - i)).mColor;
            }
            for (int i = 0; i < DIMENSION - 1; i++) {
                squares.get(DIMENSION * (DIMENSION - 1 - i)).mColor =
                        squares.get(DIMENSION * DIMENSION - 1 - i).mColor;
            }
            for (int i = 0; i < DIMENSION - 1; i++) {
                squares.get(DIMENSION * DIMENSION - 1 - i).mColor =
                        squares.get(i * DIMENSION + DIMENSION - 1).mColor;
            }
            for (int i = 0; i < DIMENSION - 1; i++) {
                squares.get(i * DIMENSION + DIMENSION - 1).mColor =
                        tempColors.get(i);
            }
        }
    }

    void rotateRandom() {
        Random random = new Random();
        mRotation.setAxis(Math.abs(random.nextInt(3)));
        mRotation.direction = random.nextBoolean() ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
        mRotation.setStartFace(Math.abs(random.nextInt(DIMENSION)));
        mRotation.status = true;
        Log.w(tag, "Next Rotation on axis " + mRotation.axis +
                " starting at face " + mRotation.startFace);
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
        createAllSquares();

        mXaxisFaceList = new ArrayList<>(DIMENSION);
        mYaxisFaceList = new ArrayList<>(DIMENSION);
        mZaxisFaceList = new ArrayList<>(DIMENSION);
        createFaces3x3();
    }

    private void createFaces() {
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
     * Create pieces from squares and store them in faces
     * TODO: This should be generic (instead of being 3x3 specific)
     * */
    private void createFaces3x3() {
        ArrayList<Piece> frontFace;
        ArrayList<Piece> backFace;
        ArrayList<Piece> topFace;
        ArrayList<Piece> leftFace;
        ArrayList<Piece> rightFace;
        ArrayList<Piece> bottomFace;
        ArrayList<Piece> centerYZ;
        ArrayList<Piece> centerXY;
        ArrayList<Piece> centerZX;

        frontFace = new ArrayList<>();
        topFace = new ArrayList<>();
        leftFace = new ArrayList<>();
        rightFace = new ArrayList<>();
        bottomFace = new ArrayList<>();
        backFace = new ArrayList<>();
        centerXY = new ArrayList<>();
        centerZX = new ArrayList<>();
        centerYZ = new ArrayList<>();

        Piece frontTop_0 = new Piece(Piece.PieceType.CORNER);
        frontTop_0.addSquare(mFrontSquares.get(0));
        frontTop_0.addSquare(mLeftSquares.get(2));
        frontTop_0.addSquare(mTopSquares.get(6));

        Piece frontTop_1 = new Piece(Piece.PieceType.EDGE);
        frontTop_1.addSquare(mFrontSquares.get(1));
        frontTop_1.addSquare(mTopSquares.get(7));

        Piece frontTop_2 = new Piece(Piece.PieceType.CORNER);
        frontTop_2.addSquare(mFrontSquares.get(2));
        frontTop_2.addSquare(mTopSquares.get(8));
        frontTop_2.addSquare(mRightSquares.get(0));

        Piece frontMid_0 = new Piece(Piece.PieceType.EDGE);
        frontMid_0.addSquare(mFrontSquares.get(3));
        frontMid_0.addSquare(mLeftSquares.get(5));

        Piece frontMid_1 = new Piece(mFrontSquares.get(4));

        Piece frontMid_2 = new Piece(Piece.PieceType.CORNER);
        frontMid_2.addSquare(mFrontSquares.get(5));
        frontMid_2.addSquare(mRightSquares.get(3));

        Piece frontBottom_0 = new Piece(Piece.PieceType.CORNER);
        frontBottom_0.addSquare(mFrontSquares.get(6));
        frontBottom_0.addSquare(mLeftSquares.get(8));
        frontBottom_0.addSquare(mBottomSquares.get(0));

        Piece frontBottom_1 = new Piece(Piece.PieceType.EDGE);
        frontBottom_1.addSquare(mFrontSquares.get(7));
        frontBottom_1.addSquare(mBottomSquares.get(1));

        Piece frontBottom_2 = new Piece(Piece.PieceType.CORNER);
        frontBottom_2.addSquare(mFrontSquares.get(8));
        frontBottom_2.addSquare(mRightSquares.get(6));
        frontBottom_2.addSquare(mBottomSquares.get(2));

        frontFace.add(frontTop_0);
        frontFace.add(frontTop_1);
        frontFace.add(frontTop_2);
        frontFace.add(frontMid_0);
        frontFace.add(frontMid_1);
        frontFace.add(frontMid_2);
        frontFace.add(frontBottom_0);
        frontFace.add(frontBottom_1);
        frontFace.add(frontBottom_2);

        Piece rightTop_0 = frontTop_2;
        Piece rightTop_1 = new Piece(Piece.PieceType.EDGE);
        rightTop_1.addSquare(mTopSquares.get(5));
        rightTop_1.addSquare(mRightSquares.get(1));

        Piece rightTop_2 = new Piece(Piece.PieceType.CORNER);
        rightTop_2.addSquare(mTopSquares.get(2));
        rightTop_2.addSquare(mRightSquares.get(2));
        rightTop_2.addSquare(mBackSquares.get(0));

        Piece rightMid_0 = frontMid_2;
        Piece rightMid_1 = new Piece(mRightSquares.get(4));
        Piece rightMid_2 = new Piece(Piece.PieceType.EDGE);
        rightMid_2.addSquare(mRightSquares.get(5));
        rightMid_2.addSquare(mBackSquares.get(3));

        Piece rightBottom_0 = frontBottom_2;
        Piece rightBottom_1 = new Piece(Piece.PieceType.EDGE);
        rightBottom_1.addSquare(mRightSquares.get(7));
        rightBottom_1.addSquare(mBottomSquares.get(5));

        Piece rightBottom_2 = new Piece(Piece.PieceType.CORNER);
        rightBottom_2.addSquare(mRightSquares.get(8));
        rightBottom_2.addSquare(mBottomSquares.get(8));
        rightBottom_2.addSquare(mBackSquares.get(6));

        rightFace.add(rightTop_0);
        rightFace.add(rightTop_1);
        rightFace.add(rightTop_2);
        rightFace.add(rightMid_0);
        rightFace.add(rightMid_1);
        rightFace.add(rightMid_2);
        rightFace.add(rightBottom_0);
        rightFace.add(rightBottom_1);
        rightFace.add(rightBottom_2);

        Piece leftTop_0 = new Piece(Piece.PieceType.CORNER);
        leftTop_0.addSquare(mLeftSquares.get(0));
        leftTop_0.addSquare(mTopSquares.get(0));
        leftTop_0.addSquare(mBackSquares.get(2));

        Piece leftTop_1 = new Piece(Piece.PieceType.EDGE);
        leftTop_1.addSquare(mTopSquares.get(3));
        leftTop_1.addSquare(mLeftSquares.get(1));

        Piece leftTop_2 = frontTop_0;

        Piece leftMid_0 = new Piece(Piece.PieceType.EDGE);
        leftMid_0.addSquare(mLeftSquares.get(3));
        leftMid_0.addSquare(mBackSquares.get(5));

        Piece leftMid_1 = new Piece(mLeftSquares.get(4));
        Piece leftMid_2 = frontMid_0;

        Piece leftBottom_0 = new Piece(Piece.PieceType.CORNER);
        leftBottom_0.addSquare(mLeftSquares.get(6));
        leftBottom_0.addSquare(mBackSquares.get(8));
        leftBottom_0.addSquare(mBottomSquares.get(6));

        Piece leftBottom_1 = new Piece(Piece.PieceType.EDGE);
        leftBottom_1.addSquare(mLeftSquares.get(7));
        leftBottom_1.addSquare(mBottomSquares.get(3));

        Piece leftBottom_2 = frontBottom_0;

        leftFace.add(leftTop_0);
        leftFace.add(leftTop_1);
        leftFace.add(leftTop_2);
        leftFace.add(leftMid_0);
        leftFace.add(leftMid_1);
        leftFace.add(leftMid_2);
        leftFace.add(leftBottom_0);
        leftFace.add(leftBottom_1);
        leftFace.add(leftBottom_2);

        Piece topFar_0 = leftTop_0;
        Piece topFar_1 = new Piece(Piece.PieceType.EDGE);
        topFar_1.addSquare(mTopSquares.get(1));
        topFar_1.addSquare(mBackSquares.get(1));
        Piece topFar_2 = rightTop_2;
        Piece topMid_0 = leftTop_1;
        Piece topMid_1 = new Piece(mTopSquares.get(4));
        Piece topMid_2 = rightTop_1;
        Piece topNear_0 = frontTop_0;
        Piece topNear_1 = frontTop_1;
        Piece topNear_2 = frontTop_2;

        topFace.add(topFar_0);
        topFace.add(topFar_1);
        topFace.add(topFar_2);
        topFace.add(topMid_0);
        topFace.add(topMid_1);
        topFace.add(topMid_2);
        topFace.add(topNear_0);
        topFace.add(topNear_1);
        topFace.add(topNear_2);

        Piece bottomNear_0 = frontBottom_0;
        Piece bottomNear_1 = frontBottom_1;
        Piece bottomNear_2 = frontBottom_2;
        Piece bottomMid_0 = leftBottom_1;
        Piece bottomMid_1 = new Piece(mBottomSquares.get(4));
        Piece bottomMid_2 = rightBottom_1;

        Piece bottomFar_0 = leftBottom_0;
        Piece bottomFar_1 = new Piece(Piece.PieceType.EDGE);
        bottomFar_1.addSquare(mBottomSquares.get(7));
        bottomFar_1.addSquare(mBackSquares.get(7));
        Piece bottomFar_2 = rightBottom_2;

        bottomFace.add(bottomNear_0);
        bottomFace.add(bottomNear_1);
        bottomFace.add(bottomNear_2);
        bottomFace.add(bottomFar_0);
        bottomFace.add(bottomFar_1);
        bottomFace.add(bottomFar_2);
        bottomFace.add(bottomMid_0);
        bottomFace.add(bottomMid_1);
        bottomFace.add(bottomMid_2);

        Piece backTop_0 = rightTop_2;
        Piece backTop_1 = topFar_1;
        Piece backTop_2 = topFar_0;
        Piece backMid_0 = rightMid_2;
        Piece backMid_1 = new Piece(mBackSquares.get(4));
        Piece backMid_2 = leftMid_0;
        Piece backBottom_0 = bottomFar_2;
        Piece backBottom_1 = bottomFar_1;
        Piece backBottom_2 = bottomFar_0;

        backFace.add(backTop_0);
        backFace.add(backTop_1);
        backFace.add(backTop_2);
        backFace.add(backMid_0);
        backFace.add(backMid_1);
        backFace.add(backMid_2);
        backFace.add(backBottom_0);
        backFace.add(backBottom_1);
        backFace.add(backBottom_2);

        centerYZ.add(frontMid_1);
        centerYZ.add(frontBottom_1);
        centerYZ.add(bottomMid_1);
        centerYZ.add(bottomFar_1);
        centerYZ.add(backMid_1);
        centerYZ.add(backTop_1);
        centerYZ.add(topMid_1);
        centerYZ.add(topNear_1);

        centerXY.add(topMid_0);
        centerXY.add(topMid_1);
        centerXY.add(topMid_2);
        centerXY.add(rightMid_1);
        centerXY.add(rightBottom_1);
        centerXY.add(bottomMid_1);
        centerXY.add(bottomMid_0);
        centerXY.add(leftMid_1);

        centerZX.add(frontMid_0);
        centerZX.add(frontMid_1);
        centerZX.add(frontMid_2);
        centerZX.add(rightMid_1);
        centerZX.add(rightMid_2);
        centerZX.add(backMid_1);
        centerZX.add(backMid_2);
        centerZX.add(leftMid_1);

        mXaxisFaceList.add(leftFace);
        mXaxisFaceList.add(centerYZ);
        mXaxisFaceList.add(rightFace);

        mZaxisFaceList.add(backFace);
        mZaxisFaceList.add(centerXY);
        mZaxisFaceList.add(frontFace);

        mYaxisFaceList.add(bottomFace);
        mYaxisFaceList.add(centerZX);
        mYaxisFaceList.add(topFace);
    }

    /**
     * From top-far to bottom-near. YZ
     * Y moves down after filling each row on Z axis
     *
     * On negative X plane
     * */
    private void createLeftSquares(int color)
    {
        float startX = 0 - SQ_SIZE * (DIMENSION/2.0f) - GAP * (DIMENSION - 1);
        float startY = (SQ_SIZE + GAP) * (DIMENSION/2.0f);
        float startZ = 0 - (SQ_SIZE + GAP) * (DIMENSION/2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY - SQ_SIZE, startZ,
                  startX,  startY - SQ_SIZE, startZ + SQ_SIZE,
                  startX,  startY, startZ + SQ_SIZE
            };

        for (int i = 0; i < DIMENSION; i++) {
            vertices[1] = startY - i * (SQ_SIZE + GAP);
            vertices[4] = vertices[1] - SQ_SIZE;
            vertices[7] = vertices[1] - SQ_SIZE;
            vertices[10] = vertices[1];

            for (int j = 0; j < DIMENSION; j++) {
                vertices[2] = startZ + j * (SQ_SIZE + GAP);
                vertices[5] = vertices[2];
                vertices[8] = vertices[2] + SQ_SIZE;
                vertices[11] = vertices[2] + SQ_SIZE;
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
        float startX = SQ_SIZE * (DIMENSION/2.0f) + GAP * (DIMENSION-1);
        float startY = (SQ_SIZE + GAP) * (DIMENSION/2.0f);
        float startZ = (SQ_SIZE + GAP) * (DIMENSION/2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY - SQ_SIZE, startZ,
                  startX,  startY - SQ_SIZE, startZ - SQ_SIZE,
                  startX,  startY, startZ - SQ_SIZE
            };

        for (int i = 0; i < DIMENSION; i++) {
            vertices[1] = startY - i * (SQ_SIZE + GAP);
            vertices[4] = vertices[1] - SQ_SIZE;
            vertices[7] = vertices[1] - SQ_SIZE;
            vertices[10] = vertices[1];

            for (int j = 0; j < DIMENSION; j++) {
                vertices[2] = startZ - j * (SQ_SIZE + GAP);
                vertices[5] = vertices[2];
                vertices[8] = vertices[2] - SQ_SIZE;
                vertices[11] = vertices[2] - SQ_SIZE;
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
        float startX = - (SQ_SIZE + GAP) * (DIMENSION/2.0f);
        float startY = (SQ_SIZE + GAP) * (DIMENSION/2.0f);
        float startZ = - (SQ_SIZE + GAP) * (DIMENSION/2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY, startZ + SQ_SIZE,
                  startX + SQ_SIZE,  startY, startZ + SQ_SIZE,
                  startX + SQ_SIZE,  startY, startZ
            };

        for (int i = 0; i < DIMENSION; i++) {
            vertices[2] = startZ + i * (SQ_SIZE + GAP);
            vertices[5] = vertices[2] + SQ_SIZE;
            vertices[8] = vertices[2] + SQ_SIZE;
            vertices[11] = vertices[2];

            for (int j = 0; j < DIMENSION; j++) {
                vertices[0] = startX + j * (SQ_SIZE + GAP);
                vertices[3] = vertices[0];
                vertices[6] = vertices[0] + SQ_SIZE;
                vertices[9] = vertices[0] + SQ_SIZE;
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
        float startX = - (SQ_SIZE + GAP) * (DIMENSION/2.0f);
        float startY = - (SQ_SIZE + GAP) * (DIMENSION/2.0f);
        float startZ = (SQ_SIZE + GAP) * (DIMENSION/2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY, startZ - SQ_SIZE,
                  startX + SQ_SIZE,  startY, startZ - SQ_SIZE,
                  startX + SQ_SIZE,  startY, startZ
            };

        for (int i = 0; i < DIMENSION; i++) {
            vertices[2] = startZ - i * (SQ_SIZE + GAP);
            vertices[5] = vertices[2] - SQ_SIZE;
            vertices[8] = vertices[2] - SQ_SIZE;
            vertices[11] = vertices[2];

            for (int j = 0; j < DIMENSION; j++) {
                vertices[0] = startX + j * (SQ_SIZE + GAP);
                vertices[3] = vertices[0];
                vertices[6] = vertices[0] + SQ_SIZE;
                vertices[9] = vertices[0] + SQ_SIZE;
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
    private void createFrontSquares(int color)
    {
        float startX = 0 - (SQ_SIZE + GAP) * (DIMENSION/2.0f);
        float startY = (SQ_SIZE + GAP) * (DIMENSION/2.0f);
        float startZ = (SQ_SIZE + GAP) * (DIMENSION/2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY - SQ_SIZE, startZ,
                  startX + SQ_SIZE,  startY - SQ_SIZE, startZ,
                  startX + SQ_SIZE,  startY, startZ
            };

        for (int i = 0; i < DIMENSION; i++) {
            vertices[1] = startY - i * (SQ_SIZE + GAP);
            vertices[4] = vertices[1] - SQ_SIZE;
            vertices[7] = vertices[1] - SQ_SIZE;
            vertices[10] = vertices[1];

            for (int j = 0; j < DIMENSION; j++) {
                vertices[0] = startX + j * (SQ_SIZE + GAP);
                vertices[3] = vertices[0];
                vertices[6] = vertices[0] + SQ_SIZE;
                vertices[9] = vertices[0] + SQ_SIZE;
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
    private void createBackSquares(int color)
    {
        float startX = (SQ_SIZE + GAP) * (DIMENSION/2.0f);
        float startY = (SQ_SIZE + GAP) * (DIMENSION/2.0f);
        float startZ = - (SQ_SIZE + GAP) * (DIMENSION/2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY - SQ_SIZE, startZ,
                  startX - SQ_SIZE,  startY - SQ_SIZE, startZ,
                  startX - SQ_SIZE,  startY, startZ
            };

        for (int i = 0; i < DIMENSION; i++) {
            vertices[1] = startY - i * (SQ_SIZE + GAP);
            vertices[4] = vertices[1] - SQ_SIZE;
            vertices[7] = vertices[1] - SQ_SIZE;
            vertices[10] = vertices[1];

            for (int j = 0; j < DIMENSION; j++) {
                vertices[0] = startX - j * (SQ_SIZE + GAP);
                vertices[3] = vertices[0];
                vertices[6] = vertices[0] - SQ_SIZE;
                vertices[9] = vertices[0] - SQ_SIZE;
                Square sq = new Square(vertices, color);
                mAllSquares.add(sq);
                mBackSquares.add(sq);
            }
        }
    }

    private void drawCube(float[] matrix) {
        for (Square sq: mAllSquares) {
            sq.draw(matrix);
        }
    }

    protected void draw(float[] mvpMatrix) {

        Square.startDrawing();

        if (!mRotation.status) {
            drawCube(mvpMatrix);
            Square.finishDrawing();
            return;
        }

        ArrayList<ArrayList<Piece>> faceList = null;

        float[] scratch = new float[16];
        float[] rotationMatrix = new float[16];
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

        Matrix.setRotateM(rotationMatrix, 0, angle, angleX, angleY, angleZ);
        Matrix.multiplyMM(scratch, 0, mvpMatrix, 0, rotationMatrix, 0);

        for (int i = 0; i < mRotation.startFace; i++) {
            ArrayList<Piece> pieces = faceList.get(i);
            for (Piece piece: pieces) {
                for (Square square: piece.mSquares) {
                    square.draw(mvpMatrix);
                }
            }
        }

        for (int i = 0; i < mRotation.faceCount; i++) {
            ArrayList<Piece> pieces = faceList.get(mRotation.startFace + i);
            for (Piece piece: pieces) {
                for (Square square: piece.mSquares) {
                    square.draw(scratch);
                }
            }
        }

        for (int i = mRotation.startFace + mRotation.faceCount; i < DIMENSION; i++) {
            ArrayList<Piece> pieces = faceList.get(i);
            for (Piece piece: pieces) {
                for (Square square: piece.mSquares) {
                    square.draw(mvpMatrix);
                }
            }
        }

        if (Math.abs(mRotation.angle) > 89.9f) {
            finishRotation();
        } else if (mRotation.direction == Direction.CLOCKWISE) {
            mRotation.angle -= ANGLE_DELTA;
        } else {
            mRotation.angle += ANGLE_DELTA;
        }

        Square.finishDrawing();
    }

    class RubikGLSurfaceView extends GLSurfaceView {

        RubikGLSurfaceView(Context context) {
            super(context);
            setEGLContextClientVersion(2);
            setPreserveEGLContextOnPause(true);
            setRenderer(new RubikRenderer(RubiksCube.this));
        }
    }

    class Rotation {
        boolean status;
        int axis;

        public void setAxis(int axis) {
            if (axis < X_AXIS || axis > Z_AXIS) {
                throw new InvalidParameterException("Axis " + axis);
            }
            this.axis = axis;
        }

        public void setStartFace(int startFace) {
            this.startFace = startFace;
        }

        Direction direction;

        /**
         * To support simultaneous rotating of multiple faces in higher order cubes
         * */
        int startFace;
        int faceCount;
        float angle;

        Rotation() {
            reset();
        }

        Rotation(int axis, Direction dir, int face) {
            reset();
            this.axis = axis;
            this.direction = dir;
            this.startFace = face;
            this.angle = 0;
        }

        Rotation duplicate() {
            return new Rotation(axis, direction, startFace);
        }

        void reset() {
            status = false;
            axis = X_AXIS;
            direction = Direction.COUNTER_CLOCKWISE;
            startFace = 2;
            faceCount = 1;
            angle = 0;
        }

    }
}

