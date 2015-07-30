package com.aluvi.android.api.users;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.request.AluviAuthRealmRequestListener;
import com.aluvi.android.api.request.AluviAuthenticatedRequest;
import com.aluvi.android.model.realm.Route;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;

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

    public static void saveRoute(Route route, OnRouteSavedListener listener) {

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
