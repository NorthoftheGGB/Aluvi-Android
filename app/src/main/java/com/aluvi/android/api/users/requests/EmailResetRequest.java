package com.aluvi.android.api.users.requests;

import com.aluvi.android.api.request.AluviPayload;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by usama on 8/25/15.
 */
public class EmailResetRequest extends AluviPayload {
    @JsonProperty("email")
    private String email;

    public EmailResetRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
