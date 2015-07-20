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
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.helpers.EasyILatLang;
import com.aluvi.android.helpers.eventBus.CommuteScheduledEvent;
import com.aluvi.android.helpers.views.MapBoxStateSaver;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;

import butterknife.Bind;
import de.greenrobot.event.EventBus;
import io.realm.RealmList;

public class AluviMapFragment extends BaseButterFragment {
    public interface OnMapEventListener {
        void onScheduleRideRequested();
    }

    @Bind(R.id.mapview) MapView mMapView;
    @Bind(R.id.map_text_view_commute_pending) TextView mCommutePendingTextView;

    private final String TAG = "AluviMapFragment",
            MAP_STATE_KEY = "map_fragment_main";

    private EventBus mBus = EventBus.getDefault();
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

        plotTrip();
        mBus.register(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(CommuteScheduledEvent event) {
        mIsCommutePending = true;
        mCommutePendingTextView.setVisibility(View.VISIBLE);
        getActivity().supportInvalidateOptionsMenu();
    }

    private void plotTrip() {
        Log.i(TAG, "Realm path: " + AluviRealm.getDefaultRealm().getPath());

        Trip savedTrip = AluviRealm.getDefaultRealm().where(Trip.class).findFirst();
        if (savedTrip != null) {
            RealmList<Ticket> tickets = savedTrip.getTickets();
            if (tickets != null) {
                for (Ticket ticket : tickets) {
                    Log.i(TAG, "Ticket state: " + ticket.getState());
                    switch (ticket.getState()) {
                        case Ticket.StateRequested:
                            onEvent(new CommuteScheduledEvent());
                            break;
                        case Ticket.StateScheduled:
                            plotTicketRoute(ticket);
                            break;

                    }
                }
            }
        }
    }

    private void plotTicketRoute(Ticket ticket) {
        Marker pickupMarker = new Marker("Pickup", "", new LatLng(ticket.getOriginLatitude(), ticket.getOriginLongitude()));
        Marker destinationMarker = new Marker("Destination", "", new LatLng(ticket.getDestinationLatitude(),
                ticket.getDestinationLongitude()));

        PathOverlay overlay = new PathOverlay();
        overlay.addPoint(ticket.getOriginLatitude(), ticket.getOriginLongitude());
        overlay.addPoint(ticket.getDestinationLatitude(), ticket.getDestinationLongitude());

        mMapView.addMarker(pickupMarker);
        mMapView.addMarker(destinationMarker);
        mMapView.addOverlay(overlay);
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

    private void cancelTrip() {
        Trip savedTrip = AluviRealm.getDefaultRealm().where(Trip.class).findFirst();
        if (savedTrip != null) {
            CommuteManager.getInstance().cancelTrip(savedTrip, new CommuteManager.Callback() {
                @Override
                public void success() {
                    if (getActivity() != null)
                        getActivity().supportInvalidateOptionsMenu();
                }

                @Override
                public void failure(String message) {
                    Log.e(TAG, message);
                    if (getView() != null) {
                        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
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

    @Override
    public void onPause() {
        super.onPause();
        MapBoxStateSaver.saveMapState(mMapView, MAP_STATE_KEY);
    }

    @Override
    public void onDestroyView() {
        mBus.unregister(this);
        super.onDestroyView();
    }
}
