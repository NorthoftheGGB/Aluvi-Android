package com.aluvi.android.model.realm;

import com.mapbox.mapboxsdk.geometry.LatLng;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by usama on 8/14/15.
 */
public class RouteDirections extends RealmObject {
    private RealmList<RealmLatLng> routePieces;
    private double startLat, startLon, endLat, endLon;

    public RealmList<RealmLatLng> getRoutePieces() {
        return routePieces;
    }

    public void setRoutePieces(RealmList<RealmLatLng> routePieces) {
        this.routePieces = routePieces;
    }

    public static RealmList<RealmLatLng> wrapLatLng(LatLng[] list) {
        RealmList<RealmLatLng> latLngs = new RealmList<>();
        for (int i = 0; i < list.length; i++) {
            RealmLatLng latLng = new RealmLatLng();
            latLng.setLatitude(list[i].getLatitude());
            latLng.setLongitude(list[i].getLongitude());
            latLngs.add(latLng);
        }

        return latLngs;
    }

    public double getStartLat() {
        return startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }

    public double getStartLon() {
        return startLon;
    }

    public void setStartLon(double startLon) {
        this.startLon = startLon;
    }

    public double getEndLat() {
        return endLat;
    }

    public void setEndLat(double endLat) {
        this.endLat = endLat;
    }

    public double getEndLon() {
        return endLon;
    }

    public void setEndLon(double endLon) {
        this.endLon = endLon;
    }
}
