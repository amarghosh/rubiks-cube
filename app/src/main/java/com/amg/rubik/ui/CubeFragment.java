package com.amg.rubik.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amg.rubik.MainActivity;
import com.amg.rubik.cube.CubeListener;
import com.amg.rubik.R;
import com.amg.rubik.graphics.RubikGLSurfaceView;
import com.amg.rubik.cube.RubiksCube;
import com.amg.rubik.cube.RubiksCube3x3x3;

public class CubeFragment extends BaseFragment
        implements CubeListener {

    private RubiksCube mCube = null;
    private RubikGLSurfaceView mRubikView = null;
    private TextView mMovesField = null;
    private boolean mGameInProgress = false;

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
        findViewById(R.id.btn_more_settings).setOnClickListener(this);
        ToggleButton btn = (ToggleButton)findViewById(R.id.btn_rotate_mode);
        btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mRubikView.setWholeCubeRotation(b);
            }
        });
        mMovesField = (TextView)findViewById(R.id.tv_movecount);
    }

    private void initializeRubikView() {
        ViewGroup view = (ViewGroup)findViewById(R.id.cube_holder);
        mRubikView = new RubikGLSurfaceView(getActivity());
        mRubikView.enableRotations();
        createCube();
        view.addView(mRubikView);
        mRubikView.setWholeCubeRotation(
                ((ToggleButton) findViewById(R.id.btn_rotate_mode)).isChecked());
    }

    @Override
    public void onResume() {
        super.onResume();
        mRubikView.onResume();
        getActivity().getActionBar().hide();
    }

    @Override
    public void onPause() {
        super.onPause();
        mRubikView.onPause();
        getActivity().getActionBar().show();
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

            case R.id.btn_more_settings:
                ((MainActivity)getActivity()).openDrawer();
                break;
        }
    }

    private void createCube() {
        if (cubeSizeX() == cubeSizeY() && cubeSizeY() == cubeSizeZ() &&
                cubeSizeZ() == 3) {
            mCube = new RubiksCube3x3x3();
        } else {
            mCube = new RubiksCube(cubeSizeX(), cubeSizeY(), cubeSizeZ());
        }
        mCube.setSpeed(0);
        mCube.setListener(this);
        mRubikView.setCube(mCube);
        if (cubeState() != null)
            mCube.restoreColors(cubeState());
    }

    private void resetButtons() {
        ImageButton btn = (ImageButton)findViewById(R.id.randomizeButton);
        btn.setEnabled(true);
        btn = (ImageButton)findViewById(R.id.solveButton);
        btn.setEnabled(true);
    }

    private void solve() {
        ImageButton btn = (ImageButton)findViewById(R.id.solveButton);
        if (mCube.getState() == RubiksCube.CubeState.IDLE) {
            int solveRet = mCube.solve();
            if (solveRet == 0) {
                btn = (ImageButton)findViewById(R.id.randomizeButton);
                btn.setEnabled(false);
            }
        } else if (mCube.getState() == RubiksCube.CubeState.SOLVING) {
            mCube.cancelSolving();
            showToast("Cancelled solving");
            btn = (ImageButton)findViewById(R.id.randomizeButton);
            btn.setEnabled(true);
        }
    }

    private void randomizeOnclick() {
        if (mCube.getState() != RubiksCube.CubeState.IDLE) {
            return;
        }
        mCube.reset();
        mCube.randomize(scrambleCount());
        updateCount();
        mGameInProgress = true;
    }

    private Toast currentToast;
    public void handleCubeMessage(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(msg);
            }
        });
    }

    private void showToast(String txt) {
        if (currentToast != null) currentToast.cancel();
        currentToast = Toast.makeText(getActivity(), txt, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    @Override
    public void handleCubeSolved() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _handleCubeSolved();
            }
        });
    }

    private void _handleCubeSolved() {
        resetButtons();
        mRubikView.printDebugInfo();
        if (mGameInProgress) {
            int moves = mCube.getMoveCount();
            mGameInProgress = false;
            showToast(String.format("Solved in %d move%c",
                    moves, moves == 1 ? ' ' : 's'));
        }
    }

    private void updateCount() {
        mMovesField.setText(String.valueOf(mCube.getMoveCount()));
    }

    @Override
    public void handleRotationCompleted() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateCount();
            }
        });
    }
}
