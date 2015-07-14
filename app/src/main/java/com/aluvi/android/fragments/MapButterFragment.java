package com.aluvi.android.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluvi.aluvi.R;
import com.aluvi.android.models.EasyILatLang;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

import butterknife.InjectView;

public class MapButterFragment extends BaseButterFragment
{
    @InjectView(R.id.mapview) MapView mMapView;

    private final String MAP_PAN_LAT_KEY = "map_pan_lat",
            MAP_PAN_LON_KEY = "map_pan_lon",
            ZOOM_KEY = "zoom";

    private final int DEFAULT_ZOOM = 17,
            INVALID_LOCATION = 360;

    public MapButterFragment()
    {
    }

    public static MapButterFragment newInstance()
    {
        return new MapButterFragment();
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void initUI()
    {
        mMapView.setUserLocationEnabled(true);

        float savedLat = getSavedPanLatitude();
        float savedLon = getSavedPanLatitude();

        EasyILatLang mMapLocation;
        if (savedLat != INVALID_LOCATION && savedLon != INVALID_LOCATION)
            mMapLocation = new EasyILatLang(savedLat, savedLon);
        else
            mMapLocation = new EasyILatLang(mMapView.getUserLocation());

        mMapView.setCenter(mMapLocation, false);
        mMapView.setZoom(getSavedZoom());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        saveMapState();
    }

    public void saveMapState()
    {
        LatLng center = mMapView.getCenter();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putFloat(MAP_PAN_LAT_KEY, (float) center.getLatitude());
        editor.putFloat(MAP_PAN_LON_KEY, (float) center.getLongitude());
        editor.putFloat(ZOOM_KEY, mMapView.getZoomLevel());
        editor.commit();
    }

    public float getSavedPanLatitude()
    {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getFloat(MAP_PAN_LAT_KEY, INVALID_LOCATION);
    }

    public float getSavedPanLongitude()
    {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getFloat(MAP_PAN_LON_KEY, INVALID_LOCATION);
    }

    public float getSavedZoom()
    {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getFloat(ZOOM_KEY, DEFAULT_ZOOM);
    }
}
