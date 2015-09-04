package com.aluvi.android.helpers.eventBus;

import com.aluvi.android.model.realm.Ticket;

/**
 * Created by usama on 9/3/15.
 */
public class LocalRefreshTicketsEvent {
    private Ticket mActiveTicket;

    public LocalRefreshTicketsEvent(Ticket mActiveTicket) {
        this.mActiveTicket = mActiveTicket;
    }

    public Ticket getActiveTicket() {
        return mActiveTicket;
    }

    public void setActiveTicket(Ticket mActiveTicket) {
        this.mActiveTicket = mActiveTicket;
    }
}
