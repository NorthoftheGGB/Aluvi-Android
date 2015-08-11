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
        if (getArguments() != null) {
            int ticketId = getArguments().getInt(TICKET_ID_KEY);
            mTicket = AluviRealm.getDefaultRealm().where(Ticket.class).equalTo("id", ticketId).findFirst();
        }

        super.onActivityCreated(savedInstanceState);
    }

    public void saveTicket(Ticket ticket) {
        Bundle args = new Bundle();
        args.putInt(TICKET_ID_KEY, ticket.getId());
        setArguments(args);
    }

    public Ticket getTicket() {
        return mTicket;
    }
}
