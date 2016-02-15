package com.amg.rubik.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amg.rubik.Constants;

import java.security.InvalidParameterException;

/**
 * Base fragment that takes care of initializing preferences so that individual classes need not
 * worry about it.
 */
public abstract class BaseFragment extends Fragment
        implements View.OnClickListener {

    String tag = "rubik-fragment";

    View mRootView;
    private SharedPreferences mPref;
    private int mCubeSize_X;
    private int mCubeSize_Y;
    private int mCubeSize_Z;
    private int mScrambleCount;
    private String mCubeState;
    private int mScrambleMode;
    private int mSpeed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        reloadPrefs();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadPrefs();
    }

    protected void reloadPrefs() {
        mCubeSize_X = mPref.getInt(Constants.CUBE_SIZEX, Constants.DEFAULT_CUBE_SIZE);
        mCubeSize_Y = mPref.getInt(Constants.CUBE_SIZEY, Constants.DEFAULT_CUBE_SIZE);
        mCubeSize_Z = mPref.getInt(Constants.CUBE_SIZEZ, Constants.DEFAULT_CUBE_SIZE);
        mCubeState = mPref.getString(Constants.CUBE_STATE, null);
        mScrambleCount = mPref.getInt(Constants.SCRAMBLE_COUNT, Constants.DEFAULT_SCRAMBLE_COUNT);
        mScrambleMode = mPref.getInt(Constants.SCRAMBLE_MODE,
                Constants.DEFAULT_SCRAMBLE_MODE_INDEX);
        mSpeed = mPref.getInt(Constants.ROTATION_SPEED, Constants.DEFAULT_SPEED_INDEX);
    }

    View findViewById(int id) {
        return mRootView.findViewById(id);
    }

    public void setSpeed(int speed) {
        this.mSpeed = speed;
    }

    public int getSpeed() {
        return mSpeed;
    }

    int cubeSizeX() {
        return mCubeSize_X;
    }

    int cubeSizeY() {
        return mCubeSize_Y;
    }
    int cubeSizeZ() {
        return mCubeSize_Z;
    }

    void setCubeSizeX(int value) {
        if (value <= 0) {
            throw new InvalidParameterException("Invalid cube size: " + value);
        }
        mCubeSize_X = value;
    }

    public void setCubeSizeY(int v) {
        this.mCubeSize_Y = v;
    }

    public void setCubeSizeZ(int v) {
        this.mCubeSize_Z = v;
    }

    String cubeState() {
        return mCubeState;
    }

    public int scrambleCount() {
        return mScrambleCount;
    }

    public void setScrambleCount(int count) {
        this.mScrambleCount = count;
    }

    private void setScrambleMode(int mode) {
        this.mScrambleMode = mode;
    }

    private int getScrambleMode() {
        return mScrambleMode;
    }

    @Override
    public void onPause() {
        super.onPause();
        boolean isDirty = false;
        int size = mPref.getInt(Constants.CUBE_SIZEX, Constants.DEFAULT_CUBE_SIZE);
        isDirty = size != mCubeSize_X;
        int scrCount = mPref.getInt(Constants.SCRAMBLE_COUNT, Constants.DEFAULT_SCRAMBLE_COUNT);
        isDirty |= scrCount != mScrambleCount;

        int scrMode = mPref.getInt(Constants.SCRAMBLE_MODE, Constants.DEFAULT_SCRAMBLE_MODE_INDEX);
        isDirty |= scrMode != mScrambleMode;

        int speed = mPref.getInt(Constants.ROTATION_SPEED, Constants.DEFAULT_SPEED_INDEX);
        isDirty |= speed != mSpeed;
        isDirty = true;

        Log.w(tag, String.format("onPause size %d, mCubeSize_X %d", size, mCubeSize_X));
        if (isDirty) {
            updateParams();
        }
    }

    private void updateParams() {
        Log.w(tag, String.format("Saving cube size %d", mCubeSize_X));
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(Constants.CUBE_SIZEX, mCubeSize_X);
        editor.putInt(Constants.CUBE_SIZEY, mCubeSize_Y);
        editor.putInt(Constants.CUBE_SIZEZ, mCubeSize_Z);
        editor.putInt(Constants.SCRAMBLE_COUNT, mScrambleCount);
        editor.putInt(Constants.SCRAMBLE_MODE, mScrambleMode);
        editor.putInt(Constants.ROTATION_SPEED, mSpeed);
        editor.apply();
    }
}
