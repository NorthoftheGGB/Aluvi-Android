package com.aluvi.android.model.realm;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.realm.RealmObject;

/**
 * Created by usama on 7/29/15.
 */
public class LocationWrapper extends RealmObject {
    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
