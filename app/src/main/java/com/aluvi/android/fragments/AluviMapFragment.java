package com.aluvi.android.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.helpers.EasyILatLang;
import com.aluvi.android.helpers.views.MapBoxStateSaver;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.model.local.TicketStateTransition;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;
import com.aluvi.android.services.push.AluviPushNotificationListenerService;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;
import io.realm.RealmResults;

/**
 * Created by usama on 7/13/15.
 */
public class AluviMapFragment extends BaseButterFragment {
    public interface OnMapEventListener {
        void onScheduleRideRequested();
    }

    @Bind(R.id.mapview) MapView mMapView;
    @Bind(R.id.map_text_view_commute_pending) TextView mCommutePendingTextView;

    private final String TAG = "AluviMapFragment",
            MAP_STATE_KEY = "map_fragment_main";

    private String mCurrentTicketState;
    private OnMapEventListener mEventListener;

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
        mMapView.setUserLocationEnabled(true);

        if (!MapBoxStateSaver.restoreMapState(mMapView, MAP_STATE_KEY))
            if (mMapView.getUserLocation() != null)
                mMapView.setCenter(new EasyILatLang(mMapView.getUserLocation()), false);
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
        CommuteManager.getInstance().refreshTickets(new CommuteManager.DataCallback<List<TicketStateTransition>>() {
            @Override
            public void success(List<TicketStateTransition> stateTransitions) {
                onTicketsRefreshed();
            }

            @Override
            public void failure(String message) {
                Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                refreshTickets();
                            }
                        })
                        .show();
            }
        });
    }

    public void onCommuteRequested() {
        mCommutePendingTextView.setVisibility(View.VISIBLE);
    }

    public void onCommuteCancelled() {
        mCommutePendingTextView.setVisibility(View.INVISIBLE);
    }

    private void onTicketsRefreshed() {
        onCommuteCancelled(); // Reset UI to original state

        RealmResults<Ticket> tickets = AluviRealm.getDefaultRealm().where(Ticket.class)
                .beginGroup()
                .equalTo("state", Ticket.StateRequested)
                .or()
                .equalTo("state", Ticket.StateScheduled)
                .endGroup()
                .findAllSorted("rideDate");

        if (tickets != null && tickets.size() > 0) {
            Ticket currentTicket = tickets.get(0);
            switch (currentTicket.getState()) {
                case Ticket.StateRequested:
                    onCommuteRequested();
                    break;
                case Ticket.StateScheduled:
                    if (currentTicket.isDriving())
                        enableDriverOverlay(currentTicket);
                    else
                        enableRiderOverlay(currentTicket);
                    break;
            }

            mCurrentTicketState = currentTicket.getState();
            plotTicketRoute(currentTicket);
        }

        getActivity().supportInvalidateOptionsMenu();
    }

    private void plotTicketRoute(final Ticket ticket) {
        mMapView.clear();

        String markerText = "";
        if (ticket.getState().equals(Ticket.StateScheduled)) {
            SimpleDateFormat pickupTime = new SimpleDateFormat("h:mm a");
            markerText = "Be here at " + pickupTime.format(ticket.getPickupTime());
        } else {
            markerText = getString(R.string.home);
        }

        Marker homeMarker = new Marker(markerText, ticket.getOriginPlaceName(),
                new LatLng(ticket.getOriginLatitude(), ticket.getOriginLongitude()));
        homeMarker.setIcon(new Icon(getActivity(), Icon.Size.MEDIUM, "marker-stroked", "FF0000"));

        Marker workMarker = new Marker(getString(R.string.work), ticket.getDestinationPlaceName(),
                new LatLng(ticket.getDestinationLatitude(), ticket.getDestinationLongitude()));
        workMarker.setIcon(new Icon(getActivity(), Icon.Size.MEDIUM, "marker-stroked", "FF0000"));

        mMapView.addMarker(homeMarker);
        mMapView.addMarker(workMarker);
        mMapView.setCenter(homeMarker.getPoint());
        mMapView.setZoom(15);

        CommuteManager.getInstance().loadRouteForTicket(ticket, new CommuteManager.DataCallback<RouteData>() {
            @Override
            public void success(RouteData result) {
                if (result != null && mMapView != null) {
                    PathOverlay overlay = new PathOverlay(getResources().getColor(R.color.pathOverlayColor), 6);

                    LatLng[] coordinates = result.getCoordinates();
                    if (coordinates != null)
                        for (LatLng coordinate : coordinates) {
                            overlay.addPoint(coordinate);
                        }

                    mMapView.addOverlay(overlay);
                }
            }

            @Override
            public void failure(String message) {
                Log.e(TAG, message);
                if (getView() != null) {
                    Snackbar.make(getView(), R.string.error_fetching_route, Snackbar.LENGTH_SHORT)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    plotTicketRoute(ticket);
                                }
                            }).show();
                }
            }
        });
    }

    public void enableRiderOverlay(Ticket ticket) {
        Log.e("Aluvi", "Map update: " + ticket.getFixedPrice());
        getChildFragmentManager().beginTransaction().replace(R.id.map_sliding_panel_container,
                TicketInfoFragment.newInstance(ticket)).commit();
    }

    public void enableDriverOverlay(Ticket ticket) {
        getChildFragmentManager().beginTransaction().replace(R.id.map_sliding_panel_container,
                TicketInfoFragment.newInstance(ticket)).commit();
    }

    /**
     * The name of this method is somewhat misleading. Besides deleting the most recently scheduled trip, this method also gets rid
     * of junk trip data that has persisted across scheduling failures.
     */
    private void cancelTrip() {
        RealmResults<Ticket> ticketsToCancel = AluviRealm.getDefaultRealm()
                .where(Ticket.class)
                .equalTo("state", Ticket.StateScheduled)
                .or()
                .equalTo("state", Ticket.StateRequested)
                .or()
                .equalTo("state", Ticket.StateCreated)
                .findAll();

        if (ticketsToCancel != null) {
            HashSet<Integer> deletedTrips = new HashSet<>();
            for (Ticket ticket : ticketsToCancel) {
                Trip parentTrip = ticket.getTrip();
                if (parentTrip != null && !deletedTrips.contains(parentTrip.getTripId())) {
                    deletedTrips.add(parentTrip.getTripId());

                    CommuteManager.getInstance().cancelTrip(parentTrip, new CommuteManager.Callback() {
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

                            if (getActivity() != null) {
                                Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT)
                                        .setAction(R.string.retry, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                cancelTrip();
                                            }
                                        })
                                        .show();
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mCurrentTicketState != null) {
            if (mCurrentTicketState.equals(Ticket.StateRequested) || mCurrentTicketState.equals(Ticket.StateScheduled)) {
                menu.findItem(R.id.action_cancel).setVisible(true);

                if (mCurrentTicketState.equals(Ticket.StateRequested))
                    menu.findItem(R.id.action_schedule_ride)
                            .setVisible(true)
                            .setEnabled(false)
                            .setTitle(R.string.action_commute_pending);
                else
                    menu.findItem(R.id.action_schedule_ride).setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_schedule_ride:
                mEventListener.onScheduleRideRequested();
                break;
            case R.id.action_cancel:
                cancelTrip();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
