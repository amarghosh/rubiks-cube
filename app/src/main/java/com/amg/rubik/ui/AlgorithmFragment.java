package com.amg.rubik.ui;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amg.rubik.R;
import com.amg.rubik.cube.AbstractCube;
import com.amg.rubik.cube.RubiksCube;
import com.amg.rubik.cube.RubiksCube3x3x3;
import com.amg.rubik.graphics.Axis;
import com.amg.rubik.graphics.RubikGLSurfaceView;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlgorithmFragment extends AbstractFragment {

    private RubiksCube mCube = null;
    private RubikGLSurfaceView mRubikView = null;

    public AlgorithmFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_algorithm, container, false);
        initializeRubikView();
        return mRootView;
    }

    private void initializeRubikView() {
        ViewGroup view = (ViewGroup)findViewById(R.id.cube_holder);
        mRubikView = new RubikGLSurfaceView(getActivity());
        mRubikView.enableRotations();
        mRubikView.setWholeCubeRotation(true);
        createCube();
        view.addView(mRubikView);
    }

    private void createCube() {
        int size = cubeSize();
        if (cubeSize() == 3) {
            mCube = new RubiksCube3x3x3();
        } else {
            mCube = new RubiksCube(cubeSize());
        }
        mCube.setColor(Color.GRAY);
        mCube.setColor(RubiksCube.FACE_TOP, Color.WHITE);
        mCube.setRowColor(AbstractCube.FACE_FRONT, 0, AbstractCube.Color_RED);
        mCube.setRowColor(AbstractCube.FACE_RIGHT, 0, Color.BLUE);
        mCube.setRowColor(AbstractCube.FACE_BACK, 0, AbstractCube.Color_ORANGE);
        mCube.setRowColor(AbstractCube.FACE_LEFT, 0, AbstractCube.Color_GREEN);
        mCube.setColumnColor(AbstractCube.FACE_BOTTOM, 0, Color.YELLOW);

        mCube.setColor(AbstractCube.FACE_FRONT, size - 1, size - 1, Color.DKGRAY);


        mCube.setSpeed(getSpeed());
        mRubikView.setCube(mCube);
        if (cubeState() != null)
            mCube.restoreColors(cubeState());
    }

    @Override
    public void onClick(View v) {

    }
}
