package com.aluvi.android.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.fragments.base.BaseTicketConsumerFragment;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.managers.packages.Callback;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.model.realm.Rider;
import com.aluvi.android.model.realm.Ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.RealmList;

public class TicketInfoFragment extends BaseTicketConsumerFragment {
    public interface OnTicketInfoLayoutListener {
        void onTicketInfoUIMeasured(int headerHeight, int panelHeight);
    }

    @Bind(R.id.ticket_info_text_view_price_info) TextView mTicketPriceTextView;
    @Bind(R.id.ticket_info_text_view_car_name) TextView mCarNameTextView;
    @Bind(R.id.ticket_info_text_view_car_license_number) TextView mCarLicenseNumberTextView;
    @Bind(R.id.ticket_info_text_view_driver_name) TextView mDriverNameTextView;
    @Bind(R.id.ticket_info_image_view_driver_profile) ImageView mDriverProfileImageView;
    @Bind(R.id.ticket_info_container_rider_info) LinearLayout mRiderProfilePictureContainer;
    @Bind(R.id.ticket_info_riders_picked_up) Button mRidersPickedUpButton;
    @Bind({R.id.ticket_info_relative_layout_driver_info, R.id.ticket_info_late_button}) List<View> mRiderViews;
    @Bind({R.id.ticket_info_riders_picked_up}) List<View> mDriverViews;

    private OnTicketInfoLayoutListener mListener;

    public static TicketInfoFragment newInstance(Ticket ticket) {
        TicketInfoFragment infoFragment = new TicketInfoFragment();
        infoFragment.saveTicket(ticket);
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
        return inflater.inflate(R.layout.fragment_ticket_info, container, false);
    }

    @Override
    public void initUI() {
        int price = getTicket().isDriving() ? getTicket().getEstimatedEarnings() : getTicket().getFixedPrice();
        mTicketPriceTextView.setText(getFormattedDollars(price));

        if (getTicket().getCar() != null) {
            mCarNameTextView.setText(getTicket().getCar().getMake());
            mCarLicenseNumberTextView.setText(getTicket().getCar().getLicensePlate());
        }

        if (getTicket().getDriver() != null)
            mDriverNameTextView.setText(getTicket().getDriver().getFirstName());

        if (getTicket().isDriving()) {
            ButterKnife.apply(mRiderViews, INVISIBILITY_ACTION);
            updateRidersPickedUpButton();
        } else {
            ButterKnife.apply(mDriverViews, INVISIBILITY_ACTION);
        }

        initRidersUI();
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
        RealmList<Rider> riders = getTicket().getRiders();
        if (riders != null) {
            for (Rider rider : riders) {
                if (rider.getId() != getTicket().getDriver().getId())
                    addRider(rider);
            }
        }
    }

    private void addRider(Rider rider) {
        View riderInfoView = View.inflate(getActivity(), R.layout.layout_rider_information, mRiderProfilePictureContainer);
        ImageView riderProfileImageView = (ImageView) riderInfoView.findViewById(R.id.rider_information_image_view_profile);
        TextView riderNameTextView = (TextView) riderInfoView.findViewById(R.id.rider_information_text_view_name);
        riderNameTextView.setText(rider.getFirstName());

        if (getTicket().isDriving()) {
            riderProfileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCallTextOptions();
                }
            });
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.ticket_info_image_view_driver_profile)
    public void onDriveProfilePictureClicked() {
        showCallTextOptions();
    }

    private void showCallTextOptions() {
        final int CALL_POS = 0, TEXT_POS = 1;
        new MaterialDialog.Builder(getActivity())
                .title(R.string.contact_driver)
                .items(R.array.contact_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        switch (i) {
                            case CALL_POS:
                                callNumber(getDriverPhoneNumber());
                                break;
                            case TEXT_POS:
                                textNumber(getDriverPhoneNumber());
                                break;
                        }
                    }
                })
                .show();
    }

    private Dialog riderStatusProgressDialog;

    @SuppressWarnings("unused")
    @OnClick(R.id.ticket_info_riders_picked_up)
    public void onRidersPickedUpButtonClicked() {
        riderStatusProgressDialog = DialogUtils.getDefaultProgressDialog(getActivity(), false);

        if (!isRideInProgress())
            CommuteManager.getInstance().ridersPickedUp(getTicket(), ridersStatusUpdatedCallback);
        else
            CommuteManager.getInstance().ridersDroppedOff(getTicket(), ridersStatusUpdatedCallback);
    }

    private Callback ridersStatusUpdatedCallback = new Callback() {
        @Override
        public void success() {
            if (riderStatusProgressDialog != null)
                riderStatusProgressDialog.cancel();

            updateRidersPickedUpButton();
        }

        @Override
        public void failure(String message) {
            if (riderStatusProgressDialog != null)
                riderStatusProgressDialog.cancel();

            if (getActivity() != null)
                Snackbar.make(getView(), R.string.unable_rider_picked_up, Snackbar.LENGTH_SHORT).show();
        }
    };

    private void updateRidersPickedUpButton() {
        int statusText = isRideInProgress() ? R.string.riders_dropped_off : R.string.riders_picked_up;
        mRidersPickedUpButton.setText(statusText);
    }

    private boolean isRideInProgress() {
        return getTicket().getState().equals(Ticket.StateInProgress) ||
                getTicket().getState().equals(Ticket.StateStarted);
    }

    private void textNumber(String phoneNumber) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", phoneNumber);
        startActivity(smsIntent);
    }

    private void callNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @Nullable
    private String getDriverPhoneNumber() {
        return getTicket().getDriver() != null ? getTicket().getDriver().getPhone() : null;
    }

    private final ButterKnife.Action INVISIBILITY_ACTION = new ButterKnife.Action() {
        @Override
        public void apply(View view, int index) {
            view.setVisibility(View.GONE);
        }
    };

    private final ButterKnife.Action VISIBILITY_ACTION = new ButterKnife.Action() {
        @Override
        public void apply(View view, int index) {
            view.setVisibility(View.VISIBLE);
        }
    };

    private String getFormattedDollars(int cents) {
        BigDecimal displayDecimal = new BigDecimal(cents)
                .divide(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_EVEN);
        return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(displayDecimal.doubleValue());
    }
}