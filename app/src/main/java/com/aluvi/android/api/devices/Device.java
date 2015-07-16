package com.aluvi.android.api.devices;

import android.content.pm.PackageInfo;

import com.aluvi.aluvi.BuildConfig;
import com.aluvi.android.api.AluviPayload;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by matthewxi on 7/15/15.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Device extends AluviPayload {

    private Integer userId;
    private String pushToken;
    private String appVersion;
    private String appIdentifier;

    public Device() {
        appVersion = String.valueOf(BuildConfig.VERSION_CODE);
        appIdentifier = BuildConfig.VERSION_NAME;
    }

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
