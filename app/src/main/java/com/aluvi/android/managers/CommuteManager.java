package com.aluvi.android.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.aluvi.android.api.ApiCallback;
import com.aluvi.android.api.gis.GeocodingApi;
import com.aluvi.android.api.gis.MapQuestApi;
import com.aluvi.android.api.gis.models.RouteData;
import com.aluvi.android.api.tickets.CommuterTicketsResponse;
import com.aluvi.android.api.tickets.RequestCommuterTicketsCallback;
import com.aluvi.android.api.tickets.TicketsApi;
import com.aluvi.android.api.tickets.model.TicketData;
import com.aluvi.android.application.AluviPreferences;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.exceptions.UserRecoverableSystemError;
import com.aluvi.android.model.RealmHelper;
import com.aluvi.android.model.local.TicketLocation;
import com.aluvi.android.model.local.TicketStateTransition;
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

    private SharedPreferences preferences;
    private Context ctx;

    private TicketLocation homeLocation, workLocation;
    private int pickupTimeHour, pickupTimeMinute;
    private int returnTimeHour, returnTimeMinute;
    private boolean driving;

    public static synchronized void initialize(Context context) {
        if (mInstance == null) {
            mInstance = new CommuteManager(context);
        }
    }

    public static synchronized CommuteManager getInstance() {
        return mInstance;
    }

    public CommuteManager(Context context) {
        ctx = context;
        preferences = context.getSharedPreferences(AluviPreferences.COMMUTER_PREFERENCES_FILE, 0);
        load();
    }

    private void load() {
        float homeLatitude = preferences.getFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, GeocodingApi.INVALID_LOCATION);
        float homeLongitude = preferences.getFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, GeocodingApi.INVALID_LOCATION);
        float workLatitude = preferences.getFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, GeocodingApi.INVALID_LOCATION);
        float workLongitude = preferences.getFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, GeocodingApi.INVALID_LOCATION);

        String homePlaceName = preferences.getString(AluviPreferences.COMMUTER_HOME_PLACENAME_KEY, "");
        String workPlaceName = preferences.getString(AluviPreferences.COMMUTER_WORK_PLACENAME_KEY, "");

        homeLocation = new TicketLocation(homeLatitude, homeLongitude, homePlaceName);
        workLocation = new TicketLocation(workLatitude, workLongitude, workPlaceName);

        pickupTimeHour = preferences.getInt(AluviPreferences.COMMUTER_PICKUP_TIME_HOUR_KEY, INVALID_TIME);
        returnTimeHour = preferences.getInt(AluviPreferences.COMMUTER_RETURN_TIME_HOUR_KEY, INVALID_TIME);

        pickupTimeMinute = preferences.getInt(AluviPreferences.COMMUTER_PICKUP_TIME_MINUTE_KEY, INVALID_TIME);
        returnTimeMinute = preferences.getInt(AluviPreferences.COMMUTER_RETURN_TIME_MINUTE_KEY, INVALID_TIME);

        driving = preferences.getBoolean(AluviPreferences.COMMUTER_IS_DRIVER_KEY, false);
    }

    public void loadFromServer() {
        // routes API
        // implement RoutesApi library file
    }

    private void store() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, homeLocation.getLatitude());
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, homeLocation.getLongitude());

        editor.putFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, workLocation.getLatitude());
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, workLocation.getLongitude());

        editor.putString(AluviPreferences.COMMUTER_HOME_PLACENAME_KEY, homeLocation.getPlaceName());
        editor.putString(AluviPreferences.COMMUTER_WORK_PLACENAME_KEY, workLocation.getPlaceName());

        editor.putInt(AluviPreferences.COMMUTER_PICKUP_TIME_HOUR_KEY, pickupTimeHour);
        editor.putInt(AluviPreferences.COMMUTER_RETURN_TIME_HOUR_KEY, returnTimeHour);

        editor.putInt(AluviPreferences.COMMUTER_PICKUP_TIME_MINUTE_KEY, pickupTimeMinute);
        editor.putInt(AluviPreferences.COMMUTER_RETURN_TIME_MINUTE_KEY, returnTimeMinute);

        editor.putBoolean(AluviPreferences.COMMUTER_IS_DRIVER_KEY, driving);
        editor.commit();
    }

    public void save(Callback callback) {
        // TODO: Implement Routes API
        store();
    }

    public void clear() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, GeocodingApi.INVALID_LOCATION);
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, GeocodingApi.INVALID_LOCATION);
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, GeocodingApi.INVALID_LOCATION);
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, GeocodingApi.INVALID_LOCATION);
        editor.putString(AluviPreferences.COMMUTER_HOME_PLACENAME_KEY, "");
        editor.putString(AluviPreferences.COMMUTER_WORK_PLACENAME_KEY, "");
        editor.putInt(AluviPreferences.COMMUTER_PICKUP_TIME_HOUR_KEY, INVALID_TIME);
        editor.putInt(AluviPreferences.COMMUTER_RETURN_TIME_HOUR_KEY, INVALID_TIME);
        editor.putInt(AluviPreferences.COMMUTER_PICKUP_TIME_MINUTE_KEY, INVALID_TIME);
        editor.putInt(AluviPreferences.COMMUTER_RETURN_TIME_MINUTE_KEY, INVALID_TIME);
        editor.putBoolean(AluviPreferences.COMMUTER_IS_DRIVER_KEY, false);
        editor.commit();
        load(); // Reset by pulling default values from shared prefs

        // Destroy any saved trip data
        Realm aluviRealm = AluviRealm.getDefaultRealm();
        aluviRealm.beginTransaction();
        aluviRealm.where(Trip.class).findAll().clear();
        aluviRealm.where(Ticket.class).findAll().clear();
        aluviRealm.commitTransaction();
    }

    public boolean routeIsSet() {
        boolean locationsIncorrect =
                homeLocation.getLatitude() == GeocodingApi.INVALID_LOCATION ||
                        homeLocation.getLongitude() == GeocodingApi.INVALID_LOCATION ||
                        workLocation.getLatitude() == GeocodingApi.INVALID_LOCATION ||
                        workLocation.getLongitude() == GeocodingApi.INVALID_LOCATION;

        boolean timesIncorrect = pickupTimeHour == INVALID_TIME || pickupTimeMinute == INVALID_TIME ||
                returnTimeHour == INVALID_TIME || returnTimeMinute == INVALID_TIME;

        return !locationsIncorrect && !timesIncorrect;
    }

    public void requestRidesForTomorrow(final Callback callback) throws UserRecoverableSystemError {
        Date rideDate = new LocalDate().plus(Period.days(1)).toDateTimeAtStartOfDay().toDate();
        if (!checkExistingTickets(rideDate))
            throw new UserRecoverableSystemError("There are already rides requested or scheduled for tomorrow. " +
                    "This is a system error but can be recovered by canceling your commuter rides and requesting again");

        Realm realm = AluviRealm.getDefaultRealm();
        realm.beginTransaction();

        final Ticket toWorkTicket = realm.createObject(Ticket.class);
        Ticket.buildNewTicket(toWorkTicket, rideDate, getHomeLocation(), getWorkLocation(),
                driving, pickupTimeHour, pickupTimeMinute);

        final Ticket fromWorkTicket = realm.createObject(Ticket.class);
        Ticket.buildNewTicket(fromWorkTicket, rideDate, getWorkLocation(), getHomeLocation(),
                driving, returnTimeHour, returnTimeMinute);

        realm.commitTransaction();

        TicketsApi.requestCommuterTickets(toWorkTicket, fromWorkTicket,
                new RequestCommuterTicketsCallback(toWorkTicket, fromWorkTicket) {
                    @Override
                    public void success(CommuterTicketsResponse response) {
                        Realm realm = AluviRealm.getDefaultRealm();
                        realm.beginTransaction();

                        toWorkTicket.setTripId(response.tripId);
                        toWorkTicket.setId(response.ticketToWorkRideId);
                        toWorkTicket.setState(Ticket.StateRequested);

                        fromWorkTicket.setTripId(response.tripId);
                        fromWorkTicket.setId(response.ticketFromWorkRideId);
                        fromWorkTicket.setState(Ticket.StateRequested);

                        Trip trip = realm.createObject(Trip.class);
                        trip.setTripId(response.tripId);
                        trip.getTickets().add(this.toWorkTicket);
                        trip.getTickets().add(this.fromWorkTicket);

                        toWorkTicket.setTrip(trip);
                        fromWorkTicket.setTrip(trip);

                        realm.commitTransaction();
                        callback.success();
                    }

                    @Override
                    public void failure(int statusCode) {
                        // just delete the ticket if it doesn't go through
                        Realm realm = AluviRealm.getDefaultRealm();
                        realm.beginTransaction();
                        toWorkTicket.removeFromRealm();
                        fromWorkTicket.removeFromRealm();
                        realm.commitTransaction();
                        callback.failure("Scheduling failure message");
                    }
                });
    }

    private boolean checkExistingTickets(Date startDate) {
        Realm realm = AluviRealm.getDefaultRealm();
        RealmResults<Ticket> results = realm.where(Ticket.class) // Look for a pre-existing request for tomorrow
                .greaterThan("rideDate", startDate) // Rides that are tomorrow or later which have been created, requested, or scheduled
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
            for (Ticket result : results) {
                result.removeFromRealm();
            }
            realm.commitTransaction();
        }

        return true;
    }

    public boolean isDriving() {
        return driving;
    }

    public void cancelTicket(final Ticket ticket, final Callback callback) {
        if (ticket.getId() != 0) {
            if (ticket.isDriving()) {
                // Driver Api - cancel driving
            } else {
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
            }
        } else {
            RealmHelper.removeFromRealm(ticket);
        }
    }

    public void refreshTickets(final DataCallback<List<TicketStateTransition>> callback) {
        TicketsApi.refreshTickets(new TicketsApi.RefreshTicketsCallback() {
            @Override
            public void success(List<TicketData> tickets) {
                List<TicketStateTransition> ticketStateTransitions = new ArrayList<>();

                if (tickets != null) {
                    Realm realm = AluviRealm.getDefaultRealm();
                    realm.beginTransaction();

                    for (TicketData ticket : tickets) {
                        updateTicketForData(ticket, ticketStateTransitions);
                    }

                    realm.commitTransaction();
                    // TODO check for any tickets in Realm that are no longer relevant
                }

                if (callback != null)
                    callback.success(ticketStateTransitions);
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
            if (tripForTicket != null) {
                tripForTicket.getTickets().add(savedTicket);
            } else {
                Trip trip = realm.createObject(Trip.class);
                trip.setTripId(ticket.getTripId());
                trip.getTickets().add(savedTicket);
            }

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
                Trip.removeTickets(trip);
                RealmHelper.removeFromRealm(trip);
                callback.success();
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Could not cancel trip.  Please try again");
            }
        });
    }

    public void ridersPickedUp(Ticket ticket, final Callback callback){
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

    public void ridersDroppedOff(Ticket ticket, final Callback callback){
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

    public void setHomeLocation(TicketLocation homeLocation) {
        this.homeLocation = homeLocation;
    }

    public void setWorkLocation(TicketLocation workLocation) {
        this.workLocation = workLocation;
    }

    public TicketLocation getHomeLocation() {
        return homeLocation;
    }

    public TicketLocation getWorkLocation() {
        return workLocation;
    }

    public int getPickupTimeHour() {
        return pickupTimeHour;
    }

    public void setPickupTimeHour(int pickupTimeHour) {
        this.pickupTimeHour = pickupTimeHour;
    }

    public int getPickupTimeMinute() {
        return pickupTimeMinute;
    }

    public void setPickupTimeMinute(int pickupTimeMinute) {
        this.pickupTimeMinute = pickupTimeMinute;
    }

    public int getReturnTimeHour() {
        return returnTimeHour;
    }

    public void setReturnTimeHour(int returnTimeHour) {
        this.returnTimeHour = returnTimeHour;
    }

    public int getReturnTimeMinute() {
        return returnTimeMinute;
    }

    public void setReturnTimeMinute(int returnTimeMinute) {
        this.returnTimeMinute = returnTimeMinute;
    }

    public void setDriving(boolean driving) {
        this.driving = driving;
    }


}
