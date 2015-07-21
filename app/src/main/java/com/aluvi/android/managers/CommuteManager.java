package com.aluvi.android.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.aluvi.android.api.ApiCallback;
import com.aluvi.android.api.tickets.CommuterTicketsResponse;
import com.aluvi.android.api.tickets.RequestCommuterTicketsCallback;
import com.aluvi.android.api.tickets.TicketsApi;
import com.aluvi.android.api.tickets.model.TicketData;
import com.aluvi.android.application.AluviPreferences;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.exceptions.UserRecoverableSystemError;
import com.aluvi.android.helpers.GeocoderUtils;
import com.aluvi.android.model.RealmHelper;
import com.aluvi.android.model.local.TicketLocation;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;

import org.joda.time.LocalDate;
import org.joda.time.Period;

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
        float homeLatitude = preferences.getFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, GeocoderUtils.INVALID_LOCATION);
        float homeLongitude = preferences.getFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, GeocoderUtils.INVALID_LOCATION);
        float workLatitude = preferences.getFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, GeocoderUtils.INVALID_LOCATION);
        float workLongitude = preferences.getFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, GeocoderUtils.INVALID_LOCATION);

        String homePlaceName = preferences.getString(AluviPreferences.COMMUTER_HOME_PLACENAME_KEY, "");
        String workPlaceName = preferences.getString(AluviPreferences.COMMUTER_WORK_PLACENAME_KEY, "");

        homeLocation = new TicketLocation(homeLatitude, homeLongitude, homePlaceName);
        workLocation = new TicketLocation(workLatitude, workLongitude, workPlaceName);

        pickupTimeHour = preferences.getInt(AluviPreferences.COMMUTER_PICKUP_TIME_HOUR_KEY, -1);
        returnTimeHour = preferences.getInt(AluviPreferences.COMMUTER_RETURN_TIME_HOUR_KEY, -1);

        pickupTimeMinute = preferences.getInt(AluviPreferences.COMMUTER_PICKUP_TIME_MINUTE_KEY, -1);
        returnTimeMinute = preferences.getInt(AluviPreferences.COMMUTER_RETURN_TIME_MINUTE_KEY, -1);

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
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, GeocoderUtils.INVALID_LOCATION);
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, GeocoderUtils.INVALID_LOCATION);
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, GeocoderUtils.INVALID_LOCATION);
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, GeocoderUtils.INVALID_LOCATION);
        editor.putString(AluviPreferences.COMMUTER_HOME_PLACENAME_KEY, "");
        editor.putString(AluviPreferences.COMMUTER_WORK_PLACENAME_KEY, "");
        editor.putInt(AluviPreferences.COMMUTER_PICKUP_TIME_HOUR_KEY, -1);
        editor.putInt(AluviPreferences.COMMUTER_RETURN_TIME_HOUR_KEY, -1);
        editor.putInt(AluviPreferences.COMMUTER_PICKUP_TIME_MINUTE_KEY, -1);
        editor.putInt(AluviPreferences.COMMUTER_RETURN_TIME_MINUTE_KEY, -1);
        editor.putBoolean(AluviPreferences.COMMUTER_IS_DRIVER_KEY, false);
        editor.commit();

        load(); // Reset by pulling default values from shared prefs
    }

    public boolean routeIsSet() {

        boolean locationsIncorrect =
                homeLocation.getLatitude() == GeocoderUtils.INVALID_LOCATION ||
                        homeLocation.getLongitude() == GeocoderUtils.INVALID_LOCATION ||
                        workLocation.getLatitude() == GeocoderUtils.INVALID_LOCATION ||
                        workLocation.getLongitude() == GeocoderUtils.INVALID_LOCATION;

        boolean timesIncorrect = pickupTimeHour == -1 || pickupTimeMinute == -1 ||
                returnTimeHour == -1 || returnTimeMinute == -1;

        return !locationsIncorrect && !timesIncorrect;
    }

    public void requestRidesForTomorrow(final Callback callback) throws UserRecoverableSystemError {
        Date rideDate = new LocalDate()
                .plus(Period.days(1))
                .toDateTimeAtStartOfDay()
                .toDate();

        Realm realm = AluviRealm.getDefaultRealm();
        RealmResults<Ticket> results = realm.where(Ticket.class) // Look for a pre-existing request for tomorrow
                .equalTo("rideDate", rideDate) // Rides that are tomorrow which have been created, requested, or scheduled
                .beginGroup()
                .equalTo("state", Ticket.StateCreated)
                .or()
                .equalTo("state", Ticket.StateRequested)
                .or()
                .equalTo("state", Ticket.StateScheduled)
                .endGroup()
                .findAll();

        if (results.size() >= 2) {
            throw new UserRecoverableSystemError("There are already rides requested or scheduled for tomorrow. " +
                    "This is a system error but can be recovered by canceling your commuter rides and requesting again");
        } else {
            realm.beginTransaction();
            for (Ticket result : results) {
                result.removeFromRealm();
            }
            realm.commitTransaction();
        }

        // Go ahead and create the tickets, then request with the server
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
                        toWorkTicket.setRideId(response.ticketToWorkRideId);
                        toWorkTicket.setState(Ticket.StateRequested);

                        fromWorkTicket.setTripId(response.tripId);
                        fromWorkTicket.setRideId(response.ticketFromWorkRideId);
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

    public boolean isDriving() {
        return driving;
    }

    public void cancelTicket(final Ticket ticket, final Callback callback) {

        if (ticket.getRideId() != 0) {
            if (ticket.isDriving()) {
                // Driver Api - cancel driving
            } else {

                if (ticket.getHovFare() == null) {
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

    public void refreshTickets(final Callback callback) {
        TicketsApi.refreshTickets(new TicketsApi.RefreshTicketsCallback() {
            @Override
            public void success(List<TicketData> tickets) {
                if (tickets != null) {
                    Realm realm = AluviRealm.getDefaultRealm();
                    realm.beginTransaction();

                    for (TicketData ticket : tickets) {
                        Ticket savedTicket = realm.where(Ticket.class)
                                .equalTo("rideId", ticket.getRideId())
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
                        }

                        Ticket.initTicketForData(savedTicket, ticket);
                    }

                    realm.commitTransaction();
                }

                if (callback != null)
                    callback.success();
            }

            @Override
            public void failure(int statusCode) {
                callback.failure("Unable to refresh tickets");
            }
        });
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
