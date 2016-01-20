package com.amg.rubik.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amg.rubik.Constants;
import com.amg.rubik.MainActivity;
import com.amg.rubik.R;

public class SettingsFragment extends AbstractFragment {

    private static final int MIN_CUBE_SIZE = 1;
    private static final int MAX_CUBE_SIZE = 9;

    private TextView cubeSizeField;
    private NumberPicker scramblingCountPicker;
    private Spinner speedSpinner;
    private Spinner scramblingModeSpinner;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        tag = "rubik-settings";
        mRootView = inflater.inflate(R.layout.fragment_settings, container, false);
        initUI();
        return mRootView;
    }

    private void initUI() {
        cubeSizeField = (TextView)findViewById(R.id.cube_size);
        cubeSizeField.setText(String.valueOf(cubeSize()));
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

        scramblingModeSpinner = (Spinner)findViewById(R.id.scrambling_mode_spinner);
        scramblingModeSpinner.setSelection(getScrambleMode());
        scramblingModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setScrambleMode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        scramblingCountPicker = (NumberPicker)findViewById(R.id.scrambling_count_picker);
        scramblingCountPicker.setValue(scrambleCount());
        scramblingCountPicker.setValueChangedListener(new NumberPicker.ValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                setScrambleCount(value);
            }
        });

        findViewById(R.id.btn_decrement_size).setOnClickListener(this);
        findViewById(R.id.btn_increment_size).setOnClickListener(this);
        findViewById(R.id.btn_reset).setOnClickListener(this);
        findViewById(R.id.btn_play).setOnClickListener(this);
    }

    private void reset() {
        // Reset values
        setCubeSize(Constants.DEFAULT_CUBE_SIZE);
        setScrambleCount(Constants.DEFAULT_SCRAMBLE_COUNT);
        setScrambleMode(Constants.DEFAULT_SCRAMBLE_MODE_INDEX);
        setSpeed(Constants.DEFAULT_SPEED_INDEX);

        // Update UI
        cubeSizeField.setText(String.valueOf(cubeSize()));
        scramblingCountPicker.setValue(scrambleCount());
        scramblingModeSpinner.setSelection(getScrambleMode(), true);
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
