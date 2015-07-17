package com.aluvi.android.application;

import android.content.Context;
import android.provider.Settings;

/**
 * Created by matthewxi on 7/15/15.
 */
public class GlobalIdentifiers
{
    private static GlobalIdentifiers mInstance;

    private String androidId;

    public static synchronized void initialize(Context context)
    {
        mInstance = new GlobalIdentifiers(context);
    }

    public static GlobalIdentifiers getInstance()
    {
        return mInstance;
    }

    public GlobalIdentifiers(Context context)
    {
        androidId = "android-" + Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getAndroidId()
    {
        return androidId;
    }

    public void setAndroidId(String androidId)
    {
        this.androidId = androidId;
    }
}
