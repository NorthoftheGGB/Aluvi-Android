package com.aluvi.android.api.tickets.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by matthewxi on 7/18/15.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TicketData {
    @JsonProperty("ride_id")
    private int rideId;

    @JsonProperty("trip_id")
    private int tripId;

    @JsonProperty("origin_place_name")
    private String originPlaceName;

    @JsonProperty("origin_latitude")
    private double originLatitude;

    @JsonProperty("origin_longitude")
    private double originLongitude;

    @JsonProperty("destination_place_name")
    private String destinationPlaceName;

    @JsonProperty("destination_latitude")
    private double destinationLatitude;

    @JsonProperty("destination_longitude")
    private double destinationLongitude;

    @JsonProperty("fixed_price")
    private double fixedPrice;

    @JsonProperty("state")
    private String state;

    @JsonProperty("driving")
    private boolean isDriving;

    @JsonProperty("pickup_time")
    private String pickUpTime;

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("origin_short_name")
    private String originShortName;

    @JsonProperty("destination_short_name")
    private String destinationShortName;

    public int getRideId() {
        return rideId;
    }

    public void setRideId(int rideId) {
        this.rideId = rideId;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getOriginPlaceName() {
        return originPlaceName;
    }

    public void setOriginPlaceName(String originPlaceName) {
        this.originPlaceName = originPlaceName;
    }

    public double getOriginLatitude() {
        return originLatitude;
    }

    public void setOriginLatitude(double originLatitude) {
        this.originLatitude = originLatitude;
    }

    public double getOriginLongitude() {
        return originLongitude;
    }

    public void setOriginLongitude(double originLongitude) {
        this.originLongitude = originLongitude;
    }

    public String getDestinationPlaceName() {
        return destinationPlaceName;
    }

    public void setDestinationPlaceName(String destinationPlaceName) {
        this.destinationPlaceName = destinationPlaceName;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public double getFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(double fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isDriving() {
        return isDriving;
    }

    public void setIsDriving(boolean isDriving) {
        this.isDriving = isDriving;
    }

    public String getPickUpTime() {
        return pickUpTime;
    }

    public void setPickUpTime(String pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getOriginShortName() {
        return originShortName;
    }

    public void setOriginShortName(String originShortName) {
        this.originShortName = originShortName;
    }

    public String getDestinationShortName() {
        return destinationShortName;
    }

    public void setDestinationShortName(String destinationShortName) {
        this.destinationShortName = destinationShortName;
    }
}