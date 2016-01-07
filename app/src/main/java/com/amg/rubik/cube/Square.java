package com.amg.rubik.cube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Point;
import android.opengl.GLES20;

import com.amg.rubik.graphics.Point3D;
import com.amg.rubik.graphics.ShaderCache;

public class Square {

    public static final int YELLOW = 0;
    public static final int GREEN = 1;
    public static final int RED = 2;
    public static final int ORANGE = 3;
    public static final int BLUE = 4;
    public static final int WHITE = 5;
    public static final int HIGHLIGHT_COLOR = 6;

    private static final int COLOR_COUNT = 7;

    private static final int COORDS_PER_VERTEX = 3;

    // 3 * size of float
    private static final int VERTEX_STRIDE = 12;

    // TODO: colors and squares are coupled here. This needs to be cleaned up.
    static float green[] = { 0.2f, 0.7f, 0.2f, 1.0f };
    static float red[] = { 0.7f, 0.2f, 0.2f, 1.0f };
    static float blue[] = { 0.2f, 0.2f, 0.7f, 1.0f };
    static float white[] = { 0.8f, 0.8f, 0.8f, 1.0f };
    static float yellow[] = { 0.8f, 0.8f, 0.2f, 1.0f };
    static float orange[] = { 0.8f, 0.4f, 0.1f, 1.0f };
    static float highlight[] = {0f, 1f, 1f, 1f}; // Cyan
    static Color[] colors;

    private int face;

    public static class Color {
        float[] rgba;
        String name;
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

    protected int mColor = WHITE;

    // Our vertex buffer.
    private FloatBuffer vertexBuffer;

    // Our index buffer.
    private static ShortBuffer indexBuffer;
    private static int PROGRAM;
    private static int POS_HANDLE;
    private static int COL_HANDLE;
    private static int MAT_HANDLE;

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
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
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

    // The order in which lines are drawn
    private static short[] indices = { 0, 1, 2, 0, 2, 3};

    static {
        /**
         *  As all squares are initialized in counter clockwise order,
         *  they can share the index buffer. Initialize it in this static block.
         * */

        // short is 2 bytes, therefore we multiply the number if
        // vertices with 2.
        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    /**
     * These needs be called once per each frame.
     * TODO: Move this to renderer class
     * */
    public static void startDrawing()
    {
        PROGRAM = ShaderCache.getInstance().getProgram();
        GLES20.glUseProgram(PROGRAM);
        POS_HANDLE = GLES20.glGetAttribLocation(PROGRAM, "vPosition");
        GLES20.glEnableVertexAttribArray(POS_HANDLE);
        COL_HANDLE = GLES20.glGetUniformLocation(PROGRAM, "vColor");
        MAT_HANDLE = GLES20.glGetUniformLocation(PROGRAM, "uMVPMatrix");
    }

    public static void finishDrawing() {
        GLES20.glDisableVertexAttribArray(POS_HANDLE);
    }


    public void draw(float[] mvpMatrix) {
        GLES20.glVertexAttribPointer(POS_HANDLE,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, vertexBuffer);

        GLES20.glUniform4fv(COL_HANDLE, 1, colors[mColor].rgba, 0);
        GLES20.glUniformMatrix4fv(MAT_HANDLE, 1, false, mvpMatrix, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);
    }

    public String colorName() {
        return colors[mColor].name;
    }

    public static String getColorName(int index) {
        return colors[index].name;
    }
}
