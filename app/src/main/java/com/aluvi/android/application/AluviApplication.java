package com.aluvi.android.application;

import android.app.Application;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.managers.UserStateManager;

/**
 * Created by matthewxi on 7/14/15.
 */
public class AluviApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AluviApi.initialize(this);
        UserStateManager.getInstance().login("paypal@fromthegut.org", "martian");
    }
}
