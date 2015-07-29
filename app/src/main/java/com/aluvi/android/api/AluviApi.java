package com.aluvi.android.api;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.spothero.volley.JacksonNetwork;
import com.spothero.volley.JacksonRequest;

/**
 * Created by matthewxi on 7/14/15.
 */
public class AluviApi implements AluviApiConstants {
    //Singleton
    private static AluviApi mInstance;

    // member variables
    private RequestQueue mRequestQueue;

    public static final String constructUrl(String path) {
        return API_BASE_URL + path;
    }

    public AluviApi(Context context) {
        this.mRequestQueue = JacksonNetwork.newRequestQueue(context.getApplicationContext());
        JacksonRequest.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static final synchronized void initialize(Context context) {
        mInstance = new AluviApi(context);
    }

    public static final AluviApi getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}
