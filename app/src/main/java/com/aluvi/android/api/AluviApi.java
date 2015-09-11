package com.aluvi.android.api;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.spothero.volley.JacksonNetwork;
import com.spothero.volley.JacksonRequest;

/**
 * Created by matthewxi on 7/14/15.
 */
public class AluviApi implements AluviApiConstants {

    public final static String API_POST_REQUEST_COMMUTER_TICKETS = "v2/rides/commute";
    public final static String CANCEL_TICKET = "v2/rides/cancel";
    public final static String API_POST_RIDER_PICKUP = "v2/rides/pickup";
    public final static String API_POST_RIDER_DROPOFF = "v2/rides/arrived";
    public final static String API_DELETE_TRIP = "v2/rides/trips/";
    public final static String API_GET_ACTIVE_TICKETS = "v2/rides/tickets"; // this path uses v2 API
    public final static String API_RECEIPTS = "v2/rides/receipts";
    public final static String API_ROUTE = "v2/rides/route";
    public final static String API_PICKUP_POINTS = "v2/rides/pickup_points";
    public final static String API_UPDATE_CAR = "v2/users/car";

    // geo API
    public final static String API_GET_DRIVER_LOCATION = "geo/driver/";
    public final static String API_UPDATE_DRIVER_LOCATION = "geo/driver";
    public final static String API_GET_RIDER_LOCATION = "geo/rider/";
    public final static String API_UPDATE_RIDER_LOCATION = "geo/rider";

    // devices API
    public final static String API_DEVICES = "v2/devices/";
    public final static String API_DEVICE_DISASSOCIATE_USER = "v2/devices/disassociate_user/";

    // users API
    public final static String API_USERS = "v2/users";
    public final static String API_LOGIN = "v2/users/login";
    public final static String API_FORGOT_PASSWORD = "v2/users/forgot_password";
    public final static String API_DRIVER_INTERESTED = "v2/users/driver_interested";
    public final static String API_USER_STATE = "v2/users/state";
    public final static String API_USER_PROFILE = "v2/users/profile";
    public final static String API_FILL_COMMUTER_PASS = "v2/users/fill_commuter_pass";
    public final static String API_CREATE_SUPPORT_REQUEST = "v2/users/support";
    public final static String API_EMAIL_RECEIPTS = "v2/users/receipts";

    // drivers
    public final static String API_DRIVER_REGISTRATION = "drivers/driver_registration";
    public final static String API_GET_DRIVER_FARE_PATH_PATTERN = "drivers/fares/:id";

    //Singleton
    private static AluviApi mInstance;

    // member variables
    private RequestQueue mRequestQueue;

    public static final String constructUrl(String path) {
        return API_BASE_URL + path;
    }

    public AluviApi(Context context) {
        this.mRequestQueue = JacksonNetwork.newRequestQueue(context.getApplicationContext());

        JacksonRequest.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static final synchronized void initialize(Context context) {
        mInstance = new AluviApi(context);
    }

    public static final AluviApi getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}
