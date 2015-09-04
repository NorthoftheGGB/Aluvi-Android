package com.aluvi.android.helpers.eventBus;

import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;

/**
 * Created by usama on 9/3/15.
 */
public class CommuteScheduledEvent {
    private Trip mScheduledTrip;
    private Ticket mActiveTicket;

    public CommuteScheduledEvent(Trip mScheduledTrip, Ticket activeTicket) {
        this.mScheduledTrip = mScheduledTrip;
        this.mActiveTicket = activeTicket;
    }

    public Trip getScheduledTrip() {
        return mScheduledTrip;
    }

    public void setScheduledTrip(Trip mScheduledTrip) {
        this.mScheduledTrip = mScheduledTrip;
    }

    public Ticket getActiveTicket() {
        return mActiveTicket;
    }

    public void setActiveTicket(Ticket mActiveTicket) {
        this.mActiveTicket = mActiveTicket;
    }
}
