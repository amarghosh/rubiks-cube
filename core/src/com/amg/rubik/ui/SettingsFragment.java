package com.amg.rubik.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.amg.rubik.Constants;
import com.amg.rubik.MainActivity;
import com.amg.rubik.R;

public class SettingsFragment extends BaseFragment {

    private static final int MIN_CUBE_SIZE = 1;
    private static final int MAX_CUBE_SIZE = 9;

    private NumberPicker cubeSizePickerX;
    private NumberPicker cubeSizePickerY;
    private NumberPicker cubeSizePickerZ;
    private NumberPicker scramblingCountPicker;
    private Spinner speedSpinner;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        tag = "rubik-settings";
        mRootView = inflater.inflate(R.layout.fragment_settings, container, false);
        initUI();
        return mRootView;
    }

    private void initNP() {
        cubeSizePickerX = (NumberPicker)findViewById(R.id.cube_size_x);
        cubeSizePickerX.setValue(cubeSizeX());
        cubeSizePickerX.setValueChangedListener(new NumberPicker.ValueChangedListener() {
            @Override
            public void onValueChanged(int id, int value) {
                onCubeSizeChanged(id, value);
            }
        });

        cubeSizePickerY = (NumberPicker)findViewById(R.id.cube_size_y);
        cubeSizePickerY.setValue(cubeSizeY());
        cubeSizePickerY.setValueChangedListener(new NumberPicker.ValueChangedListener() {
            @Override
            public void onValueChanged(int id, int value) {
                onCubeSizeChanged(id, value);
            }
        });

        cubeSizePickerZ = (NumberPicker)findViewById(R.id.cube_size_z);
        cubeSizePickerZ.setValue(cubeSizeZ());
        cubeSizePickerZ.setValueChangedListener(new NumberPicker.ValueChangedListener() {
            @Override
            public void onValueChanged(int id, int value) {
                onCubeSizeChanged(id, value);
            }
        });
    }

    private void initUI() {
        initNP();
        speedSpinner = (Spinner)findViewById(R.id.speed_spinner);
        speedSpinner.setSelection(getSpeed());
        speedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSpeed(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        scramblingCountPicker = (NumberPicker)findViewById(R.id.scrambling_count_picker);
        scramblingCountPicker.setValue(scrambleCount());
        scramblingCountPicker.setValueChangedListener(new NumberPicker.ValueChangedListener() {
            @Override
            public void onValueChanged(int id, int value) {
                setScrambleCount(value);
            }
        });

        findViewById(R.id.btn_reset).setOnClickListener(this);
        findViewById(R.id.btn_play).setOnClickListener(this);
    }

    private void reset() {
        // Reset values
        setCubeSizeX(Constants.DEFAULT_CUBE_SIZE);
        setCubeSizeY(Constants.DEFAULT_CUBE_SIZE);
        setCubeSizeZ(Constants.DEFAULT_CUBE_SIZE);
        setScrambleCount(Constants.DEFAULT_SCRAMBLE_COUNT);
        setSpeed(Constants.DEFAULT_SPEED_INDEX);

        // Update UI
        cubeSizePickerX.setValue(cubeSizeX());
        cubeSizePickerY.setValue(cubeSizeY());
        cubeSizePickerZ.setValue(cubeSizeZ());
        scramblingCountPicker.setValue(scrambleCount());
        speedSpinner.setSelection(getSpeed(), true);
    }

    /**
     * TODO: Make this independent of activity holding the fragment
     * Is it possible using fragment manager?
     * */
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
        }
    }

    private void onCubeSizeChanged(int id, int size) {
        if (size > MAX_CUBE_SIZE) {
            Toast.makeText(getActivity(),
                    "Big cubes may not be rendered properly", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.cube_size_x)
            setCubeSizeX(size);
        else if (id == R.id.cube_size_y)
            setCubeSizeY(size);
        else
            setCubeSizeZ(size);
    }
}
