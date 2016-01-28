package com.amg.rubik.cube;

import android.graphics.Color;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.amg.rubik.graphics.Point3D;

public class Square {

    private int mFace;

    private int mColor;

    // Our vertex buffer.
    private FloatBuffer mVertexBuffer;

    public int getFace() {
        return mFace;
    }

    public Square(float[] vertices, int color, int face) {
        init(vertices, color, face);
    }

    public Square(float[] vertices, int color) {
        this(vertices, color, -1);
    }

    public Square(float[] vertices) {
        this(vertices, Color.GRAY);
    }

    private void init(float[] vertices, int color, int face) {
        // a float is 4 bytes, therefore we multiply the number if
        // vertices with 4.
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
        mColor = color;
        mFace = face;
    }

    public Square(Point3D[] points, int color) {
        float[] vertices = new float[points.length * 3]; // x, y, z
        for (int i = 0; i < points.length; i++) {
            vertices[i*3] = points[i].getX();
            vertices[i*3 + 1] = points[i].getY();
            vertices[i*3 + 2] = points[i].getZ();
        }
        init(vertices, color, -1);
    }

    public FloatBuffer vertexBuffer() {
        return mVertexBuffer;
    }

    public String colorName() {
        return String.format("#%08X",  mColor);
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int value) {
        mColor = value;
    }
}
