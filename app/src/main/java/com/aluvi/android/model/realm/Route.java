package com.aluvi.android.model.realm;

import android.support.annotation.Nullable;

import com.aluvi.android.model.local.TicketLocation;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/14/15.
 */
public class Route extends RealmObject {

    @JsonProperty("origin")
    private RealmLatLng origin;

    @JsonProperty("destination")
    private RealmLatLng destination;

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

    public static JSONObject toJSON(Route route) {
        JSONObject root = new JSONObject();
        try {
            root.put("origin", RealmLatLng.toJSON(route.getOrigin()));
            root.put("origin_place_name", route.getOriginPlaceName());
            root.put("destination", RealmLatLng.toJSON(route.getDestination()));
            root.put("destination_place_name", route.getDestinationPlaceName());
            root.put("pickup_time", route.getPickupTime());
            root.put("return_time", route.getReturnTime());
            root.put("driving", route.isDriving());
            root.put("pickup_zone_center", new JSONObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return root;
    }

    public static int getHour(String time) {
        int hour = -1;
        if (time != null) {
            String[] split = time.split(":");
            try {
                if (split.length > 0)
                    hour = Integer.parseInt(split[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return hour;
    }

    public static int getMinute(String time) {
        int min = -1;
        if (time != null) {
            String[] split = time.split(":");
            try {
                if (split.length > 1)
                    min = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
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

    private static TicketLocation getTicketLocation(RealmLatLng wrapper, String placeName) {
        return new TicketLocation(wrapper.getLatitude(), wrapper.getLongitude(), placeName);
    }

    public RealmLatLng getOrigin() {
        return origin;
    }

    public void setOrigin(RealmLatLng origin) {
        this.origin = origin;
    }

    public RealmLatLng getDestination() {
        return destination;
    }

    public void setDestination(RealmLatLng destination) {
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
