package com.aluvi.android.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationTrackingService extends Service implements GoogleApiClient.ConnectionCallbacks, LocationListener
{
    private final static String TAG = "LocationTrackingService";

    public interface OnLocationStatusChanged
    {
        void onLocationChanged(Location newLoc);
    }

    private GoogleApiClient mClient;

    private boolean mIsFetchingLocation;
    private Location mPrevLocation;
    private OnLocationStatusChanged mOnLocationStatusChangedListener;

    public LocationTrackingService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return new LocationTrackingBinder();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.d(TAG, "Starting tracking service");
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        startTrackingLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    public void startTrackingLocation()
    {
        mIsFetchingLocation = true;
        mClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        Log.d(TAG, "Connected to Google API Client. Requesting location updates...");

        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, this);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.d(TAG, "Connection to Google API client suspended");
    }

    public void stopTrackingLocation()
    {
        Log.d(TAG, "Stopping location tracking");
        LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (location != null)
        {
            Log.d(TAG, "Location changed to: " + location);
            if (mPrevLocation == null)
            {
                mPrevLocation = location;
            }
            else
            {
                float[] locationResults = new float[3];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        mPrevLocation.getLatitude(), mPrevLocation.getLongitude(), locationResults);
            }

            if (mOnLocationStatusChangedListener != null)
                mOnLocationStatusChangedListener.onLocationChanged(location);
        }
    }

    public boolean isFetchingLocation()
    {
        return mIsFetchingLocation;
    }

    public void setOnLocationStatusChangedListener(OnLocationStatusChanged listener)
    {
        mOnLocationStatusChangedListener = listener;
    }

    public class LocationTrackingBinder extends Binder
    {
        public LocationTrackingService getService()
        {
            return LocationTrackingService.this;
        }
    }
}
