package com.aluvi.android.api.tickets.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by matthewxi on 7/18/15.
 */
public class FareData {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("state")
    private String state;

    @JsonProperty("meeting_point_place_name")
    private String meetingPointPlaceName;

    @JsonProperty("meeting_point_latitude")
    private String meetingPointLatitude;

    @JsonProperty("meeting_point_longitude")
    private String meetingPointLongitude;

    @JsonProperty("drop_off_point_placename")
    private String dropOffPointPlaceName;

    @JsonProperty("drop_off_point_latitude")
    private String dropOffPointLatitude;

    @JsonProperty("drop_off_point_longitude")
    private String dropOffPointLongitude;

    @JsonProperty("riders")
    private ArrayList<RiderData> riders;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMeetingPointPlaceName() {
        return meetingPointPlaceName;
    }

    public void setMeetingPointPlaceName(String meetingPointPlaceName) {
        this.meetingPointPlaceName = meetingPointPlaceName;
    }

    public String getMeetingPointLatitude() {
        return meetingPointLatitude;
    }

    public void setMeetingPointLatitude(String meetingPointLatitude) {
        this.meetingPointLatitude = meetingPointLatitude;
    }

    public String getMeetingPointLongitude() {
        return meetingPointLongitude;
    }

    public void setMeetingPointLongitude(String meetingPointLongitude) {
        this.meetingPointLongitude = meetingPointLongitude;
    }

    public String getDropOffPointPlaceName() {
        return dropOffPointPlaceName;
    }

    public void setDropOffPointPlaceName(String dropOffPointPlaceName) {
        this.dropOffPointPlaceName = dropOffPointPlaceName;
    }

    public String getDropOffPointLatitude() {
        return dropOffPointLatitude;
    }

    public void setDropOffPointLatitude(String dropOffPointLatitude) {
        this.dropOffPointLatitude = dropOffPointLatitude;
    }

    public String getDropOffPointLongitude() {
        return dropOffPointLongitude;
    }

    public void setDropOffPointLongitude(String dropOffPointLongitude) {
        this.dropOffPointLongitude = dropOffPointLongitude;
    }

    public ArrayList<RiderData> getRiders() {
        return riders;
    }

    public void setRiders(ArrayList<RiderData> riders) {
        this.riders = riders;
    }
}
