package com.amg.rubik.cube;

import com.amg.rubik.graphics.Axis;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * Created by amar on 12/1/16.
 *
 * This class handles cubes definition. It creates all squares, faces and puts them in appropriate
 * lists for each axes. It doesn't care about drawing the cube.
 */
public class AbstractCube {
    // Do not change these values. They are used in many places.
    public static final int FACE_FRONT = 0;
    public static final int FACE_RIGHT = 1;
    public static final int FACE_BACK = 2;
    public static final int FACE_LEFT = 3;
    public static final int FACE_TOP = 4;
    public static final int FACE_BOTTOM = 5;

    protected static String[] faceNames = {
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
    protected static final float TOTAL_SIZE = 2.0f;
    protected static final float PADDING = 0.8f;

    protected static final float GAP = 0.01f;

    protected int mSize;
    protected float squareSize;

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

    public AbstractCube(int size) {
        if (size <= 0) throw new AssertionError();
        mSize = size;
        squareSize = (TOTAL_SIZE - PADDING - GAP * (mSize + 1)) / mSize;
        cube();
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

    protected void updateSquareFaces() {
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
}