package com.aluvi.android.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.aluvi.android.api.devices.DeviceData;
import com.aluvi.android.api.devices.DevicesApi;
import com.aluvi.android.api.users.UsersApi;
import com.aluvi.android.application.AluviPreferences;
import com.aluvi.android.model.local.Profile;
import com.google.gson.Gson;

/**
 * Created by matthewxi on 7/14/15.
 */

public class UserStateManager {
    private static UserStateManager mInstance;

    private SharedPreferences preferences;
    private Gson gson;

    private String apiToken;
    private Profile profile;
    private String driverState;
    private String riderState;

    public interface Callback {
        void success();

        void failure(String message);
    }

    public static synchronized void initialize(Context context) {
        if (mInstance == null) {
            mInstance = new UserStateManager(context);
        }
    }

    public static synchronized UserStateManager getInstance() {
        return mInstance;
    }

    public UserStateManager(Context context) {
        gson = new Gson();
        preferences = context.getSharedPreferences(AluviPreferences.COMMUTER_PREFERENCES_FILE, 0);
        apiToken = preferences.getString(AluviPreferences.API_TOKEN_KEY, null);
        String profileString = preferences.getString(AluviPreferences.PROFILE_STRING_KEY, null);
        if (profileString != null) {
            profile = gson.fromJson(profileString, Profile.class);
        } else {
            profile = null;
        }
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
        preferences.edit().putString(AluviPreferences.API_TOKEN_KEY, apiToken).commit();
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        preferences.edit().putString(AluviPreferences.PROFILE_STRING_KEY, gson.toJson(profile)).commit();
    }

    public String getDriverState() {
        return driverState;
    }

    public void setDriverState(String driverState) {
        this.driverState = driverState;
        preferences.edit().putString(AluviPreferences.DRIVER_STATE_KEY, driverState).commit();
    }

    public String getRiderState() {
        return riderState;
    }

    public void setRiderState(String riderState) {
        this.riderState = riderState;
        preferences.edit().putString(AluviPreferences.RIDER_STATE_KEY, riderState).commit();
    }

    public void login(String email, String password, final Callback callback) {
        UsersApi.login(email, password, new UsersApi.Callback() {
            @Override
            public void success(String token) {
                setApiToken(token);

                // CommuteManager.loadFromServer()

                DevicesApi.updateUser(new DevicesApi.Callback() {
                    @Override
                    public void success() {
                        callback.success();
                    }

                    @Override
                    public void failure(int statusCode) {
                        callback.failure("Could not update user");
                    }
                });
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Could not log in");
            }
        });
    }

    public void logout(final Callback callback) {
        setApiToken(null);

        DeviceData deviceData = new DeviceData();
        deviceData.setUserId(Integer.valueOf(0));
        deviceData.setPushToken("");
        DevicesApi.patchDevice(deviceData, new DevicesApi.Callback() {
            @Override
            public void success() {
                callback.success();
            }

            @Override
            public void failure(int statusCode) {
                // we will probably translate the status code to a message here
                // using a strings file.
                callback.failure("Logout Failed for some reason");
            }
        });

        CommuteManager.getInstance().clear();
    }
}
