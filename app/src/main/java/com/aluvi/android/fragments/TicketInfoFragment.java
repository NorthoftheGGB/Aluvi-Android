package com.aluvi.android.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aluvi.android.R;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.model.realm.Rider;
import com.aluvi.android.model.realm.Ticket;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;

import butterknife.Bind;
import io.realm.RealmList;

public class TicketInfoFragment extends BaseButterFragment {

    @Bind(R.id.ticket_info_text_view_price_info) TextView mTicketPriceTextView;
    @Bind(R.id.ticket_info_text_view_car_name) TextView mCarNameTextView;
    @Bind(R.id.ticket_info_text_view_car_license_number) TextView mCarLicenseNumberTextView;
    @Bind(R.id.ticket_info_text_view_driver_name) TextView mDriverNameTextView;
    @Bind(R.id.ticket_info_image_view_driver_profile) ImageView mDriverProfileImageView;
    @Bind(R.id.ticket_info_container_rider_info) LinearLayout mRiderProfilePictureContainer;

    private final static String TICKET_ID_KEY = "ticketId";
    private Ticket mTicket;

    public static TicketInfoFragment newInstance(Ticket ticket) {
        Bundle bundle = new Bundle();
        bundle.putInt(TICKET_ID_KEY, ticket.getId());

        TicketInfoFragment infoFragment = new TicketInfoFragment();
        infoFragment.mTicket = ticket;
        infoFragment.setArguments(bundle);
        return infoFragment;
    }

    public TicketInfoFragment() {
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            int ticketId = getArguments().getInt(TICKET_ID_KEY);
            mTicket = AluviRealm.getDefaultRealm().where(Ticket.class).equalTo("rideId", ticketId).findFirst();
        }

        return inflater.inflate(R.layout.fragment_ticket_info, container, false);
    }

    @Override
    public void initUI() {
        if (mTicket != null) {
            mTicketPriceTextView.setText(NumberFormat.getCurrencyInstance().format(mTicket.getFixedPrice()));

            if (mTicket.getCar() != null) {
                mCarNameTextView.setText(mTicket.getCar().getMake());
                mCarLicenseNumberTextView.setText(mTicket.getCar().getLicensePlate());
            }

            if (mTicket.getDriver() != null) {
                mDriverNameTextView.setText(mTicket.getDriver().getFirstName());
                Picasso.with(getActivity()).load(mTicket.getDriver()
                        .getSmallImageUrl()).fit().centerCrop().into(mDriverProfileImageView);
            }

            initRidersUI();
        }
    }

    private void initRidersUI() {
        if (mTicket.getState().equals(Ticket.StateScheduled)) {
            RealmList<Rider> riders = mTicket.getRiders();
            if (riders != null) {
                for (Rider rider : riders) {
                    addRider(rider);
                }
            }
        }
    }

    private void addRider(Rider rider) {
        View riderInfoView = View.inflate(getActivity(), R.layout.layout_rider_information, null);
        ImageView riderProfileImageView = (ImageView) riderInfoView.findViewById(R.id.rider_information_image_view_profile);
        TextView riderNameTextView = (TextView) riderInfoView.findViewById(R.id.rider_information_text_view_name);

        Picasso.with(getActivity()).load(rider.getSmallImageUrl())
                .fit().centerCrop().into(riderProfileImageView);
        riderNameTextView.setText(rider.getFirstName());
        mRiderProfilePictureContainer.addView(riderInfoView);
    }
}