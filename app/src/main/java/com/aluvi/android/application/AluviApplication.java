package com.aluvi.android.application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.aluvi.android.R;
import com.aluvi.android.api.AluviApi;
import com.aluvi.android.application.push.PushManager;
import com.aluvi.android.helpers.GeoLocationUtils;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.PaymentManager;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.location.DriverLocationManager;
import com.aluvi.android.managers.location.GeocodingManager;
import com.aluvi.android.managers.location.RiderLocationManager;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.model.LatLng;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

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
        CommuteManager.initialize();
        GeocodingManager.initialize(getString(R.string.mapbox_access_token),
                new GeoLocationUtils.BoundingBox(new LatLng(49.419428, -60.254704),
                new LatLng(25.851146, -125.821106)));
        DriverLocationManager.initialize(this);
        RiderLocationManager.initialize(this);
        PaymentManager.initialize(getString(R.string.stripe_key));

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Bryant-Medium.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AluviRealm.closeDefaultRealm();
    }
}
