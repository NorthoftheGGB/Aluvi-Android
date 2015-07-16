package com.aluvi.android.api;

import com.spothero.volley.JacksonRequest;
import com.spothero.volley.JacksonRequestListener;

/**
 * Created by matthewxi on 7/15/15.
 */
public class AluviInsecureRequest extends JacksonRequest {

    private static int ALUVI_API_TIMEOUT = 30000;

    // Custom Constructors For Aluvi
    public AluviInsecureRequest(int method, String endpoint, AluviPayload payload, JacksonRequestListener listener) {
        super(AluviInsecureRequest.ALUVI_API_TIMEOUT, method, AluviApi.constructUrl(endpoint), payload.toMap(), listener);
    }

    public AluviInsecureRequest(int method, String url, JacksonRequestListener listener) {
        super(method, url, listener);
    }


}
