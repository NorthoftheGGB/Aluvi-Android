package com.aluvi.android.api.users.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by usama on 8/20/15.
 */
public class ReceiptData {
    @JsonProperty("amount")
    private int amount;

    @JsonProperty("type")
    private String type;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
