package com.amg.rubik.cube;

import com.amg.rubik.graphics.Axis;
import com.amg.rubik.graphics.Direction;

import java.security.InvalidParameterException;

/**
 * Created by amar on 11/12/15.
 */
public class Rotation {

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
    float angleDelta = RubiksCube.ANGLE_DELTA_NORMAL;

    public Rotation() {
        reset();
    }

    public Rotation(Axis axis, Direction dir, int face, int faceCount) {
        this(axis, dir, face);
        this.faceCount = faceCount;
    }

    public Rotation(Axis axis, Direction dir, int face) {
        reset();
        this.axis = axis;
        this.direction = dir;
        this.startFace = face;
        this.angle = 0;
    }

    Rotation duplicate() {
        Rotation dup = new Rotation(axis, direction, startFace, faceCount);
        dup.setAngleDelta(angleDelta);
        return dup;
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
                ", face " + startFace +
                (faceCount > 1 ? " faces " + faceCount : "");
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

    public void setFaceCount(int faceCount) {
        this.faceCount = faceCount;
    }

    public boolean getStatus() {
        return status;
    }

    public void start() {
        status = true;
    }
}
