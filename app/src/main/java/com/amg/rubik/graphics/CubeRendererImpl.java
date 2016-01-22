package com.amg.rubik.graphics;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.amg.rubik.cube.RubiksCube;
import com.amg.rubik.cube.Square;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by amar on 30/11/15.
 */
public class CubeRendererImpl extends GLRenderer
    implements CubeRenderer {

    private static final String tag = "rubik-renderer";

    private static final int COORDS_PER_VERTEX = 3;

    // 3 * size of float
    private static final int VERTEX_STRIDE = 12;

    private RubiksCube mCube;

    private int PROGRAM;
    private int POSITION_HANDLE;
    private int COLOR_HANDLE;
    private int MATRIX_HANDLE;

    public CubeRendererImpl() {
        mCube = null;
        resetRotation();
    }

    private void resetRotation() {
        mHasRotation = false;
    }

    public void setCube(RubiksCube cube) {
        mCube = cube;
        mCube.setRenderer(this);
        resetRotation();
    }

    @Override
    public void onCreate(int width, int height, boolean contextLost) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1f);
    }

    private boolean highlightFlag = false;
    private Square highlightPoint;
    private final float mSize = 0.02f;

    public void clearHighlight() {
        highlightFlag = false;
    }

    public void setHighlightPoint(Point3D point, Axis axis) {
        Point3D[] corners = new Point3D[4];

        switch (axis) {
            case X_AXIS:
                corners[0] = new Point3D(point.getX(), point.getY() + mSize, point.getZ() + mSize);
                corners[1] = new Point3D(point.getX(), point.getY() - mSize, point.getZ() + mSize);
                corners[2] = new Point3D(point.getX(), point.getY() - mSize, point.getZ() - mSize);
                corners[3] = new Point3D(point.getX(), point.getY() + mSize, point.getZ() - mSize);
                break;
            case Y_AXIS:
                corners[0] = new Point3D(point.getX() - mSize, point.getY(), point.getZ() - mSize);
                corners[1] = new Point3D(point.getX() - mSize, point.getY(), point.getZ() + mSize);
                corners[2] = new Point3D(point.getX() + mSize, point.getY(), point.getZ() + mSize);
                corners[3] = new Point3D(point.getX() + mSize, point.getY(), point.getZ() - mSize);
                break;
            case Z_AXIS:
                corners[0] = new Point3D(point.getX() - mSize, point.getY() + mSize, point.getZ());
                corners[1] = new Point3D(point.getX() - mSize, point.getY() - mSize, point.getZ());
                corners[2] = new Point3D(point.getX() + mSize, point.getY() - mSize, point.getZ());
                corners[3] = new Point3D(point.getX() + mSize, point.getY() + mSize, point.getZ());
                break;
        }
        this.highlightPoint = new Square(corners, Color.CYAN);
        highlightFlag = true;
    }

    @Override
    public void onDrawFrame(boolean firstDraw) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
                | GLES20.GL_DEPTH_BUFFER_BIT);
        if (mCube == null) {
            Log.w(tag, "no cube set");
            return;
        }

        startDrawing();
        mCube.draw();
        if (highlightFlag) {
            drawSquare(highlightPoint);
        }
        finishDrawing();
    }

    private void startDrawing()
    {
        PROGRAM = ShaderCache.getInstance().getProgram();
        GLES20.glUseProgram(PROGRAM);
        POSITION_HANDLE = GLES20.glGetAttribLocation(PROGRAM, "vPosition");
        GLES20.glEnableVertexAttribArray(POSITION_HANDLE);
        COLOR_HANDLE = GLES20.glGetUniformLocation(PROGRAM, "vColor");
        MATRIX_HANDLE = GLES20.glGetUniformLocation(PROGRAM, "uMVPMatrix");
    }

    private void finishDrawing() {
        GLES20.glDisableVertexAttribArray(POSITION_HANDLE);
    }

    // The order in which lines are drawn
    private static final short[] indices = { 0, 1, 2, 0, 2, 3};
    // Our index buffer.
    private static final ShortBuffer indexBuffer;

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

    public void drawSquare(Square square) {
        int color = square.getColor();
        float[] rgba = {
                Color.red(color) / 255.0f,
                Color.green(color) / 255.0f,
                Color.blue(color) / 255.0f,
                Color.alpha(color) / 255.0f,
        };

        float[] matrix = mHasRotation ? mRotationMatrix : mMVPMatrix;
        GLES20.glVertexAttribPointer(POSITION_HANDLE,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, square.vertexBuffer());

        GLES20.glUniform4fv(COLOR_HANDLE, 1, rgba, 0);
        GLES20.glUniformMatrix4fv(MATRIX_HANDLE, 1, false, matrix, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);
    }

    private final float[] mRotationMatrix = new float[16];
    private boolean mHasRotation;

    @Override
    public void setRotation(float angle, float x, float y, float z) {
        float[] temp = new float[16];
        for (int i = 0; i < 16; i++) {
            mRotationMatrix[i] = 0;
            temp[i] = 0;
        }
        Matrix.setRotateM(temp, 0, angle, x, y, z);
        Matrix.multiplyMM(mRotationMatrix, 0, mMVPMatrix, 0, temp, 0);

        mHasRotation = !(angle == 0f &&
                x == 0f && y == 0f && z == 0f);
    }
}
