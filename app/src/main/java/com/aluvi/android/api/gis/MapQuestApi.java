package com.aluvi.android.api.gis;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.gis.models.RouteData;
import com.aluvi.android.api.request.AluviUnauthenticatedRequest;
import com.aluvi.android.api.request.GetRequestBuilder;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.spothero.volley.JacksonRequestListener;

/**
 * Created by usama on 7/21/15.
 */
public class MapQuestApi {

    public interface MapQuestCallback {
        void onRouteFound(RouteData route);

        void onFailure(int statusCode);
    }

    public static final String MAPQUEST_BASE_URL = "http://open.mapquestapi.com/",
            MAP_QUEST_API_KEY = "Fmjtd|luur2guan0,b5=o5-9azxgz",
            DIRECTIONS_ENDPOINT = "directions/v2/route";

    public static void findRoute(LatLng startLoc, LatLng endLoc, final MapQuestCallback callback) {
        String routeSearchUrl = new GetRequestBuilder(MAPQUEST_BASE_URL + DIRECTIONS_ENDPOINT)
                .appendParameter("key", MAP_QUEST_API_KEY)
                .appendParameter("from", startLoc.getLatitude() + "," + startLoc.getLongitude())
                .appendParameter("to", endLoc.getLatitude() + "," + endLoc.getLongitude())
                .appendParameter("fullShape", true)
                .build();

        AluviUnauthenticatedRequest<RouteData> routeRequest = new AluviUnauthenticatedRequest<>(Request.Method.GET,
                routeSearchUrl,
                new JacksonRequestListener<RouteData>() {
                    @Override
                    public void onResponse(RouteData response, int statusCode, VolleyError error) {
                        if (statusCode == 200)
                            callback.onRouteFound(response);
                        else
                            callback.onFailure(statusCode);
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(RouteData.class);
                    }
                });

        routeRequest.addAcceptedStatusCodes(new int[]{200, 400});
        AluviApi.getInstance().getRequestQueue().add(routeRequest);
    }
}
