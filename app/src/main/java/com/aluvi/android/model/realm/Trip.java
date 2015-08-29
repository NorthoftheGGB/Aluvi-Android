package com.aluvi.android.model.realm;

import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.model.RealmHelper;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/17/15.
 */
public class Trip extends RealmObject {
    private int tripId;
    private String tripState;

    public static final String STATE_REQUESTED = "requested";

    private RealmList<Ticket> tickets = new RealmList<>();

    public static void removeIfEmpty(Trip trip) {
        if (trip.getTickets().size() == 0) {
            RealmHelper.removeFromRealm(trip);
        }
    }

    public static void removeTickets(Trip trip) {
        Realm realm = AluviRealm.getDefaultRealm();
        realm.beginTransaction();
        trip.getTickets().where().findAll().clear();
        realm.commitTransaction();
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

    public String getTripState() {
        return tripState;
    }

    public void setTripState(String tripState) {
        this.tripState = tripState;
    }
}
