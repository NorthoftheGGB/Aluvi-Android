package com.aluvi.android.api.devices;

import android.os.Build;
import android.util.Log;

import com.aluvi.android.BuildConfig;
import com.aluvi.android.api.request.AluviPayload;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by matthewxi on 7/15/15.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceData extends AluviPayload {

    @JsonProperty("push_token")
    private String pushToken;

    @JsonProperty("app_version")
    private String appVersion;

    @JsonProperty("app_identifier")
    private String appIdentifier;

    @JsonProperty("platform")
    private String platform;

    @JsonProperty("os")
    private String os;

    @JsonProperty("hardware")
    private String hardware;

    public DeviceData() {
        appVersion = String.valueOf(BuildConfig.VERSION_CODE);
        appIdentifier = BuildConfig.APPLICATION_ID;
        platform = DevicesApi.ANDROID_PUSH_PLATFORM_NAME;
        hardware = Build.MANUFACTURER + " " + Build.BOARD + " " + Build.DISPLAY;
        os = "android " + String.valueOf(Build.VERSION.SDK_INT);
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

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getHardware() {
        return hardware;
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }
}
