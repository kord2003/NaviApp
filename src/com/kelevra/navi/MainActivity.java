package com.kelevra.navi;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = MainActivity.class.getName();
    private FragmentManager fm;
    private DrawerLayout drawerLayout;
    private StickyListHeadersListView lstDraver;
    private ActionBarDrawerToggle mDrawerToggle;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        lstDraver = (StickyListHeadersListView) findViewById(R.id.lstDraver);
        fm = getFragmentManager();

        MainMenu.loadModel(this);
        MenuAdapter adapter = new MenuAdapter(this);
        lstDraver.setAdapter(adapter);
        lstDraver.setOnItemClickListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //getSupportActionBar().setTitle(mTitle);
                //supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getSupportActionBar().setTitle(mDrawerTitle);
                //supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerLayout.setDrawerListener(mDrawerToggle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Fragment fragment = (Fragment) fm.findFragmentById(R.id.fragmentContainer);
        switch (id) {
            case R.id.action_set_location_1:
                if (fragment != null && fragment instanceof StartFragment) {
                    ((StartFragment)fragment).setLocation1();
                }
                return true;
            case R.id.action_set_location_2:
                if (fragment != null && fragment instanceof StartFragment) {
                    ((StartFragment)fragment).setLocation2();
                }
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fragment fragment = (Fragment) fm.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            /*MenuItems menuItem = MainMenu.getByPosition(0);
            setTitle(menuItem.getName());*/
            setStartFragment();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        //selectItem(position);
    }

    private void setStartFragment() {
        //Log.d(TAG, "backstack = " + fm.getBackStackEntryCount() + " before popToStartFragment()");
        popToStartFragment();
        //Log.d(TAG, "backstack = " + fm.getBackStackEntryCount() + " after popToStartFragment()");
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fragmentContainer, StartFragment.newInstance(null), StartFragment.TAG);
        ft.addToBackStack(StartFragment.TAG);
        ft.commit();
        enableBackNavigation(false);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /*public void openAppInfoFragment() {
        Fragment appInfoFragment = AppInfoFragment.newInstance();
        addFragment(appInfoFragment, AppInfoFragment.TAG);
    }*/

    private void addFragment(Fragment fragment, String tag) {
        FragmentTransaction ft = fm.beginTransaction();
        //ft.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
        ft.add(R.id.fragmentContainer, fragment, tag);
        ft.addToBackStack(tag);
        ft.commit();
        enableBackNavigation(true);
    }

    public void popToStartFragment() {
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fm.executePendingTransactions();
        }
    }

    private void enableBackNavigation(final boolean isEnabled) {
        post(new Runnable() {
            @Override
            public void run() {
                if (isEnabled) {
                    Log.d(TAG, "enableBackNavigation");
                    mDrawerToggle.setDrawerIndicatorEnabled(false);
                } else {
                    Log.d(TAG, "disableBackNavigation");
                    mDrawerToggle.setDrawerIndicatorEnabled(true);
                }
            }
        });
    }

    private void post(Runnable runnable) {
        if(runnable != null) {
            handler.post(runnable);
        }
    }
}
