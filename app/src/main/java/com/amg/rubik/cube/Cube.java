package com.amg.rubik.cube;

import android.graphics.Color;

import com.amg.rubik.graphics.Axis;
import com.amg.rubik.graphics.Direction;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * This class handles cube's definition. It creates all squares, faces and puts them in appropriate
 * lists for each axes. It also takes care of updating the colors of squares according to user
 * specified rotation. It doesn't care about drawing the cube. You should extend this class
 * rather than using it directly.
 */
public class Cube {
    // Do not change these values. They are used in many places.
    public static final int FACE_FRONT = 0;
    public static final int FACE_RIGHT = 1;
    public static final int FACE_BACK = 2;
    public static final int FACE_LEFT = 3;
    public static final int FACE_TOP = 4;
    public static final int FACE_BOTTOM = 5;

    /**
     * Default colors don't look nice on cube.
     * */
    public static final int Color_RED = 0xFFDD2211;
    public static final int Color_GREEN = 0xFF22DD11;
    public static final int Color_ORANGE = 0xFFFF7F10;

    public static int COLOR_TOP = Color.WHITE;
    public static int COLOR_BOTTOM = Color.YELLOW;
    public static int COLOR_LEFT = Color_ORANGE;
    public static int COLOR_RIGHT = Color_RED;
    public static int COLOR_FRONT = Color.BLUE;
    public static int COLOR_BACK = Color_GREEN;


    private static final String[] faceNames = {
            "front", "right", "back", "left", "top", "bottom"
    };

    // We don't support skewed cubes yet.
    static final int CUBE_SIDES = 4;
    static final int FACE_COUNT = 6;

    /**
     * To calculate the square size:
     * Screen spans from -1f to +1f.
     * OpenGl won't draw things close to the frustrum border, hence we add padding and use
     * 1.2f instead of 2.0f as the total size
     * */
    private static final float TOTAL_SIZE = 2.0f;
    private static final float PADDING = 0.8f;

    private static final float GAP = 0.01f;

    int mSize;
    private float squareSize;

    ArrayList<Square> mAllSquares;
    ArrayList<Square> mFrontSquares;
    ArrayList<Square> mBackSquares;
    ArrayList<Square> mTopSquares;
    ArrayList<Square> mBottomSquares;
    ArrayList<Square> mLeftSquares;
    ArrayList<Square> mRightSquares;
    ArrayList<Square>[] mAllFaces;

    ArrayList<ArrayList<Piece>> mXaxisFaceList;
    ArrayList<ArrayList<Piece>> mYaxisFaceList;
    ArrayList<ArrayList<Piece>> mZaxisFaceList;

    public Cube(int size) {
        if (size <= 0) throw new AssertionError("Size is " + size);
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
        createFrontSquares(COLOR_FRONT);
        createBackSquares(COLOR_BACK);
        createLeftSquares(COLOR_LEFT);
        createRightSquares(COLOR_RIGHT);
        createTopSquares(COLOR_TOP);
        createBottomSquares(COLOR_BOTTOM);
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
                Square sq = new Square(vertices, color, FACE_LEFT);
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
                Square sq = new Square(vertices, color, FACE_RIGHT);
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
                Square sq = new Square(vertices, color, FACE_TOP);
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
                Square sq = new Square(vertices, color, FACE_BOTTOM);
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
                Square sq = new Square(vertices, color, FACE_FRONT);
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
                Square sq = new Square(vertices, color, FACE_BACK);
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

    /**
     * Rotate the colors in the border. This is the first part of rotating a layer.
     * */
    private void rotateRingColors(ArrayList<ArrayList<Square>> squareList, Direction dir) {
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
            tempColors.add(src.get(i).getColor());
        }

        for (int i = 0; i < CUBE_SIDES - 1; i++) {
            dst = workingCopy.get(i);
            src = workingCopy.get(i + 1);
            for (int j = 0; j < mSize; j++) {
                dst.get(j).setColor(src.get(j).getColor());
            }
        }

        dst = workingCopy.get(CUBE_SIDES-1);
        for (int i = 0; i < mSize; i++) {
            dst.get(i).setColor(tempColors.get(i));
        }
    }

    /**
     * Rotate colors of a given face. This is the second part of rotating a face.
     * This function calls itself recursively to rotate inner squares.
     * */
    private void rotateFaceColors(ArrayList<Square> squares, Direction direction, int size) {
        ArrayList<Integer> tempColors = new ArrayList<>(size);
        if (direction == Direction.COUNTER_CLOCKWISE) {
            for (int i = 0; i < size - 1; i++) {
                tempColors.add(squares.get(i).getColor());
                squares.get(i).setColor(squares.get(i * size + size - 1).getColor());
            }

            for (int i = 0; i < size - 1; i++) {
                squares.get(i * size + size - 1).setColor(
                        squares.get(size * size - 1 - i).getColor());
            }

            for (int i = 0; i < size - 1; i++) {
                squares.get(size * size - 1 - i).setColor(
                        squares.get(size * (size - 1 - i)).getColor());
            }

            for (int i = 0; i < size - 1; i++) {
                squares.get(size * (size - 1 - i)).setColor(tempColors.get(i));
            }
        } else {
            for (int i = 0; i < size - 1; i++) {
                tempColors.add(squares.get(i).getColor());
                squares.get(i).setColor(squares.get(size * (size - 1 - i)).getColor());
            }
            for (int i = 0; i < size - 1; i++) {
                squares.get(size * (size - 1 - i)).setColor(
                        squares.get(size * size - 1 - i).getColor());
            }
            for (int i = 0; i < size - 1; i++) {
                squares.get(size * size - 1 - i).setColor(
                        squares.get(i * size + size - 1).getColor());
            }
            for (int i = 0; i < size - 1; i++) {
                squares.get(i * size + size - 1).setColor(tempColors.get(i));
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

    /**
     * Rotate the face specified by @face and @axis.
     *
     * Note that the direction is relative to positive direction of the mentioned axis, and not
     * the visible side of face. This is against the normal cube notation where direction is
     * usually mentioned relative to the face being rotated.
     *
     * For instance, in traditional cube notation, L stands for left face clockwise where the
     * clock is running on the visible face of the left face. It will rotate left face clockwise
     * around the negative x axis. In our case, left face is face-0 on X axis and to achieve "L",
     * we should call this function with values (X, CCW, 0)
     *
     * Example mappings from traditional notation to this function (assuming 3x3x3 cube):
     * Front face clockwise: (Z, CW, 2)
     * Left face clockwise: (X, CCW, 0)
     * Bottom face clockwise: (Y, CCW, 0)
     * */
    protected void rotate(Axis axis, Direction direction, int face) {
        if (face < 0 || face >= mSize) {
            throw new InvalidParameterException(
                    String.format("Value %d is invalid for face (size is %d)", face, mSize));
        }

        ArrayList<Square> faceSquares = null;
        ArrayList<ArrayList<Square>> squareList = new ArrayList<>(CUBE_SIDES);
        for (int i = 0; i < CUBE_SIDES; i++) {
            squareList.add(new ArrayList<Square>(mSize));
        }
        switch (axis) {
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
        rotateRingColors(squareList, direction);

        if (face == mSize - 1) {
            // Rotate a face that is on the positive edge of the
            // corresponding axis (front, top or right).
            // As squares are stored in clockwise order, rotation is straightforward.
            rotateFaceColors(faceSquares, direction, mSize);
        } else if (face == 0) {
            rotateFaceColors(faceSquares,
                    direction == Direction.CLOCKWISE ?
                            Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE, mSize);
        }
    }
}

