package com.amg.rubik;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

public class RubiksCube {
	
	static final float SQ_SIZE = 0.15f;
	static final float GAP = 0.005f;
	
	private GLSurfaceView mGlView = null;
	
	public RubiksCube(Context context) {
		mGlView = new RubikGLSurfaceView(context);
		cube();
	}
	
	ArrayList<Square> mSquares;
	
	private void cube()
	{
		mSquares = new ArrayList<>();
		frontFace();
		leftFace();
		rightFace();
		backFace();
		topFace();
		bottomFace();
	}
	
	private void leftFace() 
	{
		float startX = 0 - SQ_SIZE * 1.5f - GAP * 2;
		float startY = (SQ_SIZE + GAP) * 1.5f;
		float startZ = 0 - (SQ_SIZE + GAP) * 1.5f;
		
		float vertices[] = {
			      startX,  startY, startZ,
			      startX,  startY - SQ_SIZE, startZ,
			      startX,  startY - SQ_SIZE, startZ + SQ_SIZE,
			      startX,  startY, startZ + SQ_SIZE
			};
		
		for (int i = 0; i < 3; i++) {
			vertices[1] = startY - i * (SQ_SIZE + GAP);
			vertices[4] = vertices[1] - SQ_SIZE;
			vertices[7] = vertices[1] - SQ_SIZE;
			vertices[10] = vertices[1];
			
			for (int j = 0; j < 3; j++) {
				vertices[2] = startZ + j * (SQ_SIZE + GAP);
				vertices[5] = vertices[2];
				vertices[8] = vertices[2] + SQ_SIZE;
				vertices[11] = vertices[2] + SQ_SIZE;
				mSquares.add(new Square(vertices, Square.RED));
			}
		}
	}
	
	private void rightFace() 
	{
		float startX = SQ_SIZE * 1.5f + GAP * 2;
		float startY = (SQ_SIZE + GAP) * 1.5f;
		float startZ = (SQ_SIZE + GAP) * 1.5f;
		
		float vertices[] = {
			      startX,  startY, startZ,
			      startX,  startY - SQ_SIZE, startZ,
			      startX,  startY - SQ_SIZE, startZ - SQ_SIZE,
			      startX,  startY, startZ - SQ_SIZE
			};
		
		for (int i = 0; i < 3; i++) {
			vertices[1] = startY - i * (SQ_SIZE + GAP);
			vertices[4] = vertices[1] - SQ_SIZE;
			vertices[7] = vertices[1] - SQ_SIZE;
			vertices[10] = vertices[1];
			
			for (int j = 0; j < 3; j++) {
				vertices[2] = startZ - j * (SQ_SIZE + GAP);
				vertices[5] = vertices[2];
				vertices[8] = vertices[2] - SQ_SIZE;
				vertices[11] = vertices[2] - SQ_SIZE;
				mSquares.add(new Square(vertices, Square.ORANGE));
			}
		}
	}
	
	private void topFace()
	{
		float startX = - (SQ_SIZE + GAP) * 1.5f;
		float startY = (SQ_SIZE + GAP) * 1.5f;
		float startZ = - (SQ_SIZE + GAP) * 1.5f;
		
		float vertices[] = {
			      startX,  startY, startZ,
			      startX,  startY, startZ + SQ_SIZE,
			      startX + SQ_SIZE,  startY, startZ + SQ_SIZE,
			      startX + SQ_SIZE,  startY, startZ
			};
		
		for (int i = 0; i < 3; i++) {
			vertices[2] = startZ + i * (SQ_SIZE + GAP);
			vertices[5] = vertices[2] + SQ_SIZE;
			vertices[8] = vertices[2] + SQ_SIZE;
			vertices[11] = vertices[2];
			
			for (int j = 0; j < 3; j++) {
				vertices[0] = startX + j * (SQ_SIZE + GAP);
				vertices[3] = vertices[0];
				vertices[6] = vertices[0] + SQ_SIZE;
				vertices[9] = vertices[0] + SQ_SIZE;
				mSquares.add(new Square(vertices, Square.BLUE));
			}
		}
	}

	private void bottomFace()
	{
		float startX = - (SQ_SIZE + GAP) * 1.5f;
		float startY = - (SQ_SIZE + GAP) * 1.5f;
		float startZ = (SQ_SIZE + GAP) * 1.5f;
		
		float vertices[] = {
			      startX,  startY, startZ,
			      startX,  startY, startZ - SQ_SIZE,
			      startX + SQ_SIZE,  startY, startZ - SQ_SIZE,
			      startX + SQ_SIZE,  startY, startZ
			};
		
		for (int i = 0; i < 3; i++) {
			vertices[2] = startZ - i * (SQ_SIZE + GAP);
			vertices[5] = vertices[2] - SQ_SIZE;
			vertices[8] = vertices[2] - SQ_SIZE;
			vertices[11] = vertices[2];
			
			for (int j = 0; j < 3; j++) {
				vertices[0] = startX + j * (SQ_SIZE + GAP);
				vertices[3] = vertices[0];
				vertices[6] = vertices[0] + SQ_SIZE;
				vertices[9] = vertices[0] + SQ_SIZE;
				mSquares.add(new Square(vertices, Square.GREEN));
			}
		}
	}

	
	private void frontFace()
	{
		float startX = 0 - (SQ_SIZE + GAP) * 1.5f;
		float startY = (SQ_SIZE + GAP) * 1.5f;
		float startZ = (SQ_SIZE + GAP) * 1.5f;
		
		float vertices[] = {
			      startX,  startY, startZ,
			      startX,  startY - SQ_SIZE, startZ,
			      startX + SQ_SIZE,  startY - SQ_SIZE, startZ,
			      startX + SQ_SIZE,  startY, startZ
			};
		
		for (int i = 0; i < 3; i++) {
			vertices[1] = startY - i * (SQ_SIZE + GAP);
			vertices[4] = vertices[1] - SQ_SIZE;
			vertices[7] = vertices[1] - SQ_SIZE;
			vertices[10] = vertices[1];
			
			for (int j = 0; j < 3; j++) {
				vertices[0] = startX + j * (SQ_SIZE + GAP);
				vertices[3] = vertices[0];
				vertices[6] = vertices[0] + SQ_SIZE;
				vertices[9] = vertices[0] + SQ_SIZE;
				mSquares.add(new Square(vertices, Square.YELLOW));
			}
		}
	}
	
	private void backFace()
	{
		float startX = (SQ_SIZE + GAP) * 1.5f;
		float startY = (SQ_SIZE + GAP) * 1.5f;
		float startZ = - (SQ_SIZE + GAP) * 1.5f;
		
		float vertices[] = {
			      startX,  startY, startZ,
			      startX,  startY - SQ_SIZE, startZ,
			      startX - SQ_SIZE,  startY - SQ_SIZE, startZ,
			      startX - SQ_SIZE,  startY, startZ
			};
		
		for (int i = 0; i < 3; i++) {
			vertices[1] = startY - i * (SQ_SIZE + GAP);
			vertices[4] = vertices[1] - SQ_SIZE;
			vertices[7] = vertices[1] - SQ_SIZE;
			vertices[10] = vertices[1];
			
			for (int j = 0; j < 3; j++) {
				vertices[0] = startX - j * (SQ_SIZE + GAP);
				vertices[3] = vertices[0];
				vertices[6] = vertices[0] - SQ_SIZE;
				vertices[9] = vertices[0] - SQ_SIZE;
				mSquares.add(new Square(vertices, Square.WHITE));
			}
		}
	}
	
	public GLSurfaceView getView() {
		return mGlView;
	}
	
	public void onResume() {
		mGlView.onResume();
	}
	
	public void onPause() {
		mGlView.onPause();
	}
	
	private void draw(float[] mvpMatrix) {
		for (Square sq : mSquares) {
			sq.draw(mvpMatrix);
		}
	}
	
	class RubikGLSurfaceView extends GLSurfaceView {
		
		RubikGLSurfaceView(Context context) {
			super(context);
			setEGLContextClientVersion(2);
			setPreserveEGLContextOnPause(true);
			setRenderer(new RubikRenderer());
		}
		
	}

	class RubikRenderer extends GLRenderer {
        
		@Override
		public void onCreate(int width, int height, boolean contextLost) {
			GLES20.glClearColor(0f, 0f, 0f, 1f);
		}

		@Override
		public void onDrawFrame(boolean firstDraw) {
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
	                | GLES20.GL_DEPTH_BUFFER_BIT);
			
			float[] scratch = new float[16];
			
		    long time = SystemClock.uptimeMillis() % 4000L;
		    float angle = 0.090f * ((int) time);
		    Matrix.setRotateM(mRotationMatrix, 0, angle, 0.25f, 0.25f, 0.25f);

		    // Combine the rotation matrix with the projection and camera view
		    // Note that the mMVPMatrix factor *must be first* in order
		    // for the matrix multiplication product to be correct.
		    Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

			RubiksCube.this.draw(scratch);
		}
	}
}

