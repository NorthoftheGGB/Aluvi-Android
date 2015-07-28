package com.aluvi.android.api.users;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by matthewxi on 7/14/15.
 */
public class LoginResponse {

    public String token;

    @JsonProperty("rider_state")
    public String riderState;

    @JsonProperty("driver_state")
    public String driverState;

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
