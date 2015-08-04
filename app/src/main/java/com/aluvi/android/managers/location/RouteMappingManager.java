package com.aluvi.android.managers.location;

import com.aluvi.android.api.gis.MapQuestApi;
import com.aluvi.android.api.gis.models.RouteData;
import com.aluvi.android.model.realm.Ticket;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by usama on 8/3/15.
 */
public class RouteMappingManager {
    public interface RouteMappingListener {
        void onRouteFound(RouteData data);

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

    public void loadRouteForTicket(Ticket ticket, final RouteMappingListener callback) {
        LatLng start = new LatLng(ticket.getOriginLatitude(), ticket.getOriginLongitude());
        LatLng end = new LatLng(ticket.getDestinationLatitude(), ticket.getDestinationLongitude());
        loadRoute(start, end, callback);
    }

    public void loadRoute(LatLng start, LatLng end, final RouteMappingListener callback) {
        MapQuestApi.findRoute(start, end, new MapQuestApi.MapQuestCallback() {
            @Override
            public void onRouteFound(RouteData route) {
                callback.onRouteFound(route);
            }

            @Override
            public void onFailure(int statusCode) {
                callback.onFailure("Could not fetch route");
            }
        });
    }
}
