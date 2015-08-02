package com.aluvi.android.fragments;


import android.app.Activity;
import android.app.Dialog;
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
import com.aluvi.android.helpers.EasyILatLang;
import com.aluvi.android.helpers.views.DialogUtils;
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

    private final String TAG = "AluviMapFragment",
            MAP_STATE_KEY = "map_fragment_main";

    private Ticket mCurrentTicket;
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
        resetUI();
        mMapView.setUserLocationEnabled(true);

        if (!MapBoxStateSaver.restoreMapState(mMapView, MAP_STATE_KEY)) {
            if (mMapView.getUserLocation() != null)
                mMapView.setCenter(new EasyILatLang(mMapView.getUserLocation()), false);
            else // Hover over NASA if we have neither location nor saved map data
                mMapView.setCenter(new EasyILatLang(37.420654, -122.064938), false);

            mMapView.setZoom(MapBoxStateSaver.DEFAULT_ZOOM);
        }
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
        final Dialog refreshProgressDialog = DialogUtils.getDefaultProgressDialog(getActivity(), false);
        CommuteManager.getInstance().refreshTickets(new CommuteManager.DataCallback<List<TicketStateTransition>>() {
            @Override
            public void success(List<TicketStateTransition> stateTransitions) {
                if (refreshProgressDialog != null)
                    refreshProgressDialog.cancel();

                handleTicketStateTransitions(stateTransitions);
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

                if (refreshProgressDialog != null)
                    refreshProgressDialog.cancel();
            }
        });
    }

    public void onCommuteRequested() {
        mCommutePendingTextView.setVisibility(View.VISIBLE);
    }

    public void resetUI() {
        mCommutePendingTextView.setVisibility(View.INVISIBLE);
        mSlidingPanelContainer.setVisibility(View.INVISIBLE);
        mMapView.clear();
    }

    private void onTicketsRefreshed() {
        resetUI(); // Reset UI to original state
        mCurrentTicket = CommuteManager.getInstance().getActiveTicket(); // Reset cached ticket; use most recent data
        if (mCurrentTicket != null) {
            switch (mCurrentTicket.getState()) {
                case Ticket.StateRequested:
                    onCommuteRequested();
                    break;
                case Ticket.StateScheduled:
                    if (mCurrentTicket.isDriving())
                        enableDriverOverlay(mCurrentTicket);
                    else
                        enableRiderOverlay(mCurrentTicket);
                    break;
            }

            plotTicketRoute(mCurrentTicket);
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
        mSlidingPanelContainer.setVisibility(View.VISIBLE);
        getChildFragmentManager().beginTransaction().replace(R.id.map_sliding_panel_container,
                TicketInfoFragment.newInstance(ticket)).commit();
    }

    public void enableDriverOverlay(Ticket ticket) {
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
                } else if(rStaus != null && rStaus.equals(Ticket.StateCreated)){

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
    }

    private void cancelTrip(Trip trip) {
        CommuteManager.getInstance().cancelTrip(trip, new CommuteManager.Callback() {
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
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mCurrentTicket != null && (mCurrentTicket.getState().equals(Ticket.StateRequested)
                || mCurrentTicket.getState().equals(Ticket.StateScheduled))) {
            menu.findItem(R.id.action_cancel).setVisible(true);

            if (mCurrentTicket.getState().equals(Ticket.StateRequested))
                menu.findItem(R.id.action_schedule_ride)
                        .setVisible(true)
                        .setTitle(R.string.action_view_commute);
            else
                menu.findItem(R.id.action_schedule_ride).setVisible(false);
        } else
            menu.findItem(R.id.action_cancel).setVisible(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_schedule_ride:
                Trip currentlySelectedTrip = mCurrentTicket != null ? mCurrentTicket.getTrip() : null;
                mEventListener.onCommuteSchedulerRequested(currentlySelectedTrip);
                break;
            case R.id.action_cancel:
                cancelTrip(mCurrentTicket.getTrip());
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
