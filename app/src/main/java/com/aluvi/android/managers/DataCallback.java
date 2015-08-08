package com.aluvi.android.managers;

/**
 * Created by usama on 8/8/15.
 */
public interface DataCallback<T> {
    void success(T result);

    void failure(String message);
}
