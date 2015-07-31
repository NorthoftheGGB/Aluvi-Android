package com.aluvi.android.api.request.utils;

import com.android.volley.VolleyError;

/**
 * Created by usama on 7/29/15.
 */
public class AuthenticationChecker {
    private final static int STATUS_CODE_AUTH_FAIL = 401;

    public static boolean isAuthenticated(int statusCode, VolleyError error) {
        return statusCode != STATUS_CODE_AUTH_FAIL;
    }
}
