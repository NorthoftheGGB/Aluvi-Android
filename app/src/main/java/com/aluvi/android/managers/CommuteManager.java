package com.aluvi.android.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.aluvi.android.api.tickets.CommuterTicketsResponse;
import com.aluvi.android.api.tickets.RequestCommuterTicketsCallback;
import com.aluvi.android.api.tickets.TicketsApi;
import com.aluvi.android.application.AluviPreferences;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.exceptions.UserRecoverableSystemError;
import com.aluvi.android.model.RealmHelper;
import com.aluvi.android.model.local.TicketLocation;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.api.ApiCallback;
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
        public void success();

        public void failure(String message);
    }

    private static CommuteManager mInstance;

    private SharedPreferences preferences;
    private Context ctx;

    private float homeLatitude;
    private float homeLongitude;
    private float workLatitude;
    private float workLongitude;
    private String homePlaceName;
    private String workPlaceName;
    private String pickupTime;
    private String returnTime;
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


    public double getHomeLatitude() {
        return homeLatitude;
    }

    public void setHomeLatitude(float homeLatitude) {
        this.homeLatitude = homeLatitude;
    }

    public double getHomeLongitude() {
        return homeLongitude;
    }

    public void setHomeLongitude(float homeLongitude) {
        this.homeLongitude = homeLongitude;
    }

    public double getWorkLatitude() {
        return workLatitude;
    }

    public void setWorkLatitude(float workLatitude) {
        this.workLatitude = workLatitude;
    }

    public double getWorkLongitude() {
        return workLongitude;
    }

    public void setWorkLongitude(float workLongitude) {
        this.workLongitude = workLongitude;
    }

    public String getHomePlaceName() {
        return homePlaceName;
    }

    public void setHomePlaceName(String homePlaceName) {
        this.homePlaceName = homePlaceName;
    }

    public String getWorkPlaceName() {
        return workPlaceName;
    }

    public void setWorkPlaceName(String workPlaceName) {
        this.workPlaceName = workPlaceName;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    public String getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(String returnTime) {
        this.returnTime = returnTime;
    }

    public boolean isDriving() {
        return driving;
    }

    public void setDriving(boolean driving) {
        this.driving = driving;
    }

    private TicketLocation getHomeLocation(){
        TicketLocation ticketLocation = new TicketLocation(homeLatitude, homeLongitude, homePlaceName);
        return ticketLocation;
    }

    private TicketLocation getWorkLocation(){
        TicketLocation ticketLocation = new TicketLocation(workLatitude, workLongitude, workPlaceName);
        return ticketLocation;
    }

    public void setHomeLocation(TicketLocation homeLocation){
        homeLatitude = homeLocation.getLatitude();
        homeLongitude = homeLocation.getLongitude();
        homePlaceName = homeLocation.getPlaceName();
    }

    public void setWorkLocation(TicketLocation workLocation){
        workLatitude = workLocation.getLatitude();
        workLongitude = workLocation.getLongitude();
        workPlaceName = workLocation.getPlaceName();
    }


    private void load(){
        homeLatitude = preferences.getFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, 0);
        homeLongitude = preferences.getFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, 0);
        workLatitude = preferences.getFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, 0);
        workLongitude = preferences.getFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, 0);
        homePlaceName = preferences.getString(AluviPreferences.COMMUTER_HOME_PLACENAME_KEY, "");
        workPlaceName = preferences.getString(AluviPreferences.COMMUTER_WORK_PLACENAME_KEY, "");
        pickupTime = preferences.getString(AluviPreferences.COMMUTER_PICKUP_TIME_KEY, "");
        returnTime = preferences.getString(AluviPreferences.COMMUTER_RETURN_TIME_KEY, "");
        driving = preferences.getBoolean(AluviPreferences.COMMUTER_IS_DRIVER_KEY, false);
    }

    public void loadFromServer(){
        // routes API
        // implement RoutesApi library file
    }

    private void store(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, homeLatitude);
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, homeLongitude);
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, workLatitude);
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, workLongitude);
        editor.putString(AluviPreferences.COMMUTER_HOME_PLACENAME_KEY, homePlaceName);
        editor.putString(AluviPreferences.COMMUTER_WORK_PLACENAME_KEY, workPlaceName);
        editor.putString(AluviPreferences.COMMUTER_PICKUP_TIME_KEY, pickupTime);
        editor.putString(AluviPreferences.COMMUTER_RETURN_TIME_KEY, returnTime);
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
        editor.putString(AluviPreferences.COMMUTER_PICKUP_TIME_KEY, "");
        editor.putString(AluviPreferences.COMMUTER_RETURN_TIME_KEY, "");
        editor.putBoolean(AluviPreferences.COMMUTER_IS_DRIVER_KEY, false);
        editor.commit();

        homeLatitude = 0;
        homeLongitude = 0;
        workLatitude = 0;
        workLongitude = 0;
        homePlaceName = "";
        workPlaceName = "";
        pickupTime = "";
        returnTime = "";
        driving = false;
    }

    public boolean routeIsSet() {
        if(homeLatitude == 0 || homeLongitude == 0 || workLatitude == 0 || workLongitude == 0 || pickupTime == null || returnTime == null){
            return false;
        } else {
            return true;
        }
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
        Ticket.buildNewTicket(toWorkTicket, rideDate, getHomeLocation(), getWorkLocation(), driving, pickupTime);
        final Ticket fromWorkTicket = realm.createObject(Ticket.class);
        Ticket.buildNewTicket(fromWorkTicket, rideDate, getWorkLocation(), getHomeLocation(), driving, returnTime);
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

    public void cancelTicket(final Ticket ticket, final Callback callback){

        if(ticket.getRideId() != 0) {

            if (ticket.isDriving()) {
                // Driver Api - cancel driving

            } else {

                if(ticket.getHovFare() == null) {
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


    public void cancelTrip(final Trip trip, final Callback callback){
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

}
