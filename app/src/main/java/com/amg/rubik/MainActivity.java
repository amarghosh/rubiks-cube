package com.amg.rubik;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

public class MainActivity extends Activity {

	private static final String tag = "rubik-main";

	private RubiksCube mCube = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		initializeRubikView();
    }

	private void initializeRubikView() {
		if (mCube != null) {
			Log.w(tag, "mCube is not null in init");
			return;
		}
		
		mCube = new RubiksCube(this);
		ViewGroup view = (ViewGroup)findViewById(R.id.container);
		view.addView(mCube.getView());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mCube.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mCube.onPause();
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

        return super.onOptionsItemSelected(item);
    }
}
