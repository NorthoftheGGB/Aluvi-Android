package com.aluvi.android.helpers.views;

import android.content.Context;
import android.location.Address;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Filter;
import android.widget.TextView;

import com.aluvi.android.helpers.AsyncCallback;
import com.aluvi.android.helpers.GeocoderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by usama on 7/20/15.
 */
public class GeoCodingAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    public interface OnGeoLocationUpdateListener {
        void onGeoCodeStarted();

        void onGeoCodeFinished(List<Address> addresses);
    }

    private final String TAG = "GeoCodingAutoComplete";
    private OnGeoLocationUpdateListener mLocationUpdateListener;
    private final int MIN_SEARCH_LENGTH = 5;
    private HashMap<String, List<Address>> mGeoCodeCache = new HashMap<>();

    private LocationSelectAdapter mAddressSuggestionsAutoCompleteAdapter;

    public GeoCodingAutoCompleteTextView(Context context) {
        super(context);
        init();
    }

    public GeoCodingAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GeoCodingAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        mAddressSuggestionsAutoCompleteAdapter = new LocationSelectAdapter(getContext(), new ArrayList<Address>());
        setAdapter(mAddressSuggestionsAutoCompleteAdapter);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        final String enteredLocation = text.toString();
        if (enteredLocation.length() > MIN_SEARCH_LENGTH) {
            List<Address> cachedAddresses = mGeoCodeCache.get(enteredLocation);
            if (cachedAddresses != null) {
                onAddressesFetched(cachedAddresses);
                Log.i(TAG, "Using cached addresses for geocoding");
            } else {
                if (mLocationUpdateListener != null)
                    mLocationUpdateListener.onGeoCodeStarted();

                GeocoderUtils.getAddressesForName(enteredLocation, new AsyncCallback<List<Address>>() {
                    @Override
                    public void onOperationCompleted(List<Address> result) {
                        if (mGeoCodeCache != null)
                            mGeoCodeCache.put(enteredLocation, result);

                        onAddressesFetched(result);
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

    public OnGeoLocationUpdateListener getLocationUpdateListener() {
        return mLocationUpdateListener;
    }

    public void setLocationUpdateListener(OnGeoLocationUpdateListener mLocationUpdateListener) {
        this.mLocationUpdateListener = mLocationUpdateListener;
    }

    public LocationSelectAdapter getAutoCompleteAdapter() {
        return mAddressSuggestionsAutoCompleteAdapter;
    }

    public void setAutoCompleteAdapter(LocationSelectAdapter mAddressSuggestionsAutoCompleteAdapter) {
        this.mAddressSuggestionsAutoCompleteAdapter = mAddressSuggestionsAutoCompleteAdapter;
    }

    public static class LocationSelectAdapter extends BaseArrayAdapter<Address> {
        public LocationSelectAdapter(Context context, ArrayList<Address> data) {
            super(context, android.R.layout.simple_dropdown_item_1line, data);
        }

        @Override
        protected void initView(ViewHolder holder, int position) {
            TextView addressTextView = (TextView) holder.getView(android.R.id.text1);
            addressTextView.setText(GeocoderUtils.getFormattedAddress(getItem(position)));
        }

        private final NoFilter<Address> NO_FILTER = new NoFilter<Address>() {
            @Override
            public CharSequence convertToString(Address resultValue) {
                return GeocoderUtils.getFormattedAddress(resultValue);
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
