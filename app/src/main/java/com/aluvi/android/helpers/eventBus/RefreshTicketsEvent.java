package com.aluvi.android.helpers.eventBus;

import com.aluvi.android.model.local.TicketStateTransition;
import com.aluvi.android.model.realm.Ticket;

import java.util.List;

import io.realm.RealmResults;

/**
 * Created by usama on 9/3/15.
 */
public class RefreshTicketsEvent {
    private RealmResults<Ticket> mTickets;
    private Ticket mActiveTicket;
    private List<TicketStateTransition> mTicketStateTransitions;

    public RefreshTicketsEvent(RealmResults<Ticket> tickets, List<TicketStateTransition> stateTransitions, Ticket activeTicket) {
        this.mTickets = tickets;
        this.mTicketStateTransitions = stateTransitions;
        this.mActiveTicket = activeTicket;
    }

    public RealmResults<Ticket> getTickets() {
        return mTickets;
    }

    public void setTickets(RealmResults<Ticket> mTickets) {
        this.mTickets = mTickets;
    }

    public Ticket getActiveTicket() {
        return mActiveTicket;
    }

    public void setActiveTicket(Ticket mActiveTicket) {
        this.mActiveTicket = mActiveTicket;
    }

    public List<TicketStateTransition> getTicketStateTransitions() {
        return mTicketStateTransitions;
    }

    public void setTicketStateTransitions(List<TicketStateTransition> mTicketStateTransitions) {
        this.mTicketStateTransitions = mTicketStateTransitions;
    }
}
