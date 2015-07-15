package com.aluvi.android.model;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/13/15.
 */
public class Fare extends RealmObject {

    private RealmList<Rider> riders;

    private int id;
    private int car_id;
    private int driveTime;
    private float distance;
    private double meetingPointLatitude;
    private double meetingPointLongitude;
    private String meetingPointPlaceName;
    private double dropOffPointLongitude;
    private double dropOffPointLatitude;
    private String dropOffPointPlaceName;
    private Date desiredArrival;
    private Date pickupTime;
    private String state;

    public static String routeDescription(Fare fare) {
        return fare.getMeetingPointPlaceName() + ' ' + fare.getDropOffPointPlaceName();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCar_id() {
        return car_id;
    }

    public void setCar_id(int car_id) {
        this.car_id = car_id;
    }

    public double getMeetingPointLatitude() {
        return meetingPointLatitude;
    }

    public void setMeetingPointLatitude(double meetingPointLatitude) {
        this.meetingPointLatitude = meetingPointLatitude;
    }

    public double getMeetingPointLongitude() {
        return meetingPointLongitude;
    }

    public void setMeetingPointLongitude(double meetingPointLongitude) {
        this.meetingPointLongitude = meetingPointLongitude;
    }

    public String getMeetingPointPlaceName() {
        return meetingPointPlaceName;
    }

    public void setMeetingPointPlaceName(String meetingPointPlaceName) {
        this.meetingPointPlaceName = meetingPointPlaceName;
    }

    public double getDropOffPointLongitude() {
        return dropOffPointLongitude;
    }

    public void setDropOffPointLongitude(double dropOffPointLongitude) {
        this.dropOffPointLongitude = dropOffPointLongitude;
    }

    public double getDropOffPointLatitude() {
        return dropOffPointLatitude;
    }

    public void setDropOffPointLatitude(double dropOffPointLatitude) {
        this.dropOffPointLatitude = dropOffPointLatitude;
    }

    public String getDropOffPointPlaceName() {
        return dropOffPointPlaceName;
    }

    public void setDropOffPointPlaceName(String dropOffPointPlaceName) {
        this.dropOffPointPlaceName = dropOffPointPlaceName;
    }

    public Date getDesiredArrival() {
        return desiredArrival;
    }

    public void setDesiredArrival(Date desiredArrival) {
        this.desiredArrival = desiredArrival;
    }

    public Date getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(Date pickupTime) {
        this.pickupTime = pickupTime;
    }

    public RealmList<Rider> getRiders() {
        return riders;
    }

    public void setRiders(RealmList<Rider> riders) {
        this.riders = riders;
    }


    public int getDriveTime() {
        return driveTime;
    }

    public void setDriveTime(int driveTime) {
        this.driveTime = driveTime;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
