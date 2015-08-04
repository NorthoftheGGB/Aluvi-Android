package com.aluvi.android.api;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.spothero.volley.JacksonNetwork;
import com.spothero.volley.JacksonRequest;
import com.aluvi.android.api.AluviApiConstants;

/**
 * Created by matthewxi on 7/14/15.
 */
public class AluviApi implements AluviApiConstants {

    final public static String API_POST_REQUEST_COMMUTER_TICKETS = "v2/rides/commute";
    final public static String CANCEL_TICKET = "v2/rides/rider_cancelled";
    final public static String API_POST_RIDER_PICKUP = "v2/rides/pickup";
    final public static String API_POST_RIDER_DROPOFF = "v2/rides/arrived";
    final public static String API_DELETE_TRIP = "rides/trips/";

    final public static String API_GET_ACTIVE_TICKETS = "v2/rides/tickets"; // this path uses v2 API

    final public static String API_GET_PAYMENTS = "rides/payments";
    final public static String API_GET_EARNINGS = "rides/earnings";

    final public static String API_ROUTE = "rides/route";

    // geo API
    final public static String API_GET_DRIVER_LOCATION = "geo/driver/";
    final public static String API_UPDATE_DRIVER_LOCATION = "geo/driver";
    final public static String API_GET_RIDER_LOCATION = "geo/rider/";
    final public static String API_UPDATE_RIDER_LOCATION = "geo/rider";

    // devices API
    final public static String API_DEVICES = "devices/";

    // users API
    final public static String API_USERS = "users";
    final public static String API_LOGIN = "users/login";
    final public static String API_FORGOT_PASSWORD = "users/forgot_password";
    final public static String API_DRIVER_INTERESTED = "users/driver_interested";
    final public static String API_USER_STATE = "users/state";
    final public static String API_USER_PROFILE = "users/profile";
    final public static String API_FILL_COMMUTER_PASS = "users/fill_commuter_pass";
    final public static String API_CREATE_SUPPORT_REQUEST = "users/support";

    // drivers
    final public static String API_DRIVER_REGISTRATION = "drivers/driver_registration";
    final public static String API_GET_DRIVER_FARE_PATH_PATTERN = "drivers/fares/:id";


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
