package com.aluvi.android.helpers;

/**
 * Created by usama on 7/13/15.
 */
public interface AsyncCallback<T>
{
    void onOperationCompleted(T result);
}
