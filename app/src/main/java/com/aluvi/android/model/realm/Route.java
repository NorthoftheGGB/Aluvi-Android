package com.aluvi.android.model.realm;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/14/15.
 */
public class Route extends RealmObject {

    @JsonProperty("origin")
    private LocationWrapper origin;

    @JsonProperty("destination")
    private LocationWrapper destination;

    @JsonProperty("origin_place_name")
    private String homePlaceName;

    @JsonProperty("destination_place_name")
    private String workPlaceName;

    @JsonProperty("pickup_time")
    private String pickupTime;

    @JsonProperty("return_time")
    private String returnTime;

    @JsonProperty("driving")
    private boolean driving;

    public LocationWrapper getOrigin() {
        return origin;
    }

    public void setOrigin(LocationWrapper origin) {
        this.origin = origin;
    }

    public LocationWrapper getDestination() {
        return destination;
    }

    public void setDestination(LocationWrapper destination) {
        this.destination = destination;
    }

    public String getHomePlaceName() {
        return homePlaceName;
    }

    public void setHomePlaceName(String homePlaceName) {
        this.homePlaceName = homePlaceName;
    }

    public String getWorkPlaceName() {
        return workPlaceName;
    }

    public void setWorkPlaceName(String workPlaceName) {
        this.workPlaceName = workPlaceName;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    public String getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(String returnTime) {
        this.returnTime = returnTime;
    }

    public boolean isDriving() {
        return driving;
    }

    public void setDriving(boolean driving) {
        this.driving = driving;
    }
}
