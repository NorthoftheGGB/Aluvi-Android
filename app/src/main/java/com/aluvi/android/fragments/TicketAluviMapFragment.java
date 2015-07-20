package com.aluvi.android.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.aluvi.android.R;

public class TicketAluviMapFragment extends AluviMapFragment
{
    public TicketAluviMapFragment()
    {
    }

    public static TicketAluviMapFragment newInstance()
    {
        return new TicketAluviMapFragment();
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_ticket_map, container, false);
    }

    @Override
    public void initUI()
    {
        super.initUI();

        getChildFragmentManager().beginTransaction().replace(R.id.map_sliding_panel_container,
                TicketInfoFragment.newInstance()).commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        menu.clear();
        inflater.inflate(R.menu.menu_ticket, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void cancelTicket()
    {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_cancel_ticket)
        {
            cancelTicket();
        }

        return super.onOptionsItemSelected(item);
    }
}
