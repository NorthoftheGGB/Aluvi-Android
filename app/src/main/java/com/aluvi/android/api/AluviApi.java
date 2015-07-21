package com.aluvi.android.api;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.spothero.volley.JacksonNetwork;

/**
 * Created by matthewxi on 7/14/15.
 */
public class AluviApi {
    //        public static final String API_BASE_URL = "http://52.25.169.216:3000/api/"; // AWS, single worker
    public static final String API_BASE_URL = "http://52.26.200.141:4000/api"; // Testing server

    public static final String API_POST_REQUEST_COMMUTER_TICKETS = "rides/commute";
    public static final String API_POST_RIDE_REQUEST = "rides/request";
    public static final String API_POST_REQUEST_CANCELLED = "rides/request/cancel";
    public static final String API_GET_RIDES_STATE = "rides/state";
    public static final String API_POST_DRIVER_CANCELLED = "rides/driver_cancelled";
    public static final String CANCEL_RIDER_SCHEDULED_TICKET = "rides/rider_cancelled";
    public static final String API_POST_RIDE_PICKUP = "rides/pickup";
    public static final String API_POST_FARE_COMPLETED = "rides/arrived";
    public static final String API_DELETE_TRIP = "rides/trips/";

    public static final String API_GET_ACTIVE_TICKETS = "rides/tickets"; // this path uses v2 API
    public static final String API_GET_ACTIVE_FARES = "rides/fares";

    public static final String API_GET_PAYMENTS = "rides/payments";
    public static final String API_GET_EARNINGS = "rides/earnings";

    public static final String API_ROUTE = "rides/route";

    // geo API
    public static final String API_GEO_DRIVER_PATH = "geo/drivers/:objectId";
    public static final String API_GEO_DRIVERS = "geo/drivers";
    public static final String API_GEO_RIDER_PATH = "geo/rider";
    public static final String API_GEO_RIDERS = "geo/riders";

    // devices API - RESTfl
    public static final String API_DEVICES = "devices/";

    // users API
    public static final String API_USERS = "users";
    public static final String API_LOGIN = "users/login";
    public static final String API_FORGOT_PASSWORD = "users/forgot_password";
    public static final String API_DRIVER_INTERESTED = "users/driver_interested";
    public static final String API_USER_STATE = "users/state";
    public static final String API_USER_PROFILE = "users/profile";
    public static final String API_FILL_COMMUTER_PASS = "users/fill_commuter_pass";
    public static final String API_CREATE_SUPPORT_REQUEST = "users/support";

    // drivers
    public static final String API_DRIVER_REGISTRATION = "drivers/driver_registration";
    public static final String API_GET_DRIVER_FARE_PATH_PATTERN = "drivers/fares/:id";

    // state
    public static final String API_TOKEN_KEY = "API_TOKEN";


    //Singleton
    private static AluviApi mInstance;

    // member variables
    private RequestQueue mRequestQueue;

    public static final String constructUrl(String path) {
        return API_BASE_URL + path;
    }

    public AluviApi(Context context) {
        this.mRequestQueue = JacksonNetwork.newRequestQueue(context.getApplicationContext());
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
