package com.amg.rubik.cube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.amg.rubik.graphics.Point3D;

public class Square {

    public static final int YELLOW = 0;
    public static final int GREEN = 1;
    public static final int RED = 2;
    public static final int ORANGE = 3;
    public static final int BLUE = 4;
    public static final int WHITE = 5;
    public static final int HIGHLIGHT_COLOR = 6;

    private static final int COLOR_COUNT = 7;

    // TODO: colors and squares are coupled here. This needs to be cleaned up.
    private static final float[] green = { 0.2f, 0.7f, 0.2f, 1.0f };
    private static final float[] red = { 0.7f, 0.2f, 0.2f, 1.0f };
    private static final float[] blue = { 0.2f, 0.2f, 0.7f, 1.0f };
    private static final float[] white = { 0.8f, 0.8f, 0.8f, 1.0f };
    private static final float[] yellow = { 0.8f, 0.8f, 0.2f, 1.0f };
    private static final float[] orange = { 0.8f, 0.4f, 0.1f, 1.0f };
    private static final float[] highlight = {0f, 1f, 1f, 1f}; // Cyan
    private static final Color[] colors;

    private int face;

    public static class Color {
        final float[] rgba;
        final String name;
        Color(float[] rgba, String name) {
            this.rgba = rgba;
            this.name = name;
        }
    }

    static {
        colors = new Color[COLOR_COUNT];
        colors[YELLOW] = new Color(yellow, "yellow");
        colors[RED] = new Color(red, "red");
        colors[GREEN] = new Color(green, "green");
        colors[ORANGE] = new Color(orange, "orange");
        colors[BLUE] = new Color(blue, "blue");
        colors[WHITE] = new Color(white, "white");
        colors[COLOR_COUNT - 1] = new Color(highlight, "highlight");
    }

    int mColor = WHITE;

    // Our vertex buffer.
    private FloatBuffer mVertexBuffer;

    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public Square(float[] vertices, int color) {
        init(vertices, color);
    }

    private void init(float[] vertices, int color) {
        // a float is 4 bytes, therefore we multiply the number if
        // vertices with 4.
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
        mColor = color % COLOR_COUNT;
    }

    public Square(Point3D[] points, int color) {
        float[] vertices = new float[points.length * 3]; // x, y, z
        for (int i = 0; i < points.length; i++) {
            vertices[i*3] = points[i].getX();
            vertices[i*3 + 1] = points[i].getY();
            vertices[i*3 + 2] = points[i].getZ();
        }
        init(vertices, color);
    }

    public FloatBuffer vertexBuffer() {
        return mVertexBuffer;
    }

    public float[] color() {
        return colors[mColor].rgba;
    }

    public String colorName() {
        return colors[mColor].name;
    }

    public static String getColorName(int index) {
        return colors[index].name;
    }
}
