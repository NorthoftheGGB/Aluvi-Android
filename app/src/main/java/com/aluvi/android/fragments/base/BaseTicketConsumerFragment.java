package com.aluvi.android.fragments.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.model.realm.Ticket;

/**
 * Created by usama on 8/2/15.
 */
public abstract class BaseTicketConsumerFragment extends BaseButterFragment {
    private final static String TICKET_ID_KEY = "ticketId";
    private Ticket mTicket;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        int ticketId = 0;
        if (getArguments() != null)
            ticketId = getArguments().getInt(TICKET_ID_KEY);
        else if (savedInstanceState != null)
            ticketId = savedInstanceState.getInt(TICKET_ID_KEY);

        mTicket = AluviRealm.getDefaultRealm().where(Ticket.class).equalTo("id", ticketId).findFirst();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mTicket != null)
            outState.putInt(TICKET_ID_KEY, mTicket.getId());
    }

    public void saveTicket(Ticket ticket) {
        Bundle args = new Bundle();
        args.putInt(TICKET_ID_KEY, ticket.getId());
        setArguments(args);
    }

    public void updateTicket(Ticket ticket) {
        mTicket = ticket;
    }

    public Ticket getTicket() {
        return mTicket;
    }
}
