package com.amg.rubik;

import android.app.Fragment;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final int MIN_CUBE_SIZE = 2;
    private static final int MAX_CUBE_SIZE = 10;

    private View rootView;

    private TextView cubeSizeField;
    private int cubeSize = 3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container);
        initUI();
        return rootView;
    }

    private View findViewById(int id) {
        return rootView.findViewById(id);
    }

    private void initUI() {
        cubeSizeField = (TextView)findViewById(R.id.cube_size);
        cubeSizeField.setText(String.valueOf(cubeSize));
        findViewById(R.id.btn_decrement_size).setOnClickListener(this);
        findViewById(R.id.btn_increment_size).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_decrement_size) {
            if (cubeSize > MIN_CUBE_SIZE) {
                cubeSize--;
                cubeSizeField.setText(String.valueOf(cubeSize));
            }
        }
        if (view.getId() == R.id.btn_increment_size) {
            cubeSize++;
            cubeSizeField.setText(String.valueOf(cubeSize));
        }

        if (cubeSize > MAX_CUBE_SIZE) {
            Toast.makeText(getActivity(), "Cube is too big", Toast.LENGTH_SHORT).show();
        }
    }
}
