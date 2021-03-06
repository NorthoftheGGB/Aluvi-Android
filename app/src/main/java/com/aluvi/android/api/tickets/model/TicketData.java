package com.aluvi.android.api.tickets.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by matthewxi on 7/18/15.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TicketData {
    @JsonProperty("ride_id")
    private int ticketId;

    @JsonProperty("trip_id")
    private int tripId;

    @JsonProperty("trip_state")
    private String tripState;

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

    @JsonProperty("meeting_point_place_name")
    private String meetingPointPlaceName;

    @JsonProperty("meeting_point_latitude")
    private double meetingPointLatitude;

    @JsonProperty("meeting_point_longitude")
    private double meetingPointLongitude;

    @JsonProperty("drop_off_point_placename")
    private double dropOffPointPlaceName;

    @JsonProperty("drop_off_point_latitude")
    private double dropOffPointLatitude;

    @JsonProperty("drop_off_point_longitude")
    private double dropOffPointLongitude;

    @JsonProperty("fixed_price")
    private int fixedPrice;

    @JsonProperty("estimated_earnings")
    private int estimatedEarnings;

    @JsonProperty("state")
    private String state;

    @JsonProperty("driving")
    private boolean isDriving;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'kk:mm:ss.SSSZZZZ")
    @JsonProperty("pickup_time")
    private Date pickUpTime;

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("origin_short_name")
    private String originShortName;

    @JsonProperty("destination_short_name")
    private String destinationShortName;

    @JsonProperty("driver")
    public DriverData driver;

    @JsonProperty("car")
    public CarData car;

    @JsonProperty("riders")
    private ArrayList<RiderData> riders;

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
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

    public Date getPickUpTime() {
        return pickUpTime;
    }

    public void setPickUpTime(Date pickUpTime) {
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


    public String getMeetingPointPlaceName() {
        return meetingPointPlaceName;
    }

    public void setMeetingPointPlaceName(String meetingPointPlaceName) {
        this.meetingPointPlaceName = meetingPointPlaceName;
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

    public double getDropOffPointPlaceName() {
        return dropOffPointPlaceName;
    }

    public void setDropOffPointPlaceName(double dropOffPointPlaceName) {
        this.dropOffPointPlaceName = dropOffPointPlaceName;
    }

    public double getDropOffPointLatitude() {
        return dropOffPointLatitude;
    }

    public void setDropOffPointLatitude(double dropOffPointLatitude) {
        this.dropOffPointLatitude = dropOffPointLatitude;
    }

    public double getDropOffPointLongitude() {
        return dropOffPointLongitude;
    }

    public void setDropOffPointLongitude(double dropOffPointLongitude) {
        this.dropOffPointLongitude = dropOffPointLongitude;
    }

    public int getFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(int fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public int getEstimatedEarnings() {
        return estimatedEarnings;
    }

    public void setEstimatedEarnings(int estimatedEarnings) {
        this.estimatedEarnings = estimatedEarnings;
    }

    public DriverData getDriver() {
        return driver;
    }

    public void setDriver(DriverData driver) {
        this.driver = driver;
    }

    public CarData getCar() {
        return car;
    }

    public void setCar(CarData car) {
        this.car = car;
    }

    public ArrayList<RiderData> getRiders() {
        return riders;
    }

    public void setRiders(ArrayList<RiderData> riders) {
        this.riders = riders;
    }

    public String getTripState() {
        return tripState;
    }

    public void setTripState(String tripState) {
        this.tripState = tripState;
    }
}
