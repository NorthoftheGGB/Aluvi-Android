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

    private RealmList<Ticket> tickets;

    public static void removeIfEmpty(Trip trip) {
        if (trip.getTickets().size() == 0) {
            RealmHelper.removeFromRealm(trip);
        }
    }

    public static void removeTickets(Trip trip) {
        Realm realm = AluviRealm.getDefaultRealm();
        realm.beginTransaction();

        if (trip.getTickets() != null)
            for (Ticket ticket : trip.getTickets()) {
                ticket.removeFromRealm();
            }

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
}
