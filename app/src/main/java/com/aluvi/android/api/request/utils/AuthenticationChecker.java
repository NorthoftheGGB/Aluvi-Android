package com.aluvi.android.api.request.utils;

import com.android.volley.VolleyError;

/**
 * Created by usama on 7/29/15.
 */
public class AuthenticationChecker {
    private final static int STATUS_CODE_AUTH_FAIL = 401;
    private final static String TEMP_ERROR_CHECK = "java.io.IOException: Received authentication challenge is null";

    public static boolean isAuthenticated(int statusCode, VolleyError error) { // TODO: Ask Matt if there's a better way to detect auth errors
        return error != null && TEMP_ERROR_CHECK.equals(error.getMessage()) || statusCode == STATUS_CODE_AUTH_FAIL;
    }
}
