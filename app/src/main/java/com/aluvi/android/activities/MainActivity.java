package com.aluvi.android.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;

import com.aluvi.aluvi.R;
import com.aluvi.android.fragments.MapFragment;

import butterknife.InjectView;

public class MainActivity extends BaseToolBarActivity
{
    @InjectView(R.id.main_navigation_view) NavigationView mNavigationView;
    @InjectView(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initNavigationView();
        onHomeClicked();
    }

    @Override
    public int getLayoutId()
    {
        return R.layout.activity_main;
    }

    public void initNavigationView()
    {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                getToolbar(), R.string.drawer_open, R.string.drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.drawer_home:
                        onHomeClicked();
                        break;
                }

                return false;
            }
        });
    }

    public void onHomeClicked()
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, MapFragment.newInstance()).commit();
    }

    public void onScheduleRideClicked()
    {
        startActivity(new Intent(this, ScheduleRideActivity.class));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_schedule_ride:
                onScheduleRideClicked();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
