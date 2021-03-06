package com.aluvi.android.api.gis.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by usama on 7/21/15.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RouteData {
    @JsonProperty("route")
    private InnerRouteData innerRouteData;
    private LatLng[] coordinates;

    public LatLng[] getCoordinates() {
        if (innerRouteData == null || innerRouteData.shape == null || innerRouteData.shape.shapePoints == null)
            return null;

        double[] shapePoints = innerRouteData.shape.shapePoints;
        if (coordinates == null) {
            if (innerRouteData.shape.shapePoints.length % 2 != 0)
                return null;

            int shapePointTracker = 0;
            coordinates = new LatLng[shapePoints.length / 2];
            for (int i = 0; i < coordinates.length; i++) {
                coordinates[i] = new LatLng(shapePoints[shapePointTracker], shapePoints[shapePointTracker + 1]);
                shapePointTracker += 2;
            }
        }

        return coordinates;
    }

    private static class InnerRouteData {
        @JsonProperty("shape")
        private Shape shape;

        private static class Shape {
            @JsonProperty("shapePoints")
            private double[] shapePoints;
        }
    }
}
