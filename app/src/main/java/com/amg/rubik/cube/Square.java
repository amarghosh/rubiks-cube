package com.amg.rubik.cube;

import android.graphics.Color;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.amg.rubik.graphics.Axis;
import com.amg.rubik.graphics.Point3D;

public class Square {

    private int mFace;
    private Point3D mCenter;

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

    public Point3D getCenter() {
        return mCenter;
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
        mCenter = new Point3D();
        mCenter.setX((vertices[0] + vertices[3] + vertices[6] + vertices[9]) / 4);
        mCenter.setY((vertices[1] + vertices[4] + vertices[7] + vertices[10]) / 4);
        mCenter.setZ((vertices[2] + vertices[5] + vertices[8] + vertices[11]) / 4);
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

    public void rotateCoordinates(float[] rotmatrix) {
        float[] coords = new float[12];
        mVertexBuffer.get(coords);
        mVertexBuffer.clear();
        float[] resmatrix = new float[4];
        float[] input_matrix = new float[4];

        for (int i = 0; i < 4; i++) {
            input_matrix[0] = coords[i*3];
            input_matrix[1] = coords[i*3 + 1];
            input_matrix[2] = coords[i*3 + 2];
            Matrix.multiplyMV(resmatrix, 0, rotmatrix, 0, input_matrix, 0);
            mVertexBuffer.put(resmatrix[0]);
            mVertexBuffer.put(resmatrix[1]);
            mVertexBuffer.put(resmatrix[2]);
        }
        mVertexBuffer.position(0);
    }

    public void rotateCoordinates(Axis axis, int angle) {
        int x = 0, y = 0, z = 0;
        switch (axis) {
            case X_AXIS: x = 1; break;
            case Y_AXIS: y = 1; break;
            case Z_AXIS: z = 1; break;
        }
        float[] rotmatrix = new float[16];
        Matrix.setRotateM(rotmatrix, 0, angle, x, y, z);
        rotateCoordinates(rotmatrix);
    }
}
