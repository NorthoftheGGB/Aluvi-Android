package com.aluvi.android.application;

import android.app.Application;
import android.widget.Toast;

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
        UserStateManager.initialize(this);
        GlobalIdentifiers.initialize(this);

        UserStateManager.getInstance().login("paypal@fromthegut.org", "martian", new UserStateManager.Callback(){
            @Override
            public void success() {
                Toast.makeText(getApplicationContext(), "Logged In", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(String message) {
                Toast.makeText(getApplicationContext(), "Failed to Log In", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
