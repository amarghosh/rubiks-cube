package com.amg.rubik;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amg.rubik.ui.AlgorithmFragment;
import com.amg.rubik.ui.CubeFragment;
import com.amg.rubik.ui.SettingsFragment;

public class MainActivity extends Activity implements  ListView.OnItemClickListener {

    private static final String tag = "rubik-main";

    private static final int FRAGMENT_INDEX_HOME = 0;
    private static final int FRAGMENT_INDEX_ALGO = 1;
    private static final int FRAGMENT_INDEX_SETTINGS = 3;

    private String[] mFragmentNames;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = mDrawerTitle = getTitle();
        setupNavigation();
        selectItem(0);
    }

    private void setupNavigation() {
        mFragmentNames = getResources().getStringArray(R.array.fragment_names);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item,
                mFragmentNames));
        mDrawerList.setOnItemClickListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
        selectItem(pos);
    }

    public void selectItem(int pos) {
        FragmentManager manager = getFragmentManager();
        Fragment fragment;
        if (pos == FRAGMENT_INDEX_HOME) {
            fragment = new CubeFragment();
        } else if (pos == FRAGMENT_INDEX_SETTINGS) {
            fragment = new SettingsFragment();
        } else if (pos == FRAGMENT_INDEX_ALGO) {
            fragment = new AlgorithmFragment();
        } else {
            fragment = new PlaceholderFragment();
        }

        manager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        mDrawerList.setItemChecked(pos, true);
        setTitle(mFragmentNames[pos]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
            RelativeLayout rootView = new RelativeLayout(getActivity());
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
            );
            rootView.setLayoutParams(lp);

            TextView textView = new TextView(getActivity());
            textView.setText("Not implemented");

            RelativeLayout.LayoutParams tv_lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            tv_lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            textView.setLayoutParams(tv_lp);
            rootView.addView(textView);

            return rootView;
        }
    }
}

