package com.aluvi.android.application.push;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by matthewxi on 7/18/15.
 */
public class PushManager {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static void setup(Context context){
        if (PushManager.checkPlayServices(context)) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(context, RegistrationIntentService.class);
            context.startService(intent);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private static boolean checkPlayServices(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                /* TODO Send this out to the current active activity
                GooglePlayServicesUtil.getErrorDialog(resultCode, context,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                        */
                Log.i("PushManager", "Bad news!!  Where is google play services??");

            } else {
                Log.i("PushManager", "This device is not supported.");
            }
            return false;
        }
        return true;
    }
}
