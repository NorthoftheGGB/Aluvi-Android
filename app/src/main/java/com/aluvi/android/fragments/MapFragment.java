package com.aluvi.android.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluvi.aluvi.R;
import com.aluvi.android.helpers.views.MapBoxStateSaver;
import com.aluvi.android.models.EasyILatLang;
import com.mapbox.mapboxsdk.views.MapView;

import butterknife.InjectView;

public class MapFragment extends BaseButterFragment
{
    @InjectView(R.id.mapview) MapView mMapView;
    private final String MAP_STATE_KEY = "map_fragment_main";

    public MapFragment()
    {
    }

    public static MapFragment newInstance()
    {
        return new MapFragment();
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

        if (!MapBoxStateSaver.restoreMapState(mMapView, MAP_STATE_KEY))
            mMapView.setCenter(new EasyILatLang(mMapView.getUserLocation()), false);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        MapBoxStateSaver.saveMapState(mMapView, MAP_STATE_KEY);
    }
}
