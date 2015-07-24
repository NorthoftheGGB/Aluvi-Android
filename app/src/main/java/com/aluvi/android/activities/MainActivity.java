package com.aluvi.android.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;

import com.aluvi.android.R;
import com.aluvi.android.fragments.AluviMapFragment;
import com.aluvi.android.helpers.eventBus.CommuteScheduledEvent;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

public class MainActivity extends AluviAuthActivity implements AluviMapFragment.OnMapEventListener {
    @Bind(R.id.main_navigation_view) NavigationView mNavigationView;
    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    private final String TAG = "MainActivity";

    private final int SCHEDULE_RIDE_REQUEST_CODE = 982;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavigationView();
        onHomeClicked();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    public void initNavigationView() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                getToolbar(), R.string.drawer_open, R.string.drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_drawer_home:
                        onHomeClicked();
                        break;
                    case R.id.action_log_in_toggle:
                        onLoginToggleClicked();
                        break;
                }

                return false;
            }
        });
    }

    @Override
    public void onScheduleRideRequested() {
        startActivityForResult(new Intent(this, ScheduleRideActivity.class), SCHEDULE_RIDE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCHEDULE_RIDE_REQUEST_CODE) {
            if (resultCode == ScheduleRideActivity.RESULT_SCHEDULE_OK) {
                onCommuteScheduled();
            }
        }
    }

    public void onHomeClicked() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, AluviMapFragment.newInstance()).commit();
    }

    public void onLoginToggleClicked() {
        logOut();
    }

    public void onCommuteScheduled() {
        supportInvalidateOptionsMenu();
        EventBus.getDefault().post(new CommuteScheduledEvent());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
