package com.aluvi.android.api.tickets;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by matthewxi on 7/16/15.
 */
public class CommuterTicketsResponse {
    @JsonProperty("outgoing_ride_id")
    Integer ticketToWorkRideId;

    @JsonProperty("outgoing_trip_id")
    Integer ticketToWorkTripId;

    @JsonProperty("return_ride_id")
    Integer ticketFromWorkRideId;

    @JsonProperty("return_trip_id")
    Integer ticketFromWorkTripId;
}
