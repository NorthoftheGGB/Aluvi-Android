package com.aluvi.android.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.aluvi.android.R;
import com.aluvi.android.activities.base.BaseButterActivity;
import com.aluvi.android.api.users.models.DriverRegistrationData;
import com.aluvi.android.api.users.models.UserRegistrationData;
import com.aluvi.android.fragments.onboarding.AboutUserFragment;
import com.aluvi.android.fragments.onboarding.DriverRegistrationFragment;
import com.aluvi.android.fragments.onboarding.LocationSelectFragment;
import com.aluvi.android.fragments.onboarding.RegisterFragment;
import com.aluvi.android.fragments.onboarding.TutorialFragment;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.model.local.TicketLocation;

import butterknife.Bind;

/**
 * Created by usama on 8/06/15.
 */
public class OnboardingActivity extends BaseButterActivity implements
        RegisterFragment.RegistrationListener,
        LocationSelectFragment.LocationSelectedListener,
        AboutUserFragment.AboutUserListener,
        TutorialFragment.TutorialListener,
        DriverRegistrationFragment.DriverRegistrationListener {

    @Bind(R.id.onboarding_root_container) View mRootView;

    private final String REGISTRATION_DATA_KEY = "registration_data",
            DRIVER_REGISTRATION_DATA_KEY = "driver_registration_data";

    private UserRegistrationData mRegistrationData;
    private DriverRegistrationData mDriverRegistrationData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mRegistrationData = savedInstanceState.getParcelable(REGISTRATION_DATA_KEY);
            mDriverRegistrationData = savedInstanceState.getParcelable(DRIVER_REGISTRATION_DATA_KEY);
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.onboarding_root_container);
        if (fragment == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.onboarding_root_container,
                    RegisterFragment.newInstance()).commit();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(REGISTRATION_DATA_KEY, mRegistrationData);
        outState.putParcelable(DRIVER_REGISTRATION_DATA_KEY, mDriverRegistrationData);
    }

    @Override
    public void onRegistered(UserRegistrationData data) {
        mRegistrationData = data;

        Fragment nextFragment = mRegistrationData.isInterestedDriver() ? DriverRegistrationFragment.newInstance()
                : LocationSelectFragment.newInstance();
        attachOnboardingSlideAnimation(getSupportFragmentManager().beginTransaction())
                .replace(R.id.onboarding_root_container, nextFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDriverRegistrationComplete(DriverRegistrationData data) {
        mDriverRegistrationData = data;

        attachOnboardingSlideAnimation(getSupportFragmentManager().beginTransaction())
                .replace(R.id.onboarding_root_container, LocationSelectFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLocationSelected(TicketLocation start, TicketLocation end) {
        attachOnboardingSlideAnimation(getSupportFragmentManager().beginTransaction())
                .replace(R.id.onboarding_root_container, AboutUserFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onUserDetailsPopulated() {
        attachOnboardingSlideAnimation(getSupportFragmentManager().beginTransaction())
                .replace(R.id.onboarding_root_container, TutorialFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onTutorialRequested() {
        UserStateManager.getInstance()
                .registerUser(mRegistrationData, new UserStateManager.Callback() {
                    @Override
                    public void success() {
                        onRegistrationSuccess();
                    }

                    @Override
                    public void failure(String message) {
                        onError(message);
                    }
                });
    }

    public void onRegistrationSuccess() {
        UserStateManager.getInstance().login(mRegistrationData.getEmail(),
                mRegistrationData.getPassword(), new UserStateManager.Callback() {
                    @Override
                    public void success() {
                        if (mDriverRegistrationData != null)
                            registerDriver();
                        else
                            onSignUpFinished();

                    }

                    @Override
                    public void failure(String message) {
                        onError(message);
                    }
                });
    }

    public void registerDriver() {
        UserStateManager.getInstance().registerDriver(mDriverRegistrationData, new UserStateManager.Callback() {
            @Override
            public void success() {
                onSignUpFinished();
            }

            @Override
            public void failure(String message) {
                onError(message);
            }
        });
    }

    private void onSignUpFinished() {
        finish();
    }

    private void onError(String message) {
        if (mRootView != null)
            Snackbar.make(mRootView, message, Snackbar.LENGTH_SHORT).show();
    }

    private FragmentTransaction attachOnboardingSlideAnimation(FragmentTransaction transaction) {
        return transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left,
                R.anim.slide_out_right);
    }
}
