package com.amg.rubik;

import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import com.amg.rubik.Rotation.Direction;
import com.amg.rubik.Rotation.Axis;

/**
 * Created by amar on 9/12/15.
 */
public class RubiksCube3x3x3 extends RubiksCube {

    enum SolveState {
        None,
        FirstFaceCross,
        FirstFaceCorners,
        MiddleFace,
        LastFaceCross,
        LastFaceCrossAlign,
        LastFaceCorners,
        LastFaceCornerAlign
    }
    private static final int EDGE_BOTTOM_NEAR = 1;
    private static final int EDGE_BOTTOM_RIGHT = 5;
    private static final int EDGE_BOTTOM_LEFT = 3;
    private static final int EDGE_BOTTOM_FAR = 7;

    private static final int EDGE_MIDDLE_FRONT_LEFT = 0;
    private static final int EDGE_MIDDLE_FRONT_RIGHT = 2;
    private static final int EDGE_MIDDLE_RIGHT_BACK = 4;
    private static final int EDGE_MIDDLE_LEFT_BACK = 6;

    private static final int EDGE_TOP_FAR = 1;
    private static final int EDGE_TOP_NEAR = 7;
    private static final int EDGE_TOP_LEFT = 3;
    private static final int EDGE_TOP_RIGHT = 5;

    private SolveState solveState = SolveState.None;

    private int mTopColor = 0;
    private int mBottomColor = 0;

    public RubiksCube3x3x3() {
        super(3);
    }

    @Override
    public int solve() {
        if (mState == CubeState.TESTING) {
            mState = CubeState.IDLE;
        }
        if (mState != CubeState.IDLE) {
            sendMessage("Invalid state to solve: " + mState);
            return -1;
        }
        mState = CubeState.SOLVING;
        startSolving();
        return 0;
    }

    private void startSolving() {
        solveState = SolveState.FirstFaceCross;
        mTopColor = mTopSquares.get(4).mColor;
        mBottomColor = mBottomSquares.get(4).mColor;
        sendMessage("Top is " + mTopSquares.get(4).colorName() +
                " and bottom is " + mBottomSquares.get(4).colorName());
        firstFaceCross();
    }

    private void firstFaceCross() {
        ArrayList<Square>[] sideFaces = new ArrayList[]{
                mBackSquares, mLeftSquares, mRightSquares, mFrontSquares
        };

        for (int i = 7; i > 0; i--) {
            if (i % 2 == 0) {
                continue;
            }
            ArrayList<Square> sideFace = sideFaces[i/2];
            if (mTopSquares.get(i).mColor == mTopColor &&
                    sideFace.get(1).mColor == sideFace.get(4).mColor) {
                continue;
            }
            if (i != 7) {
                // Bring the missing edge to face the user
                Direction dir = (i == 3) ? Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
                Algorithm algo = Algorithm.rotateWhole(Axis.Y_AXIS, dir, 3,
                        i == 1 ? 2 : 1);
                setAlgo(algo);
            } else {
                fixFirstFaceEdge(mTopColor, sideFace.get(4).mColor);
            }
            return;
        }

        // We didn't return.. We have the cross
        sendMessage("Top cross is done..!");
    }

    private void fixFirstFaceEdge(int topColor, int sideColor) {
        int[] colors = new int[] {topColor, sideColor};
        int row = 0, pos = -1;
        for (row = 0; row < 3; row++) {
            pos = findPieceOnFace(mYaxisFaceList.get(row), colors);
            if (pos >= 0) {
                break;
            }
        }

        Log.w(tag, "Found " +
                Square.getColorName(topColor) + '-' +
                Square.getColorName(sideColor) + " at " + row + "-" + pos);

        // White on bottom face
        if (row == INNER && mBottomSquares.get(pos).mColor == topColor) {
            firstFaceEdge_fromBottomFace(pos);
        } else if (row == INNER) {
            firstFaceEdge_fromLowerLayer(pos);
        } else if (row == MIDDLE) {
            firstFaceEdge_fromMiddleLayer(pos);
        } else {
            firstFaceEdge_fromTopLayer(pos);
        }
    }

    private static final int INNER = 0;
    private static final int MIDDLE = 1;
    private static final int OUTER = 2;

    private void firstFaceEdge_fromTopLayer(final int pos) {
        sendMessage("Edge piece from top layer");
        Algorithm algo = new Algorithm();
        ArrayList<Rotation> middleRotations;
        Rotation rot = null;
        Square topColoredSquare = getSquareByColor(mYaxisFaceList, OUTER, pos, mTopColor);


        if (pos == EDGE_TOP_FAR || pos == EDGE_TOP_NEAR) {
            int faceIndex = topColoredSquare.getFace() == FACE_TOP ?
                    FACE_RIGHT : topColoredSquare.getFace();
            rot = new Rotation(Axis.Z_AXIS,
                    Direction.CLOCKWISE,
                    pos == EDGE_TOP_FAR ? INNER : OUTER);
            algo.addStep(rot);
            middleRotations = middleEdgeToTopEdge(
                    pos == EDGE_TOP_FAR ? EDGE_MIDDLE_RIGHT_BACK : EDGE_MIDDLE_FRONT_RIGHT,
                    mTopColor, faceIndex);
        } else {
            int faceIndex = topColoredSquare.getFace() == FACE_TOP ?
                    FACE_FRONT : topColoredSquare.getFace();
            rot = new Rotation(Axis.X_AXIS,
                    Direction.COUNTER_CLOCKWISE,
                    pos == EDGE_TOP_LEFT ? INNER : OUTER);
            algo.addStep(rot);
            middleRotations = middleEdgeToTopEdge(pos == EDGE_TOP_LEFT ?
                    EDGE_MIDDLE_FRONT_LEFT : EDGE_MIDDLE_FRONT_RIGHT, mTopColor, faceIndex);
        }

        for (int i = 0; i < middleRotations.size(); i++) {
            algo.addStep(middleRotations.get(i));
        }

        setAlgo(algo);
    }

    private static ArrayList<Rotation> middleEdgeToTopEdge(int middlePos, int topColor, int faceWithTopColor) {
        ArrayList<Rotation> rotations = new ArrayList<>();

        switch (middlePos) {
            case EDGE_MIDDLE_FRONT_LEFT:
                assert faceWithTopColor == FACE_FRONT || faceWithTopColor == FACE_LEFT;
                if (faceWithTopColor == FACE_FRONT) {
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.X_AXIS, Direction.CLOCKWISE, INNER));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                } else {
                    rotations.add(new Rotation(Axis.Z_AXIS, Direction.CLOCKWISE, OUTER));
                }
                break;

            case EDGE_MIDDLE_FRONT_RIGHT:
                assert faceWithTopColor == FACE_FRONT || faceWithTopColor == FACE_RIGHT;
                if (faceWithTopColor == FACE_FRONT) {
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.X_AXIS, Direction.CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER));
                } else {
                    rotations.add(new Rotation(Axis.Z_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                }
                break;

            case EDGE_MIDDLE_RIGHT_BACK:
                assert faceWithTopColor == FACE_RIGHT || faceWithTopColor == FACE_BACK;
                if (faceWithTopColor == FACE_BACK) {
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, 2));
                    rotations.add(new Rotation(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, 2));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.CLOCKWISE, 2));
                } else {
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, 2));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, 2));
                    rotations.add(new Rotation(Axis.Z_AXIS, Direction.COUNTER_CLOCKWISE, 0));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, 2));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, 2));
                }
                break;

            case EDGE_MIDDLE_LEFT_BACK:
                assert faceWithTopColor == FACE_LEFT || faceWithTopColor == FACE_BACK;
                if (faceWithTopColor == FACE_BACK) {
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.CLOCKWISE, 2));
                    rotations.add(new Rotation(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, 0));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, 2));
                } else {
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, 2));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, 2));
                    rotations.add(new Rotation(Axis.Z_AXIS, Direction.CLOCKWISE, 0));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, 2));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, 2));
                }
                break;
        }
        return rotations;
    }

    Square getSquareByColor(ArrayList<ArrayList<Piece>> faceList, int index, int pos, int color) {
        Piece piece = faceList.get(index).get(pos);
        for (Square sq: piece.mSquares) {
            if (sq.mColor == color) {
                return sq;
            }
        }
        throw new InvalidParameterException("Square not found: Index " + index +
            ", pos " + pos + ", color " + color);
    }

    private void firstFaceEdge_fromMiddleLayer(int pos) {
        sendMessage("Edge piece from middle layer");
        Square topColorSquare = getSquareByColor(mYaxisFaceList, MIDDLE, pos, mTopColor);
        int faceIndex = topColorSquare.getFace();

        ArrayList<Rotation> rotations = middleEdgeToTopEdge(pos, mTopColor, faceIndex);
        Algorithm algo = new Algorithm();
        for (int i = 0; i < rotations.size(); i++) {
            algo.addStep(rotations.get(i));
        }
        setAlgo(algo);
    }

    /**
     * pos: position of desired piece in bottom face.
     * The piece should have non-white on bottom face
     * */
    private void firstFaceEdge_fromLowerLayer(int pos) {
        sendMessage("Edge piece from lower layer");
        Algorithm algorithm = new Algorithm();

        // the white should be on one of the sides, not front or back
        if (pos == EDGE_BOTTOM_NEAR || pos == EDGE_BOTTOM_FAR) {
            algorithm.addStep(new Rotation(Axis.Y_AXIS, Direction.CLOCKWISE, INNER));
        }

        if (pos <= EDGE_BOTTOM_LEFT) {
            algorithm.addStep(new Rotation(Axis.X_AXIS, Direction.CLOCKWISE, INNER));
            algorithm.addStep(new Rotation(Axis.Z_AXIS, Direction.CLOCKWISE, OUTER));
            if (mTopSquares.get(EDGE_TOP_LEFT).mColor == mTopColor &&
                    mLeftSquares.get(1).mColor == mLeftSquares.get(4).mColor) {
                algorithm.addStep(new Rotation(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, INNER));
            }
        } else {
            algorithm.addStep(new Rotation(Axis.X_AXIS, Direction.CLOCKWISE, OUTER));
            algorithm.addStep(new Rotation(Axis.Z_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
            if (mTopSquares.get(EDGE_TOP_RIGHT).mColor == mTopColor &&
                    mRightSquares.get(1).mColor == mRightSquares.get(4).mColor) {
                algorithm.addStep(new Rotation(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
            }
        }
        setAlgo(algorithm);
    }

    /**
     * pos: position of desired piece in bottom face.
     * The piece should have white on bottom face
     * */
    private void firstFaceEdge_fromBottomFace(int pos) {
        Algorithm algo = new Algorithm();
        sendMessage("Edge piece from bottom face");

        /**
         * Piece is not aligned yet.
         * Rotate bottom face
         * */
        if (pos != EDGE_BOTTOM_NEAR) {
            Direction dir = pos == EDGE_BOTTOM_LEFT ?
                    Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
            Rotation rot = new Rotation(Axis.Y_AXIS, dir, INNER);
            algo.addStep(rot);
            if (pos == EDGE_BOTTOM_FAR) {
                algo.addStep(rot);
            }
        }
        // Front face twice
        Rotation rot = new Rotation(Axis.Z_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algo.addStep(rot);
        algo.addStep(rot);
        setAlgo(algo);
    }

    private int findPieceOnFace(ArrayList<Piece> face, int[] colors) {
        Arrays.sort(colors);
        for (int i = 0; i < face.size(); i++) {
            Piece piece = face.get(i);
            if (piece.mSquares.size() != colors.length)
                continue;
            int[] pieceColors = new int[piece.mSquares.size()];
            for (int j = 0; j < pieceColors.length; j++) {
                pieceColors[j] = piece.mSquares.get(j).mColor;
            }
            Arrays.sort(pieceColors);
            boolean found = true;
            for (int j = 0; j < colors.length; j++) {
                if (colors[j] != pieceColors[j]) {
                    found = false;
                    break;
                }
            }
            if (found)
                return i;
        }
        return -1;
    }

    @Override
    void updateAlgo() {
        if (mState != CubeState.SOLVING)
            return;
        firstFaceCross();
    }

    @Override
    protected void finishRotation() {
        super.finishRotation();
    }
}
