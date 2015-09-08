package com.aluvi.android.api.tickets;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by matthewxi on 7/16/15.
 */
public class CommuterTicketsResponse {
    @JsonProperty("outgoing_ride_id")
    public int ticketToWorkRideId;

    @JsonProperty("return_ride_id")
    public int ticketFromWorkRideId;

    @JsonProperty("trip_id")
    public int tripId;
}
