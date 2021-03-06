package com.aluvi.android.activities;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.aluvi.android.R;
import com.aluvi.android.activities.base.BaseButterActivity;
import com.aluvi.android.api.users.models.DriverProfileData;
import com.aluvi.android.api.users.models.ProfileData;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.fragments.onboarding.LocationSelectFragment;
import com.aluvi.android.fragments.onboarding.ProfilePhotoFragment;
import com.aluvi.android.fragments.onboarding.RegisterFragment;
import com.aluvi.android.fragments.onboarding.WelcomeFragment;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.callbacks.Callback;
import com.aluvi.android.model.local.TicketLocation;
import com.aluvi.android.model.realm.Profile;
import com.aluvi.android.model.realm.RealmLatLng;
import com.aluvi.android.model.realm.Route;

import butterknife.Bind;
import io.realm.Realm;

/**
 * Created by usama on 8/06/15.
 */
public class OnboardingActivity extends BaseButterActivity implements
        RegisterFragment.RegistrationListener,
        LocationSelectFragment.LocationSelectedListener,
        ProfilePhotoFragment.AboutUserListener,
        WelcomeFragment.WelcomeListener {

    @Bind(R.id.onboarding_root_container) View mRootView;
    private Dialog mDefaultProgressDialog;

    public final static String EMAIL_KEY = "email", PASSWORD_KEY = "password";

    private final String REGISTRATION_DATA_KEY = "registration_data",
            DRIVER_REGISTRATION_DATA_KEY = "driver_registration_data",
            HOME_LOC_KEY = "home_loc",
            WORK_LOC_KEY = "work_loc";

    private String mEmail, mPassword;
    private ProfileData mRegistrationData;
    private String mProfileImagePath;
    private DriverProfileData mDriverProfileData;
    private TicketLocation mHomeLoc, mWorkLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mRegistrationData = savedInstanceState.getParcelable(REGISTRATION_DATA_KEY);
            mDriverProfileData = savedInstanceState.getParcelable(DRIVER_REGISTRATION_DATA_KEY);
            mHomeLoc = savedInstanceState.getParcelable(HOME_LOC_KEY);
            mWorkLoc = savedInstanceState.getParcelable(WORK_LOC_KEY);
        }

        if (getIntent() != null && getIntent().getExtras() != null) {
            mEmail = getIntent().getExtras().getString(EMAIL_KEY);
            mPassword = getIntent().getExtras().getString(PASSWORD_KEY);

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.onboarding_root_container);
            if (fragment == null)
                getSupportFragmentManager().beginTransaction().replace(R.id.onboarding_root_container,
                        LocationSelectFragment.newInstance(mHomeLoc, mWorkLoc)).commit();
        } else {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
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
        outState.putParcelable(DRIVER_REGISTRATION_DATA_KEY, mDriverProfileData);
        outState.putParcelable(HOME_LOC_KEY, mHomeLoc);
        outState.putParcelable(WORK_LOC_KEY, mWorkLoc);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mDefaultProgressDialog != null)
            mDefaultProgressDialog.cancel();
    }

    @Override
    public void onLocationSelected(TicketLocation start, TicketLocation end, ProfileData data) {
        mHomeLoc = start;
        mWorkLoc = end;
        mRegistrationData = data;

        attachOnboardingSlideAnimation(getSupportFragmentManager().beginTransaction())
                .replace(R.id.onboarding_root_container, ProfilePhotoFragment.newInstance(mProfileImagePath))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPhotoReady(String profileImagePath) {
        mProfileImagePath = profileImagePath;

        attachOnboardingSlideAnimation(getSupportFragmentManager().beginTransaction())
                .replace(R.id.onboarding_root_container, RegisterFragment.newInstance(mRegistrationData))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onRegistered(final ProfileData data) {
        mRegistrationData.setEmail(mEmail);
        mRegistrationData.setPassword(mPassword);

        attachOnboardingSlideAnimation(getSupportFragmentManager().beginTransaction())
                .replace(R.id.onboarding_root_container, WelcomeFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onTutorialComplete() {
        mDefaultProgressDialog = DialogUtils.showDefaultProgressDialog(this, false);
        UserStateManager.getInstance()
                .registerUser(mRegistrationData, new Callback() {
                    @Override
                    public void success() {
                        saveRoutePreferences();
                    }

                    @Override
                    public void failure(String message) {
                        onError(message);
                    }
                });
    }

    public void saveRoutePreferences() {
        AluviRealm.getDefaultRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmLatLng origin = realm.copyToRealm(
                        new RealmLatLng(mHomeLoc.getLatitude(), mHomeLoc.getLongitude()));
                RealmLatLng destination = realm.copyToRealm(
                        new RealmLatLng(mWorkLoc.getLatitude(), mWorkLoc.getLongitude()));

                Route route = CommuteManager.getInstance().getRoute();
                route.setOrigin(origin);
                route.setOriginPlaceName(mHomeLoc.getPlaceName());
                route.setDestination(destination);
                route.setDestinationPlaceName(mWorkLoc.getPlaceName());
            }
        });

        CommuteManager.getInstance().saveRoute(new Callback() {
            @Override
            public void success() {
                if (mDriverProfileData != null)
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
        UserStateManager.getInstance().registerDriver(mDriverProfileData, new Callback() {
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
        UserStateManager.getInstance().sync(new Callback() {
            @Override
            public void success() {
                onSyncComplete();
            }

            @Override
            public void failure(String message) {
                onError(message);
            }
        });
    }

    private void onSyncComplete() {
        AluviRealm.getDefaultRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Profile userProfile = UserStateManager.getInstance().getProfile();
                userProfile.setProfilePicturePath(mProfileImagePath);
            }
        });

        UserStateManager.getInstance().saveProfile(new Callback() {
            @Override
            public void success() {
                if (mDefaultProgressDialog != null)
                    mDefaultProgressDialog.cancel();

                setResult(Activity.RESULT_OK);
                finish();
            }

            @Override
            public void failure(String message) {
                onError(message);
            }
        });
    }

    private void onError(String message) {
        if (mRootView != null)
            Snackbar.make(mRootView, message, Snackbar.LENGTH_SHORT).show();

        if (mDefaultProgressDialog != null)
            mDefaultProgressDialog.cancel();
    }

    private FragmentTransaction attachOnboardingSlideAnimation(FragmentTransaction transaction) {
        return transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left,
                R.anim.slide_out_right);
    }
}
