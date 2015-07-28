package com.aluvi.android.managers.location;

import android.content.Context;

import com.aluvi.android.api.gis.LocationUpdateAPI;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by usama on 7/25/15.
 */
public class RiderLocationManager extends GeoLocationManager {

    private static RiderLocationManager mInstance;

    public static synchronized void initialize(Context context) {
        if (mInstance == null)
            mInstance = new RiderLocationManager(context);
    }

    public static synchronized RiderLocationManager getInstance() {
        return mInstance;
    }

    public RiderLocationManager(Context context) {
        super(context);
    }

    @Override
    public void onLocationUpdated(LatLng newLocation) {
        LocationUpdateAPI.updateRiderLocation(newLocation, null);
    }
}
