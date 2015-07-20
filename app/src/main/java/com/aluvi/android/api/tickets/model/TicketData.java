package com.aluvi.android.api.tickets.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by matthewxi on 7/18/15.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketData {

    @JsonProperty("ride_id")
    public Integer rideId;
    @JsonProperty("ticket_id")
    public Integer tripId;

    @JsonProperty("origin_place_name")
    public String originPlaceName;
    @JsonProperty("origin_latitude")
    public double originLatitude;
    @JsonProperty("origin_longitude")
    public double originLongitude;

    @JsonProperty("destination_place_name")
    public String destinationPlaceName;
    @JsonProperty("destination_latitude")
    public double destinationLatitude;
    @JsonProperty("destination_longitude")
    public double destinationLongitude;

    @JsonProperty("fixed_price")
    public Integer fixedPrice;
    @JsonProperty("state")
    public String state;
    @JsonProperty("driving")
    public Boolean driving;

    @JsonProperty("pickup_time")
    public Date pickupTime;

    @JsonProperty("direction")
    public String direction;

    @JsonProperty("origin_short_name")
    public String originShortName;
    @JsonProperty("destination_short_name")
    public String destinationShortName;

    @JsonProperty("driver")
    public DriverData driver;

    @JsonProperty("fare")
    public FareData fare;

    @JsonProperty("car")
    public CarData car;
}
