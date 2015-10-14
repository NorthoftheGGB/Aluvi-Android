package com.aluvi.android.managers.location;

import android.location.Address;

import com.aluvi.android.api.gis.GeocodingApi;
import com.aluvi.android.helpers.GeoLocationUtils;
import com.aluvi.android.managers.callbacks.DataCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by usama on 7/13/15.
 */
public class GeocodingManager {
    private static GeocodingManager mInstance;
    private String mToken;
    private GeoLocationUtils.BoundingBox mBoundingBox;

    public static void initialize(String mapBoxToken) {
        initialize(mapBoxToken, null);
    }

    public static void initialize(String mapBoxToken, GeoLocationUtils.BoundingBox boundingBox) {
        mInstance = new GeocodingManager(mapBoxToken, boundingBox);
    }

    public static GeocodingManager getInstance() {
        return mInstance;
    }

    private GeocodingManager(String mapBoxToken, GeoLocationUtils.BoundingBox boundingBox) {
        mToken = mapBoxToken;
        mBoundingBox = boundingBox;
    }

    public void getAddressesForName(String name, final DataCallback<List<Address>> addressCallback) {
        GeocodingApi.getAddressesForName(name, new GeoCodingCallback(addressCallback));
    }

    public void getAddressesForLocation(double lat, double lon, final DataCallback<List<Address>> addressCallback) {
        GeocodingApi.getAddressesForLocation(lat, lon, new GeoCodingCallback(addressCallback));
    }

    private class GeoCodingCallback implements GeocodingApi.GeocodingApiCallback {
        private DataCallback<List<Address>> mAddressCallback;

        public GeoCodingCallback(DataCallback<List<Address>> addressCallback) {
            mAddressCallback = addressCallback;
        }

        @Override
        public void onFailure(int statusCode) {
            mAddressCallback.failure("Unable to fetch addresses");
        }

        @Override
        public void onAddressesFound(List<Address> data) {
            if (mBoundingBox != null && data != null) {
                for (int i = 0; i < data.size(); i++) {
                    LatLng loc = new LatLng(data.get(i).getLatitude(), data.get(i).getLongitude());
                    if (!mBoundingBox.contains(loc)) {
                        data.remove(i);
                        i--;
                    }
                }
            }

            mAddressCallback.success(data);
        }
    }
}
