package com.amg.rubik;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

public class Square {

    public static final int YELLOW = 0;
    public static final int GREEN = 1;
    public static final int RED = 2;
    public static final int ORANGE = 3;
    public static final int BLUE = 4;
    public static final int WHITE = 5;
    private static final int COLOR_COUNT = 6;

    private static final int COORDS_PER_VERTEX = 3;

    // 3 * size of float
    private static final int VERTEX_STRIDE = 12;

    static float green[] = { 0.2f, 0.7f, 0.2f, 1.0f };
    static float red[] = { 0.7f, 0.2f, 0.2f, 1.0f };
    static float blue[] = { 0.2f, 0.2f, 0.7f, 1.0f };
    static float white[] = { 0.8f, 0.8f, 0.8f, 1.0f };
    static float yellow[] = { 0.8f, 0.8f, 0.2f, 1.0f };
    static float orange[] = { 0.8f, 0.4f, 0.1f, 1.0f };
    static float[][] colors;

    static {
        colors = new float[COLOR_COUNT][];
        colors[YELLOW] = yellow;
        colors[RED] = red;
        colors[GREEN] = green;
        colors[ORANGE] = orange;
        colors[BLUE] = blue;
        colors[WHITE] = white;
    }

    private int mColor = WHITE;

    // Our vertex buffer.
    private FloatBuffer vertexBuffer;

    // Our index buffer.
    private static ShortBuffer indexBuffer;
    private static int PROGRAM;
    private static int POS_HANDLE;
    private static int COL_HANDLE;
    private static int MAT_HANDLE;

    public Square(float[] vertices, int color) {
        // a float is 4 bytes, therefore we multiply the number if
        // vertices with 4.
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
        mColor = color % COLOR_COUNT;
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
        PROGRAM = ShaderCache.getProgram();
        GLES20.glUseProgram(PROGRAM);
        POS_HANDLE = GLES20.glGetAttribLocation(PROGRAM, "vPosition");
        GLES20.glEnableVertexAttribArray(POS_HANDLE);
        COL_HANDLE = GLES20.glGetUniformLocation(PROGRAM, "vColor");
        MAT_HANDLE = GLES20.glGetUniformLocation(PROGRAM, "uMVPMatrix");
    }

    public static void finishDrawing()
    {
        GLES20.glDisableVertexAttribArray(POS_HANDLE);
    }


    public void draw(float[] mvpMatrix) {
        GLES20.glVertexAttribPointer(POS_HANDLE,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, vertexBuffer);

        GLES20.glUniform4fv(COL_HANDLE, 1, colors[mColor], 0);
        GLES20.glUniformMatrix4fv(MAT_HANDLE, 1, false, mvpMatrix, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);
    }
}
