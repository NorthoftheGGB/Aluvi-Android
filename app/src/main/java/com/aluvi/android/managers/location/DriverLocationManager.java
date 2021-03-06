package com.aluvi.android.managers.location;

import android.content.Context;

import com.aluvi.android.api.gis.GeoLocationUpdateApi;
import com.aluvi.android.managers.callbacks.DataCallback;
import com.google.android.gms.maps.model.LatLng;

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

    public void getRiderLocation(final DataCallback<LatLng> riderLocationCallback) {
        GeoLocationUpdateApi.getRiderLocation(new GeoLocationUpdateApi.OnLocationFetchedListener() {
            @Override
            public void onLocationFetched(GeoLocationUpdateApi.LocationUpdateResponse location) {
                LatLng out = location != null ? new LatLng(location.getLatitude(), location.getLongitude()) : null;
                riderLocationCallback.success(out);
            }

            @Override
            public void onFailure(int statusCode) {
                riderLocationCallback.failure("Unable to fetch rider's location");
            }
        });
    }

    @Override
    public void onLocationUpdated(LatLng newLocation) {
        GeoLocationUpdateApi.updateDriverLocation(newLocation, null);
    }
}
