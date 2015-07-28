package com.aluvi.android.managers.location;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aluvi.android.services.LocationTrackingService;
import com.mapbox.mapboxsdk.geometry.LatLng;

import de.greenrobot.event.EventBus;

/**
 * Created by usama on 7/25/15.
 * Package private on purpose - clients should not be have access to this class because
 * they should only be allowed to access subclasses of this class via singletons.
 */
abstract class GeoLocationManager {

    private final String TAG = "GeolocationManager";
    private Context mContext;

    public GeoLocationManager(Context context) {
        mContext = context;
    }

    public void startLocationTracking() {
        EventBus.getDefault().register(this);
        mContext.startService(new Intent(mContext, LocationTrackingService.class));
    }

    public void stopLocationTracking() {
        EventBus.getDefault().unregister(this);
        mContext.stopService(new Intent(mContext, LocationTrackingService.class));
    }

    @SuppressWarnings("unused")
    public void onEvent(LocationTrackingService.LocationChangedEvent event) {
        LatLng currentLocation = event.getLocation();
        if (currentLocation != null) {
            Log.e(TAG, "User's location updated to: " + currentLocation);
        }
    }

    public abstract void onLocationUpdated(LatLng newLocation);
}
