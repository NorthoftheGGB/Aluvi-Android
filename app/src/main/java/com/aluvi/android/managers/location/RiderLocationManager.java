package com.aluvi.android.managers.location;

import android.content.Context;

import com.aluvi.android.api.gis.LocationUpdateApi;
import com.aluvi.android.managers.callbacks.DataCallback;
import com.google.android.gms.maps.model.LatLng;

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

    public void getDriverLocation(final DataCallback<LatLng> driverLocationCallback) {
        LocationUpdateApi.getDriverLocation(new LocationUpdateApi.OnLocationFetchedListener() {
            @Override
            public void onLocationFetched(LocationUpdateApi.LocationUpdateResponse location) {
                LatLng out = location != null ? new LatLng(location.getLatitude(), location.getLongitude()) : null;
                driverLocationCallback.success(out);
            }

            @Override
            public void onFailure(int statusCode) {
                driverLocationCallback.failure("Unable to fetch driver's location");
            }
        });
    }

    public void queueDriverLocationUpdates(final long durationBetweenUpdatesMillis, final DataCallback<LatLng> driverLocationCallback) {
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                getDriverLocation(driverLocationCallback);
//                handler.postDelayed(this, durationBetweenUpdatesMillis);
//            }
//        }, durationBetweenUpdatesMillis);
    }

    @Override
    public void onLocationUpdated(LatLng newLocation) {
        LocationUpdateApi.updateRiderLocation(newLocation, null);
    }
}
