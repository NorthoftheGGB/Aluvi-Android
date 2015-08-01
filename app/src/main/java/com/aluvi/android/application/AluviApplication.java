package com.aluvi.android.application;

import android.app.Application;

import com.aluvi.android.R;
import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.gis.GeocodingApi;
import com.aluvi.android.application.push.PushManager;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.location.DriverLocationManager;
import com.aluvi.android.managers.location.RiderLocationManager;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by matthewxi on 7/14/15.
 */
public class AluviApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        PushManager.setup(this);
        AluviRealm.initialize(this);
        GlobalIdentifiers.initialize(this);
        AluviApi.initialize(this);
        UserStateManager.initialize(this);
        GeocodingApi.initialize(this, getString(R.string.mapbox_access_token));
        DriverLocationManager.initialize(this);
        RiderLocationManager.initialize(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AluviRealm.closeDefaultRealm();
    }
}
