package com.aluvi.android.api.users;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.request.AluviAuthRealmRequestListener;
import com.aluvi.android.api.request.AluviAuthenticatedRequest;
import com.aluvi.android.model.realm.Route;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;

import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Created by usama on 7/29/15.
 */
public class RoutesApi {

    public interface OnRouteSavedListener {
        void onSaved(Route route);

        void onFailure(int statusCode);
    }

    public interface OnRouteFetchedListener {
        void onFetched(Route route);

        void onFailure(int statusCode);
    }

    public static void saveRoute(final Route route, final OnRouteSavedListener listener) {
        JsonObjectRequest saveRouteRequest = new JsonObjectRequest(AluviApi.API_BASE_URL + AluviApi.API_ROUTE, Route.toJSON(route),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onSaved(route);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onFailure(HttpURLConnection.HTTP_BAD_REQUEST);
                    }
                });

        AluviApi.getInstance().getRequestQueue().add(saveRouteRequest);
    }

    public static void getSavedRoute(final OnRouteFetchedListener listener) {
        AluviAuthenticatedRequest<Route> routeRequest = new AluviAuthenticatedRequest<>(
                Request.Method.GET,
                AluviApi.API_ROUTE,
                new AluviAuthRealmRequestListener<Route>() {
                    @Override
                    public void onAuthRealmResponse(Route response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK) {
                            listener.onFetched(response);
                        } else {
                            listener.onFailure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(Route.class);
                    }
                }
        );

        routeRequest.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(routeRequest);
    }
}
