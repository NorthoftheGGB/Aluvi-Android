package com.aluvi.android.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.aluvi.android.R;
import com.aluvi.android.activities.base.BaseButterActivity;
import com.aluvi.android.fragments.onboarding.LocationSelectFragment;
import com.aluvi.android.fragments.onboarding.RegisterFragment;
import com.aluvi.android.model.local.TicketLocation;

/**
 * Created by usama on 8/06/15.
 */
public class OnboardingActivity extends BaseButterActivity implements RegisterFragment.RegistrationListener,
        LocationSelectFragment.LocationSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.onboarding_root_container);
        if (fragment == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.onboarding_root_container,
                    RegisterFragment.newInstance()).addToBackStack(null).commit();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_onboarding;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_onboarding, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRegistered() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left,
                R.anim.slide_out_right);
        transaction.replace(R.id.onboarding_root_container, LocationSelectFragment.newInstance())
                .addToBackStack(null).commit();
    }

    @Override
    public void onLocationSelected(TicketLocation start, TicketLocation end) {

    }
}
