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
import com.aluvi.android.activities.base.AluviAuthActivity;
import com.aluvi.android.fragments.CommuteMapFragment;
import com.aluvi.android.fragments.NavigationDrawerHeaderFragment;
import com.aluvi.android.helpers.eventBus.CommuteScheduledEvent;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.location.DriverLocationManager;
import com.aluvi.android.managers.location.RiderLocationManager;
import com.aluvi.android.managers.packages.DataCallback;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;
import com.google.android.gms.maps.model.LatLng;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

public class MainActivity extends AluviAuthActivity implements CommuteMapFragment.OnMapEventListener,
        NavigationDrawerHeaderFragment.ProfileRequestedListener {

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

    @Override
    public void onResume() {
        super.onResume();

        boolean isDriver = UserStateManager.getInstance().isUserDriver();
        mNavigationView.getMenu().findItem(R.id.action_car_info)
                .setVisible(isDriver);
        mNavigationView.getMenu().findItem(R.id.action_car_payments)
                .setVisible(isDriver);
    }

    public void initNavigationView() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                getToolbar(), R.string.drawer_open, R.string.drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_my_commute:
                        onHomeClicked();
                        break;
                }

                return false;
            }
        });
    }

    @Override
    public void onProfileRequested() {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    @Override
    public void onCommuteSchedulerRequested(Trip commuteToView) {
        Intent scheduleRideIntent = new Intent(this, ScheduleRideActivity.class);
        if (commuteToView != null)
            scheduleRideIntent.putExtra(ScheduleRideActivity.COMMUTE_TO_VIEW_ID_KEY, commuteToView.getTripId());
        startActivityForResult(scheduleRideIntent, SCHEDULE_RIDE_REQUEST_CODE);
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

    @Override
    public void startLocationTracking(Ticket ticket) {
        if (ticket.isDriving()) {
            DriverLocationManager.getInstance().startLocationTracking();
        } else {
            RiderLocationManager.getInstance().startLocationTracking();
            RiderLocationManager.getInstance().queueDriverLocationUpdates(5000, new DataCallback<LatLng>() {
                @Override
                public void success(LatLng result) {

                }

                @Override
                public void failure(String message) {

                }
            });
        }
    }

    public void onHomeClicked() {
        if (getSupportFragmentManager().findFragmentById(R.id.container) == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.container, CommuteMapFragment.newInstance()).commit();
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
