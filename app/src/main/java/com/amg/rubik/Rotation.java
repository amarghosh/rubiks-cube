package com.amg.rubik;

import java.security.InvalidParameterException;

/**
 * Created by amar on 11/12/15.
 */
public     class Rotation {
    public enum Direction {
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }

    public enum Axis {
        X_AXIS, Y_AXIS, Z_AXIS
    }
    /*
    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;
    */

    private boolean status;
    Axis axis;
    Direction direction;

    /**
     * To support simultaneous rotating of multiple faces in higher order cubes
     * */
    int startFace;
    int faceCount;
    float angle;
    float angleDelta = 1f;

    Rotation() {
        reset();
    }

    Rotation(Axis axis, Direction dir, int face, int faceCount) {
        this(axis, dir, face);
        this.faceCount = faceCount;
    }

    Rotation(Axis axis, Direction dir, int face) {
        reset();
        this.axis = axis;
        this.direction = dir;
        this.startFace = face;
        this.angle = 0;
    }

    Rotation duplicate() {
        return new Rotation(axis, direction, startFace, faceCount);
    }

    void reset() {
        status = false;
        axis = Axis.Z_AXIS;
        direction = Direction.CLOCKWISE;
        startFace = 0;
        faceCount = 1;
        angle = 0;
    }

    @Override
    public String toString() {
        String axes = "XYZ";
        return "Axis " + axes.charAt(axis.ordinal()) +
                ", direction " + direction +
                ", face " + startFace;
    }

    public void setAxis(Axis axis) {
        this.axis = axis;
    }

    public void setAngleDelta(float angleDelta) {
        if (angleDelta > 90) {
            throw new InvalidParameterException("Delta should be less than 90: " + angleDelta);
        }
        this.angleDelta = angleDelta;
    }

    public void setStartFace(int startFace) {
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

    public boolean getStatus() {
        return status;
    }

    public void start() {
        status = true;
    }
}
