package com.aluvi.android.helpers.views;

import android.content.Context;
import android.location.Address;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.TextView;

import com.aluvi.android.api.gis.GeocodingApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by usama on 7/25/15.
 */
public class GeocodingAutoCompleteBinder {
    public interface OnGeoLocationUpdateListener {
        void onGeoCodeStarted();

        void onGeoCodeFinished(List<Address> addresses);
    }


    private final String TAG = "GeocodingBinder";
    private OnGeoLocationUpdateListener mLocationUpdateListener;
    private final int MIN_SEARCH_LENGTH = 5;
    private HashMap<String, List<Address>> mGeoCodeCache = new HashMap<>();
    private LocationSelectAdapter mAddressSuggestionsAutoCompleteAdapter;

    private AutoCompleteTextView mAutoCompleteTextView;

    public GeocodingAutoCompleteBinder(AutoCompleteTextView mAutoCompleteTextView) {
        this.mAutoCompleteTextView = mAutoCompleteTextView;
        init();
    }

    public void init() {
        mAddressSuggestionsAutoCompleteAdapter = new LocationSelectAdapter(mAutoCompleteTextView.getContext(),
                new ArrayList<Address>());
        mAutoCompleteTextView.setAdapter(mAddressSuggestionsAutoCompleteAdapter);

        mAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onSearchRequested(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void onSearchRequested(final String query) {
        if (query.length() > MIN_SEARCH_LENGTH) {
            List<Address> cachedAddresses = mGeoCodeCache.get(query);
            if (cachedAddresses != null) {
                onAddressesFetched(cachedAddresses);
                Log.i(TAG, "Using cached addresses for geocoding");
            } else {
                if (mLocationUpdateListener != null)
                    mLocationUpdateListener.onGeoCodeStarted();

                GeocodingApi.getInstance()
                        .getAddressesForName(query, new GeocodingApi.GeocodingApiCallback() {
                            @Override
                            public void onAddressesFound(String query, List<Address> data) {
                                if (mGeoCodeCache != null)
                                    mGeoCodeCache.put(query, data);

                                onAddressesFetched(data);
                            }

                            @Override
                            public void onFailure(int statusCode) {
                                Log.e(TAG, "Error fetching geocode data. Status code: " + statusCode);
                            }
                        });
            }
        }
    }

    private void onAddressesFetched(List<Address> addresses) {
        if (addresses != null) {
            if (mAddressSuggestionsAutoCompleteAdapter != null) {
                mAddressSuggestionsAutoCompleteAdapter.clear();
                for (Address address : addresses)
                    mAddressSuggestionsAutoCompleteAdapter.add(address);

                mAddressSuggestionsAutoCompleteAdapter.notifyDataSetChanged();
            }

            if (mLocationUpdateListener != null)
                mLocationUpdateListener.onGeoCodeFinished(addresses);
        }
    }

    public void updateAddresses(List<Address> addresses) {
        onAddressesFetched(addresses);
    }

    public LocationSelectAdapter getAdapter() {
        return mAddressSuggestionsAutoCompleteAdapter;
    }

    public OnGeoLocationUpdateListener getLocationUpdateListener() {
        return mLocationUpdateListener;
    }

    public void setLocationUpdateListener(OnGeoLocationUpdateListener mLocationUpdateListener) {
        this.mLocationUpdateListener = mLocationUpdateListener;
    }

    public static class LocationSelectAdapter extends BaseArrayAdapter<Address> {
        public LocationSelectAdapter(Context context, ArrayList<Address> data) {
            super(context, android.R.layout.simple_dropdown_item_1line, data);
        }

        @Override
        protected void initView(ViewHolder holder, int position) {
            TextView addressTextView = (TextView) holder.getView(android.R.id.text1);
            addressTextView.setText(GeocodingApi.getFormattedAddress(getItem(position)));
        }

        private final NoFilter<Address> NO_FILTER = new NoFilter<Address>() {
            @Override
            public CharSequence convertToString(Address resultValue) {
                return GeocodingApi.getFormattedAddress(resultValue);
            }
        };

        /**
         * Override ArrayAdapter.getFilter() to return our own filtering.
         */
        @Override
        public Filter getFilter() {
            return NO_FILTER;
        }

        /**
         * Class which does not perform any filtering. Filtering is already done by
         * the web service when asking for the list, so there is no need to do any
         * more as well. This way, ArrayAdapter.mOriginalValues is not used when
         * calling e.g. ArrayAdapter.add(), but instead ArrayAdapter.mObjects is
         * updated directly and methods like getCount() return the expected result.
         */
        private static abstract class NoFilter<T> extends Filter {
            protected FilterResults performFiltering(CharSequence prefix) {
                return new FilterResults();
            }

            protected void publishResults(CharSequence constraint, FilterResults results) {
                // Do nothing
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return convertToString((T) resultValue);
            }

            public abstract CharSequence convertToString(T resultValue);
        }
    }
}
