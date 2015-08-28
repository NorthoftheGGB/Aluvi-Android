package com.aluvi.android.helpers.views.mapbox;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.SafeDrawOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas;
import com.mapbox.mapboxsdk.views.safecanvas.SafePaint;
import com.mapbox.mapboxsdk.views.util.Projection;

/**
 * Created by usama on 8/26/15.
 */
public class CircleOverlay extends SafeDrawOverlay {
    private SafePaint mPaint;
    private ILatLng mCircleCenter;
    private double mCircleRadiusKm;

    public CircleOverlay(ILatLng circleCenter, double circleRadiusKm, int color) {
        init(circleCenter, circleRadiusKm, color);
    }

    public CircleOverlay(ILatLng circleCenter, double circleRadiusKm) {
        init(circleCenter, circleRadiusKm, -1);
    }

    public void init(ILatLng circleCenter, double circleRadiusKm, int color) {
        mCircleCenter = circleCenter;
        mCircleRadiusKm = circleRadiusKm;

        mPaint = new SafePaint();
        mPaint.setColor(color != -1 ? color : Color.argb(150, 255, 0, 0));
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void drawSafe(ISafeCanvas iSafeCanvas, MapView mapView, boolean b) {
        Projection projection = mapView.getProjection();
        LatLng circleEdge = getDestinationPoint(mCircleCenter, mCircleRadiusKm, 0);

        PointF circleCenter = projection.toMapPixels(mCircleCenter, null);
        PointF circleEdgePoint = projection.toMapPixels(circleEdge, null);

        float dx = circleCenter.x - circleEdgePoint.x;
        float dy = circleCenter.y - circleEdgePoint.y;
        float circleRadiusPixels = (float) Math.sqrt(dx * dx + dy * dy);
        iSafeCanvas.drawCircle(circleCenter.x, circleCenter.y, circleRadiusPixels, mPaint);
    }

    /**
     * φ2 = asin( sin φ1 ⋅ cos δ + cos φ1 ⋅ sin δ ⋅ cos θ )
     * λ2 = λ1 + atan2( sin θ ⋅ sin δ ⋅ cos φ1, cos δ − sin φ1 ⋅ sin φ2 )
     * <p/>
     * where φ is latitude, λ is longitude, θ is the bearing (clockwise from north),
     * δ is the angular distance d/R; d being the distance travelled, R the earth’s radius
     */
    private static LatLng getDestinationPoint(ILatLng start, double distanceKm, double bearing) {
        final double EARTH_RADIUS = 6371;
        final double DEG_RAD = Math.PI / 180.0;
        final double RAD_DEG = 180.0 / Math.PI;

        double angularDistance = distanceKm / EARTH_RADIUS;
        double φ1 = start.getLatitude() * DEG_RAD;
        double λ1 = start.getLongitude() * DEG_RAD;
        double φ2 = Math.asin(Math.sin(φ1) * Math.cos(angularDistance) +
                Math.cos(φ1) * Math.sin(angularDistance) * Math.cos(bearing));
        double λ2 = λ1 + Math.atan2(Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(φ1),
                Math.cos(angularDistance) - Math.sin(φ1) * Math.sin(φ2));
        return new LatLng(φ2 * RAD_DEG, λ2 * RAD_DEG);
    }
}
