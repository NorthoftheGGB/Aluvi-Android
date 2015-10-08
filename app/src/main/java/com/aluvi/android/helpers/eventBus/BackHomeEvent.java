package com.aluvi.android.helpers.eventBus;

import com.aluvi.android.model.realm.Ticket;

/**
 * Created by usama on 9/17/15.
 */
public class BackHomeEvent {
    private Ticket mActiveTicket;

    public BackHomeEvent(Ticket ticket) {
        mActiveTicket = ticket;
    }

    public Ticket getActiveTicket() {
        return mActiveTicket;
    }

    public void setActiveTicket(Ticket mActiveTicket) {
        this.mActiveTicket = mActiveTicket;
    }
}
