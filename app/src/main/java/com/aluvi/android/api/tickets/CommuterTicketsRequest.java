package com.aluvi.android.api.tickets;

import com.aluvi.android.api.request.AluviPayload;
import com.aluvi.android.model.realm.Ticket;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by matthewxi on 7/16/15.
 */
public class CommuterTicketsRequest extends AluviPayload {

    @JsonProperty("departure_latitude")
    double homeLatitude;

    @JsonProperty("departure_longitude")
    double homeLongitude;

    @JsonProperty("departure_place_name")
    String homePlaceName;

    @JsonProperty("destination_latitude")
    double workLatitude;

    @JsonProperty("destination_longitude")
    double workLongitude;

    @JsonProperty("destination_place_name")
    String workPlaceName;

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'kk:mm:ss.SSS")
    @JsonProperty("departure_pickup_time")
    Date toWorkPickupTime;

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'kk:mm:ss.SSS")
    @JsonProperty("return_pickup_time")
    Date fromWorkPickupTime;

    @JsonProperty("driving")
    boolean driving;

    public CommuterTicketsRequest(Ticket ticketToWork, Ticket ticketFromWork) {
        homeLatitude = ticketToWork.getOriginLatitude();
        homeLongitude = ticketToWork.getOriginLongitude();
        homePlaceName = ticketToWork.getOriginPlaceName();
        workLatitude = ticketToWork.getDestinationLatitude();
        workLongitude = ticketToWork.getDestinationLongitude();
        workPlaceName = ticketToWork.getDestinationPlaceName();
        toWorkPickupTime = ticketToWork.getPickupTime();
        fromWorkPickupTime = ticketFromWork.getPickupTime();
        driving = ticketToWork.isDriving();
    }
}
