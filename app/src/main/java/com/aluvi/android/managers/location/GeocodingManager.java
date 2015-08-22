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
    private String mToken;

    public static void initialize(String mapBoxToken) {
        mInstance = new GeocodingManager(mapBoxToken);
    }

    public static GeocodingManager getInstance() {
        return mInstance;
    }

    private GeocodingManager(String mapBoxToken) {
        mToken = mapBoxToken;
    }

    public void getAddressesForName(String name, final DataCallback<List<Address>> addressCallback) {
        GeocodingApi.getAddressesForName(name, mToken, new GeocodingApi.GeocodingApiCallback() {
            @Override
            public void onAddressesFound(String query, List<Address> data) {
                addressCallback.success(data);
            }

            @Override
            public void onFailure(int statusCode) {
                addressCallback.failure("Unable to fetch addresses");
            }
        });
    }

    public void getAddressesForLocation(double lat, double lon, final DataCallback<List<Address>> addressCallback) {
        GeocodingApi.getAddressesForLocation(lat, lon, mToken, new GeocodingApi.GeocodingApiCallback() {
            @Override
            public void onAddressesFound(String query, List<Address> data) {
                addressCallback.success(data);
            }

            @Override
            public void onFailure(int statusCode) {
                addressCallback.failure("Unable to fetch addresses");
            }
        });
    }
}
