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

}
