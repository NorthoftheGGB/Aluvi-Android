package com.aluvi.android.api.tickets.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by usama on 9/6/15.
 */
public class PickupPointData {
    @JsonProperty("point")
    private Point location;

    @JsonProperty("number_of_riders")
    private int numRiders;

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public int getNumRiders() {
        return numRiders;
    }

    public void setNumRiders(int numRiders) {
        this.numRiders = numRiders;
    }

    public static class Point {
        @JsonProperty("latitude")
        private double latitude;

        @JsonProperty("longitude")
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
