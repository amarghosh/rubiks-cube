package com.amg.rubik;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

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

    static final float SQ_SIZE = 0.15f;
    static final float GAP = 0.005f;

    enum Axis {
        X_AXIS,
        Y_AXIS,
        Z_AXIS
    };

    enum Direction {
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }

    class Rotation {
        Axis mAxis;
        Direction mDirection;

        /**
         * To support simultaneous rotating of multiple faces in higher order cubes
        * */
        int mStartFace;
        int mFaceCount;
    }

    private int DIMENSION = 3;
    private GLSurfaceView mGlView = null;

    public RubiksCube(Context context, int size) {
        mGlView = new RubikGLSurfaceView(context);
        /*
        TODO: remove this hard coding once generic cubes are implemented.
        * */
        DIMENSION = 3; //size
        cube();
    }

    ArrayList<Square> mAllSquares;
    ArrayList<Square> mFrontSquares;
    ArrayList<Square> mBackSquares;
    ArrayList<Square> mTopSquares;
    ArrayList<Square> mBottomSquares;
    ArrayList<Square> mLeftSquares;
    ArrayList<Square> mRightSquares;

    ArrayList<ArrayList<Piece>> mXaxisFaceList;
    ArrayList<ArrayList<Piece>> mYaxisFaceList;
    ArrayList<ArrayList<Piece>> mZaxisFaceList;

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

    public GLSurfaceView getView() {
        return mGlView;
    }

    public void onResume() {
        mGlView.onResume();
    }

    public void onPause() {
        mGlView.onPause();
    }

    protected void draw(float[] mvpMatrix) {
        Square.startDrawing();

        float[] scratch = new float[16];
        float[] rotationMatrix = new float[16];
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        Matrix.setRotateM(rotationMatrix, 0, angle, 0, 1, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mvpMatrix, 0, rotationMatrix, 0);

        for (Piece p: mYaxisFaceList.get(0)) {
            for (Square sq : p.mSquares) {
                sq.draw(mvpMatrix);
            }
        }
        for (Piece p: mYaxisFaceList.get(1)) {
            for (Square sq : p.mSquares) {
                sq.draw(mvpMatrix);
            }
        }
        for (Piece p: mYaxisFaceList.get(2)) {
            for (Square sq : p.mSquares) {
                sq.draw(scratch);
            }
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
}