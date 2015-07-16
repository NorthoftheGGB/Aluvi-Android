package com.aluvi.android.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluvi.android.R;

public class TicketInfoFragment extends BaseButterFragment
{

    public static TicketInfoFragment newInstance()
    {
        return new TicketInfoFragment();
    }

    public TicketInfoFragment()
    {
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_ticket_info, container, false);
    }

    @Override
    public void initUI()
    {

    }
}