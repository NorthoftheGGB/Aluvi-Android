package com.aluvi.android.model;

import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/14/15.
 */
public class Route extends RealmObject {

    private double homeLatitude;
    private double homeLongitude;
    private double workLatitude;
    private double workLongitude;
    private String homePlaceName;
    private String workPlaceName;
    private String pickupTime;
    private String returnTime;
    private boolean driving;

    public double getHomeLatitude() {
        return homeLatitude;
    }

    public void setHomeLatitude(double homeLatitude) {
        this.homeLatitude = homeLatitude;
    }

    public double getHomeLongitude() {
        return homeLongitude;
    }

    public void setHomeLongitude(double homeLongitude) {
        this.homeLongitude = homeLongitude;
    }

    public double getWorkLatitude() {
        return workLatitude;
    }

    public void setWorkLatitude(double workLatitude) {
        this.workLatitude = workLatitude;
    }

    public double getWorkLongitude() {
        return workLongitude;
    }

    public void setWorkLongitude(double workLongitude) {
        this.workLongitude = workLongitude;
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
