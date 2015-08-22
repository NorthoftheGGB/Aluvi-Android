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

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by usama on 8/21/15.
 */
public class GeocodingApi {
    public interface GeocodingApiCallback {
        void onAddressesFound(String query, List<Address> data);

        void onFailure(int statusCode);
    }

    private final static String BASE_GEOCODE_URL = "https://api.mapbox.com/v4/",
            ENDPOINT_GEOCODE = "geocode/mapbox.places/";

    public static void getAddressesForName(final String name, String token, GeocodingApiCallback addressCallback) {
        try {
            String geocodeUrl = BASE_GEOCODE_URL + ENDPOINT_GEOCODE + URLEncoder.encode(name, "UTF-8").toString() + ".json";
            sendGeoCodeRequest(geocodeUrl, name, token, addressCallback);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            addressCallback.onFailure(HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    public static void getAddressesForLocation(final double lat, final double lon, String token, GeocodingApiCallback addressCallback) {
        try {
            String reverseGeocodeUrl = BASE_GEOCODE_URL + ENDPOINT_GEOCODE + URLEncoder.encode(lon + "," + lat, "UTF-8") + ".json";
            sendGeoCodeRequest(reverseGeocodeUrl, token, addressCallback);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            addressCallback.onFailure(HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    private static void sendGeoCodeRequest(String url, final String query, String token, final GeocodingApiCallback addressCallback) {
        GetRequestBuilder builder = new GetRequestBuilder(url)
                .appendParameter("access_token", token);

        AluviUnauthenticatedRequest<GeocodeData> geoCodeRequest = new AluviUnauthenticatedRequest<>(Request.Method.GET,
                builder.build(),
                new JacksonRequestListener<GeocodeData>() {
                    @Override
                    public void onResponse(GeocodeData response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK && response != null) {
                            List<Address> out = new ArrayList<>();
                            GeocodeData.Feature[] locations = response.getFeatures();
                            if (locations != null)
                                for (int i = 0; i < locations.length; i++)
                                    out.add(addressForGeocodeData(locations[i]));

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

        geoCodeRequest.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(geoCodeRequest);
    }

    private static void sendGeoCodeRequest(String url, String token, final GeocodingApiCallback addressCallback) {
        sendGeoCodeRequest(url, null, token, addressCallback);
    }

    private static Address addressForGeocodeData(GeocodeData.Feature location) {
        Address address = new Address(Locale.getDefault());
        address.setLatitude(location.getLat());
        address.setLongitude(location.getLon());

        int addressLine = 0;

        String street = location.getStreet();
        street = street != null && !location.getStreet().contains("null") ? location.getStreet() : "";
        address.setThoroughfare(street);
        address.setAddressLine(addressLine, street);

        String city = location.getCity();
        String state = location.getState();
        address.setLocality(city);
        address.setAdminArea(state);

        String cityState = "";
        if (city != null && !city.equals("")) {
            cityState += location.getCity();
            if (state != null && !state.equals(""))
                cityState += ", ";
        }

        cityState += state;
        address.setAddressLine(++addressLine, cityState);

        address.setPostalCode(location.getPostalCode());
        address.setCountryName(location.getCountry());
        return address;
    }
}
