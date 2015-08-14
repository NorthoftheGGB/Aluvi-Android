package com.aluvi.android.managers.location;

import com.aluvi.android.api.gis.MapQuestApi;
import com.aluvi.android.api.gis.models.RouteData;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.model.realm.RouteDirections;
import com.mapbox.mapboxsdk.geometry.LatLng;

import io.realm.Realm;

/**
 * Created by usama on 8/3/15.
 */
public class RouteMappingManager {
    public interface RouteMappingListener {
        void onRouteFound(RouteDirections data);

        void onFailure(String message);
    }

    private static RouteMappingManager instance;

    private RouteMappingManager() {
    }

    public static synchronized RouteMappingManager getInstance() {
        if (instance == null)
            instance = new RouteMappingManager();
        return instance;
    }

    public void loadRoute(final LatLng start, final LatLng end, final RouteMappingListener callback) {
        RouteDirections route = AluviRealm.getDefaultRealm().where(RouteDirections.class)
                .equalTo("startLat", start.getLatitude())
                .equalTo("startLon", start.getLongitude())
                .equalTo("endLat", end.getLatitude())
                .equalTo("endLon", end.getLongitude())
                .findFirst();

        if (route != null) {
            callback.onRouteFound(route);
        } else {
            MapQuestApi.findRoute(start, end, new MapQuestApi.MapQuestCallback() {
                @Override
                public void onRouteFound(RouteData route) {
                    callback.onRouteFound(cacheRoute(start, end, route.getCoordinates()));
                }

                @Override
                public void onFailure(int statusCode) {
                    callback.onFailure("Could not fetch route");
                }
            });
        }
    }

    private RouteDirections cacheRoute(LatLng start, LatLng end, LatLng[] coordinates) {
        RouteDirections directionsRoute = new RouteDirections();
        directionsRoute.setStartLat(start.getLatitude());
        directionsRoute.setStartLon(start.getLongitude());
        directionsRoute.setEndLat(end.getLatitude());
        directionsRoute.setEndLon(end.getLongitude());
        directionsRoute.setRoutePieces(RouteDirections.wrapLatLng(coordinates));

        Realm realm = AluviRealm.getDefaultRealm();
        realm.beginTransaction();
        directionsRoute = realm.copyToRealm(directionsRoute);
        realm.commitTransaction();
        return directionsRoute;
    }
}
