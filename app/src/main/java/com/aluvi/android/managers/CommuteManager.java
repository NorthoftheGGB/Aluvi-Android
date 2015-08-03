package com.aluvi.android.managers;

import android.support.annotation.Nullable;
import android.util.Log;

import com.aluvi.android.api.ApiCallback;
import com.aluvi.android.api.gis.MapQuestApi;
import com.aluvi.android.api.gis.models.RouteData;
import com.aluvi.android.api.tickets.CommuterTicketsResponse;
import com.aluvi.android.api.tickets.RequestCommuterTicketsCallback;
import com.aluvi.android.api.tickets.TicketsApi;
import com.aluvi.android.api.tickets.model.TicketData;
import com.aluvi.android.api.users.RoutesApi;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.exceptions.UserRecoverableSystemError;
import com.aluvi.android.model.RealmHelper;
import com.aluvi.android.model.local.TicketStateTransition;
import com.aluvi.android.model.realm.Route;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by matthewxi on 7/15/15.
 */
public class CommuteManager {

    public interface Callback {
        void success();

        void failure(String message);
    }

    public interface DataCallback<T> {
        void success(T result);

        void failure(String message);
    }

    public final static int INVALID_TIME = -1;
    private final String TAG = "CommuteManager";
    private static CommuteManager mInstance;

    private Route userRoute;

    public static synchronized void initialize() {
        if (mInstance == null)
            mInstance = new CommuteManager();
    }

    public static synchronized CommuteManager getInstance() {
        return mInstance;
    }

    private CommuteManager() {
        userRoute = AluviRealm.getDefaultRealm().where(Route.class).findFirst();
        if (userRoute == null)
            userRoute = new Route();
    }

    /**
     * Sync this commute manager by fetching the latest route that the user has set. Initialization isn't dependent
     * on a route being found, but is dependent on fetching the latest copy we can find. If there aren't any routes
     * stored server or client side, then create an empty route.
     *
     * @param callback
     */
    public void sync(final Callback callback) {
        new ManagerRequestQueue(new ManagerRequestQueue.RequestQueueListener() {
            @Override
            public void onRequestsFinished() {
                callback.success();
            }

            @Override
            public void onError(String message) {
                callback.failure(message);
            }
        }).addRequest(new ManagerRequestQueue.RequestTask() {
            @Override
            public void run() {
                refreshRoutePreferences(new Callback() {
                    @Override
                    public void success() {
                        onComplete();
                    }

                    @Override
                    public void failure(String message) {
                        onError(message);
                    }
                });
            }
        }).addRequest(new ManagerRequestQueue.RequestTask() {
            @Override
            public void run() {
                refreshTickets(new DataCallback<List<TicketStateTransition>>() {
                    @Override
                    public void success(List<TicketStateTransition> result) {
                        onComplete();
                    }

                    @Override
                    public void failure(String message) {
                        onError(message);
                    }
                });
            }
        }).execute();
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

    public void save(final Callback callback) {
        RoutesApi.saveRoute(userRoute, new RoutesApi.OnRouteSavedListener() {
            @Override
            public void onSaved(Route route) {
                if (callback != null)
                    callback.success();
            }

            @Override
            public void onFailure(int statusCode) {
                if (callback != null)
                    callback.failure("Error, couldn't save user route");
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

    public void requestRidesForTomorrow(final Callback callback) throws UserRecoverableSystemError {
        if (!isMinViableRouteAvailable()) {
            callback.failure("You do not have a commute set up yet");
            return;
        }

        Date rideDate = new LocalDate().plus(Period.days(1)).toDateTimeAtStartOfDay().toDate();
        if (!checkExistingTickets(rideDate))
            throw new UserRecoverableSystemError("There are already rides requested or scheduled for tomorrow. " +
                    "This is a system error but can be recovered by canceling your commuter rides and requesting again");

        final Ticket toWorkTicket = Ticket.buildNewTicket(rideDate, userRoute);
        final Ticket fromWorkTicket = Ticket.buildNewTicket(rideDate, userRoute, true);
        TicketsApi.requestCommuterTickets(toWorkTicket, fromWorkTicket,
                new RequestCommuterTicketsCallback(toWorkTicket, fromWorkTicket) {
                    @Override
                    public void success(CommuterTicketsResponse response) {
                        updateTickets(toWorkTicket, fromWorkTicket, response);
                        callback.success();
                    }

                    @Override
                    public void failure(int statusCode) {
                        callback.failure("Scheduling failure message");
                    }
                });
    }

    private void updateTickets(Ticket toWorkTicket, Ticket fromWorkTicket, CommuterTicketsResponse response) {
        toWorkTicket.setId(response.ticketToWorkRideId);
        toWorkTicket.setState(Ticket.StateRequested);

        fromWorkTicket.setId(response.ticketFromWorkRideId);
        fromWorkTicket.setState(Ticket.StateRequested);

        Trip trip = new Trip();
        trip.setTripId(response.tripId);
        trip.getTickets().add(toWorkTicket);
        trip.getTickets().add(fromWorkTicket);

        toWorkTicket.setTrip(trip);
        fromWorkTicket.setTrip(trip);

        Realm realm = AluviRealm.getDefaultRealm();
        realm.beginTransaction();
        realm.copyToRealm(trip);
        realm.commitTransaction();
    }

    private boolean checkExistingTickets(Date startDate) {
        Realm realm = AluviRealm.getDefaultRealm();
        RealmResults<Ticket> results = realm.where(Ticket.class) // Look for a pre-existing request for tomorrow
                .greaterThanOrEqualTo("rideDate", startDate) // Rides that are tomorrow or later which have been created, requested, or scheduled
                .beginGroup()
                .equalTo("state", Ticket.StateCreated)
                .or()
                .equalTo("state", Ticket.StateRequested)
                .or()
                .equalTo("state", Ticket.StateScheduled)
                .endGroup()
                .findAll();

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

            if (!ticket.getState().equals(Ticket.StateScheduled)) {
                // ride has not been scheduled yet
                TicketsApi.cancelRiderTicketRequest(ticket, new ApiCallback() {
                    @Override
                    public void success() {
                        Trip trip = ticket.getTrip();
                        RealmHelper.removeFromRealm(ticket);
                        Trip.removeIfEmpty(trip);
                        callback.success();
                    }

                    @Override
                    public void failure(int statusCode) {
                        callback.failure("We had a problem deleting your ticket.  Please try again.");
                    }
                });
            } else {
                TicketsApi.cancelRiderScheduledTicket(ticket, new ApiCallback() {
                    @Override
                    public void success() {
                        Trip trip = ticket.getTrip();
                        RealmHelper.removeFromRealm(ticket);
                        Trip.removeIfEmpty(trip);
                        callback.success();
                    }

                    @Override
                    public void failure(int statusCode) {
                        callback.failure("We had a problem deleting your ticket.  Please try again.");
                    }
                });
            }

        } else {
            RealmHelper.removeFromRealm(ticket);
            callback.success();
        }
    }

    public void refreshTickets(final DataCallback<List<TicketStateTransition>> callback) {
        TicketsApi.refreshTickets(new TicketsApi.RefreshTicketsCallback() {
            @Override
            public void success(final List<TicketData> tickets) {
                Realm realm = AluviRealm.getDefaultRealm();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        List<TicketStateTransition> ticketStateTransitions = new ArrayList<>();

                        if (tickets != null)
                            for (TicketData ticket : tickets)
                                updateTicketForData(ticket, ticketStateTransitions);


                        if (callback != null)
                            callback.success(ticketStateTransitions);
                    }
                });
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Unable to refresh tickets");
            }
        });
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

        Ticket.updateTicketWithTicketData(savedTicket, ticket, realm);
        savedTicket.setLastUpdated(new Date());
    }

    public void cancelTrip(final Trip trip, final Callback callback) {
        TicketsApi.cancelTrip(trip, new ApiCallback() {
            @Override
            public void success() {
                AluviRealm.getDefaultRealm().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        trip.getTickets().where().findAll().clear();
                        trip.removeFromRealm();
                        callback.success();
                    }
                });
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Could not cancel trip.  Please try again");
            }
        });
    }

    public void ridersPickedUp(Ticket ticket, final Callback callback) {
        TicketsApi.ridersPickedUp(ticket, new ApiCallback() {
            @Override
            public void success() {
                // TODO update ticket state
                callback.success();
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Problem communicating with server");

            }
        });
    }

    public void ridersDroppedOff(Ticket ticket, final Callback callback) {
        TicketsApi.ridersDroppedOff(ticket, new ApiCallback() {
            @Override
            public void success() {
                // TODO update ticket state to irrelevant
                callback.success();
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Problem communicating with server");

            }
        });
    }

    public void loadRouteForTicket(Ticket ticket, final DataCallback<RouteData> callback) {
        LatLng start = new LatLng(ticket.getOriginLatitude(), ticket.getOriginLongitude());
        LatLng end = new LatLng(ticket.getDestinationLatitude(), ticket.getDestinationLongitude());

        MapQuestApi.findRoute(start, end, new MapQuestApi.MapQuestCallback() {
            @Override
            public void onRouteFound(RouteData route) {
                callback.success(route);
            }

            @Override
            public void onFailure(int statusCode) {
                callback.failure("Could not fetch route");
            }
        });
    }

    /**
     * Minimize calling this method - it is relatively expensive to look through a potentially large list of tickets
     * looking for the most relevant one (nearest in the future, requested or scheduled state).
     *
     * @return
     */
    @Nullable
    public Ticket getActiveTicket() {
        RealmResults<Ticket> tickets = AluviRealm.getDefaultRealm()
                .where(Ticket.class)
                .greaterThan("pickupTime", new Date())
                .beginGroup()
                .equalTo("state", Ticket.StateRequested)
                .or()
                .equalTo("state", Ticket.StateScheduled)
                .endGroup()
                .findAllSorted("pickupTime");

        return tickets.size() > 0 ? tickets.get(0) : null;
    }

    public Route getRoute() {
        return userRoute;
    }

    public void setRoute(Route route) {
        userRoute = route;
    }
}
