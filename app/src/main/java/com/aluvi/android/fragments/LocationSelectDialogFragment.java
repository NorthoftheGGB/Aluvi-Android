package com.aluvi.android.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.api.gis.GeocodingApi;
import com.aluvi.android.helpers.EasyILatLang;
import com.aluvi.android.helpers.views.GeocodingAutoCompleteBinder;
import com.aluvi.android.helpers.views.MapBoxStateSaver;
import com.aluvi.android.helpers.views.ViewHelpers;
import com.aluvi.android.model.local.TicketLocation;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by usama on 7/13/15.
 */
public class LocationSelectDialogFragment extends DialogFragment {
    public interface OnLocationSelectedListener {
        void onLocationSelected(TicketLocation address, LocationSelectDialogFragment fragment);
    }

    @Bind(R.id.location_select_progress_bar) ProgressBar mSearchProgressBar;
    @Bind(R.id.location_select_auto_complete_search) AutoCompleteTextView mLocationSearchAutoCompleteTextView;
    @Bind(R.id.location_select_map_view) MapView mMapView;

    private final static String TAG = "LocationSelectFragment", MAP_STATE_KEY = "location_select_map",
            TICKET_KEY = "ticket_location";

    private OnLocationSelectedListener mLocationSelectedListener;
    private TicketLocation mCurrentlySelectedLocation;
    private GeocodingAutoCompleteBinder mGeocodingAutoCompleteBinder;

    public static LocationSelectDialogFragment newInstance(TicketLocation currentlySelectionLocation) {
        Bundle args = new Bundle();
        args.putParcelable(TICKET_KEY, currentlySelectionLocation);

        LocationSelectDialogFragment fragment = new LocationSelectDialogFragment();
        fragment.mCurrentlySelectedLocation = currentlySelectionLocation;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mLocationSelectedListener = (OnLocationSelectedListener) activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null)
            mCurrentlySelectedLocation = getArguments().getParcelable(TICKET_KEY);

        final View rootView = View.inflate(getActivity(), R.layout.fragment_location_select, null);
        ButterKnife.bind(this, rootView);

        initMap();
        initAutoCompleteTextView(rootView);

        return new MaterialDialog.Builder(getActivity())
                .customView(rootView, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        if (mLocationSelectedListener != null) {
                            mLocationSelectedListener.onLocationSelected(mCurrentlySelectedLocation,
                                    LocationSelectDialogFragment.this);
                        }
                    }
                })
                .build();
    }

    private void initMap() {
        mMapView.setUserLocationEnabled(true);
        MapBoxStateSaver.restoreMapState(mMapView, MAP_STATE_KEY);

        if (mCurrentlySelectedLocation != null &&
                mCurrentlySelectedLocation.getLatitude() != GeocodingApi.INVALID_LOCATION &&
                mCurrentlySelectedLocation.getLongitude() != GeocodingApi.INVALID_LOCATION)
            mMapView.setCenter(new EasyILatLang(mCurrentlySelectedLocation.getLatitude(), mCurrentlySelectedLocation.getLongitude()));
        else if (mMapView.getUserLocation() != null)
            mMapView.setCenter(mMapView.getUserLocation());

        mMapView.setMapViewListener(new MapViewListener() {
            @Override
            public void onShowMarker(MapView mapView, Marker marker) {
            }

            @Override
            public void onHideMarker(MapView mapView, Marker marker) {
            }

            @Override
            public void onTapMarker(MapView mapView, Marker marker) {
            }

            @Override
            public void onLongPressMarker(MapView mapView, Marker marker) {
            }

            @Override
            public void onTapMap(MapView mapView, ILatLng iLatLng) {
                addMarker(iLatLng);
            }

            @Override
            public void onLongPressMap(MapView mapView, ILatLng iLatLng) {
            }
        });
    }

    private void initAutoCompleteTextView(final View rootView) {
        if (mCurrentlySelectedLocation != null)
            mLocationSearchAutoCompleteTextView.setText(mCurrentlySelectedLocation.getPlaceName());

        mGeocodingAutoCompleteBinder = new GeocodingAutoCompleteBinder(mLocationSearchAutoCompleteTextView);
        mLocationSearchAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ViewHelpers.hideKeyboardFragment(getActivity(), rootView); // Can't hide using the parent activity because of focus issues

                Address clickedAddress = mGeocodingAutoCompleteBinder.getAdapter().getItem(position);
                TicketLocation parsedAddress = new TicketLocation(clickedAddress);
                addMarkerForAddress(parsedAddress);

                mMapView.setCenter(new EasyILatLang(clickedAddress.getLatitude(), clickedAddress.getLongitude()));
                mCurrentlySelectedLocation = parsedAddress;
            }
        });

        mGeocodingAutoCompleteBinder.setLocationUpdateListener(new GeocodingAutoCompleteBinder.OnGeoLocationUpdateListener() {
            @Override
            public void onGeoCodeStarted() {
                onLocationSearchStarted();
            }

            @Override
            public void onGeoCodeFinished(List<Address> addresses) {
                onLocationSearchFinished();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        MapBoxStateSaver.saveMapState(mMapView, MAP_STATE_KEY);
    }

    private void addMarkerForAddress(TicketLocation address) {
        Marker marker = getDefaultMarker(address.getPlaceName(), "",
                new LatLng(address.getLatitude(), address.getLongitude()));

        mMapView.clear();
        mMapView.addMarker(marker);
    }

    private void addMarker(final ILatLng latLng) {
        mMapView.clear();
        mMapView.addMarker(getDefaultMarker(null, null, new LatLng(latLng.getLatitude(), latLng.getLongitude())));

        Log.d(TAG, "Looking for address for custom marker location");
        onLocationSearchStarted();
        GeocodingApi.getInstance(getActivity())
                .getAddressesForLocation(latLng.getLatitude(), latLng.getLongitude(), new GeocodingApi.GeocodingApiCallback() {
                    @Override
                    public void onAddressesFound(String query, List<Address> data) {
                        onAddressesFoundForLocation(latLng.getLatitude(), latLng.getLongitude(), data);
                    }

                    @Override
                    public void onFailure(int statusCode) {
                        Log.e(TAG, "Error geocoding latlng. Status code: " + statusCode);
                    }
                });
    }

    private void onAddressesFoundForLocation(double lat, double lng, List<Address> data) {
        Log.d(TAG, "Found addresses for custom marker location");

        if (data != null && data.size() > 0) {
            Address address = data.get(0);

            float[] distanceArr = new float[3];
            Location.distanceBetween(address.getLatitude(), address.getLongitude(), lat, lng, distanceArr);
            float distance = distanceArr[0];

            if (distance > 10) // Store the address only if its less than 10 meters away from the placed pin
            {
                Log.d(TAG, "Custom location is far away from fetched address");
                address.setLocality(null);
            }

            TicketLocation parsedAddress = new TicketLocation(address);
            addMarkerForAddress(parsedAddress);
            mGeocodingAutoCompleteBinder.updateAddresses(data);
            if (address != null && mLocationSearchAutoCompleteTextView != null)
                mLocationSearchAutoCompleteTextView.setText(GeocodingApi.getFormattedAddress(address));

            mCurrentlySelectedLocation = parsedAddress;
        }
    }

    private void onLocationSearchStarted() {
        mSearchProgressBar.setVisibility(View.VISIBLE);
    }

    private void onLocationSearchFinished() {
        if (mSearchProgressBar != null) { // Frequently called in callbacks, so check that we still have access to these guys
            mSearchProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private Marker getDefaultMarker(String title, String description, LatLng latLng) {
        Marker marker = new Marker(title, description, latLng);
        marker.setIcon(new Icon(getActivity(), Icon.Size.MEDIUM, "marker-stroked", "FF0000"));
        return marker;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
