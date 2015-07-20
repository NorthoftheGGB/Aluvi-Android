package com.aluvi.android.api.tickets.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by matthewxi on 7/18/15.
 */
public class FareData {
    @JsonProperty("id")
    Integer id;

    @JsonProperty("state")
    String state;

    @JsonProperty("meeting_point_place_name")
    String meetingPointPlaceName;

    @JsonProperty("meeting_point_latitude")
    String meetingPointLatitude;

    @JsonProperty("meeting_point_longitude")
    String meetingPointLongitude;

    @JsonProperty("drop_off_point_placename")
    String dropOffPointPlaceName;

    @JsonProperty("drop_off_point_latitude")
    String dropOffPointLatitude;

    @JsonProperty("drop_off_point_longitude")
    String dropOffPointLongitude;

    @JsonProperty("riders")
    ArrayList<RiderData> riders;
}
