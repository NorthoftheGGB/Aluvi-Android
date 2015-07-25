package com.aluvi.android.managers.location;

import android.content.Context;

import com.aluvi.android.api.gis.LocationUpdateAPI;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by usama on 7/25/15.
 */
public class DriverLocationManager extends GeoLocationManager {

    private static DriverLocationManager mInstance;

    public static synchronized void initialize(Context context) {
        if (mInstance == null)
            mInstance = new DriverLocationManager(context);
    }

    public static synchronized DriverLocationManager getInstance() {
        return mInstance;
    }

    public DriverLocationManager(Context context) {
        super(context);
    }

    @Override
    public void onLocationUpdated(LatLng newLocation) {
        LocationUpdateAPI.updateDriverLocation(newLocation, null);
    }
}
