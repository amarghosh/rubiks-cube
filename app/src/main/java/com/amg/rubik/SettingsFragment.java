package com.amg.rubik;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity implements View.OnClickListener {

    private static final int MIN_CUBE_SIZE = 2;
    private static final int MAX_CUBE_SIZE = 10;

    private TextView cubeSizeField;
    private int cubeSize = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        initUI();
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
            Toast.makeText(this, "Cube is too big", Toast.LENGTH_SHORT).show();
        }
    }
}
