package com.aluvi.android.api;

/**
 * Created by matthewxi on 7/17/15.
 */
public interface ApiCallback {

    public void success();
    public void failure(int statusCode);

}
