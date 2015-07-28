package com.aluvi.android.helpers.views;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aluvi.android.helpers.EasyILatLang;
import com.aluvi.android.api.gis.GeocodingApi;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * Created by usama on 7/14/15.
 */
public class MapBoxStateSaver {
    private static final String MAP_PAN_LAT_KEY = "map_pan_lat",
            MAP_PAN_LON_KEY = "map_pan_lon",
            ZOOM_KEY = "zoom";

    public static final int DEFAULT_ZOOM = 17;

    public static void saveMapState(MapView mapView, String key) {
        LatLng center = mapView.getCenter();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mapView.getContext()).edit();
        editor.putFloat(MAP_PAN_LAT_KEY + key, (float) center.getLatitude());
        editor.putFloat(MAP_PAN_LON_KEY + key, (float) center.getLongitude());
        editor.putFloat(ZOOM_KEY + key, mapView.getZoomLevel());
        editor.commit();
    }

    public static boolean restoreMapState(MapView mapView, String key) {
        return restoreMapState(mapView, DEFAULT_ZOOM, key);
    }

    /**
     * @param mapView
     * @param defaultZoom
     * @param key
     * @return false if there was no saved location
     */
    public static boolean restoreMapState(MapView mapView, int defaultZoom, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mapView.getContext());
        float savedLat = prefs.getFloat(MAP_PAN_LAT_KEY + key, GeocodingApi.INVALID_LOCATION);
        float savedLon = prefs.getFloat(MAP_PAN_LON_KEY + key, GeocodingApi.INVALID_LOCATION);
        float savedZoom = prefs.getFloat(ZOOM_KEY + key, defaultZoom);
        mapView.setZoom(savedZoom);

        if (savedLat != GeocodingApi.INVALID_LOCATION && savedLon != GeocodingApi.INVALID_LOCATION) {
            mapView.setCenter(new EasyILatLang(savedLat, savedLon));
            return true;
        } else {
            mapView.setZoom(mapView.getMinZoomLevel());
        }

        return false;
    }
}
