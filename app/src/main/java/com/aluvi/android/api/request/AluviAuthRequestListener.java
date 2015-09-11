package com.aluvi.android.api.request;

import android.util.Log;

import com.aluvi.android.api.AuthFailEvent;
import com.aluvi.android.api.request.utils.AuthenticationChecker;
import com.android.volley.VolleyError;
import com.spothero.volley.JacksonRequestListener;

import java.io.UnsupportedEncodingException;

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

        //get response body and parse with appropriate encoding
        if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
            String statusCodeStr = String.valueOf(error.networkResponse.statusCode);
            try {
                String body = new String(error.networkResponse.data, "UTF-8");
                Log.e("AluviResponse", "Status code: " + statusCodeStr + " with error: " + body);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        onAuthenticatedResponse(response, statusCode, error);
    }

    public abstract void onAuthenticatedResponse(T response, int statusCode, VolleyError error);
}
