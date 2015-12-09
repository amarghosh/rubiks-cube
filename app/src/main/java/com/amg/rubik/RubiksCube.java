package com.amg.rubik;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Random;

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

    private static final String tag = "rubik-cube";

    // We don't support skewed cubes yet.
    private static final int CUBE_SIDES = 4;

    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;

    public enum Direction {
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }


    private static final float SQ_SIZE = 0.15f;
    private static final float GAP = 0.005f;

    // Default value for incrementing angle during rotation
    private static final float ANGLE_DELTA = 2f;

    private int mSize = 3;

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


    enum RotateMode {
        NONE,
        RANDOM,
        ALGORITHM,
        REPEAT
    }

    private RotateMode rotateMode = RotateMode.NONE;

    private Rotation[] currentAlgo = new Rotation[4];

    void populateAlgo() {
        currentAlgo[0] = new Rotation(X_AXIS, Direction.COUNTER_CLOCKWISE, 2);
        currentAlgo[1] = new Rotation(Y_AXIS, Direction.CLOCKWISE, 0);
        currentAlgo[2] = new Rotation(X_AXIS, Direction.CLOCKWISE, 2);
        currentAlgo[3] = new Rotation(Y_AXIS, Direction.COUNTER_CLOCKWISE, 0);
    }

    public RubiksCube(int size) {
        mSize = size;
        cube();
        populateAlgo();
        if (rotateMode == RotateMode.ALGORITHM) {
            mRotation = currentAlgo[0].duplicate();
        } else {
            mRotation = new Rotation();
        }
    }

    public void randomize() {
        rotateMode = RotateMode.RANDOM;
        mRotation.status = true;
    }

    public void stopRandomize() {
        rotateMode = RotateMode.NONE;
        mRotation.reset();
        finishRotation();
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
    void finishRotation() {
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

        switch (rotateMode) {
            case ALGORITHM:
                mCurrentIndex = (mCurrentIndex + 1) % currentAlgo.length;
                mRotation = currentAlgo[mCurrentIndex].duplicate();
                mRotation.status = true;
                break;

            case REPEAT:
                repeatRotation();
                break;

            case RANDOM:
                rotateRandom();
                break;

            default:
                mRotation.reset();
                break;
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
        mRotation.status = true;
    }

    void rotateRandom() {
        mRotation.reset();
        Random random = new Random();
        mRotation.setAxis(Math.abs(random.nextInt(3)));
        mRotation.direction = random.nextBoolean() ?
                Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;

        // Do not rotate the center piece in case of odd cubes
        if (mSize % 2 == 1) {
            int startFace = Math.abs(random.nextInt(mSize - 1));
            if (startFace >= mSize / 2) {
                startFace++;
            }
            mRotation.setStartFace(startFace);
        } else {
            mRotation.setStartFace(Math.abs(random.nextInt(mSize)));
        }
        Log.w(tag, "Next rotation " + mRotation);
        mRotation.status = true;
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

        mXaxisFaceList = new ArrayList<>(mSize);
        mYaxisFaceList = new ArrayList<>(mSize);
        mZaxisFaceList = new ArrayList<>(mSize);
        createFaces();
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
        float startX = 0 - SQ_SIZE * (mSize / 2.0f) - GAP * (mSize - 1);
        float startY = (SQ_SIZE + GAP) * (mSize / 2.0f);
        float startZ = 0 - (SQ_SIZE + GAP) * (mSize / 2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY - SQ_SIZE, startZ,
                  startX,  startY - SQ_SIZE, startZ + SQ_SIZE,
                  startX,  startY, startZ + SQ_SIZE
            };

        for (int i = 0; i < mSize; i++) {
            vertices[1] = startY - i * (SQ_SIZE + GAP);
            vertices[4] = vertices[1] - SQ_SIZE;
            vertices[7] = vertices[1] - SQ_SIZE;
            vertices[10] = vertices[1];

            for (int j = 0; j < mSize; j++) {
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
        float startX = SQ_SIZE * (mSize / 2.0f) + GAP * (mSize -1);
        float startY = (SQ_SIZE + GAP) * (mSize / 2.0f);
        float startZ = (SQ_SIZE + GAP) * (mSize / 2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY - SQ_SIZE, startZ,
                  startX,  startY - SQ_SIZE, startZ - SQ_SIZE,
                  startX,  startY, startZ - SQ_SIZE
            };

        for (int i = 0; i < mSize; i++) {
            vertices[1] = startY - i * (SQ_SIZE + GAP);
            vertices[4] = vertices[1] - SQ_SIZE;
            vertices[7] = vertices[1] - SQ_SIZE;
            vertices[10] = vertices[1];

            for (int j = 0; j < mSize; j++) {
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
        float startX = - (SQ_SIZE + GAP) * (mSize / 2.0f);
        float startY = (SQ_SIZE + GAP) * (mSize / 2.0f);
        float startZ = - (SQ_SIZE + GAP) * (mSize / 2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY, startZ + SQ_SIZE,
                  startX + SQ_SIZE,  startY, startZ + SQ_SIZE,
                  startX + SQ_SIZE,  startY, startZ
            };

        for (int i = 0; i < mSize; i++) {
            vertices[2] = startZ + i * (SQ_SIZE + GAP);
            vertices[5] = vertices[2] + SQ_SIZE;
            vertices[8] = vertices[2] + SQ_SIZE;
            vertices[11] = vertices[2];

            for (int j = 0; j < mSize; j++) {
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
        float startX = - (SQ_SIZE + GAP) * (mSize / 2.0f);
        float startY = - (SQ_SIZE + GAP) * (mSize / 2.0f);
        float startZ = (SQ_SIZE + GAP) * (mSize / 2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY, startZ - SQ_SIZE,
                  startX + SQ_SIZE,  startY, startZ - SQ_SIZE,
                  startX + SQ_SIZE,  startY, startZ
            };

        for (int i = 0; i < mSize; i++) {
            vertices[2] = startZ - i * (SQ_SIZE + GAP);
            vertices[5] = vertices[2] - SQ_SIZE;
            vertices[8] = vertices[2] - SQ_SIZE;
            vertices[11] = vertices[2];

            for (int j = 0; j < mSize; j++) {
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
        float startX = 0 - (SQ_SIZE + GAP) * (mSize / 2.0f);
        float startY = (SQ_SIZE + GAP) * (mSize / 2.0f);
        float startZ = (SQ_SIZE + GAP) * (mSize / 2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY - SQ_SIZE, startZ,
                  startX + SQ_SIZE,  startY - SQ_SIZE, startZ,
                  startX + SQ_SIZE,  startY, startZ
            };

        for (int i = 0; i < mSize; i++) {
            vertices[1] = startY - i * (SQ_SIZE + GAP);
            vertices[4] = vertices[1] - SQ_SIZE;
            vertices[7] = vertices[1] - SQ_SIZE;
            vertices[10] = vertices[1];

            for (int j = 0; j < mSize; j++) {
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
        float startX = (SQ_SIZE + GAP) * (mSize / 2.0f);
        float startY = (SQ_SIZE + GAP) * (mSize / 2.0f);
        float startZ = - (SQ_SIZE + GAP) * (mSize / 2.0f);

        float vertices[] = {
                  startX,  startY, startZ,
                  startX,  startY - SQ_SIZE, startZ,
                  startX - SQ_SIZE,  startY - SQ_SIZE, startZ,
                  startX - SQ_SIZE,  startY, startZ
            };

        for (int i = 0; i < mSize; i++) {
            vertices[1] = startY - i * (SQ_SIZE + GAP);
            vertices[4] = vertices[1] - SQ_SIZE;
            vertices[7] = vertices[1] - SQ_SIZE;
            vertices[10] = vertices[1];

            for (int j = 0; j < mSize; j++) {
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

        if (rotateMode == RotateMode.NONE ||
                mRotation.status == false) {
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

        for (int i = mRotation.startFace + mRotation.faceCount; i < mSize; i++) {
            ArrayList<Piece> pieces = faceList.get(i);
            for (Piece piece: pieces) {
                for (Square square: piece.mSquares) {
                    square.draw(mvpMatrix);
                }
            }
        }

        if (Math.abs(mRotation.angle) > 89.9f) {
            finishRotation();
        } else {
            mRotation.increment();
        }

        Square.finishDrawing();
    }

    class Rotation {
        boolean status;
        int axis;

        Direction direction;

        /**
         * To support simultaneous rotating of multiple faces in higher order cubes
         * */
        int startFace;
        int faceCount;
        float angle;
        float angleDelta = ANGLE_DELTA;

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
            axis = Z_AXIS;
            direction = Direction.CLOCKWISE;
            startFace = mSize - 1;
            faceCount = 1;
            angle = 0;
        }

        @Override
        public String toString() {
            String axes = "XYZ";
            return "Axis " + axes.charAt(axis) +
                    ", direction " + direction +
                    ", face " + startFace;
        }

        public void setAxis(int axis) {
            if (axis < X_AXIS || axis > Z_AXIS) {
                throw new InvalidParameterException("Axis " + axis);
            }
            this.axis = axis;
        }

        public void setAngleDelta(float angleDelta) {
            if (angleDelta > 90) {
                throw new InvalidParameterException("Delta should be less than 90: " + angleDelta);
            }
            this.angleDelta = angleDelta;
        }

        public void setStartFace(int startFace) {
            if (startFace >= mSize) {
                throw new InvalidParameterException("StartFace " + startFace);
            }
            this.startFace = startFace;
        }

        void increment() {
            if (direction == Direction.CLOCKWISE) {
                angle -= angleDelta;
                if (angle < -90) {
                    angle = -90;
                }
            } else {
                angle += angleDelta;
                if (angle > 90) {
                    angle = 90;
                }
            }
        }
    }
}
