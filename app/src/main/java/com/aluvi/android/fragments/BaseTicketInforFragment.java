package com.aluvi.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluvi.android.R;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.model.realm.Ticket;

/**
 * Created by usama on 8/2/15.
 */
public class BaseTicketInforFragment extends BaseButterFragment {
    public interface OnTicketInfoLayoutListener {
        void onTicketInfoUIMeasured(int headerHeight, int panelHeight);
    }

    private final static String TICKET_ID_KEY = "ticketId";
    private Ticket mTicket;
    private OnTicketInfoLayoutListener mListener;

    public static TicketInfoFragment newInstance(Ticket ticket) {
        Bundle bundle = new Bundle();
        bundle.putInt(TICKET_ID_KEY, ticket.getId());

        TicketInfoFragment infoFragment = new TicketInfoFragment();
        infoFragment.setArguments(bundle);
        return infoFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getParentFragment() != null) {
            mListener = (OnTicketInfoLayoutListener) getParentFragment();
        } else {
            mListener = (OnTicketInfoLayoutListener) activity;
        }
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            int ticketId = getArguments().getInt(TICKET_ID_KEY);
            mTicket = AluviRealm.getDefaultRealm().where(Ticket.class).equalTo("id", ticketId).findFirst();
        }

        return inflater.inflate(R.layout.fragment_ticket_info, container, false);
    }

    @Override
    public void initUI() {

    }
}
