package com.aluvi.android.api;

/**
 * Created by matthewxi on 7/17/15.
 */
public interface ApiCallback {
    void success();

    void failure(int statusCode);
}
