package com.amg.rubik.cube;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;

import com.amg.rubik.Log;
import com.amg.rubik.graphics.Direction;
import com.amg.rubik.graphics.Axis;

/**
 * An algorithm can't be smarter than the human who devised it.
 *
 * This is a beginner's approach to solving 3x3 cube. You will see many easy to think of
 * improvements in the logic. Like how it takes 150 moves for a cube that could have been
 * solved in a single move.
 * */
public class RubiksCube3x3x3 extends RubiksCube {

    private static final String tag = "rubik-3x3x3";

    enum SolveState {
        None,
        FirstFaceCross,
        FirstFaceCorners,
        MiddleLayer,
        LastFaceCross,
        LastFaceCrossAlign,
        LastFaceCorners,
        LastFaceCornerAlign
    }

    private static final int SIZE = 3;

    private static final int INNER = 0;
    private static final int MIDDLE = 1;
    private static final int OUTER = 2;

    private static final int FIRST_ROW_LEFT = 0;
    private static final int FIRST_ROW_CENTER = 1;
    private static final int FIRST_ROW_RIGHT = 2;
    private static final int MID_ROW_LEFT = 3;
    private static final int CENTER = 4;
    private static final int MID_ROW_RIGHT = 5;
    private static final int LAST_ROW_LEFT = 6;
    private static final int LAST_ROW_MIDDLE = 7;
    private static final int LAST_ROW_RIGHT = 8;

    // Middle row in Y axis starts from mid-front-left and continues anticlockwise
    private static final int EDGE_MIDDLE_FRONT_LEFT = 0;
    private static final int EDGE_MIDDLE_FRONT_RIGHT = 2;
    private static final int EDGE_MIDDLE_RIGHT_BACK = 4;
    private static final int EDGE_MIDDLE_LEFT_BACK = 6;

    private static final int CORNER_INDEX_FRONT_RIGHT = 0;
    private static final int CORNER_INDEX_RIGHT_BACK = 1;
    private static final int CORNER_INDEX_BACK_LEFT = 2;
    private static final int CORNER_INDEX_LEFT_FRONT = 3;

    // bottom row numbering is similar to front face after a clockwise rotation around X axis
    private static final int EDGE_BOTTOM_NEAR = FIRST_ROW_CENTER;
    private static final int EDGE_BOTTOM_RIGHT = MID_ROW_RIGHT;
    private static final int EDGE_BOTTOM_LEFT = MID_ROW_LEFT;
    private static final int EDGE_BOTTOM_FAR = LAST_ROW_MIDDLE;

    // top row numbering is similar to front face after a counter clockwise rotation around X axis
    private static final int EDGE_TOP_FAR = FIRST_ROW_CENTER;
    private static final int EDGE_TOP_NEAR = LAST_ROW_MIDDLE;
    private static final int EDGE_TOP_LEFT = MID_ROW_LEFT;
    private static final int EDGE_TOP_RIGHT = MID_ROW_RIGHT;

    private SolveState solveState = SolveState.None;

    private int mTopColor = 0;
    private int mBottomColor = 0;

    public RubiksCube3x3x3() {
        super(SIZE);
    }

    protected void ut() {
        mState = CubeState.TESTING;
        ut_test();
    }

    private void ut_test() {
        Algorithm algorithm = new Algorithm();
        algorithm.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, 0, SIZE);
        setAlgo(algorithm);
    }

    private void ut_final_steps() {
        Algorithm algorithm = new Algorithm();
        algorithm.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, 0, SIZE);
        algorithm.repeatLastStep();
        algorithm.append(theFinalAlgorithm());
        algorithm.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, 0, SIZE);
        algorithm.repeatLastStep();
        setAlgo(algorithm);
    }

    private void ut_topcorner_pos() {
        Algorithm algorithm = new Algorithm();
        algorithm.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, 0, SIZE);
        algorithm.repeatLastStep();
        algorithm.append(lastFaceCornerPositionAlgo(Direction.COUNTER_CLOCKWISE));
        algorithm.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, 0, SIZE);
        algorithm.repeatLastStep();
        setAlgo(algorithm);
    }

    private void ut_topcross() {
        Algorithm algorithm = new Algorithm();
        algorithm.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, 0, SIZE);
        algorithm.repeatLastStep();
        algorithm.append(lastFaceCrossAlignAlgo(Direction.COUNTER_CLOCKWISE));
        algorithm.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, 0, SIZE);
        algorithm.append(lastFaceCrossAlignAlgo(Direction.COUNTER_CLOCKWISE));
        algorithm.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, 0, SIZE);
        algorithm.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, 0, SIZE);
        algorithm.repeatLastStep();
        setAlgo(algorithm);
    }

    private void ut_middle() {
        Algorithm algorithm = new Algorithm();
        algorithm.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, 0, SIZE);
        algorithm.repeatLastStep();
        algorithm.append(fixMiddleLayerFromFrontFace());
        algorithm.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algorithm.repeatLastStep();
        algorithm.append(fixMiddleLayerFromFrontFace());
        algorithm.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, 0, SIZE);
        algorithm.repeatLastStep();
        setAlgo(algorithm);
    }

    private void ut_ffcorner_top() {
        Algorithm algorithm = new Algorithm();
        algorithm.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algorithm.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, INNER);
        algorithm.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
        algorithm.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
        algorithm.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algorithm.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, INNER);
        algorithm.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
        algorithm.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, INNER, SIZE);
        setAlgo(algorithm);
    }

    private void ut_ffcorner_proper() {
        Algorithm algorithm = new Algorithm();
        algorithm.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algorithm.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, INNER);
        algorithm.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
        algorithm.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
        setAlgo(algorithm);
    }

    private void ut_ffcorner_bottom() {
        Algorithm algorithm = new Algorithm();
        algorithm.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algorithm.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, INNER);
        algorithm.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
        algorithm.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
        algorithm.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algorithm.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
        algorithm.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
        algorithm.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
        algorithm.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
        setAlgo(algorithm);
    }


    @Override
    public int solve() {
        if (mState == CubeState.TESTING) {
            sendMessage("wait please");
            return -1;
        }
        if (mState != CubeState.IDLE) {
            sendMessage("Invalid state to solve: " + mState);
            return -1;
        }
        clearUndoStack();
        mState = CubeState.SOLVING;
        startSolving();
        return 0;
    }

    @Override
    public int cancelSolving() {
        solveState = SolveState.None;
        return super.cancelSolving();
    }

    /**
     * TODO: scope for lot of optimizations.
     * 1. Check for already solved faces with proper T (possibly attainable with a rotation)
     * 2. If there are no T, see if there are any top face crosses
     * 3. If first face cross is not solved, start with a face that is closer to the cross
     * 4. On the selected face, start with the already solved side (rotate if needed).
     * */
    @Override
    protected void startSolving() {
        super.startSolving();
        solveState = SolveState.FirstFaceCross;
        mTopColor = mTopSquares.get(CENTER).getColor();
        mBottomColor = mBottomSquares.get(CENTER).getColor();
        sendMessage("Top is " + mTopSquares.get(CENTER).colorName() +
                " and bottom is " + mBottomSquares.get(CENTER).colorName());
        firstFaceCross();
    }

    private void firstFaceCross() {
        ArrayList<Square>[] sideFaces = new ArrayList[]{
                mBackSquares, mLeftSquares, mRightSquares, mFrontSquares
        };

        // TODO: Handle already aligned pieces here
        for (int i = EDGE_TOP_NEAR; i > 0; i--) {
            if (i % 2 == 0) {
                continue;
            }
            ArrayList<Square> sideFace = sideFaces[i / 2];
            if (mTopSquares.get(i).getColor() == mTopColor &&
                    sideFace.get(FIRST_ROW_CENTER).getColor() == sideFace.get(CENTER).getColor()) {
                continue;
            }

            // If the other color in the missing edge is not the front-color, rotate the cube
            // until it becomes so.
            if (i != EDGE_TOP_NEAR) {
                Direction dir = (i == EDGE_TOP_LEFT) ?
                        Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
                Algorithm algo = Algorithm.rotateWhole(Axis.Y_AXIS, dir, SIZE,
                        i == EDGE_TOP_FAR ? 2 : 1);
                setAlgo(algo);
            } else {
                fixFirstFaceEdge(mTopColor, sideFace.get(CENTER).getColor());
            }
            return;
        }

        sendMessage("Top cross is done, cutting corners now");
        proceedToNextState();
    }

    private void fixFirstFaceEdge(int topColor, int sideColor) {
        int[] colors = new int[]{topColor, sideColor};
        int row = 0, pos = -1;
        for (row = 0; row < SIZE; row++) {
            pos = findPieceOnFace(mYaxisLayers.get(row), colors);
            if (pos >= 0) {
                break;
            }
        }

        Log.w(tag, "Found " +
                topColor + '-' +
                sideColor + " at " + row + "-" + pos);

        // White on bottom face
        if (row == INNER && mBottomSquares.get(pos).getColor() == topColor) {
            firstFaceEdge_fromBottomFace(pos);
        } else if (row == INNER) {
            firstFaceEdge_fromLowerLayer(pos);
        } else if (row == MIDDLE) {
            firstFaceEdge_fromMiddleLayer(pos);
        } else {
            firstFaceEdge_fromTopLayer(pos);
        }
    }

    private void firstFaceEdge_fromTopLayer(final int pos) {
        Log.d(tag, "Edge piece from top layer");
        Algorithm algo = new Algorithm();
        ArrayList<Rotation> middleRotations;
        Rotation rot = null;
        Square topColoredSquare = getSquareByColor(mYaxisLayers, OUTER, pos, mTopColor);


        if (pos == EDGE_TOP_FAR || pos == EDGE_TOP_NEAR) {
            int faceIndex = topColoredSquare.getFace() == FACE_TOP ?
                    FACE_RIGHT : topColoredSquare.getFace();
            rot = new Rotation(Axis.Z_AXIS,
                    Direction.CLOCKWISE,
                    pos == EDGE_TOP_FAR ? INNER : OUTER);
            algo.addStep(rot);
            middleRotations = middleEdgeToTopEdge(
                    pos == EDGE_TOP_FAR ? EDGE_MIDDLE_RIGHT_BACK : EDGE_MIDDLE_FRONT_RIGHT,
                    faceIndex);
        } else {
            int faceIndex = topColoredSquare.getFace() == FACE_TOP ?
                    FACE_FRONT : topColoredSquare.getFace();
            rot = new Rotation(Axis.X_AXIS,
                    Direction.COUNTER_CLOCKWISE,
                    pos == EDGE_TOP_LEFT ? INNER : OUTER);
            algo.addStep(rot);
            middleRotations = middleEdgeToTopEdge(pos == EDGE_TOP_LEFT ?
                    EDGE_MIDDLE_FRONT_LEFT : EDGE_MIDDLE_FRONT_RIGHT, faceIndex);
        }

        for (int i = 0; i < middleRotations.size(); i++) {
            algo.addStep(middleRotations.get(i));
        }

        setAlgo(algo);
    }

    private static ArrayList<Rotation> middleEdgeToTopEdge(int middlePos, int faceWithTopColor) {
        ArrayList<Rotation> rotations = new ArrayList<>();

        switch (middlePos) {
            case EDGE_MIDDLE_FRONT_LEFT:
                if (!(faceWithTopColor == FACE_FRONT || faceWithTopColor == FACE_LEFT))
                    throw new AssertionError();
                if (faceWithTopColor == FACE_FRONT) {
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.X_AXIS, Direction.CLOCKWISE, INNER));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                } else {
                    rotations.add(new Rotation(Axis.Z_AXIS, Direction.CLOCKWISE, OUTER));
                }
                break;

            case EDGE_MIDDLE_FRONT_RIGHT:
                if (!(faceWithTopColor == FACE_FRONT || faceWithTopColor == FACE_RIGHT))
                    throw new AssertionError();
                if (faceWithTopColor == FACE_FRONT) {
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.X_AXIS, Direction.CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER));
                } else {
                    rotations.add(new Rotation(Axis.Z_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                }
                break;

            case EDGE_MIDDLE_RIGHT_BACK:
                if (!(faceWithTopColor == FACE_RIGHT || faceWithTopColor == FACE_BACK))
                    throw new AssertionError();
                if (faceWithTopColor == FACE_BACK) {
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER));
                } else {
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.Z_AXIS, Direction.COUNTER_CLOCKWISE, INNER));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                }
                break;

            case EDGE_MIDDLE_LEFT_BACK:
                if (!(faceWithTopColor == FACE_LEFT || faceWithTopColor == FACE_BACK))
                    throw new AssertionError();
                if (faceWithTopColor == FACE_BACK) {
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, INNER));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                } else {
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.Z_AXIS, Direction.CLOCKWISE, INNER));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                    rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
                }
                break;
        }
        return rotations;
    }

    private Square getSquareByColor(ArrayList<ArrayList<Piece>> faceList,
                                    int index, int pos, int color) {

        Piece piece = faceList.get(index).get(pos);
        for (Square sq : piece.mSquares) {
            if (sq.getColor() == color) {
                return sq;
            }
        }
        throw new InvalidParameterException("Square not found: Index " + index +
                ", pos " + pos + ", color " + color);
    }

    private void firstFaceEdge_fromMiddleLayer(int pos) {
        Log.d(tag, "Edge piece from middle layer");
        Square topColorSquare = getSquareByColor(mYaxisLayers, MIDDLE, pos, mTopColor);
        int faceIndex = topColorSquare.getFace();

        ArrayList<Rotation> rotations = middleEdgeToTopEdge(pos, faceIndex);
        Algorithm algo = new Algorithm();
        for (int i = 0; i < rotations.size(); i++) {
            algo.addStep(rotations.get(i));
        }
        setAlgo(algo);
    }

    /**
     * pos: position of desired piece in bottom face.
     * The piece should have non-white on bottom face
     */
    private void firstFaceEdge_fromLowerLayer(int pos) {
        Log.d(tag, "Edge piece from lower layer");
        Algorithm algorithm = new Algorithm();

        // the white should be on one of the sides, not front or back
        if (pos == EDGE_BOTTOM_NEAR || pos == EDGE_BOTTOM_FAR) {
            algorithm.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, INNER);
        }

        if (pos <= EDGE_BOTTOM_LEFT) {
            algorithm.addStep(Axis.X_AXIS, Direction.CLOCKWISE, INNER);
            algorithm.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, OUTER);
            if (mTopSquares.get(EDGE_TOP_LEFT).getColor() == mTopColor &&
                    mLeftSquares.get(FIRST_ROW_CENTER).getColor() ==
                            mLeftSquares.get(CENTER).getColor()) {
                algorithm.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
            }
        } else {
            algorithm.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
            algorithm.addStep(Axis.Z_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            if (mTopSquares.get(EDGE_TOP_RIGHT).getColor() == mTopColor &&
                    mRightSquares.get(FIRST_ROW_CENTER).getColor() ==
                            mRightSquares.get(CENTER).getColor()) {

                algorithm.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            }
        }
        setAlgo(algorithm);
    }

    /**
     * pos: position of desired piece in bottom face.
     * The piece should have white on bottom face
     */
    private void firstFaceEdge_fromBottomFace(int pos) {
        Algorithm algo = new Algorithm();
        Log.d(tag, "Edge piece from bottom face");

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

    private boolean isCornerAligned(Piece piece) {
        if (piece.mSquares.size() != 3) throw new AssertionError();
        for (Square sq : piece.mSquares) {
            if (sq.getColor() != mAllFaces[sq.getFace()].get(CENTER).getColor()) {
                return false;
            }
        }
        return true;
    }

    private void firstFaceCorners() {
        int[] corners = new int[]{
                LAST_ROW_RIGHT, LAST_ROW_LEFT, FIRST_ROW_LEFT, FIRST_ROW_RIGHT
        };
        /**
         * Look for any corners in the lower layer with white facing sideways (not bottom).
         * */
        for (int i = 0; i < corners.length; i++) {
            Piece cornerPiece = mYaxisLayers.get(INNER).get(corners[i]);
            Square topColoredSquare = cornerPiece.getSquare(mTopColor);
            if (topColoredSquare == null) continue;
            if (topColoredSquare.getFace() == FACE_BOTTOM) continue;
            Log.d(tag, "Found " + cornerPiece + " at " + corners[i]);
            firstFaceCorner(corners[i]);
            return;
        }

        // No whites in the lower layer. Bring up whites from bottom face

        for (int i = 0; i < corners.length; i++) {
            Piece cornerPiece = mYaxisLayers.get(INNER).get(corners[i]);
            Square topColoredSquare = cornerPiece.getSquare(mTopColor);
            if (topColoredSquare == null) continue;
            if (topColoredSquare.getFace() != FACE_BOTTOM) {
                throw new AssertionError("white faces " +
                        topColoredSquare.getFace() + " at " + corners[i]);
            }
            Log.d(tag, "White faces down in " + cornerPiece + " at " + corners[i]);
            firstFaceCornerWhiteOnBottom(corners[i]);
            return;
        }

        // Look for whites in top layer
        for (int i = 0; i < corners.length; i++) {
            Piece cornerPiece = mYaxisLayers.get(OUTER).get(corners[i]);
            if (isCornerAligned(cornerPiece)) {
                continue;
            }
            Log.d(tag, "unaligned at top row " + cornerPiece + " at " + corners[i]);
            firstFaceCornerFromTopLayer(corners[i]);
            return;
        }

        sendMessage("We have a perfect first layer..!");
        proceedToNextState();
    }

    private static int corner2index(int face, int corner) {
        if (face == FACE_BOTTOM) {
            switch (corner) {
                case FIRST_ROW_RIGHT:
                    return CORNER_INDEX_FRONT_RIGHT;
                case LAST_ROW_RIGHT:
                    return CORNER_INDEX_RIGHT_BACK;
                case LAST_ROW_LEFT:
                    return CORNER_INDEX_BACK_LEFT;
                case FIRST_ROW_LEFT:
                    return CORNER_INDEX_LEFT_FRONT;
                default:
                    throw new InvalidParameterException("Invalid corner " + corner);
            }
        } else if (face == FACE_TOP) {
            switch (corner) {
                case FIRST_ROW_LEFT:
                    return CORNER_INDEX_BACK_LEFT;
                case FIRST_ROW_RIGHT:
                    return CORNER_INDEX_RIGHT_BACK;
                case LAST_ROW_LEFT:
                    return CORNER_INDEX_LEFT_FRONT;
                case LAST_ROW_RIGHT:
                    return CORNER_INDEX_FRONT_RIGHT;
                default:
                    throw new InvalidParameterException("Invalid corner " + corner);
            }
        } else {
            throw new InvalidParameterException("not implemented for " + face);
        }
    }

    private void firstFaceCornerFromTopLayer(int corner) {
        Algorithm algorithm = new Algorithm();
        Piece piece = mYaxisLayers.get(OUTER).get(corner);
        if (piece.getType() != Piece.PieceType.CORNER) throw new AssertionError();
        final int topColor = mTopSquares.get(CENTER).getColor();
        int topColorFace = -1;
        for (Square sq : piece.mSquares) {
            if (sq.getColor() == topColor) {
                topColorFace = sq.getFace();
            }
        }

        int desiredCornerIndex = CORNER_INDEX_FRONT_RIGHT;
        int currentCornerIndex = corner2index(FACE_TOP, corner);

        if (desiredCornerIndex != currentCornerIndex) {
            /**
             * Bring the desired corner to front-right. Make sure that orientation of white
             * is updated to reflect this
             * */
            Direction direction = (currentCornerIndex == CORNER_INDEX_LEFT_FRONT) ?
                    Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
            algorithm.addStep(Axis.Y_AXIS, direction, 0, SIZE);
            if (topColorFace != FACE_TOP) {
                topColorFace += (direction == Direction.COUNTER_CLOCKWISE) ? 1 : -1;
            }
            if (currentCornerIndex == CORNER_INDEX_BACK_LEFT) {
                algorithm.repeatLastStep();
                if (topColorFace != FACE_TOP) {
                    topColorFace += (direction == Direction.COUNTER_CLOCKWISE) ? 1 : -1;
                }
            }
        }

        topColorFace = (topColorFace + CUBE_SIDES) % CUBE_SIDES;

        if (topColorFace == FACE_FRONT || topColorFace == FACE_TOP) {
            algorithm.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, OUTER);
            algorithm.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
            algorithm.addStep(Axis.Z_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        } else if (topColorFace == FACE_RIGHT) {
            algorithm.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            algorithm.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, INNER);
            algorithm.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
        } else {
            throw new AssertionError("white should not be facing " + topColorFace);
        }

        setAlgo(algorithm);
    }

    private void firstFaceCornerWhiteOnBottom(int corner) {
        Algorithm algorithm = new Algorithm();
        Direction direction;
        Piece piece = mYaxisLayers.get(INNER).get(corner);
        if (piece.getType() != Piece.PieceType.CORNER) throw new AssertionError();
        final int topColor = mTopSquares.get(CENTER).getColor();
        int sideColor1 = -1;
        int sideColor2 = -1;
        for (Square sq : piece.mSquares) {
            if (sq.getColor() == topColor) {
                if (sq.getFace() != FACE_BOTTOM) throw new AssertionError();
                continue;
            }
            if (sideColor1 == -1) {
                sideColor1 = sq.getColor();
            } else if (sideColor2 == -1) {
                sideColor2 = sq.getColor();
            }
        }

        int face1 = getColorFace(sideColor1);
        int face2 = getColorFace(sideColor2);
        int desiredCorner = FIRST_ROW_LEFT;

        if (face1 == FACE_BACK || face2 == FACE_BACK) {
            desiredCorner = LAST_ROW_LEFT;
        }

        if (face1 == FACE_RIGHT || face2 == FACE_RIGHT) {
            desiredCorner += 2;
        }

        int currentCornerIndex = corner2index(FACE_BOTTOM, corner);
        int desiredCornerIndex = corner2index(FACE_BOTTOM, desiredCorner);
        int delta = Math.abs(currentCornerIndex - desiredCornerIndex);

        if (desiredCornerIndex != CORNER_INDEX_FRONT_RIGHT) {
            // Bring the desired corner to front-right
            direction = (desiredCorner == FIRST_ROW_LEFT) ?
                    Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
            algorithm.addStep(Axis.Y_AXIS, direction, 0, SIZE);
            if (desiredCorner == LAST_ROW_LEFT) {
                algorithm.addStep(Axis.Y_AXIS, direction, 0, SIZE);
            }
        }

        // Rotate lower layer to bring the piece to front-right
        direction = desiredCornerIndex < currentCornerIndex ?
                Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
        if (delta == 3) {
            delta = 1;
            direction = direction == Direction.CLOCKWISE ?
                    Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
        }
        for (int i = 0; i < delta; i++) {
            algorithm.addStep(Axis.Y_AXIS, direction, INNER);
        }

        algorithm.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algorithm.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, INNER);
        algorithm.repeatLastStep();
        algorithm.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
        setAlgo(algorithm);
    }

    private void firstFaceCorner(int corner) {
        Piece piece = mYaxisLayers.get(INNER).get(corner);
        if (piece.getType() != Piece.PieceType.CORNER) throw new AssertionError();
        int topColor = mTopSquares.get(CENTER).getColor();
        int topColorFace = -1;
        int sideColor = -1;
        int bottomColor = -1;
        int sideFace = -1;
        for (Square sq : piece.mSquares) {
            if (sq.getColor() == topColor) {
                topColorFace = sq.getFace();
                if (topColorFace == FACE_BOTTOM) throw new AssertionError();
                continue;
            }
            if (sq.getFace() == FACE_BOTTOM) {
                bottomColor = sq.getColor();
            } else {
                sideColor = sq.getColor();
                sideFace = sq.getFace();
            }
        }
        int sideColorCenterFace = getColorFace(sideColor);
        if (sideColorCenterFace > FACE_LEFT) throw new AssertionError();
        ArrayList<Rotation> rotations = bringColorToFront(sideColor);

        int count = Math.abs(sideColorCenterFace - sideFace);
        Direction direction;
        direction = sideColorCenterFace > sideFace ?
                Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;

        if (count == 3) {
            count = 1;
            direction = direction == Direction.CLOCKWISE ?
                    Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
        }

        for (int i = 0; i < count; i++) {
            rotations.add(new Rotation(Axis.Y_AXIS, direction, INNER));
        }

        topColorFace -= sideFace;
        topColorFace = (topColorFace + CUBE_SIDES) % CUBE_SIDES;

        if (topColorFace == FACE_RIGHT) {
            rotations.add(new Rotation(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER));
            rotations.add(new Rotation(Axis.Y_AXIS, Direction.CLOCKWISE, INNER));
            rotations.add(new Rotation(Axis.X_AXIS, Direction.CLOCKWISE, OUTER));
        } else if (topColorFace == FACE_LEFT) {
            rotations.add(new Rotation(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, INNER));
            rotations.add(new Rotation(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, INNER));
            rotations.add(new Rotation(Axis.X_AXIS, Direction.CLOCKWISE, INNER));
        } else {
            throw new AssertionError("topColorFace should be left or right, not: " + topColorFace);
        }

        Algorithm algorithm = new Algorithm(rotations);
        setAlgo(algorithm);
    }

    private int getColorFace(int color) {
        for (int i = 0; i < FACE_COUNT; i++) {
            if (mAllFaces[i].get(CENTER).getColor() == color) {
                return i;
            }
        }
        throw new InvalidParameterException("Color not found: " + color);
    }

    private int findPieceOnFace(ArrayList<Piece> face, int[] colors) {
        Arrays.sort(colors);
        for (int i = 0; i < face.size(); i++) {
            Piece piece = face.get(i);
            if (piece.mSquares.size() != colors.length)
                continue;
            int[] pieceColors = new int[piece.mSquares.size()];
            for (int j = 0; j < pieceColors.length; j++) {
                pieceColors[j] = piece.mSquares.get(j).getColor();
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
    protected void updateAlgo() {
        super.updateAlgo();

        if (mState != CubeState.SOLVING)
            return;

        switch (solveState) {
            case FirstFaceCross:
                firstFaceCross();
                break;

            case FirstFaceCorners:
                firstFaceCorners();
                break;

            case MiddleLayer:
                middleLayer();
                break;

            case LastFaceCross:
                lastFaceCross();
                break;

            case LastFaceCrossAlign:
                lastFaceCrossAlign();
                break;

            case LastFaceCorners:
                lastFaceCorners();
                break;

            case LastFaceCornerAlign:
                lastFaceCornerAlign();
                break;

            default:
                mState = CubeState.IDLE;
                Log.w(tag, "STATE: " + solveState);
                sendMessage("Something is fishy: check the code");
                break;
        }
    }

    private ArrayList<Rotation> bringColorToFront(int color) {
        ArrayList<Rotation> rotations = new ArrayList<>();
        if (color == mFrontSquares.get(CENTER).getColor()) {
            return rotations;
        }
        Axis axis = Axis.Y_AXIS;
        Direction dir = Direction.CLOCKWISE;
        if (color == mTopSquares.get(CENTER).getColor()) {
            axis = Axis.X_AXIS;
            dir = Direction.COUNTER_CLOCKWISE;
        } else if (color == mBottomSquares.get(CENTER).getColor()) {
            axis = Axis.X_AXIS;
        } else if (color == mLeftSquares.get(CENTER).getColor()) {
            dir = Direction.COUNTER_CLOCKWISE;
        }
        rotations.add(new Rotation(axis, dir, 0, SIZE));
        if (color == mBackSquares.get(CENTER).getColor()) {
            rotations.add(new Rotation(axis, dir, 0, SIZE));
        }
        return rotations;
    }

    private void middleLayer() {
        int[] edges = new int[]{
                LAST_ROW_MIDDLE, MID_ROW_RIGHT, FIRST_ROW_CENTER, MID_ROW_LEFT
        };
        for (int i = 0; i < edges.length; i++) {
            Piece piece = mYaxisLayers.get(OUTER).get(edges[i]);
            if (piece.hasColor(mBottomColor)) continue;
            Log.d(tag, "Found Edge " + piece + " at " + edges[i]);
            fixMiddleLayer(edges[i]);
            return;
        }

        // search for misaligned middle pieces
        edges = new int[]{
                EDGE_MIDDLE_FRONT_LEFT, EDGE_MIDDLE_FRONT_RIGHT,
                EDGE_MIDDLE_RIGHT_BACK, EDGE_MIDDLE_LEFT_BACK
        };

        for (int i = 0; i < edges.length; i++) {
            Piece piece = mYaxisLayers.get(MIDDLE).get(edges[i]);
            if (isEdgeAligned(piece)) {
                continue;
            }
            Log.d(tag, "bring to top " + piece);
            bringUpUnalignedMiddleEdge(edges[i]);
            return;
        }

        sendMessage("Fixed middle layer..!");
        proceedToNextState();
    }

    private void bringUpUnalignedMiddleEdge(int edgeIndex) {
        Algorithm algo = new Algorithm();
        if (edgeIndex != EDGE_MIDDLE_FRONT_RIGHT) {
            int count = 1;
            Direction direction = Direction.CLOCKWISE;
            if (edgeIndex == EDGE_MIDDLE_FRONT_LEFT) {
                direction = Direction.COUNTER_CLOCKWISE;
            }
            if (edgeIndex == EDGE_MIDDLE_LEFT_BACK) {
                count++;
            }
            for (int i = 0; i < count; i++) {
                algo.addStep(Axis.Y_AXIS, direction, 0, SIZE);
            }
        }
        algo.append(fixMiddleLayerFromFrontFace());
        setAlgo(algo);
    }

    private Algorithm alignMiddlePiece(int startFace, int destFace) {
        if (!(startFace >= 0 && startFace < 4 && destFace >= 0 && destFace < 4))
            throw new AssertionError();
        int delta = Math.abs(startFace - destFace);
        if (delta == 0) {
            return null;
        }

        Direction dir = startFace > destFace ? Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE;
        if (delta == 3) {
            delta = 1;
            dir = dir == Direction.CLOCKWISE ? Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
        }

        Algorithm algo = new Algorithm();
        for (int i = 0; i < delta; i++) {
            algo.addStep(Axis.Y_AXIS, dir, OUTER);
        }

        return algo;
    }

    private void fixMiddleLayer(int edge) {
        Piece piece = mYaxisLayers.get(OUTER).get(edge);
        Algorithm alignPiece = null;
        Direction direction;
        Algorithm algo = new Algorithm();
        if (piece.getType() != Piece.PieceType.EDGE) throw new AssertionError();
        int color1 = -1;
        int color2 = -1;
        int outerColor = -1;
        int outerFace = -1;
        for (Square sq : piece.mSquares) {
            if (sq.getColor() == mBottomColor) {
                throw new InvalidParameterException("Yellow shouldn't be there");
            }
            if (sq.getFace() != FACE_TOP) {
                outerColor = sq.getColor();
                outerFace = sq.getFace();
            }
            if (color1 == -1) {
                color1 = sq.getColor();
            } else if (color2 == -1) {
                color2 = sq.getColor();
            }
        }

        // TODO: Do this in the same cycle and avoid redundant moves
        if (outerFace == FACE_RIGHT && mRightSquares.get(CENTER).getColor() == outerColor) {
            alignPiece = fixMiddleLayerFromRightFace();
            setAlgo(alignPiece);
            return;
        } else if (outerFace == FACE_FRONT && mFrontSquares.get(CENTER).getColor() == outerColor) {
            alignPiece = fixMiddleLayerFromFrontFace();
            setAlgo(alignPiece);
            return;
        }

        int face1 = getColorFace(color1);
        int face2 = getColorFace(color2);
        int face_delta = 0;

        if (color1 == outerColor) {
            alignPiece = alignMiddlePiece(outerFace, face1);
            face_delta = face1 - outerFace;
        } else {
            alignPiece = alignMiddlePiece(outerFace, face2);
            face_delta = face2 - outerFace;
        }

        int currentCorner = FIRST_ROW_LEFT;
        if (face1 == FACE_FRONT || face2 == FACE_FRONT) {
            currentCorner = LAST_ROW_LEFT;
        }
        if (face1 == FACE_RIGHT || face2 == FACE_RIGHT) {
            currentCorner += 2;
        }
        int currentCornerIndex = corner2index(FACE_TOP, currentCorner);
        int desiredCornerIndex = CORNER_INDEX_FRONT_RIGHT;

        if (currentCornerIndex != desiredCornerIndex) {
            direction = currentCornerIndex == CORNER_INDEX_LEFT_FRONT ?
                    Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
            outerFace += direction == Direction.COUNTER_CLOCKWISE ? 1 : -1;
            algo.addStep(Axis.Y_AXIS, direction, 0, SIZE);
            if (currentCornerIndex == CORNER_INDEX_BACK_LEFT) {
                algo.repeatLastStep();
                outerFace += direction == Direction.COUNTER_CLOCKWISE ? 1 : -1;
            }
            outerFace = (outerFace + CUBE_SIDES) % CUBE_SIDES;
        }

        algo.append(alignPiece);
        setAlgo(algo);
    }

    private Algorithm fixMiddleLayerFromFrontFace() {
        Algorithm algo = new Algorithm();
        algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
        algo.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
        algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algo.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algo.addStep(Axis.Z_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
        algo.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, OUTER);
        return algo;
    }

    private Algorithm fixMiddleLayerFromRightFace() {
        Algorithm algo = new Algorithm();
        algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algo.addStep(Axis.Z_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
        algo.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, OUTER);
        algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
        algo.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
        algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algo.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        return algo;
    }

    private boolean isEdgeAligned(Piece piece) {
        if (piece.getType() != Piece.PieceType.EDGE) throw new AssertionError();
        for (Square sq : piece.mSquares) {
            if (sq.getColor() != mAllFaces[sq.getFace()].get(CENTER).getColor()) {
                return false;
            }
        }
        return true;
    }

    private void lastFaceCross() {
        int lastFaceColor = mTopSquares.get(CENTER).getColor();
        int[] edges = new int[]{
                EDGE_TOP_NEAR, EDGE_TOP_RIGHT, EDGE_TOP_FAR, EDGE_TOP_LEFT
        };
        int[] colors = new int[CUBE_SIDES];
        int yellowCount = 0;

        int front = mTopSquares.get(LAST_ROW_MIDDLE).getColor();
        int right = mTopSquares.get(MID_ROW_RIGHT).getColor();
        int back = mTopSquares.get(FIRST_ROW_CENTER).getColor();
        int left = mTopSquares.get(MID_ROW_LEFT).getColor();
        Algorithm algo = new Algorithm();

        for (int i = 0; i < edges.length; i++) {
            Square sq = mTopSquares.get(edges[i]);
            colors[i] = sq.getColor();
            if (colors[i] == lastFaceColor) {
                yellowCount++;
            }
        }

        if (yellowCount == CUBE_SIDES) {
            sendMessage("top cross is in place..!");
            proceedToNextState();
            return;
        }

        if (yellowCount != 2) {
            algo.append(lastFaceCrossAlgo(1));
            setAlgo(algo);
            return;
        }

        /**
         * If it is a line, apply the algo once
         * */
        if (colors[FACE_FRONT] == colors[FACE_BACK] ||
                colors[FACE_LEFT] == colors[FACE_RIGHT]) {
            // if the line is not horizontal, make it so
            if (colors[FACE_FRONT] == colors[FACE_BACK]) {
                algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, 0, SIZE);
            }
            algo.append(lastFaceCrossAlgo(1));
            setAlgo(algo);
            return;
        }

        int count = 0;
        Direction direction = Direction.CLOCKWISE;

        if (colors[FACE_FRONT] == lastFaceColor) {
            if (colors[FACE_RIGHT] == lastFaceColor) {
                count = 2;
            } else if (colors[FACE_LEFT] == lastFaceColor) {
                count = 1;
            } else {
                throw new AssertionError();
            }
        } else if (colors[FACE_BACK] == lastFaceColor) {
            if (colors[FACE_RIGHT] == lastFaceColor) {
                count = 1;
                direction = Direction.COUNTER_CLOCKWISE;
            }
        }

        for (int i = 0; i < count; i++) {
            algo.addStep(Axis.Y_AXIS, direction, OUTER);
        }

        algo.append(lastFaceCrossAlgo(2));
        setAlgo(algo);
    }

    private Algorithm lastFaceCrossAlgo(int count) {
        if (count < 0 || count > 2) {
            throw new InvalidParameterException("Invalid count: " + count);
        }
        Algorithm algo = new Algorithm();
        algo.addStep(Axis.Z_AXIS, Direction.CLOCKWISE, OUTER);
        for (int i = 0; i < count; i++) {
            algo.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
            algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
            algo.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        }
        algo.addStep(Axis.Z_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        return algo;
    }

    private Algorithm lastFaceCrossAlignAlgo(Direction direction) {
        Algorithm algo = new Algorithm();
        if (direction == Direction.CLOCKWISE) {
            algo.addStep(Axis.X_AXIS, Direction.CLOCKWISE, INNER);
            algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            algo.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
            algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            algo.addStep(Axis.X_AXIS, Direction.CLOCKWISE, INNER);
            algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            algo.repeatLastStep();
            algo.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
        } else {
            algo.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
            algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
            algo.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
            algo.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
            algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            algo.repeatLastStep();
            algo.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        }
        return algo;
    }

    private void lastFaceCrossAlign() {

        /**
         * Find the difference between the actual position and desired position of each colors
         * */
        int[] offsets = new int[]{0, 0, 0, 0};
        int[] edges = new int[]{
                LAST_ROW_MIDDLE, MID_ROW_RIGHT, FIRST_ROW_CENTER, MID_ROW_LEFT
        };
        String dbg = "offsets:";
        for (int i = 0; i < edges.length; i++) {
            Piece piece = mYaxisLayers.get(OUTER).get(edges[i]);
            Square sideSquare = null;
            for (Square sq: piece.mSquares) {
                if (sq.getFace() == FACE_TOP) {
                    continue;
                }
                sideSquare = sq;
                break;
            }
            if (sideSquare == null) {
                throw new AssertionError("side square null at " + i + " for piece: " + piece);
            }
            int face = getColorFace(sideSquare.getColor());
            if (face == FACE_TOP || face == FACE_BOTTOM) {
                throw new AssertionError("color and face mismatch: " +
                        sideSquare.getColor() + ", face: " + face);
            }
            offsets[i] = face - i;
            offsets[i] = (offsets[i] + CUBE_SIDES) % CUBE_SIDES;
            dbg += " " + offsets[i];
        }
        Log.w(tag, dbg);

        /**
         * If all offsets are equal, we just need to rotate the top layer to align the centers.
         * If at least one of them is different, we should align it and apply algorithms
         * */
        for (int i = 0; i < offsets.length - 1; i++) {
            if (offsets[i] != offsets[i + 1]) {
                fixLastFaceCrossAlignment(offsets);
                return;
            }
        }

        if (offsets[0] != 0) {
            Algorithm algo = new Algorithm();
            algo.addStep(Axis.Y_AXIS, (offsets[0] == 3) ?
                    Direction.CLOCKWISE : Direction.COUNTER_CLOCKWISE, OUTER);
            if (Math.abs(offsets[0]) == 2) {
                algo.repeatLastStep();
            }
            setAlgo(algo);
        } else {
            // Offsets are all zero; this is the best case scenario
            sendMessage("top cross is now aligned");
            proceedToNextState();
        }
    }

    private void fixLastFaceCrossAlignment(int[] offsets) {
        Algorithm algorithm = new Algorithm();
        Direction direction;
        int alignedCount = 0;
        int firstAlignedIndex = -1;
        for (int i = 0; i < offsets.length; i++) {
            if (offsets[i] == 0) {
                alignedCount++;
                if (firstAlignedIndex == -1)
                    firstAlignedIndex = i;
            }
        }

        Log.w(tag, "Aligned count " + alignedCount);

        /**
         * If nothing is aligned, rotate the top once and try again.
         * */
        if (alignedCount == 0) {
            algorithm.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
            setAlgo(algorithm);
            return;
        }

        /**
         * Two squares are aligned.
         *
         * case 1: They are adjacent:
         *   The other two are in swapped state now.
         *   Rotate the cube once to misalign the currently aligned one and align
         *   one of the other two
         * case 2: They are opposite
         *   Apply algo and hope for the best
         * TODO: Last face cross align: handle opposite-squares-aligned case efficiently
         * */
        if (alignedCount == 2) {
            if (offsets[(firstAlignedIndex + 1) % CUBE_SIDES] == 0 ||
                    offsets[(firstAlignedIndex - 1 + CUBE_SIDES) % CUBE_SIDES] == 0) {
                // case: 1
                algorithm.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
                setAlgo(algorithm);
            } else {
                // case: 2
                algorithm.append(lastFaceCrossAlignAlgo(Direction.CLOCKWISE));
                setAlgo(algorithm);
            }
            return;
        }

        /**
         * Only one of them is aligned. Turn the cube to bring that color to front and apply
         * appropriate algorithm
         * */
        if (alignedCount == 1) {
            int color = mAllFaces[firstAlignedIndex].get(CENTER).getColor();
            if (mAllFaces[firstAlignedIndex].get(FIRST_ROW_CENTER).getColor() != color) {
                throw new AssertionError("color mismatch at " + firstAlignedIndex + ": " +
                        color + " - " +
                        mAllFaces[firstAlignedIndex].get(FIRST_ROW_CENTER).getColor());
            }
            if (firstAlignedIndex != FACE_FRONT) {
                direction = firstAlignedIndex == FACE_LEFT ? Direction.COUNTER_CLOCKWISE :
                    Direction.CLOCKWISE;
                algorithm.addStep(Axis.Y_AXIS, direction, 0, SIZE);
                if (firstAlignedIndex == FACE_BACK) {
                    algorithm.repeatLastStep();
                }
            }
            if (offsets[(firstAlignedIndex + 1) % CUBE_SIDES] == 1) {
                algorithm.append(lastFaceCrossAlignAlgo(Direction.COUNTER_CLOCKWISE));
            } else {
                algorithm.append(lastFaceCrossAlignAlgo(Direction.CLOCKWISE));
            }
            setAlgo(algorithm);
        }
    }

    private void lastFaceCorners() {
        int[] corners = new int[]{
                LAST_ROW_RIGHT, FIRST_ROW_RIGHT, FIRST_ROW_LEFT, LAST_ROW_LEFT
        };
        int positionedCorners = 0;
        int firstPositionedCorner = -1;
        for (int i = 0; i < corners.length; i++) {
            Piece piece = mYaxisLayers.get(OUTER).get(corners[i]);
            if (isCornerPositioned(piece)) {
                positionedCorners++;
                if (firstPositionedCorner == -1) {
                    firstPositionedCorner = corner2index(FACE_TOP, corners[i]);
                }
            }
        }

        Log.w(tag, "positioned corners " + positionedCorners + " first " + firstPositionedCorner);

        if (positionedCorners == CUBE_SIDES) {
            proceedToNextState();
            return;
        }

        Algorithm algorithm = new Algorithm();

        // TODO: Last face corner position: calculate instead of guessing here.
        if (positionedCorners == 0) {
            algorithm.append(lastFaceCornerPositionAlgo(Direction.CLOCKWISE));
            setAlgo(algorithm);
            return;
        }

        if (positionedCorners != 1) {
            sendMessage("Something went wrong in top corner positioning");
            return;
        }

        if (firstPositionedCorner != FACE_FRONT) {
            Direction direction = firstPositionedCorner == FACE_LEFT ?
                    Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
            algorithm.addStep(Axis.Y_AXIS, direction, 0, SIZE);
            if (firstPositionedCorner == FACE_BACK) {
                algorithm.repeatLastStep();
            }
        }

        algorithm.append(lastFaceCornerPositionAlgo(Direction.CLOCKWISE));
        setAlgo(algorithm);
    }

    private Algorithm lastFaceCornerPositionAlgo(Direction direction) {
        Algorithm algo = new Algorithm();
        if (direction == Direction.COUNTER_CLOCKWISE) {
            algo.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
            algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            algo.addStep(Axis.X_AXIS, Direction.CLOCKWISE, INNER);
            algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
            algo.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            algo.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
            algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
        } else {
            algo.addStep(Axis.X_AXIS, Direction.CLOCKWISE, INNER);
            algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
            algo.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
            algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            algo.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
            algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
            algo.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
            algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        }
        return algo;
    }

    private boolean isCornerPositioned(Piece piece) {
        if (piece.getType() != Piece.PieceType.CORNER) throw new AssertionError();
        int[] faces = new int[3];
        for (int i = 0; i < 3; i++) {
            faces[i] = piece.mSquares.get(i).getFace();
        }

        for (Square sq: piece.mSquares) {
            boolean found = false;
            for (int face: faces) {
                if (sq.getColor() == mAllFaces[face].get(CENTER).getColor()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }
    private boolean checkTopCorners() {
        int[] corners = new int[]{
                LAST_ROW_RIGHT, FIRST_ROW_RIGHT, FIRST_ROW_LEFT, LAST_ROW_LEFT
        };
        for (int i = 0; i < corners.length; i++) {
            Piece piece = mYaxisLayers.get(OUTER).get(corners[i]);
            if (!isCornerAligned(piece)) {
                Log.w(tag, piece + " is not aligned");
                return false;
            }
        }
        return true;
    }




    private void lastFaceCornerAlign() {
        int lastColor = mTopSquares.get(CENTER).getColor();
        Algorithm algorithm = new Algorithm();
        if (mTopSquares.get(LAST_ROW_RIGHT).getColor() == lastColor) {
            if (checkTopCorners()) {
                proceedToNextState();
            }
            else {
                algorithm.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, OUTER);
                setAlgo(algorithm);
            }
        } else {
            algorithm.append(theFinalAlgorithm());
            setAlgo(algorithm);
        }
    }

    private Algorithm theFinalAlgorithm() {
        Algorithm algo = new Algorithm();
        algo.addStep(Axis.X_AXIS, Direction.COUNTER_CLOCKWISE, OUTER);
        algo.addStep(Axis.Y_AXIS, Direction.CLOCKWISE, INNER);
        algo.addStep(Axis.X_AXIS, Direction.CLOCKWISE, OUTER);
        algo.addStep(Axis.Y_AXIS, Direction.COUNTER_CLOCKWISE, INNER);
        return algo;
    }

    private void proceedToNextState() {
        if (mState != CubeState.SOLVING) {
            Log.e(tag, "invalid state " + mState);
            return;
        }
        switch (solveState) {
            case FirstFaceCross:
                solveState = SolveState.FirstFaceCorners;
                firstFaceCorners();
                break;

            case FirstFaceCorners:
                solveState = SolveState.MiddleLayer;
                setAlgo(Algorithm.rotateWhole(Axis.Z_AXIS, Direction.CLOCKWISE, SIZE, 2));
                break;

            case MiddleLayer:
                solveState = SolveState.LastFaceCross;
                lastFaceCross();
                break;

            case LastFaceCross:
                solveState = SolveState.LastFaceCrossAlign;
                lastFaceCrossAlign();
                break;

            case LastFaceCrossAlign:
                solveState = SolveState.LastFaceCorners;
                lastFaceCorners();
                break;

            case LastFaceCorners:
                solveState = SolveState.LastFaceCornerAlign;
                lastFaceCornerAlign();
                break;

            case LastFaceCornerAlign:
                solveState = SolveState.None;
                mListener.handleCubeSolved();
                mState = CubeState.IDLE;
                break;

            default:
                throw new AssertionError();
        }
    }
}
