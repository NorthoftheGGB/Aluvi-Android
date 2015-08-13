package com.aluvi.android.api.request;

import android.util.Log;

import com.aluvi.android.api.AuthFailEvent;
import com.aluvi.android.api.request.utils.AuthenticationChecker;
import com.android.volley.VolleyError;
import com.spothero.volley.JacksonRequestListener;

import de.greenrobot.event.EventBus;

/**
 * Created by usama on 7/24/15.
 */
public abstract class AluviAuthRequestListener<T> extends JacksonRequestListener<T> {
    @Override
    public void onResponse(T response, int statusCode, VolleyError error) {
        if (!AuthenticationChecker.isAuthenticated(statusCode, error)) {
            EventBus.getDefault().post(new AuthFailEvent());
        }

        if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
            String viewableError = new String(error.networkResponse.data);
            Log.e("AluviResponse", viewableError);
        }

        onAuthenticatedResponse(response, statusCode, error);
    }

    public abstract void onAuthenticatedResponse(T response, int statusCode, VolleyError error);
}
