package com.aluvi.android.api;

/**
 * Created by usama on 7/28/15.
 */
public interface AluviApiConstants {
    String API_BASE_URL = "http://52.24.74.155:4000/api/"; // Dev server

    String API_POST_REQUEST_COMMUTER_TICKETS = "rides/commute";
    String API_POST_RIDE_REQUEST = "rides/request";
    String API_POST_REQUEST_CANCELLED = "rides/request/cancel";
    String API_GET_RIDES_STATE = "rides/state";
    String API_POST_DRIVER_CANCELLED = "rides/driver_cancelled";
    String CANCEL_RIDER_SCHEDULED_TICKET = "v2/rides/rider_cancelled";
    String API_POST_RIDE_PICKUP = "rides/pickup";
    String API_POST_FARE_COMPLETED = "rides/arrived";
    String API_DELETE_TRIP = "rides/trips/";

    String API_GET_ACTIVE_TICKETS = "v2/rides/tickets"; // this path uses v2 API
    String API_GET_ACTIVE_FARES = "rides/fares";

    String API_GET_PAYMENTS = "rides/payments";
    String API_GET_EARNINGS = "rides/earnings";

    String API_ROUTE = "rides/route";

    // geo API
    String API_GET_DRIVER_LOCATION = "geo/driver/";
    String API_UPDATE_DRIVER_LOCATION = "geo/driver";
    String API_GET_RIDER_LOCATION = "geo/rider/";
    String API_UPDATE_RIDER_LOCATION = "geo/rider";

    // devices API - RESTfl
    String API_DEVICES = "devices/";

    // users API
    String API_USERS = "users";
    String API_LOGIN = "users/login";
    String API_FORGOT_PASSWORD = "users/forgot_password";
    String API_DRIVER_INTERESTED = "users/driver_interested";
    String API_USER_STATE = "users/state";
    String API_USER_PROFILE = "users/profile";
    String API_FILL_COMMUTER_PASS = "users/fill_commuter_pass";
    String API_CREATE_SUPPORT_REQUEST = "users/support";

    // drivers
    String API_DRIVER_REGISTRATION = "drivers/driver_registration";
    String API_GET_DRIVER_FARE_PATH_PATTERN = "drivers/fares/:id";

    // state
    String API_TOKEN_KEY = "API_TOKEN";
}
