package com.aluvi.android.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.aluvi.android.R;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.eventBus.CommuteScheduledEvent;
import com.aluvi.android.helpers.eventBus.LocalRefreshTicketsEvent;
import com.aluvi.android.helpers.eventBus.RefreshTicketsEvent;
import com.aluvi.android.helpers.eventBus.SlidingPanelEvent;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.callbacks.Callback;
import com.aluvi.android.managers.callbacks.DataCallback;
import com.aluvi.android.model.local.TicketStateTransition;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;
import com.aluvi.android.services.push.AluviPushNotificationListenerService;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;
import io.realm.RealmResults;

/**
 * Created by usama on 9/3/15.
 */
public class CommuteFragment extends BaseButterFragment implements TicketInfoFragment.TicketInfoListener {
    public interface OnMapEventListener {
        void onCommuteSchedulerRequested(Trip trip);

        void startLocationTracking(Ticket ticket);
    }

    @Bind(R.id.sliding_layout) SlidingUpPanelLayout mSlidingLayout;
    @Bind(R.id.commute_fragment_ticket_info_container) View mTicketInfoContainer;

    private Ticket mCurrentTicket;
    private Dialog mDefaultProgressDialog;
    private OnMapEventListener mEventListener;

    public static CommuteFragment newInstance() {
        return new CommuteFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mEventListener = (OnMapEventListener) activity;
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_commute, container, false);
    }

    @Override
    public void initUI() {
        mSlidingLayout.setPanelSlideListener(new SimpleOnPanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
                EventBus.getDefault().post(new SlidingPanelEvent(view.getHeight() * v));
            }
        });

        mTicketInfoContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshTickets();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mDefaultProgressDialog != null) {
            mDefaultProgressDialog.cancel();
            mDefaultProgressDialog = null;
        }
    }

    @Override
    public void onTicketInfoUIMeasured(int headerHeight, int panelHeight) {
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        EventBus.getDefault().post(new SlidingPanelEvent(panelHeight));
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

    public void refreshTickets() {
        mDefaultProgressDialog = DialogUtils.getDefaultProgressDialog(getActivity(), false);
        CommuteManager.getInstance().refreshTickets(new DataCallback<List<TicketStateTransition>>() {
            @Override
            public void success(List<TicketStateTransition> stateTransitions) {
                if (getView() != null)
                    onTicketsRefreshed(stateTransitions);

                if (mDefaultProgressDialog != null)
                    mDefaultProgressDialog.cancel();
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

    private void onTicketsRefreshed(List<TicketStateTransition> transitions) {
        mCurrentTicket = CommuteManager.getInstance().getActiveTicket();

        if (mCurrentTicket != null) {
            RealmResults<Ticket> tickets = AluviRealm.getDefaultRealm().where(Ticket.class).findAll();
            EventBus.getDefault().post(new RefreshTicketsEvent(tickets, transitions, mCurrentTicket));

            if (Ticket.isTicketActive(mCurrentTicket))
                onTicketScheduled(mCurrentTicket, false);
        }
    }

    private void onTicketScheduled(Ticket ticket, boolean overrideBackHome) {
        mEventListener.startLocationTracking(ticket);
        EventBus.getDefault().post(new CommuteScheduledEvent(mCurrentTicket.getTrip(), mCurrentTicket));

        if (!isDriveHomeEnabled(ticket.getTrip()) || overrideBackHome) {
            mTicketInfoContainer.setVisibility(View.VISIBLE);
        }
    }

    private void onLocalTicketsRefreshed() {
        EventBus.getDefault().post(new LocalRefreshTicketsEvent(CommuteManager.getInstance().getActiveTicket()));
    }

    @Override
    public void onRiderStateChanged() {
        EventBus.getDefault().post(new LocalRefreshTicketsEvent(CommuteManager.getInstance().getActiveTicket()));
    }

    @SuppressWarnings("unused")
    public void onEvent(AluviPushNotificationListenerService.PushNotificationEvent event) {
        refreshTickets();
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
            if (getActivity() != null) {
                Snackbar.make(getView(), R.string.cancelled_trips, Snackbar.LENGTH_SHORT).show();
                onLocalTicketsRefreshed();
            }
        }

        @Override
        public void failure(String message) {
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
