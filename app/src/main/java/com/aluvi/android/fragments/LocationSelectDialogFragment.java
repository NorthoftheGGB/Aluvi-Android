package com.aluvi.android.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.aluvi.R;
import com.aluvi.android.helpers.AsyncCallback;
import com.aluvi.android.helpers.GeocoderUtils;
import com.aluvi.android.helpers.views.BaseArrayAdapter;
import com.aluvi.android.helpers.views.MapBoxStateSaver;
import com.aluvi.android.helpers.views.ViewHelpers;
import com.aluvi.android.helpers.views.ViewHolder;
import com.aluvi.android.models.EasyILatLang;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by usama on 7/13/15.
 */
public class LocationSelectDialogFragment extends DialogFragment
{
    public interface OnLocationSelectedListener
    {
        void onLocationSelected(Address address, LocationSelectDialogFragment fragment);
    }

    @InjectView(R.id.location_select_image_button_search) ImageButton mSearchImageButton;
    @InjectView(R.id.location_select_progress_bar) ProgressBar mSearchProgressBar;
    @InjectView(R.id.location_select_auto_complete_search) AutoCompleteTextView mLocationSearchAutoCompleteTextView;
    @InjectView(R.id.location_select_map_view) MapView mMapView;

    private final String TAG = "LocationSelectFragment", MAP_STATE_KEY = "location_select_map";
    private LocationSelectAdapter mAddressSuggestionsAutoCompleteAdapter;
    private OnLocationSelectedListener mLocationSelectedListener;
    private Address mCurrentlySelectedLocation;

    public static LocationSelectDialogFragment newInstance()
    {
        return new LocationSelectDialogFragment();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mLocationSelectedListener = (OnLocationSelectedListener) activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final View rootView = View.inflate(getActivity(), R.layout.fragment_location_select, null);
        ButterKnife.inject(this, rootView);
        initMap();

        mAddressSuggestionsAutoCompleteAdapter = new LocationSelectAdapter(getActivity(), new ArrayList<Address>());
        mLocationSearchAutoCompleteTextView.setAdapter(mAddressSuggestionsAutoCompleteAdapter);
        mLocationSearchAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                ViewHelpers.hideKeyboardFragment(getActivity(), rootView); // Can't hide using the parent activity because of focus issues

                Address clickedAddress = mAddressSuggestionsAutoCompleteAdapter.getItem(position);
                addMarkerForAddress(clickedAddress);

                mMapView.setCenter(new EasyILatLang(clickedAddress.getLatitude(), clickedAddress.getLongitude()));
                mCurrentlySelectedLocation = clickedAddress;
            }
        });

        return new MaterialDialog.Builder(getActivity())
                .customView(rootView, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        super.onPositive(dialog);
                        if (mLocationSelectedListener != null)
                        {
                            mLocationSelectedListener.onLocationSelected(mCurrentlySelectedLocation,
                                    LocationSelectDialogFragment.this);
                        }
                    }
                })
                .build();
    }

    public void initMap()
    {
        MapBoxStateSaver.restoreMapState(mMapView, MAP_STATE_KEY);
        mMapView.setMapViewListener(new MapViewListener()
        {
            @Override
            public void onShowMarker(MapView mapView, Marker marker)
            {
            }

            @Override
            public void onHideMarker(MapView mapView, Marker marker)
            {
            }

            @Override
            public void onTapMarker(MapView mapView, Marker marker)
            {
            }

            @Override
            public void onLongPressMarker(MapView mapView, Marker marker)
            {
            }

            @Override
            public void onTapMap(MapView mapView, ILatLng iLatLng)
            {
                addMarker(iLatLng);
            }

            @Override
            public void onLongPressMap(MapView mapView, ILatLng iLatLng)
            {
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        MapBoxStateSaver.saveMapState(mMapView, MAP_STATE_KEY);
    }

    @OnClick(R.id.location_select_image_button_search)
    public void onLocationSearchClicked()
    {
        String enteredLocation = mLocationSearchAutoCompleteTextView.getText().toString();
        if (!"".equals(enteredLocation))
        {
            onLocationSearchStarted();
            GeocoderUtils.getAddressesForName(enteredLocation, 3, getActivity(), new AsyncCallback<List<Address>>()
            {
                @Override
                public void onOperationCompleted(List<Address> result)
                {
                    onAddressesFetched(result);
                }
            });
        }
    }

    private void onAddressesFetched(List<Address> addresses)
    {
        onLocationSearchFinished();

        Log.d(TAG, "Received address result");
        if (addresses != null)
        {
            Log.d(TAG, "Addresses: " + addresses.toString());

            if (mAddressSuggestionsAutoCompleteAdapter != null)
            {
                mAddressSuggestionsAutoCompleteAdapter.clear();
                for (Address address : addresses)
                    mAddressSuggestionsAutoCompleteAdapter.add(address);

                mAddressSuggestionsAutoCompleteAdapter.notifyDataSetChanged();
            }
        }
        else
        {
            if (getActivity() != null)
            {
                Toast.makeText(getActivity(), R.string.no_results_address, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addMarkerForAddress(Address address)
    {
        Marker marker = getDefaultMarker(address.getThoroughfare(),
                address.getThoroughfare() + ", " + address.getLocality(),
                new LatLng(address.getLatitude(), address.getLongitude()));

        mMapView.clear();
        mMapView.addMarker(marker);
    }

    private void addMarker(final ILatLng latLng)
    {
        mMapView.clear();
        mMapView.addMarker(getDefaultMarker(null, null, new LatLng(latLng.getLatitude(), latLng.getLongitude())));

        Log.d(TAG, "Looking for address for custom marker location");

        onLocationSearchStarted();
        GeocoderUtils.getAddressesForLocation(latLng.getLatitude(), latLng.getLongitude(), 1,
                getActivity(), new AsyncCallback<List<Address>>()
                {
                    @Override
                    public void onOperationCompleted(List<Address> result)
                    {
                        Log.d(TAG, "Found addresses for custom marker location");

                        if (result != null && result.size() > 0)
                        {
                            Address address = result.get(0);

                            float[] distanceArr = new float[3];
                            Location.distanceBetween(address.getLatitude(), address.getLongitude(), latLng.getLatitude(), latLng.getLongitude(), distanceArr);
                            float distance = distanceArr[0];

                            if (distance < 10) // Store the address only if its less than 10 meters away from the placed pin
                            {
                                Log.d(TAG, "Custom location is close enough to found address");

                                addMarkerForAddress(address);
                                onAddressesFetched(result);

                                if (address != null && mLocationSearchAutoCompleteTextView != null)
                                {
                                    mLocationSearchAutoCompleteTextView.setText(GeocoderUtils.getFormattedAddress(address));
                                }

                                mCurrentlySelectedLocation = address;
                            }
                            else
                            {
                                onLocationSearchFinished();
                            }
                        }
                    }
                });

        mCurrentlySelectedLocation = new Address(Locale.getDefault());
        mCurrentlySelectedLocation.setLatitude(latLng.getLatitude());
        mCurrentlySelectedLocation.setLongitude(latLng.getLongitude());
    }

    private void onLocationSearchStarted()
    {
        mSearchImageButton.setVisibility(View.INVISIBLE);
        mSearchProgressBar.setVisibility(View.VISIBLE);
    }

    private void onLocationSearchFinished()
    {
        if (mSearchImageButton != null && mSearchProgressBar != null) // Frequently called in callbacks, so check that we still have access to these guys
        {
            mSearchImageButton.setVisibility(View.VISIBLE);
            mSearchProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private Marker getDefaultMarker(String title, String description, LatLng latLng)
    {
        Marker marker = new Marker(title, description, latLng);
        marker.setIcon(new Icon(getActivity(), Icon.Size.MEDIUM, "marker-stroked", "FF0000"));
        return marker;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private static class LocationSelectAdapter extends BaseArrayAdapter<Address>
    {
        public LocationSelectAdapter(Context context, ArrayList<Address> data)
        {
            super(context, android.R.layout.simple_dropdown_item_1line, data);
        }

        @Override
        protected void initView(ViewHolder holder, int position)
        {
            TextView addressTextView = (TextView) holder.getView(android.R.id.text1);
            addressTextView.setText(GeocoderUtils.getFormattedAddress(getItem(position)));
        }

        private final NoFilter<Address> NO_FILTER = new NoFilter<Address>()
        {
            @Override
            public CharSequence convertToString(Address resultValue)
            {
                return GeocoderUtils.getFormattedAddress(resultValue);
            }
        };

        /**
         * Override ArrayAdapter.getFilter() to return our own filtering.
         */
        @Override
        public Filter getFilter()
        {
            return NO_FILTER;
        }

        /**
         * Class which does not perform any filtering. Filtering is already done by
         * the web service when asking for the list, so there is no need to do any
         * more as well. This way, ArrayAdapter.mOriginalValues is not used when
         * calling e.g. ArrayAdapter.add(), but instead ArrayAdapter.mObjects is
         * updated directly and methods like getCount() return the expected result.
         */
        private static abstract class NoFilter<T> extends Filter
        {
            protected FilterResults performFiltering(CharSequence prefix)
            {
                return new FilterResults();
            }

            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                // Do nothing
            }

            @Override
            public CharSequence convertResultToString(Object resultValue)
            {
                return convertToString((T) resultValue);
            }

            public abstract CharSequence convertToString(T resultValue);
        }
    }
}
