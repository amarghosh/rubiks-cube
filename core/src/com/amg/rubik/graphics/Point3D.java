package com.amg.rubik.graphics;

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

    public Point3D(float[] xyz, int offset) {
        this(xyz[offset], xyz[offset+1], xyz[offset+2]);
    }

    public Point3D(float[] xyz) {
        this(xyz, 0);
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

    public float dist(float ptx, float pty, float ptz) {
        return (float)Math.sqrt((x - ptx)*(x - ptx) +
                (y - pty)*(y - pty) +
                (z - ptz)*(z - ptz)
        );
    }

    public float dist(Point3D pt) {
        return dist(pt.x, pt.y, pt.z);
    }

    @Override
    public String toString() {
        return String.format("(%f, %f, %f)", x, y, z);
    }
}

