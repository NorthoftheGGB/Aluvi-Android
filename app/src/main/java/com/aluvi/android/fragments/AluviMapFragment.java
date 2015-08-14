package com.aluvi.android.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.api.gis.models.RouteData;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.EasyILatLang;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.helpers.views.MapBoxStateSaver;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.location.RouteMappingManager;
import com.aluvi.android.managers.packages.Callback;
import com.aluvi.android.managers.packages.DataCallback;
import com.aluvi.android.model.local.TicketStateTransition;
import com.aluvi.android.model.realm.LocationWrapper;
import com.aluvi.android.model.realm.Route;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;
import com.aluvi.android.services.push.AluviPushNotificationListenerService;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * Created by usama on 7/13/15.
 */
public class AluviMapFragment extends BaseButterFragment implements TicketInfoFragment.OnTicketInfoLayoutListener {
    public interface OnMapEventListener {
        void onCommuteSchedulerRequested(Trip trip);
    }

    @Bind(R.id.sliding_layout) SlidingUpPanelLayout mSlidingLayout;
    @Bind(R.id.mapview) MapView mMapView;
    @Bind(R.id.map_text_view_commute_pending) TextView mCommutePendingTextView;
    @Bind(R.id.map_sliding_panel_container) View mSlidingPanelContainer;

    private final String TAG = "AluviMapFragment", MAP_STATE_KEY = "map_fragment_main";

    private Ticket mCurrentTicket;
    private Marker mCurrentlyFocusedMarker;
    private PathOverlay mCurrentPathOverlay;
    private OnMapEventListener mEventListener;

    private Dialog mDefaultProgressDialog;

    public AluviMapFragment() {
    }

    public static AluviMapFragment newInstance() {
        return new AluviMapFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mEventListener = (OnMapEventListener) activity;
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void initUI() {
        resetUI();
        mMapView.setUserLocationEnabled(true);

        if (!MapBoxStateSaver.restoreMapState(mMapView, MAP_STATE_KEY)) {
            if (mMapView.getUserLocation() != null)
                mMapView.setCenter(new EasyILatLang(mMapView.getUserLocation()), false);
            else // Hover over NASA if we have neither location nor saved map data
                mMapView.setCenter(new EasyILatLang(37.420654, -122.064938), false);

            mMapView.setZoom(MapBoxStateSaver.DEFAULT_ZOOM);
        }

        mSlidingLayout.setPanelSlideListener(new SimpleOnPanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
                centerMapOnCurrentPin(view.getHeight() * v);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshTickets();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MapBoxStateSaver.saveMapState(mMapView, MAP_STATE_KEY);
        EventBus.getDefault().unregister(this);

        if (mDefaultProgressDialog != null) {
            mDefaultProgressDialog.cancel();
            mDefaultProgressDialog = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean isTicketRequested = mCurrentTicket != null && mCurrentTicket.getState().equals(Ticket.StateRequested);
        boolean isTicketScheduled = isTicketScheduled(mCurrentTicket);
        boolean isTicketRequestedOrScheduled = isTicketRequested || isTicketScheduled;

        menu.findItem(R.id.action_cancel).setVisible(isTicketRequestedOrScheduled);
        if (isTicketRequestedOrScheduled) {
            MenuItem scheduleRideItem = menu.findItem(R.id.action_schedule_ride);
            scheduleRideItem.setVisible(isTicketRequested); // If the ride has been requested, show "View Commute"

            if (isTicketRequested)
                scheduleRideItem.setTitle(R.string.action_view_commute);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Trip activeTrip = CommuteManager.getInstance().getActiveTrip();
        switch (item.getItemId()) {
            case R.id.action_schedule_ride:
                mEventListener.onCommuteSchedulerRequested(activeTrip);
                break;
            case R.id.action_cancel:
                if (mCurrentTicket != null && mCurrentTicket.getState().equals(Ticket.StateScheduled))
                    cancelTicket(mCurrentTicket);
                else
                    cancelTrip(activeTrip);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    public void onEvent(AluviPushNotificationListenerService.PushNotificationEvent event) {
        refreshTickets();

        new MaterialDialog.Builder(getActivity())
                .title(R.string.update)
                .content(event.getPushData())
                .positiveText(android.R.string.ok)
                .build()
                .show();
    }

    public void refreshTickets() {
        mDefaultProgressDialog = DialogUtils.getDefaultProgressDialog(getActivity(), false);
        CommuteManager.getInstance().refreshTickets(new DataCallback<List<TicketStateTransition>>() {
            @Override
            public void success(List<TicketStateTransition> stateTransitions) {
                if (mDefaultProgressDialog != null)
                    mDefaultProgressDialog.cancel();

                handleTicketStateTransitions(stateTransitions);
                onTicketsRefreshed();
            }

            @Override
            public void failure(String message) {
                if (getView() != null)
                    Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();

                if (mDefaultProgressDialog != null)
                    mDefaultProgressDialog.cancel();
            }
        });
    }

    private void resetUI() {
        mCommutePendingTextView.setVisibility(View.INVISIBLE);
        mSlidingPanelContainer.setVisibility(View.INVISIBLE);
        mMapView.clear();

        if (mCurrentPathOverlay != null)
            mMapView.removeOverlay(mCurrentPathOverlay);

        Fragment ticketInfoFragment = getChildFragmentManager().findFragmentById(R.id.map_sliding_panel_container);
        if (ticketInfoFragment != null)
            getChildFragmentManager().beginTransaction().remove(ticketInfoFragment).commit();
    }

    private void onTicketsRefreshed() {
        resetUI(); // Reset UI to original state
        mCurrentTicket = CommuteManager.getInstance().getActiveTicket(); // Reset cached ticket; use most recent data

        if (mCurrentTicket != null) {
            plotTicketRoute(mCurrentTicket);
            switch (mCurrentTicket.getState()) {
                case Ticket.StateRequested:
                    mCommutePendingTextView.setVisibility(View.VISIBLE);
                    break;
                case Ticket.StateInProgress:
                case Ticket.StateStarted:
                case Ticket.StateScheduled:
                    if (mCurrentTicket.isDriving())
                        enableDriverOverlay(mCurrentTicket);
                    else
                        enableRiderOverlay(mCurrentTicket);
                    break;
            }
        } else {
            plotRoute(CommuteManager.getInstance().getRoute());
        }

        getActivity().supportInvalidateOptionsMenu();
    }

    private void plotTicketRoute(final Ticket ticket) {
        String markerText = "";
        if (ticket.getState().equals(Ticket.StateScheduled)) {
            SimpleDateFormat pickupTime = new SimpleDateFormat("h:mm a");
            markerText = "Be here at " + pickupTime.format(ticket.getPickupTime());
        } else {
            markerText = getString(R.string.home);
        }

        Marker homeMarker = new Marker(markerText, ticket.getOriginPlaceName(),
                new LatLng(ticket.getOriginLatitude(), ticket.getOriginLongitude()));
        setMarkerIcon(homeMarker);

        Marker workMarker = new Marker(getString(R.string.work), ticket.getDestinationPlaceName(),
                new LatLng(ticket.getDestinationLatitude(), ticket.getDestinationLongitude()));
        setMarkerIcon(workMarker);

        plotRoute(homeMarker, workMarker);
        mCurrentlyFocusedMarker = homeMarker;
    }

    private void plotRoute(Route savedRoute) {
        LocationWrapper origin = savedRoute.getOrigin();
        LocationWrapper destination = savedRoute.getDestination();
        if (origin != null && destination != null) {
            Marker homeMarker = new Marker(getString(R.string.home), savedRoute.getOriginPlaceName(),
                    new LatLng(origin.getLatitude(), origin.getLongitude()));
            setMarkerIcon(homeMarker);

            Marker workMarker = new Marker(getString(R.string.work), savedRoute.getDestinationPlaceName(),
                    new LatLng(destination.getLatitude(), destination.getLongitude()));
            setMarkerIcon(workMarker);

            plotRoute(homeMarker, workMarker);
        }
    }

    private void plotRoute(Marker startMarker, Marker endMarker) {
        mMapView.clear();
        mMapView.addMarker(startMarker);
        mMapView.addMarker(endMarker);
        mMapView.setCenter(startMarker.getPoint());
        mMapView.setZoom(15);
        RouteMappingManager.getInstance().loadRoute(startMarker.getPoint(), endMarker.getPoint(),
                new RouteMappingManager.RouteMappingListener() {
                    @Override
                    public void onRouteFound(RouteData result) {
                        if (result != null && mMapView != null) {
                            mCurrentPathOverlay = new PathOverlay(getResources().getColor(R.color.pathOverlayColor), 6);
                            LatLng[] coordinates = result.getCoordinates();
                            if (coordinates != null)
                                for (LatLng coordinate : coordinates)
                                    mCurrentPathOverlay.addPoint(coordinate);
                            mMapView.addOverlay(mCurrentPathOverlay);
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        Log.e(TAG, message);
                        if (getView() != null)
                            Snackbar.make(getView(), R.string.error_fetching_route, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void setMarkerIcon(Marker marker) {
        marker.setIcon(new Icon(getActivity(), Icon.Size.MEDIUM, "marker-stroked", "FF0000"));
    }

    private void enableRiderOverlay(Ticket ticket) {
        mSlidingPanelContainer.setVisibility(View.VISIBLE);
        getChildFragmentManager().beginTransaction().replace(R.id.map_sliding_panel_container,
                TicketInfoFragment.newInstance(ticket)).commit();
    }

    private void enableDriverOverlay(Ticket ticket) {
        mSlidingPanelContainer.setVisibility(View.VISIBLE);
        getChildFragmentManager().beginTransaction().replace(R.id.map_sliding_panel_container,
                TicketInfoFragment.newInstance(ticket)).commit();
    }

    private void handleTicketStateTransitions(List<TicketStateTransition> transitions) {
        Collections.sort(transitions, new Comparator<TicketStateTransition>() {
            @Override
            public int compare(TicketStateTransition lhs, TicketStateTransition rhs) {
                String lStatus = lhs.getNewState();
                String rStaus = rhs.getNewState();

                if (lStatus != null && lStatus.equals(Ticket.StateCreated)) {
                    return -1;
                } else if (rStaus != null && rStaus.equals(Ticket.StateCreated)) {

                }
                return 0;
            }
        });
    }

    @Override
    public void onTicketInfoUIMeasured(int headerHeight, int panelHeight) {
        float rootHeight = getView().getHeight();
        float anchor = panelHeight / rootHeight;

        mSlidingLayout.setAnchorPoint(anchor);
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

        centerMapOnCurrentPin(panelHeight);
    }

    private void centerMapOnCurrentPin(float panelHeight) {
        if (mCurrentlyFocusedMarker != null) {
            float rootHeight = getView().getHeight();
            float remainingHeight = rootHeight - panelHeight;

            ILatLng desiredCenterLoc = mMapView.getProjection().fromPixels(mMapView.getWidth() / 2, remainingHeight / 2);
            ILatLng currentCenterLoc = mMapView.getCenter();

            double dy = mCurrentlyFocusedMarker.getPosition().getLatitude() - desiredCenterLoc.getLatitude();
            double newLat = currentCenterLoc.getLatitude() + dy;
            mMapView.setCenter(new LatLng(newLat, currentCenterLoc.getLongitude()));
        }
    }

    private void cancelTicket(Ticket ticket) {
        CommuteManager.getInstance().cancelTicket(ticket, cancelCallback);
    }

    private void cancelTrip(Trip trip) {
        CommuteManager.getInstance().cancelTrip(trip, cancelCallback);
    }

    private Callback cancelCallback = new Callback() {
        @Override
        public void success() {
            Log.d(TAG, "Successfully cancelled trips");

            if (getActivity() != null) {
                Snackbar.make(getView(), R.string.cancelled_trips, Snackbar.LENGTH_SHORT).show();
                refreshTickets();
            }
        }

        @Override
        public void failure(String message) {
            Log.e(TAG, message);

            if (getActivity() != null)
                Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    };

    private boolean isTicketScheduled(Ticket ticket) {
        if (ticket != null) {
            String state = ticket.getState();
            return state.equals(Ticket.StateScheduled) ||
                    state.equals(Ticket.StateStarted) ||
                    state.equals(Ticket.StateInProgress);
        }

        return false;
    }

    private abstract class SimpleOnPanelSlideListener implements SlidingUpPanelLayout.PanelSlideListener {
        @Override
        public void onPanelCollapsed(View view) {

        }

        @Override
        public void onPanelExpanded(View view) {

        }

        @Override
        public void onPanelAnchored(View view) {

        }

        @Override
        public void onPanelHidden(View view) {

        }
    }
}
