package com.aluvi.android.api.gis;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.request.AluviAuthRequestListener;
import com.aluvi.android.api.request.AluviAuthenticatedRequest;
import com.aluvi.android.api.request.AluviPayload;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.android.gms.maps.model.LatLng;

import java.net.HttpURLConnection;

/**
 * Created by usama on 7/25/15.
 */
public class LocationUpdateApi {

    public interface OnLocationUpdatedListener {
        void onLocationUpdated(LatLng newLocation);

        void onFailure(int statusCode);
    }

    public interface OnLocationFetchedListener {
        void onLocationFetched(LocationUpdateResponse location);

        void onFailure(int statusCode);
    }

    public static void updateDriverLocation(LatLng newLocation, OnLocationUpdatedListener listener) {
        updateLocation(AluviApi.API_BASE_URL + AluviApi.API_UPDATE_DRIVER_LOCATION, newLocation, listener);
    }

    public static void updateRiderLocation(final LatLng newLocation, final OnLocationUpdatedListener listener) {
        updateLocation(AluviApi.API_BASE_URL + AluviApi.API_UPDATE_RIDER_LOCATION, newLocation, listener);
    }

    private static void updateLocation(String apiPath, final LatLng newLocation, final OnLocationUpdatedListener listener) {
        AluviAuthenticatedRequest locationRequest = new AluviAuthenticatedRequest(
                Request.Method.PUT,
                apiPath,
                new LocationUpdateRequest(newLocation),
                new AluviAuthRequestListener() {
                    @Override
                    public void onAuthenticatedResponse(Object response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK) {
                            listener.onLocationUpdated(newLocation);
                        } else {
                            listener.onFailure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return null;
                    }
                });

        locationRequest.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(locationRequest);
    }

    public static void getDriverLocation(final OnLocationFetchedListener listener) {
        getLocation(AluviApi.API_BASE_URL + AluviApi.API_GET_DRIVER_LOCATION, listener);
    }

    public static void getRiderLocation(final OnLocationFetchedListener listener) {
        getLocation(AluviApi.API_BASE_URL + AluviApi.API_GET_RIDER_LOCATION, listener);
    }

    private static void getLocation(String apiPath, final OnLocationFetchedListener listener) {
        AluviAuthenticatedRequest<LocationUpdateResponse> locationRequest = new AluviAuthenticatedRequest<>(
                Request.Method.GET,
                apiPath,
                new AluviAuthRequestListener<LocationUpdateResponse>() {
                    @Override
                    public void onAuthenticatedResponse(LocationUpdateResponse response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK) {
                            listener.onLocationFetched(response);
                        } else {
                            listener.onFailure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(LocationUpdateResponse.class);
                    }
                }
        );

        locationRequest.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(locationRequest);
    }

    private static class LocationUpdateRequest extends AluviPayload {
        @JsonProperty("latitude")
        private double mLatitude;

        @JsonProperty("longitude")
        private double mLongitude;

        public LocationUpdateRequest(double mLatitude, double mLongitude) {
            this.mLatitude = mLatitude;
            this.mLongitude = mLongitude;
        }

        public LocationUpdateRequest(LatLng location) {
            this(location != null ? location.latitude : 0, location != null ? location.longitude : 0);
        }
    }

    public static class LocationUpdateResponse {
        @JsonProperty("latitude")
        private double mLatitude;

        @JsonProperty("longitude")
        private double mLongitude;

        public double getLatitude() {
            return mLatitude;
        }

        public void setLatitude(double mLatitude) {
            this.mLatitude = mLatitude;
        }

        public double getLongitude() {
            return mLongitude;
        }

        public void setLongitude(double mLongitude) {
            this.mLongitude = mLongitude;
        }
    }
}
