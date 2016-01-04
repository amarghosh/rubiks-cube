package com.amg.rubik;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity
        implements View.OnClickListener, CubeListener {

    private static final String tag = "rubik-main";

    private RubiksCube mCube = null;
    private RubikGLSurfaceView mRubikView = null;
    private int cubeSize = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(tag, "onCreate");
        setContentView(R.layout.activity_main);
        initializeRubikView();
        initUi();
    }

    private void initUi() {
        findViewById(R.id.randomizeButton).setOnClickListener(this);
        findViewById(R.id.solveButton).setOnClickListener(this);
        findViewById(R.id.incButton).setOnClickListener(this);
        findViewById(R.id.decButton).setOnClickListener(this);
    }

    private void initializeRubikView() {
        if (mCube != null) {
            Log.w(tag, "mCube is not null in init");
            return;
        }

        ViewGroup view = (ViewGroup)findViewById(R.id.container);
        mRubikView = new RubikGLSurfaceView(this);
        createCube();
        view.addView(mRubikView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRubikView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRubikView.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        Handle action bar item clicks here. The action bar will
        automatically handle clicks on the Home/Up button, so long
        as you specify a parent activity in AndroidManifest.xml.
        */
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.author) {
            return true;
        }

        if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
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

            case R.id.incButton:
                changeCubeSize(1);
                break;

            case R.id.decButton:
                changeCubeSize(-1);
                break;
        }
    }

    private void createCube() {
        if (cubeSize == 3) {
            mCube = new RubiksCube3x3x3();
        } else {
            mCube = new RubiksCube(cubeSize);
        }
        mCube.setListener(this);
        mRubikView.setCube(mCube);
    }

    private void changeCubeSize(int factor) {
        if (mCube.getState() != RubiksCube.CubeState.IDLE) {
            Toast.makeText(this, "Cube is in state " + mCube.getState(), Toast.LENGTH_SHORT).show();
            return;
        }
        if (cubeSize == 1 && factor < 0) {
            return;
        }
        cubeSize += factor;
        createCube();
        resetButtons();
        if (cubeSize > 9) {
            Toast.makeText(this,
                    "Cube is too big. May not render correctly", Toast.LENGTH_SHORT).show();
        }
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
        int solveRet = mCube.solve();
        if (solveRet == 0) {
            Button btn = (Button)findViewById(R.id.solveButton);
            btn.setEnabled(false);
            btn = (Button)findViewById(R.id.randomizeButton);
            btn.setEnabled(false);
        }
    }

    private void randomizeOnclick() {
        Button btn = (Button)findViewById(R.id.randomizeButton);
        if (mCube.getState() == RubiksCube.CubeState.IDLE) {
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
        final Context ctx = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentToast != null) currentToast.cancel();
                currentToast = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT);
                currentToast.show();
            }
        });
    }

    @Override
    public void handleCubeSolved() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button btn = (Button) findViewById(R.id.solveButton);
                btn.setEnabled(true);
                btn = (Button) findViewById(R.id.randomizeButton);
                btn.setEnabled(true);
                mRubikView.printDebugInfo();
            }
        });
    }
}
