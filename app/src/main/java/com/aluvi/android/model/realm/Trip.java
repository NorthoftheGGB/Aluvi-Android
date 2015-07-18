package com.aluvi.android.model.realm;

import com.aluvi.android.model.RealmHelper;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/17/15.
 */
public class Trip extends RealmObject {

    private int tripId;

    private RealmList<Ticket> tickets;


    public static void removeIfEmpty(Trip trip) {
        if(trip.getTickets().size() == 0){
            RealmHelper.removeFromRealm(trip);
        }
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public RealmList<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(RealmList<Ticket> tickets) {
        this.tickets = tickets;
    }

}
