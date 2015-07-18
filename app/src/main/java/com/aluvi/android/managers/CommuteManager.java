package com.aluvi.android.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.aluvi.android.api.ApiCallback;
import com.aluvi.android.api.tickets.CommuterTicketsResponse;
import com.aluvi.android.api.tickets.RequestCommuterTicketsCallback;
import com.aluvi.android.api.tickets.TicketData;
import com.aluvi.android.api.tickets.TicketsApi;
import com.aluvi.android.application.AluviPreferences;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.exceptions.UserRecoverableSystemError;
import com.aluvi.android.model.RealmHelper;
import com.aluvi.android.model.local.TicketLocation;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;

import org.joda.time.LocalDate;
import org.joda.time.Period;


import java.util.Date;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by matthewxi on 7/15/15.
 */
public class CommuteManager {

    public interface Callback {
        void success();

        void failure(String message);
    }

    private static CommuteManager mInstance;

    private SharedPreferences preferences;
    private Context ctx;

    private TicketLocation homeLocation, workLocation;
    private int pickupTimeHour, pickupTimeMinute;
    private int returnTimeHour, returnTimeMinute;
    private boolean driving;

    public static synchronized void initialize(Context context){
        if(mInstance == null){
            mInstance = new CommuteManager(context);
        }
    }

    public static synchronized CommuteManager getInstance(){
        return mInstance;
    }

    public CommuteManager(Context context) {
        ctx = context;
        preferences = context.getSharedPreferences(AluviPreferences.COMMUTER_PREFERENCES_FILE, 0);
        load();
    }

    private void load() {
        float homeLatitude = preferences.getFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, 0);
        float homeLongitude = preferences.getFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, 0);
        float workLatitude = preferences.getFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, 0);
        float workLongitude = preferences.getFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, 0);

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

    public void loadFromServer(){
        // routes API
        // implement RoutesApi library file
    }

    private void store(){
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

    public void save(Callback callback){
        // Implement Routes API

        store();
    }

    public void clear(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, 0);
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, 0);
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, 0);
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, 0);
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
        return homeLocation.getLatitude() == 0 || homeLocation.getLongitude() == 0 || workLocation.getLatitude() == 0
                || workLocation.getLongitude() == 0 || pickupTimeHour == -1 || pickupTimeMinute == -1 || returnTimeHour == -1
                || returnTimeMinute == -1;
    }

    public void requestRidesForTomorrow(final Callback callback) throws UserRecoverableSystemError {
        LocalDate today = new LocalDate();
        LocalDate tomorrow = today.plus(Period.days(1));
        Date rideDate = tomorrow.toDateTimeAtStartOfDay().toDate();

        Realm realm = AluviRealm.getDefaultRealm();

        // Lood for a prexisting request for tomorrow
        RealmQuery<Ticket> query = realm.where(Ticket.class);
        query.equalTo("rideDate", rideDate);

        RealmResults<Ticket> results = query.findAll();
        int count = 0;
        Ticket orphan = null;
        if(results.size() != 0){
            // since we can't do an IN query, we check here for statuses
            for(Ticket ticket : results){
                String state = ticket.getState();
                if( state.equals(Ticket.StateCreated) || state.equals(Ticket.StateRequested) || state.equals(Ticket.StateScheduled) ){
                    // already have a ticket in there for tomorrow
                    count++;
                    orphan = ticket;
                }
            }

        }

        if(count == 2){
            throw new UserRecoverableSystemError( "There are already rides requested or scheduled for tomorrow, this is a system error but can be recovered by canceling your commuter rides and requesting again");
            // AluviStrings.commuter_rides_already_in_database);
        }

        if(count == 1 && orphan != null){
            // Orphaned request, delete it
            realm.beginTransaction();
            orphan.removeFromRealm();
            realm.commitTransaction();
        }

        // go ahead and create the tickets, then request with the server
        realm.beginTransaction();
        final Ticket toWorkTicket = realm.createObject(Ticket.class);
        Ticket.buildNewTicket(toWorkTicket, rideDate, getHomeLocation(), getWorkLocation(),
                driving, pickupTimeHour, pickupTimeMinute);
        final Ticket fromWorkTicket = realm.createObject(Ticket.class);
        Ticket.buildNewTicket(fromWorkTicket, rideDate, getWorkLocation(), getHomeLocation(),
                driving, returnTimeHour, returnTimeMinute);
        realm.commitTransaction();


        class Callback extends RequestCommuterTicketsCallback {

            public Callback(Ticket toWorkTicket, Ticket fromWorkTicket) {
                super(toWorkTicket, fromWorkTicket);
            }

            @Override
            public void success(CommuterTicketsResponse response) {
                Realm realm = AluviRealm.getDefaultRealm();
                realm.beginTransaction();
                this.toWorkTicket.setTripId(response.tripId);
                this.toWorkTicket.setRideId(response.ticketToWorkRideId);
                this.fromWorkTicket.setTripId(response.tripId);
                this.fromWorkTicket.setRideId(response.ticketFromWorkRideId);
                Trip trip = realm.createObject(Trip.class);
                trip.setTripId(response.tripId);
                trip.getTickets().add(this.toWorkTicket);
                trip.getTickets().add(this.fromWorkTicket);
                this.toWorkTicket.setTrip(trip);
                this.fromWorkTicket.setTrip(trip);
                realm.commitTransaction();
                callback.success();
            }

            @Override
            public void failure(int statusCode) {
                // just delete the ticket if it doesn't go through
                Realm realm = AluviRealm.getDefaultRealm();
                realm.beginTransaction();
                toWorkTicket.removeFromRealm();
                fromWorkTicket.removeFromRealm();;
                realm.commitTransaction();
                callback.failure("Scheduling failure message");
            }
        }

        TicketsApi.requestCommuterTickets(toWorkTicket, fromWorkTicket, new Callback(toWorkTicket, fromWorkTicket));
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

    public void setDriving(boolean driving) {
        this.driving = driving;
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

    public void refreshTickets(final Callback callback){
        TicketsApi.refreshTickets(new TicketsApi.RefreshTicketsCallback(){
            @Override
            public void success(TicketData[] tickets) {
                //TODO Here we need to update the tickets in realm with details from the server
                //need to be careful to recognize any missed state transitions
            }

            @Override
            public void failure(int statusCode) {

            }
        });
    }

}
