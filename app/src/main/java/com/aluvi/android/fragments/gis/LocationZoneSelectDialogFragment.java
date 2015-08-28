package com.aluvi.android.fragments.gis;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aluvi.android.R;
import com.aluvi.android.helpers.views.mapbox.CircleOverlay;
import com.aluvi.android.model.local.TicketLocation;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.overlay.Overlay;

/**
 * Created by usama on 8/26/15.
 */
public class LocationZoneSelectDialogFragment extends LocationSelectDialogFragment {
    protected static String ZONE_RADIUS_KEY = "zone_radius_key";
    private Overlay mCircleOverlay;
    private double mZoneRadiusMiles;

    public static LocationZoneSelectDialogFragment newInstance(TicketLocation currentlySelectionLocation,
                                                               double zoneRadius) {
        Bundle args = new Bundle();
        args.putParcelable(LocationSelectDialogFragment.TICKET_KEY, currentlySelectionLocation);
        args.putDouble(ZONE_RADIUS_KEY, zoneRadius);

        LocationZoneSelectDialogFragment fragment = new LocationZoneSelectDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static LocationZoneSelectDialogFragment newInstance(boolean defaultToUserLocation,
                                                               int zoneRadius) {
        Bundle args = new Bundle();
        args.putBoolean(DEFAULT_USER_LOCATION_KEY, defaultToUserLocation);
        args.getDouble(ZONE_RADIUS_KEY, zoneRadius);

        LocationZoneSelectDialogFragment fragment = new LocationZoneSelectDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mZoneRadiusMiles = getArguments().getInt(ZONE_RADIUS_KEY);
        }
    }

    @Override
    protected boolean addMarker(ILatLng latLng) {
        if (super.addMarker(latLng)) {
            getMapView().removeOverlay(mCircleOverlay);
            mCircleOverlay = new CircleOverlay(latLng, milesToKm(mZoneRadiusMiles),
                    getResources().getColor(R.color.zone_radius_background_color));
            getMapView().addOverlay(mCircleOverlay);
            return true;
        }

        return false;
    }

    private double milesToKm(double miles) {
        return miles * 1.60934;
    }
}
