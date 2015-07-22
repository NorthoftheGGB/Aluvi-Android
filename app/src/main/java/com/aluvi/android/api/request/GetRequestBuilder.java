package com.aluvi.android.api.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by usama on 7/21/15.
 */
public class GetRequestBuilder {
    private String baseUrl;
    private String urlWithParams;
    private boolean isFirstParameter = true;

    public GetRequestBuilder(String baseUrl) {
        this.baseUrl = baseUrl;
        urlWithParams = baseUrl + "?";
    }

    public GetRequestBuilder appendParameter(String key, String value) {
        if (isFirstParameter) {
            isFirstParameter = false;
        } else {
            urlWithParams += "&";
        }

        try {
            if (value != null) {
                value = value == null ? "" : value;
                urlWithParams += key + "=" + URLEncoder.encode(value, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return this;
    }

    public GetRequestBuilder appendParameter(String key, double value) {
        return appendParameter(key, Double.toString(value));
    }

    public GetRequestBuilder appendParameter(String key, int value) {
        return appendParameter(key, Integer.toString(value));
    }

    public GetRequestBuilder appendParameter(String key, boolean value) {
        return appendParameter(key, Boolean.toString(value));
    }

    public GetRequestBuilder appendParameter(String key, float value) {
        return appendParameter(key, Float.toString(value));
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        if (urlWithParams != null) {
            // Update the base url in case the user has already started building the request
            urlWithParams = urlWithParams.replace(this.baseUrl, baseUrl);
        }

        this.baseUrl = baseUrl;
    }

    public String build() {
        return urlWithParams;
    }
}
