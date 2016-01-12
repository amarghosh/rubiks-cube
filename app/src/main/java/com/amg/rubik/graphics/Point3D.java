package com.amg.rubik.graphics;

/**
 * Created by amar on 7/1/16.
 */
public class Point3D {
    private float x;
    private float y;
    private float z;

    public Point3D() {
        x = y = z = 0;
    }

    public Point3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(Point3D that) {
        this(that.x, that.y, that.z);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setXYZ(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

