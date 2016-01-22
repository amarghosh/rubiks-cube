package com.amg.rubik.cube;

import com.amg.rubik.graphics.Axis;
import com.amg.rubik.graphics.Direction;

/**
 * Created by amar on 11/12/15.
 */

/**
 * Note that the direction is relative to positive direction of the mentioned axis, and not
 * the visible side of face. This is against the normal cube notation where direction is
 * usually mentioned relative to the face being rotated.
 *
 * @see Cube :: rotate(Axis axis, Direction direction, int face)
 * */
public class Rotation {

    private boolean status;
    Axis axis;
    Direction direction;

    /**
     * To support simultaneous rotating of multiple faces in higher order cubes
     * */
    int startFace;
    int faceCount;
    float angle;

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
        return dup;
    }

    public Rotation getReverse() {
        Rotation rot = duplicate();
        rot.direction = rot.direction == Direction.CLOCKWISE ?
                Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
        return rot;
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

    public void setStartFace(int startFace) {
        this.startFace = startFace;
    }

    void increment(float angleDelta) {
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
