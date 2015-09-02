package com.aluvi.android.fragments.gis;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.aluvi.android.fragments.TicketInfoFragment;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.helpers.views.mapbox.MapBoxStateSaver;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.callbacks.Callback;
import com.aluvi.android.managers.callbacks.DataCallback;
import com.aluvi.android.managers.location.RouteMappingManager;
import com.aluvi.android.model.local.TicketStateTransition;
import com.aluvi.android.model.realm.RealmLatLng;
import com.aluvi.android.model.realm.Route;
import com.aluvi.android.model.realm.RouteDirections;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;
import com.aluvi.android.services.push.AluviPushNotificationListenerService;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by usama on 7/13/15.
 */
public class CommuteMapFragment extends BaseButterFragment implements TicketInfoFragment.TicketInfoListener {
    public interface OnMapEventListener {
        void onCommuteSchedulerRequested(Trip trip);

        void startLocationTracking(Ticket ticket);
    }

    @Bind(R.id.sliding_layout) SlidingUpPanelLayout mSlidingLayout;
    @Bind(R.id.mapview) MapView mMapView;
    @Bind(R.id.map_text_view_commute_pending) TextView mCommutePendingTextView;
    @Bind(R.id.map_sliding_panel_container) View mSlidingPanelContainer;

    private final String TAG = "AluviMapFragment", MAP_STATE_KEY = "map_fragment_main";

    private Dialog mDefaultProgressDialog;

    private Ticket mCurrentTicket;
    private OnMapEventListener mEventListener;

    private Marker mCurrentlyFocusedMarker;
    private InfoWindow mCurrentlyOpenedInfoWindow;
    private PathOverlay mCurrentPathOverlay;

    public static CommuteMapFragment newInstance() {
        return new CommuteMapFragment();
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
                mMapView.setCenter(new LatLng(mMapView.getUserLocation()), false);
            else // Hover over NASA if we have neither location nor saved map data
                mMapView.setCenter(new LatLng(37.420654, -122.064938), false);

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
        boolean isTicketRequested = mCurrentTicket != null && mCurrentTicket.getState().equals(Ticket.STATE_REQUESTED);
        boolean isTicketScheduled = mCurrentTicket != null ? Ticket.isTicketActive(mCurrentTicket) : false;
        boolean isTicketRequestedOrScheduled = isTicketRequested || isTicketScheduled;

        menu.findItem(R.id.action_cancel).setVisible(isTicketRequestedOrScheduled);
        if (isTicketRequestedOrScheduled) {
            MenuItem scheduleRideItem = menu.findItem(R.id.action_schedule_ride);
            scheduleRideItem.setVisible(isTicketRequested); // If the ride has been requested, show "View Commute"

            if (isTicketRequested)
                scheduleRideItem.setTitle(R.string.action_view_commute);
        }

        if (mCurrentTicket != null) {
            menu.findItem(R.id.action_back_home).setVisible(isDriveHomeEnabled(mCurrentTicket.getTrip()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Trip activeTrip = CommuteManager.getInstance().getActiveTrip();
        switch (item.getItemId()) {
            case R.id.action_schedule_ride:
                mEventListener.onCommuteSchedulerRequested(activeTrip);
                break;
            case R.id.action_back_home:
                onTicketScheduled(mCurrentTicket, true);
                break;
            case R.id.action_cancel:
                if (activeTrip.getTripState().equals(Trip.STATE_REQUESTED))
                    cancelTrip(activeTrip);
                else if (mCurrentTicket != null)
                    cancelTicket(mCurrentTicket);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    public void onEvent(AluviPushNotificationListenerService.PushNotificationEvent event) {
        refreshTickets();
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

    private void onTicketsRefreshed() {
        if (getActivity() != null) {
            resetUI(); // Reset UI to original state
            mCurrentTicket = CommuteManager.getInstance().getActiveTicket(); // Reset cached ticket; use most recent data

            if (mCurrentTicket != null) {
                plotTicketRoute(mCurrentTicket);
                switch (mCurrentTicket.getState()) {
                    case Ticket.STATE_REQUESTED:
                        mCommutePendingTextView.setVisibility(View.VISIBLE);
                        break;
                    case Ticket.STATE_IN_PROGRESS:
                    case Ticket.STATE_STARTED:
                    case Ticket.STATE_SCHEDULED:
                        onTicketScheduled(mCurrentTicket, false);
                        break;
                }
            } else {
                plotRoute(CommuteManager.getInstance().getRoute());
            }

            getActivity().supportInvalidateOptionsMenu();
        }
    }

    private void onTicketScheduled(Ticket ticket, boolean overrideBackHome) {
        if (!isDriveHomeEnabled(ticket.getTrip()) || overrideBackHome)
            if (ticket.isDriving())
                enableDriverOverlay(ticket);
            else
                enableRiderOverlay(ticket);

        mEventListener.startLocationTracking(ticket);
    }

    private void resetUI() {
        mCommutePendingTextView.setVisibility(View.INVISIBLE);
        mSlidingPanelContainer.setVisibility(View.INVISIBLE);
        mMapView.clear();

        if (mCurrentlyOpenedInfoWindow != null)
            mCurrentlyOpenedInfoWindow.close();

        if (mCurrentPathOverlay != null)
            mMapView.removeOverlay(mCurrentPathOverlay);

        Fragment ticketInfoFragment = getChildFragmentManager().findFragmentById(R.id.map_sliding_panel_container);
        if (ticketInfoFragment != null)
            getChildFragmentManager().beginTransaction().remove(ticketInfoFragment).commit();
    }

    private void plotTicketRoute(final Ticket ticket) {
        String markerText = "Be here at " + new SimpleDateFormat("h:mm a").format(ticket.getPickupTime());

        Marker originMarker = new Marker(markerText, ticket.getOriginPlaceName(),
                new LatLng(ticket.getOriginLatitude(), ticket.getOriginLongitude()));
        setMarkerIcon(originMarker);
        mCurrentlyOpenedInfoWindow = originMarker.getToolTip(mMapView);
        originMarker.showBubble(mCurrentlyOpenedInfoWindow, mMapView, true);

        Marker destinationMarker = new Marker(ticket.getDestinationShortName(), ticket.getDestinationPlaceName(),
                new LatLng(ticket.getDestinationLatitude(), ticket.getDestinationLongitude()));
        setMarkerIcon(destinationMarker);

        plotRoute(originMarker, destinationMarker);
        mCurrentlyFocusedMarker = originMarker;
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
        mMapView.setCenter(startMarker.getPoint());
        mMapView.setZoom(19);

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

    private void setMarkerIcon(Marker marker) {
        marker.setIcon(new Icon(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_marker)));
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
        if (transitions != null) {
            Dialog currTransitionDialog = null;
            HashMap<String, String> shownTransitions = new HashMap<>();
            for (TicketStateTransition transition : transitions) {
                if (shownTransitions.get(transition.getOldState()) == null) {
                    currTransitionDialog = showTransitionDialog(transition, currTransitionDialog);
                    shownTransitions.put(transition.getOldState(), transition.getNewState());
                }
            }

            if (currTransitionDialog != null)
                currTransitionDialog.show();
        }
    }

    @Override
    public void onRiderStateChanged() {
        onTicketsRefreshed();
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
                onTicketsRefreshed();
            }
        }

        @Override
        public void failure(String message) {
            Log.e(TAG, message);

            if (getActivity() != null)
                Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    };

    private boolean isDriveHomeEnabled(Trip trip) {
        RealmResults<Ticket> tickets = trip.getTickets()
                .where().findAllSorted("pickupTime");
        if (tickets.size() == 2) {
            Ticket aSide = tickets.get(0);
            Ticket bSide = tickets.get(1);
            return !Ticket.isTicketActive(aSide) && bSide.getState().equals(Ticket.STATE_SCHEDULED);
        }

        return false;
    }

    private MaterialDialog showTransitionDialog(TicketStateTransition transition, final Dialog nextDialog) {
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.ticket_updated)
                .content(getMessageForTransition(transition))
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.no)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onAny(MaterialDialog dialog) {
                        super.onAny(dialog);
                        if (nextDialog != null)
                            nextDialog.show();
                    }
                })
                .build();
    }

    private String getMessageForTransition(TicketStateTransition transition) {
        int res = getMessageResouceForTransition(transition);
        return res != -1 ? getString(res) : null;
    }

    private int getMessageResouceForTransition(TicketStateTransition transition) {
        String oldState = transition.getOldState();
        String newState = transition.getNewState();

        if (newState == null || oldState == null) {
            return getIllDefinedTransitionMessage(newState);
        } else if (oldState.equals(Ticket.STATE_REQUESTED)) {
            if (newState.equals(Ticket.STATE_COMMUTE_SCHEDULER_FAILED)) {
                return R.string.unable_schedule_commute;
            } else if (Ticket.isTicketActive(newState)) {
                return R.string.trip_fulfilled;
            }
        } else if (Ticket.isTicketCancelled(newState)) {
            return R.string.ticket_cancelled;
        } else {
            return getIllDefinedTransitionMessage(newState);
        }

        return -1;
    }

    private int getIllDefinedTransitionMessage(String newState) {
        switch (newState) {
            case Ticket.STATE_REQUESTED:
                return R.string.trip_requested;
            case Ticket.STATE_SCHEDULED:
            case Ticket.STATE_IN_PROGRESS:
            case Ticket.STATE_STARTED:
                return R.string.trip_fulfilled;
            case Ticket.STATE_ABORTED:
            case Ticket.STATE_CANCELLED:
            case Ticket.STATE_RIDER_CANCELLED:
            case Ticket.STATE_DRIVER_CANCELLED:
                return R.string.ticket_cancelled;
        }

        return -1;
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
