package com.aluvi.android.api.gis;

import android.content.Context;
import android.location.Address;

import com.aluvi.android.api.gis.models.GeocodeData;
import com.aluvi.android.api.request.AluviUnauthenticatedRequest;
import com.aluvi.android.api.request.utils.GetRequestBuilder;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.spothero.volley.JacksonRequestListener;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by usama on 7/13/15.
 */
public class GeocodingApi {

    public interface GeocodingApiCallback {
        void onAddressesFound(String query, List<Address> data);

        void onFailure(int statusCode);
    }

    public final static float INVALID_LOCATION = 360;
    private final static String ENDPOINT_GEOCODE = "geocoding/v1/address",
            ENDPOINT_REVERSE_GEOCODE = "geocoding/v1/reverse";

    private static GeocodingApi mInstance;
    private RequestQueue mRequestQueue;

    public static GeocodingApi getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GeocodingApi(context);
        }

        return mInstance;
    }

    private GeocodingApi(Context context) {
        // Set up a request queue that has only 1 worker thread, so all requests get executed sequentially.
        // This is important because some geocoding requests take longer than others to execute and we can't handle out-of-order responses.
        mRequestQueue = new RequestQueue(new DiskBasedCache(context.getCacheDir(), 1024 * 1024),
                new BasicNetwork(new HurlStack()), 1);
        mRequestQueue.start();
    }

    public void getAddressesForName(final String name, GeocodingApiCallback addressCallback) {
        GetRequestBuilder builder = new GetRequestBuilder(MapQuestApi.MAPQUEST_BASE_URL + ENDPOINT_GEOCODE)
                .appendParameter("key", MapQuestApi.MAP_QUEST_API_KEY)
                .appendParameter("location", name);

        sendGeoCodeRequest(builder.build(), name, addressCallback);
    }

    public void getAddressesForLocation(final double lat, final double lon, GeocodingApiCallback addressCallback) {
        GetRequestBuilder builder = new GetRequestBuilder(MapQuestApi.MAPQUEST_BASE_URL + ENDPOINT_REVERSE_GEOCODE)
                .appendParameter("key", MapQuestApi.MAP_QUEST_API_KEY)
                .appendParameter("location", lat + "," + lon);

        sendGeoCodeRequest(builder.build(), addressCallback);
    }

    private void sendGeoCodeRequest(String url, final String query, final GeocodingApiCallback addressCallback) {
        AluviUnauthenticatedRequest<GeocodeData> geoCodeRequest = new AluviUnauthenticatedRequest<>(Request.Method.GET,
                url,
                new JacksonRequestListener<GeocodeData>() {
                    @Override
                    public void onResponse(GeocodeData response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK) {
                            List<Address> out = new ArrayList<>();
                            GeocodeData.GeocodedLocation[] locations = response.getLocations();
                            if (locations != null) {
                                for (int i = 0; i < locations.length; i++) {
                                    GeocodeData.GeocodedLocation location = locations[i];
                                    out.add(addressForGeocodeLocation(location));
                                }
                            }

                            addressCallback.onAddressesFound(query, out);
                        } else {
                            addressCallback.onFailure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(GeocodeData.class);
                    }
                });

        geoCodeRequest.setShouldCache(true);
        geoCodeRequest.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_BAD_REQUEST});
        mRequestQueue.add(geoCodeRequest);
    }

    private void sendGeoCodeRequest(String url, final GeocodingApiCallback addressCallback) {
        sendGeoCodeRequest(url, null, addressCallback);
    }

    private Address addressForGeocodeLocation(GeocodeData.GeocodedLocation location) {
        Address address = new Address(Locale.getDefault());
        address.setLatitude(location.getLatLng().getLat());
        address.setLongitude(location.getLatLng().getLng());

        address.setThoroughfare(location.getStreet());
        address.setAddressLine(0, location.getStreet());

        address.setLocality(location.getCity());
        address.setAdminArea(location.getState());
        address.setAddressLine(1, location.getCity() + ", " + location.getState());

        address.setPostalCode(location.getPostalCode());
        address.setAddressLine(2, location.getPostalCode());

        address.setCountryName(location.getCountry());
        return address;
    }

    public static String getFormattedAddress(Address address) {
        StringBuilder out = new StringBuilder();
        int addressLines = Math.min(2, address.getMaxAddressLineIndex());
        for (int i = 0; i < addressLines; i++) {
            String line = address.getAddressLine(i);
            if (line != null && !"".equals(line)) {
                out.append(address.getAddressLine(i));
                if (i + 1 < addressLines)
                    out.append(", ");
            }
        }

        return out.toString();
    }
}

