package com.aluvi.android.api;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.spothero.volley.JacksonNetwork;

/**
 * Created by matthewxi on 7/14/15.
 */
public class AluviApi {
    public static String API_BASE_URL = "http://52.25.169.216:3000/api/"; // AWS, single worker

    public static String API_POST_RIDE_REQUEST = "rides/request";
    public static String API_POST_REQUEST_CANCELLED = "rides/request/cancel";
    public static String API_GET_RIDES_STATE = "rides/state";
    public static String API_POST_DRIVER_CANCELLED = "rides/driver_cancelled";
    public static String API_POST_RIDER_CANCELLED = "rides/rider_cancelled";
    public static String API_POST_RIDE_PICKUP = "rides/pickup";
    public static String API_POST_FARE_COMPLETED = "rides/arrived";
    public static String API_DELETE_TRIP = "rides/trips/:trip_id";

    public static String API_GET_ACTIVE_TICKETS = "rides/tickets";
    public static String API_GET_ACTIVE_FARES = "rides/fares";

    public static String API_GET_PAYMENTS = "rides/payments";
    public static String API_GET_EARNINGS = "rides/earnings";

    public static String API_ROUTE = "rides/route";

    // geo API
    public static String API_GEO_DRIVER_PATH = "geo/drivers/:objectId";
    public static String API_GEO_DRIVERS = "geo/drivers";
    public static String API_GEO_RIDER_PATH = "geo/rider";
    public static String API_GEO_RIDERS = "geo/riders";

    // devices API - RESTfl
    public static String API_DEVICES = "devices/";

    // users API
    public static String API_USERS = "users";
    public static String API_LOGIN = "users/login";
    public static String API_FORGOT_PASSWORD = "users/forgot_password";
    public static String API_DRIVER_INTERESTED = "users/driver_interested";
    public static String API_USER_STATE = "users/state";
    public static String API_USER_PROFILE = "users/profile";
    public static String API_FILL_COMMUTER_PASS = "users/fill_commuter_pass";
    public static String API_CREATE_SUPPORT_REQUEST = "users/support";

    // drivers
    public static String API_DRIVER_REGISTRATION = "drivers/driver_registration";
    public static String API_GET_DRIVER_FARE_PATH_PATTERN = "drivers/fares/:id";

    // state
    public static String API_TOKEN_KEY = "API_TOKEN";


    //Singleton
    private static AluviApi mInstance;

    // member variables
    private RequestQueue mRequestQueue;

    public static String constructUrl(String path){
        return API_BASE_URL + path;
    }

    public AluviApi(Context context) {
        this.mRequestQueue = JacksonNetwork.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized void initialize(Context context) {
        mInstance = new AluviApi(context);
    }

    public static AluviApi getInstance(){
        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        return mRequestQueue;
    }





}
