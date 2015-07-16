package com.aluvi.android.helpers;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by usama on 7/13/15.
 */
public class EasyILatLang implements ILatLng
{
    private double mLat, mLon, mAltitude;

    public EasyILatLang(double latitude, double longitude)
    {
        mLat = latitude;
        mLon = longitude;
    }

    public EasyILatLang(LatLng location)
    {
        this(location != null ? location.getLatitude() : 0, location != null ? location.getLongitude() : 0);
    }

    @Override
    public double getLatitude()
    {
        return mLat;
    }

    @Override
    public double getLongitude()
    {
        return mLon;
    }

    @Override
    public double getAltitude()
    {
        return mAltitude;
    }

    public void setAltitude(double altitude)
    {
        mAltitude = altitude;
    }
}
