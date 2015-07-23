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

import com.aluvi.android.R;
import com.aluvi.android.api.gis.RouteData;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.helpers.EasyILatLang;
import com.aluvi.android.helpers.views.MapBoxStateSaver;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;

import org.joda.time.LocalDate;

import java.util.HashSet;

import butterknife.Bind;
import io.realm.RealmResults;

public class AluviMapFragment extends BaseButterFragment {
    public interface OnMapEventListener {
        void onScheduleRideRequested();
    }

    @Bind(R.id.mapview) MapView mMapView;
    @Bind(R.id.map_text_view_commute_pending) TextView mCommutePendingTextView;

    private final String TAG = "AluviMapFragment",
            MAP_STATE_KEY = "map_fragment_main";

    private boolean mIsCommutePending;
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
    }

    public void refreshTickets() {
        CommuteManager.getInstance().refreshTickets(new CommuteManager.Callback() {
            @Override
            public void success() {
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
        mIsCommutePending = true;
        mCommutePendingTextView.setVisibility(View.VISIBLE);
        getActivity().supportInvalidateOptionsMenu();
    }

    public void onCommuteCancelled() {
        mIsCommutePending = false;
        mCommutePendingTextView.setVisibility(View.INVISIBLE);
        getActivity().supportInvalidateOptionsMenu();
    }

    private void onTicketsRefreshed() {
        Log.i(TAG, "Realm path: " + AluviRealm.getDefaultRealm().getPath());

        RealmResults<Trip> savedTrips = AluviRealm.getDefaultRealm()
                .where(Trip.class)
                .findAll();

        if (savedTrips != null) {
            for (Trip trip : savedTrips) {
                RealmResults<Ticket> tickets = trip.getTickets()
                        .where().greaterThan("rideDate", new LocalDate().toDate())
                        .findAll(); // Show tickets for today and beyond

                if (tickets != null) {
                    for (Ticket ticket : tickets) {
                        Log.i(TAG, "Ticket state: " + ticket.getState());
                        switch (ticket.getState()) {
                            case Ticket.StateRequested:
                                onCommuteRequested();
                                break;
                            case Ticket.StateScheduled:
                                break;
                        }

                        plotTicketRoute(ticket);
                    }
                }
            }
        }
    }

    private void plotTicketRoute(final Ticket ticket) {
        CommuteManager.getInstance().loadRouteForTicket(ticket, new CommuteManager.DataCallback<RouteData>() {
            @Override
            public void success(RouteData result) {
                if (result != null && mMapView != null) {
                    PathOverlay overlay = new PathOverlay();

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mIsCommutePending)
            menu.findItem(R.id.action_schedule_ride)
                    .setEnabled(false)
                    .setTitle(R.string.action_commute_pending);

        menu.findItem(R.id.action_cancel_pending_ride)
                .setVisible(mIsCommutePending);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_schedule_ride:
                mEventListener.onScheduleRideRequested();
                break;
            case R.id.action_cancel_pending_ride:
                cancelTrip();
                break;
        }

        return super.onOptionsItemSelected(item);
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
                                onCommuteCancelled();
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
    public void onPause() {
        super.onPause();
        MapBoxStateSaver.saveMapState(mMapView, MAP_STATE_KEY);
    }
}
