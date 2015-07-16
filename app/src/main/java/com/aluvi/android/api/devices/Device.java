package com.aluvi.android.api.devices;

import com.aluvi.android.api.AluviPayload;

/**
 * Created by matthewxi on 7/15/15.
 */
public class Device extends AluviPayload {
    private int userId;
    private String pushToken;
    private String appVersion;
    private String appIdentifier;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppIdentifier() {
        return appIdentifier;
    }

    public void setAppIdentifier(String appIdentifier) {
        this.appIdentifier = appIdentifier;
    }
}
