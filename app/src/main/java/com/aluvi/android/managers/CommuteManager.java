package com.aluvi.android.managers;

import android.support.annotation.Nullable;
import android.util.Log;

import com.aluvi.android.api.ApiCallback;
import com.aluvi.android.api.tickets.TicketsApi;
import com.aluvi.android.api.tickets.model.PickupPointData;
import com.aluvi.android.api.tickets.model.TicketData;
import com.aluvi.android.api.users.RoutesApi;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.exceptions.UserRecoverableSystemError;
import com.aluvi.android.helpers.RequestQueue;
import com.aluvi.android.managers.callbacks.Callback;
import com.aluvi.android.managers.callbacks.DataCallback;
import com.aluvi.android.model.RealmHelper;
import com.aluvi.android.model.local.TicketStateTransition;
import com.aluvi.android.model.realm.Route;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by matthewxi on 7/15/15.
 */
public class CommuteManager {
    public final static int INVALID_TIME = -1;

    private final String TAG = "CommuteManager";
    private static CommuteManager mInstance;
    private Route userRoute;

    public interface RequestRidesCallback extends Callback {
        void onPaymentDetailsRequired();
    }

    public static synchronized void initialize() {
        if (mInstance == null)
            mInstance = new CommuteManager();
    }

    public static synchronized CommuteManager getInstance() {
        return mInstance;
    }

    private CommuteManager() {
        Realm realm = AluviRealm.getDefaultRealm();
        userRoute = realm.where(Route.class).findFirst();

        if (userRoute == null) {
            realm.beginTransaction();
            userRoute = realm.createObject(Route.class);
            realm.commitTransaction();
        }
    }

    /**
     * Sync this commute manager by fetching the latest route that the user has set. Initialization isn't dependent
     * on a route being found, but is dependent on fetching the latest copy we can find. If there aren't any routes
     * stored server or client side, then create an empty route.
     *
     * @param callback
     */
    public void sync(final Callback callback) {
        buildSyncQueue(new RequestQueue.RequestQueueListener() {
            @Override
            public void onRequestsFinished() {
                if (callback != null)
                    callback.success();
            }

            @Override
            public void onError(String message) {
                if (callback != null)
                    callback.failure(message);
            }
        }).execute();
    }

    public RequestQueue buildSyncQueue(RequestQueue.RequestQueueListener listener) {
        return new RequestQueue(listener).addRequest(new RequestQueue.Task() {
            @Override
            public void run() {
                refreshRoutePreferences(new Callback() {
                    @Override
                    public void success() {
                        onTaskComplete();
                    }

                    @Override
                    public void failure(String message) {
                        onTaskError(message);
                    }
                });
            }
        }).addRequest(new RequestQueue.Task() {
            @Override
            public void run() {
                refreshTickets(new DataCallback<List<TicketStateTransition>>() {
                    @Override
                    public void success(List<TicketStateTransition> result) {
                        onTaskComplete();
                    }

                    @Override
                    public void failure(String message) {
                        onTaskError(message);
                    }
                });
            }
        }).buildQueue();
    }

    public void refreshRoutePreferences(final Callback callback) {
        RoutesApi.getSavedRoute(new RoutesApi.OnRouteFetchedListener() {
            @Override
            public void onFetched(Route route) {
                onRouteFetched(route);
                callback.success();
            }

            @Override
            public void onFailure(int statusCode) {
                Log.e(TAG, "Error fetching route. Status code: " + statusCode);
                callback.failure("Error fetching status code");
            }
        });
    }

    private void onRouteFetched(final Route route) {
        if (route != null) {
            AluviRealm.getDefaultRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.clear(Route.class); // Remove previously saved routes
                    userRoute = realm.copyToRealm(route);
                }
            });
        }
    }

    public void saveRoute(final Callback callback) {
        RoutesApi.saveRoute(userRoute, new RoutesApi.OnRouteSavedListener() {
            @Override
            public void onSaved(Route route) {
                if (callback != null)
                    callback.success();
            }

            @Override
            public void onFailure(int statusCode) {
                if (callback != null)
                    callback.failure("Error, couldn't saveRoute user route");
            }
        });
    }

    public boolean isMinViableRouteAvailable() {
        return userRoute != null && userRoute.getOrigin() != null && userRoute.getDestination() != null;
    }

    public void clear() {
        // Destroy any saved trip data
        Realm aluviRealm = AluviRealm.getDefaultRealm();
        aluviRealm.beginTransaction();

        aluviRealm.where(Trip.class).findAll().clear();
        aluviRealm.where(Ticket.class).findAll().clear();
        aluviRealm.commitTransaction();
    }

    public void requestRidesForTomorrow(final RequestRidesCallback callback) throws UserRecoverableSystemError {
        if (!isMinViableRouteAvailable()) {
            callback.failure("You do not have a commute set up yet");
            return;
        }

        LocalDate rideDate = new LocalDate().plusDays(1);
        Date date = rideDate.toDate();

        if (!checkExistingTickets(date))
            throw new UserRecoverableSystemError("There are already rides requested or scheduled for tomorrow. " +
                    "This is a system error but can be recovered by canceling your commuter rides and requesting again.");

        final Ticket toWorkTicket = Ticket.buildNewTicket(rideDate, userRoute);
        final Ticket fromWorkTicket = Ticket.buildNewTicket(rideDate, userRoute, true);
        TicketsApi.requestCommuterTickets(toWorkTicket, fromWorkTicket,
                new TicketsApi.RefreshTicketsCallback() {
                    @Override
                    public void success(List<TicketData> tickets) {
                        onTicketsRefreshed(tickets);
                        callback.success();
                    }

                    @Override
                    public void failure(int statusCode) {
                        if (statusCode == HttpURLConnection.HTTP_PAYMENT_REQUIRED)
                            callback.onPaymentDetailsRequired();
                        else
                            callback.failure("Unable to request rides");
                    }
                });
    }

    private boolean checkExistingTickets(Date startDate) {
        Realm realm = AluviRealm.getDefaultRealm();

        // @formatter:off
        RealmResults<Ticket> results = realm.where(Ticket.class) // Look for a pre-existing request for tomorrow
                .greaterThanOrEqualTo("rideDate", startDate) // Rides that are tomorrow or later which have been created, requested, or scheduled
                .beginGroup()
                    .equalTo("state", Ticket.STATE_CREATED)
                    .or()
                    .equalTo("state", Ticket.STATE_REQUESTED)
                    .or()
                    .equalTo("state", Ticket.STATE_SCHEDULED)
                .endGroup()
                .findAll();
        // @formatter:on

        if (results.size() >= 2) {
            return false;
        } else {
            realm.beginTransaction();
            results.clear();
            realm.commitTransaction();
        }

        return true;
    }

    public void cancelTicket(final Ticket ticket, final Callback callback) {
        if (ticket.getId() != 0) {
            TicketsApi.cancelTicket(ticket, new TicketsApi.RefreshTicketsCallback() {
                @Override
                public void success(List<TicketData> tickets) {
                    onTicketsRefreshed(tickets);
                    callback.success();
                }

                @Override
                public void failure(int statusCode) {
                    callback.failure("Error - unable to delete your ticket. Please try again.");
                }
            });
        } else {
            RealmHelper.removeFromRealm(ticket);
            callback.success();
        }
    }

    /**
     * Load tickets using the tickets Aluvi API. Tickets saved to realm. You will typically load the currently active ticket by using
     * the {@link #getActiveTicket()} method.
     *
     * @param callback Non-null callback that provides a list of ticket state transitions that clients can use
     *                 to update UI/state.
     */
    public void refreshTickets(final DataCallback<List<TicketStateTransition>> callback) {
        TicketsApi.refreshTickets(new TicketsApi.RefreshTicketsCallback() {
            @Override
            public void success(final List<TicketData> tickets) {
                List<TicketStateTransition> transitions = onTicketsRefreshed(tickets);
                callback.success(transitions);
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Unable to refresh tickets");
            }
        });
    }

    private List<TicketStateTransition> onTicketsRefreshed(final List<TicketData> tickets) {
        Realm realm = AluviRealm.getDefaultRealm();
        realm.beginTransaction();
        List<TicketStateTransition> ticketStateTransitions = new ArrayList<>();
        if (tickets != null) {
            for (TicketData ticket : tickets)
                updateTicketForData(ticket, ticketStateTransitions);
            removeNonRelevantTickets(tickets, ticketStateTransitions);
        }
        realm.commitTransaction();
        return ticketStateTransitions;
    }

    /**
     * Check for tickets that are no longer relevant. This method must be called within a realm transaction handler
     * or a begin/commit transaction block.
     *
     * @param tickets
     * @return
     */
    private void removeNonRelevantTickets(List<TicketData> tickets, List<TicketStateTransition> ticketStateTransitions) {
        Realm realm = AluviRealm.getDefaultRealm();
        RealmQuery<Ticket> query = realm.where(Ticket.class);
        for (int i = 0; i < tickets.size(); i++)
            query.notEqualTo("id", tickets.get(i).getTicketId());

        List<Ticket> nonRelevantTickets = query.findAll();
        for (int i = 0; i < nonRelevantTickets.size(); i++) {
            Ticket t = nonRelevantTickets.get(i);
            ticketStateTransitions.add(new TicketStateTransition(t.getId(), t.getState(), Ticket.STATE_IRRELEVANT));
            t.removeFromRealm();
        }
    }

    private void updateTicketForData(TicketData ticket, List<TicketStateTransition> ticketStateTransitions) {
        Realm realm = AluviRealm.getDefaultRealm();
        Ticket savedTicket = realm.where(Ticket.class)
                .equalTo("id", ticket.getTicketId())
                .findFirst();

        // If the ticket doesn't exist (because the device's memory was cleared, tickets created on another device, etc)
        // then create it and copy over data from the TicketData object
        if (savedTicket == null) {
            savedTicket = realm.createObject(Ticket.class);

            Trip tripForTicket = realm.where(Trip.class).equalTo("tripId", ticket.getTripId())
                    .findFirst();
            if (tripForTicket == null) {
                tripForTicket = realm.createObject(Trip.class);
                tripForTicket.setTripId(ticket.getTripId());
            }

            tripForTicket.getTickets().add(savedTicket);
            savedTicket.setTrip(tripForTicket);

            TicketStateTransition stateTransition = new TicketStateTransition(savedTicket.getId(), null, ticket.getState());
            ticketStateTransitions.add(stateTransition);

        } else {
            if (!savedTicket.getState().equals(ticket.getState())) {
                TicketStateTransition stateTransition = new TicketStateTransition(savedTicket.getId(), savedTicket.getState(),
                        ticket.getState());
                ticketStateTransitions.add(stateTransition);
            }
        }

        savedTicket.getTrip().setTripState(ticket.getTripState());
        Ticket.updateTicketWithTicketData(savedTicket, ticket, realm);
        savedTicket.setLastUpdated(new Date());
    }

    public void cancelTrip(final Trip trip, final Callback callback) {
        TicketsApi.cancelTrip(trip, new ApiCallback() {
            @Override
            public void success() {
                final RealmResults<Ticket> tickets = trip.getTickets().where().findAll();
                Realm realm = AluviRealm.getDefaultRealm();
                realm.beginTransaction();
                for (int i = 0; i < tickets.size(); i++)
                    tickets.get(i).setState(Ticket.STATE_CANCELLED);
                realm.commitTransaction();
                callback.success();
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Could not cancel trip.  Please try again");
            }
        });
    }

    public void ridersPickedUp(final Ticket ticket, final Callback callback) {
        TicketsApi.ridersPickedUp(ticket, new TicketsApi.RefreshTicketsCallback() {
            @Override
            public void success(List<TicketData> data) {
                onTicketsRefreshed(data);
                onRiderStateUpdated(callback);
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Problem communicating with server");
            }
        });
    }

    public void ridersDroppedOff(final Ticket ticket, final Callback callback) {
        TicketsApi.ridersDroppedOff(ticket, new TicketsApi.RefreshTicketsCallback() {
            @Override
            public void success(List<TicketData> data) {
                onTicketsRefreshed(data);
                onRiderStateUpdated(callback);
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Problem communicating with server");
            }
        });
    }

    private void onRiderStateUpdated(final Callback callback) {
        refreshTickets(new DataCallback<List<TicketStateTransition>>() {
            @Override
            public void success(List<TicketStateTransition> result) {
                callback.success();
            }

            @Override
            public void failure(String message) {
                callback.failure(message);
            }
        });
    }

    public void getPickupPoints(final DataCallback<List<PickupPointData>> callback) {
        TicketsApi.getPickupPoints(new TicketsApi.PickupPointsCallback() {
            @Override
            public void success(List<PickupPointData> points) {
                callback.success(points);
            }

            @Override
            public void failure(int statueCode) {
                callback.failure("Unable to fetch pickup points");
            }
        });
    }

    public void notifyLate(Ticket ticket, final Callback callback) {
        if (callback != null) {
            callback.success();
        }
    }

    /**
     * Returns tickets that are requested or scheduled that are in the future.
     *
     * @return
     */
    @Nullable
    public Ticket getActiveTicket() {
        // @formatter:off
        RealmResults<Ticket> tickets = AluviRealm.getDefaultRealm()
                .where(Ticket.class)
                .greaterThan("pickupTime", new Date())
                .beginGroup()
                    .equalTo("state", Ticket.STATE_REQUESTED)
                    .or()
                    .equalTo("state", Ticket.STATE_SCHEDULED)
                    .or()
                    .equalTo("state", Ticket.STATE_IN_PROGRESS)
                    .or()
                    .equalTo("state", Ticket.STATE_STARTED)
                .endGroup()
                .findAllSorted("pickupTime");
        // @formatter:on
        return tickets.size() > 0 ? tickets.get(0) : null;
    }

    @Nullable
    public Trip getActiveTrip() {
        Ticket activeTicket = getActiveTicket();
        return activeTicket != null ? activeTicket.getTrip() : null;
    }

    public boolean isTripNotStarted() {
        Trip activeTrip = getActiveTrip();
        if (activeTrip != null) {
            RealmResults<Ticket> tickets = activeTrip.getTickets()
                    .where().findAllSorted("pickupTime");
            if (tickets.size() == 2) {
                Ticket aSide = tickets.get(0);
                Ticket bSide = tickets.get(1);
                return aSide.getState().equals(Ticket.STATE_SCHEDULED) && bSide.getState().equals(Ticket.STATE_SCHEDULED);
            }
        }

        return false;

    }

    public boolean isDriveHomeEnabled() {
        return isDriveHomeEnabled(getActiveTrip());
    }

    public boolean isDriveHomeEnabled(Trip activeTrip) {
        if (activeTrip != null) {
            RealmResults<Ticket> tickets = activeTrip.getTickets()
                    .where().findAllSorted("pickupTime");
            if (tickets.size() == 2) {
                Ticket aSide = tickets.get(0);
                Ticket bSide = tickets.get(1);
                return bSide.getState().equals(Ticket.STATE_SCHEDULED);
            }
        }

        return false;
    }

    public Route getRoute() {
        return userRoute;
    }

    public void setRoute(Route route) {
        userRoute = route;
    }
}
