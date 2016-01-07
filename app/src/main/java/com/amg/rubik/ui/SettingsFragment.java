package com.amg.rubik.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amg.rubik.Constants;
import com.amg.rubik.MainActivity;
import com.amg.rubik.R;
import com.amg.rubik.ui.AbstractFragment;

public class SettingsFragment extends AbstractFragment {

    private static final int MIN_CUBE_SIZE = 1;
    private static final int MAX_CUBE_SIZE = 9;

    private TextView cubeSizeField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tag = "rubik-settings";
        mRootView = inflater.inflate(R.layout.fragment_settings, container, false);
        initUI();
        return mRootView;
    }

    private void initUI() {
        cubeSizeField = (TextView)findViewById(R.id.cube_size);
        cubeSizeField.setText(String.valueOf(cubeSize()));
        findViewById(R.id.btn_decrement_size).setOnClickListener(this);
        findViewById(R.id.btn_increment_size).setOnClickListener(this);
        findViewById(R.id.btn_reset).setOnClickListener(this);
        findViewById(R.id.btn_play).setOnClickListener(this);
    }

    private void reset() {
        // Reset values
        setCubeSize(Constants.DEFAULT_CUBE_SIZE);

        // Update UI
        cubeSizeField.setText(String.valueOf(cubeSize()));
    }

    // TODO: Can we do this using getActivity.getFragmentManager alone?
    private void play() {
        MainActivity activity = (MainActivity)getActivity();
        activity.selectItem(0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reset:
                reset();
                break;

            case R.id.btn_play:
                play();
                break;

            case R.id.btn_decrement_size:
            case R.id.btn_increment_size:
                onCubeSizeButtonClick(view);
                break;
        }
    }

    private void onCubeSizeButtonClick(View view) {
        int size = cubeSize();
        if (view.getId() == R.id.btn_decrement_size && size > MIN_CUBE_SIZE) {
            size--;
        }
        if (view.getId() == R.id.btn_increment_size) {
            size++;
        }

        cubeSizeField.setText(String.valueOf(size));

        if (size > MAX_CUBE_SIZE) {
            Toast.makeText(getActivity(),
                    "Big cubes may not be rendered properly", Toast.LENGTH_SHORT).show();
        }
        setCubeSize(size);
    }
}
