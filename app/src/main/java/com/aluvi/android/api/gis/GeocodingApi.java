package com.aluvi.android.api.gis;

import android.location.Address;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.gis.models.GeocodeData;
import com.aluvi.android.api.request.AluviUnauthenticatedRequest;
import com.aluvi.android.api.request.utils.GetRequestBuilder;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.spothero.volley.JacksonRequestListener;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by usama on 8/21/15.
 */
public class GeocodingApi {
    public interface GeocodingApiCallback {
        void onAddressesFound(List<Address> data);

        void onFailure(int statusCode);
    }

    private final static String MAPQUEST_GEOCODE_URL = "http://www.mapquestapi.com/",
            ENDPOINT_GEOCODE = "geocoding/v1/address",
            ENDPOINT_REVERSE_GEOCODE = "geocoding/v1/reverse";

    public static void getAddressesForName(final String name, GeocodingApiCallback addressCallback) {
        GetRequestBuilder builder = new GetRequestBuilder(MAPQUEST_GEOCODE_URL + ENDPOINT_GEOCODE)
                .appendParameter("key", MapQuestApi.MAP_QUEST_API_KEY)
                .appendParameter("location", name);

        sendGeoCodeRequest(builder.build(), addressCallback);
    }

    public static void getAddressesForLocation(final double lat, final double lon, GeocodingApiCallback addressCallback) {
        GetRequestBuilder builder = new GetRequestBuilder(MAPQUEST_GEOCODE_URL + ENDPOINT_REVERSE_GEOCODE)
                .appendParameter("key", MapQuestApi.MAP_QUEST_API_KEY)
                .appendParameter("location", lat + "," + lon);

        sendGeoCodeRequest(builder.build(), addressCallback);
    }

    private static void sendGeoCodeRequest(String url, final GeocodingApiCallback addressCallback) {
        AluviUnauthenticatedRequest<GeocodeData> routeRequest = new AluviUnauthenticatedRequest<>(Request.Method.GET,
                url,
                new JacksonRequestListener<GeocodeData>() {
                    @Override
                    public void onResponse(GeocodeData response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK) {
                            List<Address> out = new ArrayList<>();
                            if (response != null) {
                                GeocodeData.GeocodedLocation[] locations = response.getLocations();
                                if (locations != null) {
                                    for (int i = 0; i < locations.length; i++) {
                                        GeocodeData.GeocodedLocation location = locations[i];
                                        Address address = addressForGeocodeLocation(location);
                                        out.add(address);
                                    }
                                }
                            }

                            addressCallback.onAddressesFound(out);
                        } else {
                            addressCallback.onFailure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(GeocodeData.class);
                    }
                });

        routeRequest.addAcceptedStatusCodes(new int[]{200, 400});
        AluviApi.getInstance().getRequestQueue().add(routeRequest);
    }

    private static Address addressForGeocodeLocation(GeocodeData.GeocodedLocation location) {
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
}
