package com.aluvi.android.model.realm;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;

/**
 * Created by usama on 7/29/15.
 */
public class LocationWrapper extends RealmObject {
    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;

    public LocationWrapper() {
    }

    public LocationWrapper(double latitude, double longitude) {
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public static JSONObject toJSON(LocationWrapper wrapper) {
        JSONObject root = new JSONObject();
        try {
            root.put("latitude", wrapper.getLatitude());
            root.put("longitude", wrapper.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return root;
    }

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
