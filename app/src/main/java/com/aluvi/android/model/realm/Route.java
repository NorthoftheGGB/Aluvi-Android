package com.aluvi.android.model.realm;

import android.support.annotation.Nullable;

import com.aluvi.android.model.local.TicketLocation;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/14/15.
 */
public class Route extends RealmObject {

    @JsonProperty("origin")
    private LocationWrapper origin;

    @JsonProperty("destination")
    private LocationWrapper destination;

    @JsonProperty("origin_place_name")
    private String originPlaceName;

    @JsonProperty("destination_place_name")
    private String destinationPlaceName;

    @JsonProperty("pickup_time")
    private String pickupTime;

    @JsonProperty("return_time")
    private String returnTime;

    @JsonProperty("driving")
    private boolean driving;

    public static int getHour(String time) {
        int hour = -1;
        if (time != null) {
            String[] split = time.split(":");
            if (split.length > 0)
                hour = Integer.parseInt(split[0]);
        }

        return hour;
    }

    public static int getMinute(String time) {
        int min = -1;
        if (time != null) {
            String[] split = time.split(":");
            if (split.length > 1)
                min = Integer.parseInt(split[1]);
        }

        return min;
    }

    public static String getTime(int hour, int min) {
        String sHour = hour < 10 ? "0" + hour : "" + hour;
        String sMin = min < 10 ? "0" + min : "" + min;
        return sHour + ":" + sMin;
    }

    @Nullable
    public static TicketLocation getOriginTicketLocation(Route route) {
        TicketLocation location = null;
        if (route != null) {
            return getTicketLocation(route.getOrigin(), route.getOriginPlaceName());
        }

        return location;
    }

    @Nullable
    public static TicketLocation getDestinationTicketLocation(Route route) {
        TicketLocation location = null;
        if (route != null) {
            return getTicketLocation(route.getDestination(), route.getDestinationPlaceName());
        }

        return location;
    }

    private static TicketLocation getTicketLocation(LocationWrapper wrapper, String placeName) {
        return new TicketLocation(wrapper.getLatitude(), wrapper.getLongitude(), placeName);
    }

    public LocationWrapper getOrigin() {
        return origin;
    }

    public void setOrigin(LocationWrapper origin) {
        this.origin = origin;
    }

    public LocationWrapper getDestination() {
        return destination;
    }

    public void setDestination(LocationWrapper destination) {
        this.destination = destination;
    }

    public String getOriginPlaceName() {
        return originPlaceName;
    }

    public void setOriginPlaceName(String originPlaceName) {
        this.originPlaceName = originPlaceName;
    }

    public String getDestinationPlaceName() {
        return destinationPlaceName;
    }

    public void setDestinationPlaceName(String destinationPlaceName) {
        this.destinationPlaceName = destinationPlaceName;
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
