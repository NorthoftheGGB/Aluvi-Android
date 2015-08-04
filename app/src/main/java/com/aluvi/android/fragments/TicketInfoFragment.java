package com.aluvi.android.fragments;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.model.realm.Rider;
import com.aluvi.android.model.realm.Ticket;

import java.text.NumberFormat;

import butterknife.Bind;
import butterknife.OnClick;
import io.realm.RealmList;

public class TicketInfoFragment extends BaseButterFragment {

    public interface OnTicketInfoLayoutListener {
        void onTicketInfoUIMeasured(int headerHeight, int panelHeight);
    }

    @Bind(R.id.ticket_info_text_view_price_info) TextView mTicketPriceTextView;
    @Bind(R.id.ticket_info_text_view_car_name) TextView mCarNameTextView;
    @Bind(R.id.ticket_info_text_view_car_license_number) TextView mCarLicenseNumberTextView;
    @Bind(R.id.ticket_info_text_view_driver_name) TextView mDriverNameTextView;
    @Bind(R.id.ticket_info_image_view_driver_profile) ImageView mDriverProfileImageView;
    @Bind(R.id.ticket_info_container_rider_info) LinearLayout mRiderProfilePictureContainer;

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

    public TicketInfoFragment() {
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
        if (mTicket != null) {
            mTicketPriceTextView.setText(NumberFormat.getCurrencyInstance().format(mTicket.getFixedPrice()));

            if (mTicket.getCar() != null) {
                mCarNameTextView.setText(mTicket.getCar().getMake());
                mCarLicenseNumberTextView.setText(mTicket.getCar().getLicensePlate());
            }

            if (mTicket.getDriver() != null) {
                mDriverNameTextView.setText(mTicket.getDriver().getFirstName());
            }

            initRidersUI();
        }

        getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (mListener != null)
                    mListener.onTicketInfoUIMeasured(mTicketPriceTextView.getHeight(), getView().getHeight());
            }
        });
    }

    private void initRidersUI() {
        RealmList<Rider> riders = mTicket.getRiders();
        if (riders != null) {
            for (Rider rider : riders) {
                if (rider.getId() != mTicket.getDriver().getId())
                    addRider(rider);
            }
        }
    }

    private void addRider(Rider rider) {
        View riderInfoView = View.inflate(getActivity(), R.layout.layout_rider_information, mRiderProfilePictureContainer);
        ImageView riderProfileImageView = (ImageView) riderInfoView.findViewById(R.id.rider_information_image_view_profile);
        TextView riderNameTextView = (TextView) riderInfoView.findViewById(R.id.rider_information_text_view_name);
        riderNameTextView.setText(rider.getFirstName());
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.ticket_info_image_view_driver_profile)
    public void onDriveProfilePictureClicked() {
        final int CALL_POS = 0, TEXT_POS = 1;

        new MaterialDialog.Builder(getActivity())
                .title(R.string.contact_driver)
                .items(R.array.contact_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        switch (i) {
                            case CALL_POS:
                                callDriver(getDriverPhoneNumber());
                                break;
                            case TEXT_POS:
                                textDriver(getDriverPhoneNumber());
                                break;
                        }
                    }
                })
                .show();
    }

    private void textDriver(String phoneNumber) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", phoneNumber);
        startActivity(smsIntent);
    }

    private void callDriver(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @Nullable
    private String getDriverPhoneNumber() {
        return mTicket.getDriver() != null ? mTicket.getDriver().getPhone() : null;
    }
}