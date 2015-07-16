package com.aluvi.android.api.tickets;

import com.aluvi.android.model.realm.Ticket;

/**
 * Created by matthewxi on 7/16/15.
 */
abstract public class RequestCommuterTicketsCallback {
    Ticket toWorkTicket;
    Ticket fromWorkTicket;

    public RequestCommuterTicketsCallback(Ticket toWorkTicket, Ticket fromWorkTicket) {
        this.toWorkTicket = toWorkTicket;
        this.fromWorkTicket = fromWorkTicket;
    }

    abstract public void success(CommuterTicketsResponse response);
    abstract public void failure(int statusCode);
}
