package com.aluvi.android.application;

import android.app.Application;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.UserStateManager;
import com.splunk.mint.Mint;

/**
 * Created by matthewxi on 7/14/15.
 */
public class AluviApplication extends Application
{
    private final String TAG = "AluviApplication";

    @Override
    public void onCreate()
    {
        super.onCreate();

        Mint.initAndStartSession(this, "9a3f54d4");

        GlobalIdentifiers.initialize(this);
        AluviApi.initialize(this);
        UserStateManager.initialize(this);
        CommuteManager.initialize(this);
    }
}
