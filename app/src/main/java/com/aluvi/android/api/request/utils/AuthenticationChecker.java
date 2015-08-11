package com.aluvi.android.api.request.utils;

import com.android.volley.VolleyError;

import java.net.HttpURLConnection;

/**
 * Created by usama on 7/29/15.
 */
public class AuthenticationChecker {
    public static boolean isAuthenticated(int statusCode, VolleyError error) {
        return statusCode != HttpURLConnection.HTTP_UNAUTHORIZED;
    }
}
