package com.aluvi.android.managers.location;

import android.location.Address;

import com.aluvi.android.api.gis.GeocodingApi;
import com.aluvi.android.managers.callbacks.DataCallback;

import java.util.List;

/**
 * Created by usama on 7/13/15.
 */
public class GeocodingManager {
    private static GeocodingManager mInstance;
    private String mToken, mRestrictedCountry;

    public static void initialize(String mapBoxToken) {
        mInstance = new GeocodingManager(mapBoxToken, null);
    }

    public static void initialize(String mapBoxToken, String restrictedCountry) {
        mInstance = new GeocodingManager(mapBoxToken, restrictedCountry);
    }

    public static GeocodingManager getInstance() {
        return mInstance;
    }

    private GeocodingManager(String mapBoxToken, String restrictedCountry) {
        mToken = mapBoxToken;
        mRestrictedCountry = restrictedCountry;
    }

    public void getAddressesForName(String name, final DataCallback<List<Address>> addressCallback) {
        GeocodingApi.getAddressesForName(name, mToken, new GeoCodingCallback(addressCallback));
    }

    public void getAddressesForLocation(double lat, double lon, final DataCallback<List<Address>> addressCallback) {
        GeocodingApi.getAddressesForLocation(lat, lon, mToken, new GeoCodingCallback(addressCallback));
    }

    public void getAddressForLocation(double lat, double lon, final DataCallback<Address> addressDataCallback) {
        getAddressesForLocation(lat, lon, new DataCallback<List<Address>>() {
            @Override
            public void success(List<Address> result) {
                if (result.size() > 0)
                    addressDataCallback.success(result.get(0));
                else
                    addressDataCallback.failure("No addresses");
            }

            @Override
            public void failure(String message) {
                addressDataCallback.failure(message);
            }
        });
    }

    private class GeoCodingCallback implements GeocodingApi.GeocodingApiCallback {
        private DataCallback<List<Address>> mAddressCallback;

        public GeoCodingCallback(DataCallback<List<Address>> addressCallback) {
            mAddressCallback = addressCallback;
        }

        @Override
        public void onAddressesFound(String query, List<Address> data) {
            if (mRestrictedCountry != null)
                applyFilter(data, mRestrictedCountry);
            mAddressCallback.success(data);
        }

        @Override
        public void onFailure(int statusCode) {
            mAddressCallback.failure("Unable to fetch addresses");
        }
    }

    private void applyFilter(List<Address> addresses, String countryFilter) {
        for (int i = 0; i < addresses.size(); i++) {
            Address address = addresses.get(i);
            if (!address.getCountryName().equals(countryFilter)) {
                addresses.remove(i);
                i--;
            }
        }
    }
}
