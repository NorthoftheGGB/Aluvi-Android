package com.aluvi.android.api.request;

import com.android.volley.AuthFailureError;
import com.spothero.volley.JacksonRequestListener;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by usama on 7/31/15.
 */
public class AluviAuthJSONPostRequest<T> extends AluviAuthenticatedRequest<T> {
    private JSONObject payload;

    public AluviAuthJSONPostRequest(int method, String endpoint, JSONObject payload, JacksonRequestListener<T> listener) {
        super(method, endpoint, new HashMap<String, String>(), listener);
        this.payload = payload;
    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=" + getParamsEncoding();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        String body = payload.toString();
        try {
            return body.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
