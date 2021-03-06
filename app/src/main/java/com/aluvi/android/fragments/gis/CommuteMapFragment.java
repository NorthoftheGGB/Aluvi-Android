package com.aluvi.android.fragments.gis;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aluvi.android.R;
import com.aluvi.android.api.tickets.model.PickupPointData;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.eventBus.BackHomeEvent;
import com.aluvi.android.helpers.eventBus.RefreshTicketsEvent;
import com.aluvi.android.helpers.eventBus.SlidingPanelEvent;
import com.aluvi.android.helpers.views.mapbox.MapBoxStateSaver;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.callbacks.DataCallback;
import com.aluvi.android.managers.location.RouteMappingManager;
import com.aluvi.android.model.realm.RealmLatLng;
import com.aluvi.android.model.realm.Route;
import com.aluvi.android.model.realm.RouteDirections;
import com.aluvi.android.model.realm.Ticket;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.RotateEvent;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;
import io.realm.RealmList;

/**
 * Created by usama on 7/13/15.
 */
public class CommuteMapFragment extends BaseButterFragment {
    public interface CommuteMapListener {
        void onMapPanned();
    }

    @Bind(R.id.mapview) MapView mMapView;
    @Bind(R.id.map_text_view_commute_pending) TextView mCommutePendingTextView;

    private final String MAP_STATE_KEY = "map_fragment_main";

    private Marker mCurrentlyFocusedMarker;
    private InfoWindow mCurrentlyOpenedInfoWindow;
    private PathOverlay mCurrentPathOverlay;

    private CommuteMapListener mMapListener;

    public static CommuteMapFragment newInstance() {
        return new CommuteMapFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMapListener = (CommuteMapListener) (getParentFragment() != null ? getParentFragment() : context);
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void initUI() {
        resetUI();

        mMapView.setUserLocationEnabled(true);
        if (!MapBoxStateSaver.restoreMapState(mMapView, MAP_STATE_KEY)) {
            if (mMapView.getUserLocation() != null)
                mMapView.setCenter(new LatLng(mMapView.getUserLocation()), false);
            else // Hover over NASA if we have neither location nor saved map data
                mMapView.setCenter(new LatLng(37.420654, -122.064938), false);

            mMapView.setZoom(MapBoxStateSaver.DEFAULT_ZOOM);
        }

        mMapView.addListener(new PanHelper(new PanHelper.PanListener() {
            @Override
            public void onUserPanned() {
                mMapListener.onMapPanned();
            }
        }));
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        fetchPickupPoints();
    }

    @Override
    public void onPause() {
        super.onPause();
        MapBoxStateSaver.saveMapState(mMapView, MAP_STATE_KEY);
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(RefreshTicketsEvent event) {
        onTicketsRefreshed(event.getActiveTicket());
    }

    @SuppressWarnings("unused")
    public void onEvent(SlidingPanelEvent event) {
        centerMapOnCurrentPin(event.getPanelHeight());
    }

    @SuppressWarnings("unused")
    public void onEvent(BackHomeEvent event) {
        showInfoForCurrentMarker(event.getActiveTicket(), true);
    }

    private void onTicketsRefreshed(Ticket activeTicket) {
        if (getActivity() != null) {
            resetUI(); // Reset UI to original state
            if (activeTicket != null) {
                plotTicketRoute(activeTicket);
                switch (activeTicket.getState()) {
                    case Ticket.STATE_REQUESTED:
                        mCommutePendingTextView.setVisibility(View.VISIBLE);
                        break;
                    case Ticket.STATE_IN_PROGRESS:
                    case Ticket.STATE_STARTED:
                }
            } else {
                plotRoute(CommuteManager.getInstance().getRoute());
            }

            getActivity().supportInvalidateOptionsMenu();
        }
    }

    private void resetUI() {
        mCommutePendingTextView.setVisibility(View.INVISIBLE);
        mMapView.clear();

        if (mCurrentlyOpenedInfoWindow != null)
            mCurrentlyOpenedInfoWindow.close();

        if (mCurrentPathOverlay != null)
            mMapView.removeOverlay(mCurrentPathOverlay);
    }

    private void plotTicketRoute(final Ticket ticket) {
        plotTicketRoute(ticket, false);
    }

    private void plotTicketRoute(final Ticket ticket, boolean backHomeEnabled) {
        String markerText = ticket.getOriginShortName();
        if (Ticket.isTicketActive(ticket))
            markerText = "Be here at " + new SimpleDateFormat("h:mm a").format(ticket.getPickupTime());

        String originPlaceName = ticket.getOriginPlaceName();
        String destinationPlaceName = ticket.getDestinationPlaceName();

        // Fastest way to get just the street address without submitting a geocoding request
        if (originPlaceName != null && originPlaceName.contains(","))
            originPlaceName = originPlaceName.substring(0, originPlaceName.indexOf(","));

        if (destinationPlaceName != null && destinationPlaceName.contains(","))
            destinationPlaceName = destinationPlaceName.substring(0, destinationPlaceName.indexOf(","));

        mCurrentlyFocusedMarker = new Marker(markerText, originPlaceName,
                new LatLng(ticket.getOriginLatitude(), ticket.getOriginLongitude()));
        mCurrentlyFocusedMarker.setIcon(new Icon(ContextCompat.getDrawable(getActivity(), R.mipmap.pickup_marker)));
        showInfoForCurrentMarker(ticket, backHomeEnabled);

        Marker destinationMarker = new Marker(ticket.getDestinationShortName(), destinationPlaceName,
                new LatLng(ticket.getDestinationLatitude(), ticket.getDestinationLongitude()));
        destinationMarker.setIcon(new Icon(ContextCompat.getDrawable(getActivity(), R.mipmap.dropoff_marker)));

        plotRoute(mCurrentlyFocusedMarker, destinationMarker);
    }

    private void showInfoForCurrentMarker(Ticket ticket, boolean backHomeEnabled) {
        boolean isBackHomeMode = ticket != null &&
                CommuteManager.getInstance().isDriveHomeEnabled(ticket.getTrip());

        if (mCurrentlyFocusedMarker != null &&
                Ticket.isTicketActive(ticket) && (!isBackHomeMode || isBackHomeMode && backHomeEnabled)) {
            mCurrentlyOpenedInfoWindow = mCurrentlyFocusedMarker.getToolTip(mMapView);
            mCurrentlyFocusedMarker.showBubble(mCurrentlyOpenedInfoWindow, mMapView, true);
        }
    }

    private void plotRoute(Route savedRoute) {
        RealmLatLng origin = savedRoute.getOrigin();
        RealmLatLng destination = savedRoute.getDestination();
        if (origin != null && destination != null) {
            Marker originMarker = new Marker(getString(R.string.home), savedRoute.getOriginPlaceName(),
                    new LatLng(origin.getLatitude(), origin.getLongitude()));
            setMarkerIcon(originMarker);

            Marker destinationMarker = new Marker(getString(R.string.work), savedRoute.getDestinationPlaceName(),
                    new LatLng(destination.getLatitude(), destination.getLongitude()));
            setMarkerIcon(destinationMarker);

            plotRoute(originMarker, destinationMarker);
        }
    }

    private void plotRoute(final Marker startMarker, final Marker endMarker) {
        mMapView.clear();
        mMapView.addMarker(startMarker);
        mMapView.addMarker(endMarker);

//        zoomBoundingBox(startMarker, endMarker);
        RouteMappingManager.getInstance().loadRoute(startMarker.getPoint(), endMarker.getPoint(),
                new RouteMappingManager.RouteMappingListener() {
                    @Override
                    public void onRouteFound(RouteDirections result) {
                        if (result != null && mMapView != null) {
                            mCurrentPathOverlay = new PathOverlay(getResources().getColor(R.color.pathOverlayColor), 12);

                            RealmList<RealmLatLng> coordinates = result.getRoutePieces();
                            if (coordinates != null)
                                for (RealmLatLng coordinate : coordinates)
                                    mCurrentPathOverlay.addPoint(RealmLatLng.toLatLng(coordinate));

                            mMapView.addOverlay(mCurrentPathOverlay);
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        if (getView() != null)
                            Snackbar.make(getView(), R.string.error_fetching_route, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void zoomBoundingBox(Marker startMarker, Marker endMarker) {
        final double paddingFactor = .25;
        double latSpan = mMapView.getBoundingBox().getLatitudeSpan();
        double lonSpan = mMapView.getBoundingBox().getLongitudeSpan();
        double latPadding = latSpan * paddingFactor;
        double lonPadding = lonSpan * paddingFactor;

        LatLng startLatLng = startMarker.getPoint();
        LatLng endLatLng = endMarker.getPoint();

        double latPaddingFactor = startLatLng.getLatitude() > endLatLng.getLatitude() ? 1 : -1;
        double lonPaddingFactor = startLatLng.getLongitude() > endLatLng.getLongitude() ? 1 : -1;

        startLatLng = new LatLng(startLatLng.getLatitude() + latPadding * latPaddingFactor,
                startLatLng.getLongitude() + lonPadding * lonPaddingFactor);
        endLatLng = new LatLng(endLatLng.getLatitude() - latPadding * latPaddingFactor,
                endLatLng.getLongitude() - lonPadding * lonPaddingFactor);

        BoundingBox mapBBox = BoundingBox.fromLatLngs(Arrays.asList(startLatLng, endLatLng));
        mMapView.zoomToBoundingBox(mapBBox, true, true);
    }

    private void fetchPickupPoints() {
        CommuteManager.getInstance().getPickupPoints(new DataCallback<List<PickupPointData>>() {
            @Override
            public void success(List<PickupPointData> result) {
                if (mMapView != null)
                    plotPickupPoints(result);
            }

            @Override
            public void failure(String message) {
                if (getView() != null)
                    Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void plotPickupPoints(List<PickupPointData> data) {
        // Skip pickup points until correct icon is implemented
        /*
        if (data != null)
            for (PickupPointData point : data)
                plotPickupPoint(point);
                */
    }

    private void plotPickupPoint(PickupPointData data) {
        String pluralPerson = data.getNumRiders() == 1 ? "person" : "people";
        Marker pickupPoint = new Marker(data.getNumRiders() + " " + pluralPerson + " using this pickup point", "",
                new LatLng(data.getLocation().getLatitude(), data.getLocation().getLongitude()));
        setMarkerIcon(pickupPoint);
        mMapView.addMarker(pickupPoint);
    }

    private void setMarkerIcon(Marker marker) {
        marker.setIcon(new Icon(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_marker)));
    }

    private void centerMapOnCurrentPin(float panelHeight) {
        if (mCurrentlyFocusedMarker != null) {
            float rootHeight = getView().getHeight();
            float remainingHeight = rootHeight - panelHeight;

            ILatLng desiredCenterLoc = mMapView.getProjection().fromPixels(mMapView.getWidth() / 2, remainingHeight / 2);
            ILatLng currentCenterLoc = mMapView.getCenter();

            double dy = mCurrentlyFocusedMarker.getPosition().getLatitude() - desiredCenterLoc.getLatitude();
            double newLat = currentCenterLoc.getLatitude() + dy;
            mMapView.setCenter(new LatLng(newLat, mCurrentlyFocusedMarker.getPosition().getLongitude()));
        }
    }

    private static class PanHelper implements MapListener {
        public interface PanListener {
            void onUserPanned();
        }

        private PanListener mListener;

        public PanHelper(PanListener mListener) {
            this.mListener = mListener;
        }

        @Override
        public void onScroll(ScrollEvent event) {
            if (event.getUserAction())
                mListener.onUserPanned();
        }

        @Override
        public void onZoom(ZoomEvent event) {
            if (event.getUserAction())
                mListener.onUserPanned();
        }

        @Override
        public void onRotate(RotateEvent event) {
            if (event.getUserAction())
                mListener.onUserPanned();
        }
    }
}
