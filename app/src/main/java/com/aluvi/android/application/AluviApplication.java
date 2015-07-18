package com.aluvi.android.application;

import android.app.Application;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.application.push.PushManager;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.UserStateManager;
import com.splunk.mint.Mint;

/**
 * Created by matthewxi on 7/14/15.
 */
public class AluviApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Mint.initAndStartSession(this, "9a3f54d4");

        AluviRealm.initialize(this);
        PushManager.setup(this);
        GlobalIdentifiers.initialize(this);
        AluviApi.initialize(this);
        UserStateManager.initialize(this);
        CommuteManager.initialize(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AluviRealm.closeDefaultRealm();
    }
}
