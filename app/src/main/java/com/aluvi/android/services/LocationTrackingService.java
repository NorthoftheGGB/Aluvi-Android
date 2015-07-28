package com.aluvi.android.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mapbox.mapboxsdk.geometry.LatLng;

import de.greenrobot.event.EventBus;

public class LocationTrackingService extends Service implements GoogleApiClient.ConnectionCallbacks, LocationListener {
    private final static String TAG = "LocationTrackingService";
    private GoogleApiClient mClient;

    public LocationTrackingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Starting tracking service");

        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        startTrackingLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void startTrackingLocation() {
        mClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected to Google API Client. Requesting location updates...");

        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection to Google API client suspended");
    }

    public void stopTrackingLocation() {
        Log.d(TAG, "Stopping location tracking");
        LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
    }

    @Override
    public void onDestroy() {
        stopTrackingLocation();
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            EventBus.getDefault().post(new LocationChangedEvent(new LatLng(location.getLatitude(), location.getLongitude())));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class LocationChangedEvent {
        private LatLng mLocation;

        public LocationChangedEvent(LatLng mLocation) {
            this.mLocation = mLocation;
        }

        public LatLng getLocation() {
            return mLocation;
        }

        public void setLocation(LatLng mLocation) {
            this.mLocation = mLocation;
        }
    }
}
