package com.aluvi.android.api.devices;

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
public class DeviceData extends AluviPayload {

    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("push_token")
    private String pushToken;

    @JsonProperty("app_version")
    private String appVersion;

    @JsonProperty("app_identifier")
    private String appIdentifier;

    @JsonProperty("platform")
    private String platform;

    public DeviceData() {
        appVersion = String.valueOf(BuildConfig.VERSION_CODE);
        appIdentifier = BuildConfig.VERSION_NAME;
        platform = DevicesApi.ANDROID_PUSH_PLATFORM_NAME;
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
