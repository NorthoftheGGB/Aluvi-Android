package com.aluvi.android.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.aluvi.android.R;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.fragments.gis.CommuteMapFragment;
import com.aluvi.android.helpers.eventBus.BackHomeEvent;
import com.aluvi.android.helpers.eventBus.CommuteScheduledEvent;
import com.aluvi.android.helpers.eventBus.RefreshTicketsEvent;
import com.aluvi.android.helpers.eventBus.SlidingPanelEvent;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.callbacks.Callback;
import com.aluvi.android.managers.callbacks.DataCallback;
import com.aluvi.android.model.local.TicketStateTransition;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.services.push.AluviPushNotificationListenerService;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;
import io.realm.RealmResults;

/**
 * Created by usama on 9/3/15.
 */
public class CommuteFragment extends BaseButterFragment implements TicketInfoFragment.TicketInfoListener, CommuteMapFragment.CommuteMapListener {
    public interface OnMapEventListener {
        void onCommuteSchedulerRequested();

        void startLocationTracking(Ticket ticket);
    }

    @Bind(R.id.sliding_layout) SlidingUpPanelLayout mSlidingLayout;

    private Ticket mCurrentTicket;
    private OnMapEventListener mEventListener;
    private SlidingUpPanelLayout.PanelState mCurrentPanelState;

    public static CommuteFragment newInstance() {
        return new CommuteFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mEventListener = (OnMapEventListener) context;
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

        // Can't add nested fragments via XML, so add them dynamically
        if (getChildFragmentManager().findFragmentById(R.id.commute_fragment_map_container) == null)
            getChildFragmentManager().beginTransaction().replace(R.id.commute_fragment_map_container,
                    CommuteMapFragment.newInstance()).commit();

        if (getChildFragmentManager().findFragmentById(R.id.commute_fragment_ticket_info_container) == null)
            getChildFragmentManager().beginTransaction().replace(R.id.commute_fragment_ticket_info,
                    TicketInfoFragment.newInstance()).commit();
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
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onTicketInfoUIMeasured(int headerHeight, int panelHeight) {
        mSlidingLayout.setPanelState(mCurrentPanelState);
        EventBus.getDefault().post(new SlidingPanelEvent(panelHeight));
    }

    @Override
    public void onMapPanned() {
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_commute_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean isTicketRequested = mCurrentTicket != null && mCurrentTicket.getState().equals(Ticket.STATE_REQUESTED);
        boolean isTicketActive = Ticket.isTicketActive(mCurrentTicket);
        boolean isBackHomeEnabled = mCurrentTicket != null &&
                CommuteManager.getInstance().isDriveHomeEnabled(mCurrentTicket.getTrip());

        menu.findItem(R.id.action_view_commute)
                .setVisible(isTicketRequested);

        menu.findItem(R.id.action_schedule_ride)
                .setVisible(!isTicketRequested && !isTicketActive);

        menu.findItem(R.id.action_cancel)
                .setVisible(isTicketActive && !isBackHomeEnabled);

        menu.findItem(R.id.action_back_home)
                .setVisible(isBackHomeEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_commute:
            case R.id.action_schedule_ride:
                mEventListener.onCommuteSchedulerRequested();
                break;
            case R.id.action_back_home:
                EventBus.getDefault().post(new BackHomeEvent(mCurrentTicket));
                onTicketScheduled(mCurrentTicket, true);
                break;
            case R.id.action_cancel:
                cancelTicket(mCurrentTicket);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshTickets() {
        showDefaultProgressDialog();
        CommuteManager.getInstance().refreshTickets(new DataCallback<List<TicketStateTransition>>() {
            @Override
            public void success(List<TicketStateTransition> stateTransitions) {
                if (getView() != null)
                    onTicketsRefreshed(stateTransitions);
                cancelProgressDialogs();
            }

            @Override
            public void failure(String message) {
                if (getView() != null)
                    Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
                cancelProgressDialogs();
            }
        });
    }

    private void onTicketsRefreshed(List<TicketStateTransition> transitions) {
        mCurrentTicket = CommuteManager.getInstance().getActiveTicket();
        handleTicketStateTransitions(transitions);

        if (Ticket.isTicketActive(mCurrentTicket)) {
            onTicketScheduled(mCurrentTicket, false);
        } else {
            mCurrentPanelState = SlidingUpPanelLayout.PanelState.HIDDEN;
            onTicketInfoUIMeasured(-1, -1);
        }

        RealmResults<Ticket> tickets = AluviRealm.getDefaultRealm().where(Ticket.class).findAll();
        EventBus.getDefault().post(new RefreshTicketsEvent(tickets, transitions, mCurrentTicket));
    }

    private void onTicketsRefreshed() {
        onTicketsRefreshed(new ArrayList<TicketStateTransition>());
    }

    private void handleTicketStateTransitions(List<TicketStateTransition> transitions) {
        if (transitions != null) {
            Dialog currTransitionDialog = null;
            HashMap<String, String> shownTransitions = new HashMap<>();
            for (TicketStateTransition transition : transitions) {
                if (shownTransitions.get(transition.getOldState()) == null) {
                    currTransitionDialog = buildTransitionDialog(transition, currTransitionDialog);
                    shownTransitions.put(transition.getOldState(), transition.getNewState());
                }
            }

            if (currTransitionDialog != null) {
                addDialog(currTransitionDialog);
                currTransitionDialog.show();
            }
        }
    }

    private void onTicketScheduled(Ticket ticket, boolean overrideBackHome) {
        boolean isTicketInfoVisible = !CommuteManager.getInstance().isDriveHomeEnabled(ticket.getTrip()) || overrideBackHome;
        mCurrentPanelState = isTicketInfoVisible ? SlidingUpPanelLayout.PanelState.EXPANDED : SlidingUpPanelLayout.PanelState.HIDDEN;

        mEventListener.startLocationTracking(ticket);
        EventBus.getDefault().post(new CommuteScheduledEvent(mCurrentTicket.getTrip(), mCurrentTicket));
    }

    private void cancelTicket(final Ticket ticket) {
        final Callback onCancelFinishedCallback = new Callback() {
            @Override
            public void success() {
                if (getView() != null) {
                    Snackbar.make(getView(), R.string.cancelled_trips, Snackbar.LENGTH_SHORT).show();
                    onTicketsRefreshed();
                }
            }

            @Override
            public void failure(String message) {
                if (getView() != null)
                    Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
            }
        };

        addDialog(new AlertDialog.Builder(getActivity())
                .setTitle(R.string.cancel_ride_question)
                .setPositiveButton(R.string.only_work, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommuteManager.getInstance().cancelTicket(ticket, onCancelFinishedCallback);
                    }
                })
                .setNegativeButton(R.string.both_directions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommuteManager.getInstance().cancelTrip(ticket.getTrip(), onCancelFinishedCallback);
                    }
                })
                .setNeutralButton(android.R.string.cancel, null)
                .show());
    }

    @Override
    public void onRiderStateChanged() {
        onTicketsRefreshed();
    }

    @SuppressWarnings("unused")
    public void onEvent(AluviPushNotificationListenerService.PushNotificationEvent event) {
        refreshTickets();
    }

    @SuppressWarnings("unused")
    public void onEvent(BackHomeEvent event) {
        onTicketScheduled(mCurrentTicket, true);
    }

    private Dialog buildTransitionDialog(TicketStateTransition transition, final Dialog nextDialog) {
        TransitionMessageHelper messageHelper = getMessageResouceForTransition(transition);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(messageHelper.getTitle() != -1 ? messageHelper.getTitle() : R.string.ticket_updated)
                .setPositiveButton(messageHelper.getPositiveText() != -1 ? messageHelper.getPositiveText() : android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (nextDialog != null)
                                    nextDialog.show();
                            }
                        });

        if (messageHelper.getMessage() != -1)
            builder.setMessage(messageHelper.getMessage());

        if (messageHelper.getNegativeText() != -1)
            builder.setNegativeButton(messageHelper.getNegativeText(), null);
        return builder.create();
    }

    private TransitionMessageHelper getMessageResouceForTransition(TicketStateTransition transition) {
        String newState = transition.getNewState();

        switch (newState) {
            case Ticket.STATE_COMMUTE_SCHEDULER_FAILED:
                return new TransitionMessageHelper(R.string.trip_unfulfilled_title, R.string.trip_unfulfilled, R.string.great, -1);
            case Ticket.STATE_REQUESTED:
                return new TransitionMessageHelper(R.string.trip_requested, -1, R.string.great, -1);
            case Ticket.STATE_SCHEDULED:
                return new TransitionMessageHelper(R.string.trip_fulfilled_title, R.string.trip_fulfilled, R.string.great, -1);
            case Ticket.STATE_ABORTED:
            case Ticket.STATE_CANCELLED:
            case Ticket.STATE_RIDER_CANCELLED:
                return new TransitionMessageHelper(R.string.ride_cancelled, R.string.fare_cancelled_by_rider, android.R.string.ok, -1);
            case Ticket.STATE_DRIVER_CANCELLED:
                return new TransitionMessageHelper(R.string.ride_cancelled, R.string.fare_cancelled_by_driver, android.R.string.ok, -1);
            case Ticket.STATE_COMPLETE:
                return new TransitionMessageHelper(R.string.trip_complete, -1, R.string.great, -1);
        }

        return new TransitionMessageHelper(-1, -1, -1, -1);
    }

    private static class TransitionMessageHelper {
        private int title = -1, message = -1, positiveText = -1, negativeText = -1;

        public TransitionMessageHelper(int title, int message, int positiveText, int negativeText) {
            this.title = title;
            this.message = message;
            this.positiveText = positiveText;
            this.negativeText = negativeText;
        }

        public int getTitle() {
            return title;
        }

        public void setTitle(int title) {
            this.title = title;
        }

        public int getMessage() {
            return message;
        }

        public void setMessage(int message) {
            this.message = message;
        }

        public int getPositiveText() {
            return positiveText;
        }

        public void setPositiveText(int positiveText) {
            this.positiveText = positiveText;
        }

        public int getNegativeText() {
            return negativeText;
        }

        public void setNegativeText(int negativeText) {
            this.negativeText = negativeText;
        }
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
