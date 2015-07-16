package com.aluvi.android.api.request;

import com.aluvi.android.api.AluviApi;
import com.spothero.volley.JacksonRequest;
import com.spothero.volley.JacksonRequestListener;

import java.util.Map;

/**
 * Created by matthewxi on 7/15/15.
 */
public class AluviUnauthenticatedRequest<T> extends JacksonRequest<T> {

    private static int ALUVI_API_TIMEOUT = 30000;

    // Custom Constructors For Aluvi
    public AluviUnauthenticatedRequest(int method, String endpoint, AluviPayload payload, JacksonRequestListener listener) {
        super(AluviUnauthenticatedRequest.ALUVI_API_TIMEOUT, method, AluviApi.constructUrl(endpoint), payload.toMap(), listener);
    }

    public AluviUnauthenticatedRequest(int method, String url, JacksonRequestListener listener) {
        super(method, url, listener);
    }

    public AluviUnauthenticatedRequest(int method, String endpoint, Map params, JacksonRequestListener listener) {
        super(method, AluviApi.API_BASE_URL, endpoint, params, listener);
    }
}
