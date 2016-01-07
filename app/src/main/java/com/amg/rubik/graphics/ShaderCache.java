package com.amg.rubik.graphics;

import android.opengl.GLES20;
import android.util.Log;

public class ShaderCache {

    private static final String tag = "shadercache";
    private static ShaderCache instance = null;
    public static ShaderCache getInstance() {
        if (!GLES20.glIsProgram(instance.mProgram)) {
            Log.w(tag, "refreshing");
            instance.refresh();
        }
        return instance;
    }

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // the matrix must be included as a modifier of gl_Position
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private static final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vColor;" +
        "}";

    int mProgram;
    int mVertexShader;
    int mFragmentShader;

    private ShaderCache() {
        Log.w(tag, "creating shadercache");
        refresh();
    }

    private void refresh() {
        mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, mVertexShader);
        GLES20.glAttachShader(mProgram, mFragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    private int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private static void init() {
        if (instance == null)
            instance = new ShaderCache();
    }

    static {
        init();
    }

    public int getVertexShader() {
        return mVertexShader;
    }

    public int getFragmentShader() {
        return mFragmentShader;
    }

    public int getProgram() {
        return mProgram;
    }

    public void onResume() {
        refresh();
    }
}
