package com.amg.rubik;

import android.opengl.GLES20;

public class ShaderCache {
	
	private static ShaderCache instance = null;

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
	
	public static int getVertexShader() {
		return instance.mVertexShader;
	}
	
	public static int getFragmentShader() {
		return instance.mFragmentShader;
	}
	
	public static int getProgram() {
		return instance.mProgram;
	}

}
