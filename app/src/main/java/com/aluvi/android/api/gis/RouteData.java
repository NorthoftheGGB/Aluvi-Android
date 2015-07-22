package com.aluvi.android.api.gis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by usama on 7/21/15.
 */
@JsonRootName(value = "shape")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RouteData {
    @JsonProperty("shapePoints")
    private double[] shapePoints;

    private LatLng[] coordinates;

    public LatLng[] getCoordinates() {
        if (coordinates == null) {
            if (shapePoints.length % 2 != 0)
                return null;

            int shapePointTracker = 0;
            LatLng[] coords = new LatLng[shapePoints.length / 2];
            for (int i = 0; i < coords.length; i++) {
                coords[i] = new LatLng(shapePoints[shapePointTracker], shapePoints[shapePointTracker + 1]);
                shapePointTracker += 2;
            }
        }

        return coordinates;
    }

    public double[] getShapePoints() {
        return shapePoints;
    }

    public void setShapePoints(double[] shapePoints) {
        this.shapePoints = shapePoints;
    }
}
