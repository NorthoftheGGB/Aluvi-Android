package com.aluvi.android.api.request;

import com.aluvi.android.api.AuthFailEvent;
import com.android.volley.VolleyError;
import com.spothero.volley.JacksonRequestListener;

import de.greenrobot.event.EventBus;

/**
 * Created by usama on 7/24/15.
 */
public abstract class AluviRequestListener<T> extends JacksonRequestListener<T> {
    private final int STATUS_CODE_AUTH_FAIL = 401;
    private final String TEMP_ERROR_CHECK = "java.io.IOException: Received authentication challenge is null";

    @Override
    public void onResponse(T response, int statusCode, VolleyError error) {
        if (error != null && TEMP_ERROR_CHECK.equals(error.getMessage()) || statusCode == STATUS_CODE_AUTH_FAIL) // TODO: Ask Matt if there's a better way to detect auth errors
            EventBus.getDefault().post(new AuthFailEvent());
        else
            onAuthenticatedResponse(response, statusCode, error);
    }

    public abstract void onAuthenticatedResponse(T response, int statusCode, VolleyError error);
}
