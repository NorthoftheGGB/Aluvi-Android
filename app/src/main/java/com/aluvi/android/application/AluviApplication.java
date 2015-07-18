package com.aluvi.android.application;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.application.push.PushManager;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.model.local.TicketLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by matthewxi on 7/14/15.
 */
public class AluviApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AluviRealm.initialize(this);
        PushManager.setup(this);
        GlobalIdentifiers.initialize(this);
        AluviApi.initialize(this);
        UserStateManager.initialize(this);
        CommuteManager.initialize(this);

        UserStateManager.getInstance().login("paypal@fromthegut.org", "martian", new UserStateManager.Callback() {
            @Override
            public void success() {
                Toast.makeText(getApplicationContext(), "Logged In", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(String message) {
                Toast.makeText(getApplicationContext(), "Failed to Log In", Toast.LENGTH_SHORT).show();
            }
        });

        // Pretend to set up the commuter prefernces
        CommuteManager.getInstance().setHomeLocation(new TicketLocation(67.2f, 102.3f, "Lost at sea"));
        CommuteManager.getInstance().setWorkLocation(new TicketLocation(67.3f, 102.155f, "Lost at sea"));
        CommuteManager.getInstance().setPickupTime("7:00");
        CommuteManager.getInstance().setReturnTime("16:00");
        CommuteManager.getInstance().setDriving(false);
        CommuteManager.getInstance().save(new CommuteManager.Callback() {
            @Override
            public void success() {
                Toast.makeText(getApplicationContext(), "Set up commute with weird locations", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(String message) {

            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AluviRealm.closeDefaultRealm();
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
