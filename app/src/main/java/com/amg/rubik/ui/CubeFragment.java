package com.amg.rubik.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amg.rubik.Constants;
import com.amg.rubik.cube.CubeListener;
import com.amg.rubik.R;
import com.amg.rubik.graphics.RubikGLSurfaceView;
import com.amg.rubik.cube.RubiksCube;
import com.amg.rubik.cube.RubiksCube3x3x3;

public class CubeFragment extends AbstractFragment
        implements CubeListener {

    private int SCR_MODE_INSTANT = 0;
    private int SCR_MODE_ANIMATED = 1;

    private int SPEED_SLOW = 0;
    private int SPEED_MEDIUM = 1;
    private int SPEED_FAST = 2;

    private RubiksCube mCube = null;
    private RubikGLSurfaceView mRubikView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tag = "rubik-game";
        mRootView = inflater.inflate(R.layout.fragment_cube, container, false);
        initUi();
        initializeRubikView();
        return mRootView;
    }

    private void initUi() {
        findViewById(R.id.randomizeButton).setOnClickListener(this);
        findViewById(R.id.solveButton).setOnClickListener(this);
        findViewById(R.id.btn_undo).setOnClickListener(this);
        ToggleButton btn = (ToggleButton)findViewById(R.id.btn_rotate_mode);
        btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mRubikView.setWholeCubeRotation(b);
            }
        });
    }

    private void initializeRubikView() {
        ViewGroup view = (ViewGroup)findViewById(R.id.cube_holder);
        mRubikView = new RubikGLSurfaceView(getActivity());
        createCube();
        view.addView(mRubikView);
        mRubikView.setWholeCubeRotation(
                ((ToggleButton) findViewById(R.id.btn_rotate_mode)).isChecked());
    }

    @Override
    public void onResume() {
        super.onResume();
        mRubikView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mRubikView.onPause();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.randomizeButton:
                randomizeOnclick();
                break;

            case R.id.solveButton:
                mRubikView.printDebugInfo();
                solve();
                break;

            case R.id.btn_undo:
                mCube.undo();
                break;
        }
    }

    private void createCube() {
        if (cubeSize() == 3) {
            mCube = new RubiksCube3x3x3();
        } else {
            mCube = new RubiksCube(cubeSize());
        }
        mCube.setSpeed(getSpeed());
        mCube.setListener(this);
        mRubikView.setCube(mCube);
        if (cubeState() != null)
            mCube.restoreColors(cubeState());
    }

    private void resetButtons() {
        Button btn = (Button)findViewById(R.id.randomizeButton);
        btn.setEnabled(true);
        btn.setText(R.string.randomize);
        btn = (Button)findViewById(R.id.solveButton);
        btn.setEnabled(true);
        btn.setText(R.string.solve);
    }

    private void solve() {
        Button btn = (Button)findViewById(R.id.solveButton);
        if (mCube.getState() == RubiksCube.CubeState.IDLE) {
            int solveRet = mCube.solve();
            if (solveRet == 0) {
                btn.setText(R.string.cancel);
                btn = (Button)findViewById(R.id.randomizeButton);
                btn.setEnabled(false);
            }
        } else if (mCube.getState() == RubiksCube.CubeState.SOLVING) {
            mCube.cancelSolving();
            btn.setText(R.string.solve);
            btn = (Button)findViewById(R.id.randomizeButton);
            btn.setEnabled(true);
        }
    }

    private void randomizeOnclick() {
        Button btn = (Button)findViewById(R.id.randomizeButton);
        if (mCube.getState() == RubiksCube.CubeState.IDLE) {
            if (getScrambleMode() == SCR_MODE_INSTANT) {
                mCube.randomize(scrambleCount());
                return;
            }
            mRubikView.printDebugInfo();
            mCube.randomize();
            btn.setText(R.string.stop);
            btn = (Button)findViewById(R.id.solveButton);
            btn.setEnabled(false);
        } else if (mCube.getState() == RubiksCube.CubeState.RANDOMIZE) {
            mCube.stopRandomize();
            mRubikView.printDebugInfo();
            btn.setText(R.string.randomize);
            btn = (Button)findViewById(R.id.solveButton);
            btn.setEnabled(true);
        }
    }

    private Toast currentToast;
    public void handleCubeMessage(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentToast != null) currentToast.cancel();
                currentToast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
                currentToast.show();
            }
        });
    }

    @Override
    public void handleCubeSolved() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resetButtons();
                mRubikView.printDebugInfo();
            }
        });
    }
}
