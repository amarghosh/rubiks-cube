package com.amg.rubik;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.security.InvalidParameterException;

/**
 * Created by amar on 6/1/16.
 */
public abstract class AbstractFragment extends Fragment
        implements View.OnClickListener {

    protected String tag = "rubik-fragment";

    protected View mRootView;
    private SharedPreferences mPref;
    private int mCubeSize;
    private String mCubeState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        mCubeSize = mPref.getInt(Constants.CUBE_SIZE, Constants.DEFAULT_CUBE_SIZE);
        mCubeState = mPref.getString(Constants.CUBE_STATE, null);
    }

    protected View findViewById(int id) {
        return mRootView.findViewById(id);
    }

    protected int cubeSize() {
        return mCubeSize;
    }

    protected void setCubeSize(int value) {
        if (value <= 0) {
            throw new InvalidParameterException("Invalid cube size: " + value);
        }
        mCubeSize = value;
    }

    protected String cubeState() {
        return mCubeState;
    }

    @Override
    public void onPause() {
        super.onPause();
        boolean isDirty = false;
        int size = mPref.getInt(Constants.CUBE_SIZE, Constants.DEFAULT_CUBE_SIZE);
        isDirty = size != mCubeSize;
        Log.w(tag, String.format("onPause size %d, mCubeSize %d", size, mCubeSize));
        if (isDirty) {
            updateParams();
        }
    }

    private void updateParams() {
        Log.w(tag, String.format("Saving cube size %d", mCubeSize));
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(Constants.CUBE_SIZE, mCubeSize);
        editor.commit();
    }
}
