package com.aluvi.android.api;

import com.aluvi.android.managers.UserStateManager;
import com.android.volley.AuthFailureError;
import com.spothero.volley.JacksonRequest;
import com.spothero.volley.JacksonRequestListener;

import java.util.Map;

/**
 * Created by matthewxi on 7/15/15.
 */
public class AluviSecureRequest extends JacksonRequest {

    private static int ALUVI_API_TIMEOUT = 30000;

    // Custom Constructors For Aluvi
    public AluviSecureRequest(int method, String endpoint, AluviPayload payload, JacksonRequestListener listener) {
        super(AluviSecureRequest.ALUVI_API_TIMEOUT, method, AluviApi.constructUrl(endpoint), payload.toMap(), listener);
    }

    public AluviSecureRequest(int method, String url, JacksonRequestListener listener) {
        super(method, url, listener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        headers.put("Token", UserStateManager.getInstance().getToken());
        return headers;
    }
}
