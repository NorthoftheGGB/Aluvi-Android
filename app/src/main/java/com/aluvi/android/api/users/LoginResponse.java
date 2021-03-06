package com.aluvi.android.api.users;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by matthewxi on 7/14/15.
 */
public class LoginResponse {
    @JsonProperty("rider_state")
    private String riderState;

    @JsonProperty("driver_state")
    private String driverState;

    @JsonProperty("token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRiderState() {
        return riderState;
    }

    public void setRiderState(String riderState) {
        this.riderState = riderState;
    }

    public String getDriverState() {
        return driverState;
    }

    public void setDriverState(String driverState) {
        this.driverState = driverState;
    }
}
