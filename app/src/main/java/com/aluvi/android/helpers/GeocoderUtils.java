package com.aluvi.android.helpers;

import android.location.Address;
import android.os.Handler;

import com.aluvi.android.api.request.GetRequestBuilder;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by usama on 7/13/15.
 */
public class GeocoderUtils {
    public final static float INVALID_LOCATION = 360;

    private final static String GOOGLE_GEOCODE_API_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json",
            GOOGLE_GEOCODE_API_KEY = "AIzaSyBFja136UfPi8MECGzVQps84seINY_AyYI";

    public static void getAddressesForName(final String name, final AsyncCallback<List<Address>> addressCallback) {
        GetRequestBuilder builder = new GetRequestBuilder(GOOGLE_GEOCODE_API_BASE_URL)
                .appendParameter("key", GOOGLE_GEOCODE_API_KEY)
                .appendParameter("address", name);

        sendGeoCodeRequest(builder.build(), addressCallback);
    }

    public static void getAddressesForLocation(final double lat, final double lon, final AsyncCallback<List<Address>> addressCallback) {
        GetRequestBuilder builder = new GetRequestBuilder(GOOGLE_GEOCODE_API_BASE_URL)
                .appendParameter("key", GOOGLE_GEOCODE_API_KEY)
                .appendParameter("latlng", lat + "," + lon);

        sendGeoCodeRequest(builder.build(), addressCallback);
    }

    private static void sendGeoCodeRequest(String url, final AsyncCallback<List<Address>> addressCallback) {
        final Handler handler = new Handler();
        OkHttpClient client = new OkHttpClient();
        client.newCall(new Request.Builder().url(url).build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        if (addressCallback != null)
                            addressCallback.onOperationCompleted(null);

                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        final List<Address> addresses = getAddressesForGeocodeResponse(response.body().string());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (addressCallback != null)
                                    addressCallback.onOperationCompleted(addresses);
                            }
                        });
                    }
                });
    }

    private static List<Address> getAddressesForGeocodeResponse(String response) {
        List<Address> addresses = new ArrayList<Address>();

        try {
            JSONObject root = new JSONObject(response);
            JSONArray serializedAddresses = root.optJSONArray("results");
            if (serializedAddresses != null) {
                for (int i = 0; i < serializedAddresses.length(); i++) {
                    Address address = getAddressForSerializedInfo(serializedAddresses.optJSONObject(i));
                    addresses.add(address);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return addresses;
    }

    private static Address getAddressForSerializedInfo(JSONObject addressInfo) {
        Address address = new Address(Locale.getDefault());

        JSONArray addressComponents = addressInfo.optJSONArray("address_components");
        if (addressComponents != null) {
            for (int i = 0; i < addressComponents.length(); i++) {
                try { // Try catch purposefully declared multiple times in this method - easy way to avoid NPE
                    JSONObject component = addressComponents.getJSONObject(i);
                    address.setAddressLine(i, component.getString("long_name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            JSONObject location = addressInfo.getJSONObject("geometry").getJSONObject("location");
            double latitude = location.getDouble("lat");
            double longitude = location.getDouble("lng");
            address.setLatitude(latitude);
            address.setLongitude(longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return address;
    }

    public static String getFormattedAddress(Address address) {
        StringBuilder out = new StringBuilder();
        int addressLines = Math.min(2, address.getMaxAddressLineIndex());
        for (int i = 0; i < addressLines; i++) {
            out.append(address.getAddressLine(i));
            if (i + 1 < addressLines)
                out.append(", ");
        }

        return out.toString();
    }
}

