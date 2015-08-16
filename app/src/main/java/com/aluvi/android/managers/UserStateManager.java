package com.aluvi.android.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.aluvi.android.api.devices.DevicesApi;
import com.aluvi.android.api.users.LoginResponse;
import com.aluvi.android.api.users.UsersApi;
import com.aluvi.android.api.users.models.DriverProfileData;
import com.aluvi.android.api.users.models.ProfileData;
import com.aluvi.android.application.AluviPreferences;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.managers.packages.Callback;
import com.aluvi.android.managers.packages.DataCallback;
import com.aluvi.android.model.realm.Profile;

import java.net.HttpURLConnection;

import io.realm.Realm;

/**
 * Created by matthewxi on 7/14/15.
 */

public class UserStateManager {
    private static UserStateManager mInstance;
    private SharedPreferences preferences;

    private Profile profile;
    private String apiToken, driverState, riderState;

    public final static String DRIVER_STATE_ACTIVE = "active",
            DRIVER_STATE_INACTIVE = "uninterested";

    public static synchronized void initialize(Context context) {
        if (mInstance == null)
            mInstance = new UserStateManager(context);
    }

    public static synchronized UserStateManager getInstance() {
        return mInstance;
    }

    public UserStateManager(Context context) {
        preferences = context.getSharedPreferences(AluviPreferences.COMMUTER_PREFERENCES_FILE, 0);
        apiToken = preferences.getString(AluviPreferences.API_TOKEN_KEY, null);
        profile = AluviRealm.getDefaultRealm().where(Profile.class).findFirst();
    }

    public void sync(final Callback callback) {
        buildSyncQueue(new RequestQueue.RequestQueueListener() {
            @Override
            public void onRequestsFinished() {
                callback.success();
            }

            @Override
            public void onError(String message) {
                callback.failure(message);
            }
        }).execute();
    }

    public RequestQueue buildSyncQueue(RequestQueue.RequestQueueListener listener) {
        return new RequestQueue(listener).addRequest(new RequestQueue.Task() {
            @Override
            public void run() {
                fetchProfile(new DataCallback<Profile>() {
                    @Override
                    public void success(final Profile result) {
                        AluviRealm.getDefaultRealm().executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.clear(Profile.class);

                                profile = result;
                                realm.copyToRealm(result);
                                onTaskComplete();
                            }
                        });
                    }

                    @Override
                    public void failure(String message) {
                        onTaskError(message);
                    }
                });
            }
        }).buildQueue();
    }

    @Nullable
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

    public String getDriverState() {
        return driverState;
    }

    public boolean isUserDriver() {
        return driverState != null && driverState.equals(DRIVER_STATE_ACTIVE);
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
        UsersApi.login(email, password, new UsersApi.LoginCallback() {
            @Override
            public void success(LoginResponse response) {
                setApiToken(response.getToken());
                setDriverState(response.getDriverState());
                setRiderState(response.getRiderState());

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

    public void registerUser(ProfileData data, final Callback callback) {
        UsersApi.registerUser(data, new UsersApi.RegistrationCallback() {
            @Override
            public void success() {
                callback.success();
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Unable to register user");
            }
        });
    }

    public void registerDriver(DriverProfileData data, final Callback callback) {
        UsersApi.registerDriver(data, new UsersApi.RegistrationCallback() {
            @Override
            public void success() {
                setDriverState(DRIVER_STATE_ACTIVE);
                callback.success();
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Unable to register driver");
            }
        });
    }

    public void fetchProfile(final DataCallback<Profile> profileDataCallback) {
        UsersApi.refreshProfile(new UsersApi.ProfileCallback() {
            @Override
            public void success(Profile profile) {
                profileDataCallback.success(profile);
            }

            @Override
            public void failure(int statusCode) {
                profileDataCallback.failure("Unable to refresh profile");
            }
        });
    }

    public void saveProfile(final Callback callback) {
        UsersApi.saveProfile(profile, new UsersApi.ProfileCallback() {
            @Override
            public void success(final Profile responseProfile) {
                AluviRealm.getDefaultRealm().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.clear(Profile.class);
                        profile = realm.copyToRealm(responseProfile);
                    }
                });

                callback.success();
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Unable to save profile");
            }
        });
    }

    public void logout(final Callback callback) {
        DevicesApi.disassociateUser(new DevicesApi.Callback() {
            @Override
            public void success() {
                setApiToken(null);
                callback.success();
            }

            @Override
            public void failure(int statusCode) {
                if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED)
                    setApiToken(null);

                // we will probably translate the status code to a message here
                // using a strings file.
                if (callback != null)
                    callback.failure("Logout failed for some reason");
            }
        });

        CommuteManager.getInstance().clear();
    }
}
