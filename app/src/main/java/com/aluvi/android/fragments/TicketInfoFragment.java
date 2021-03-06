package com.aluvi.android.fragments;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aluvi.android.R;
import com.aluvi.android.fragments.base.BaseTicketConsumerFragment;
import com.aluvi.android.helpers.CurrencyUtils;
import com.aluvi.android.helpers.eventBus.CommuteScheduledEvent;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.callbacks.Callback;
import com.aluvi.android.model.realm.Car;
import com.aluvi.android.model.realm.Profile;
import com.aluvi.android.model.realm.Rider;
import com.aluvi.android.model.realm.Ticket;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.realm.RealmList;

public class TicketInfoFragment extends BaseTicketConsumerFragment {
    public interface TicketInfoListener {
        void onTicketInfoUIMeasured(int headerHeight, int panelHeight);

        void onRiderStateChanged();
    }

    @Bind(R.id.ticket_info_text_view_price_info) TextView mTicketPriceTextView;
    @Bind(R.id.ticket_info_text_view_car_name) TextView mCarNameTextView;
    @Bind(R.id.ticket_info_text_view_car_license_number) TextView mCarLicenseNumberTextView;
    @Bind(R.id.ticket_info_text_view_driver_name) TextView mDriverNameTextView;
    @Bind(R.id.ticket_info_image_view_driver_profile) ImageView mDriverProfileImageView;
    @Bind(R.id.ticket_info_container_rider_info) LinearLayout mRiderProfilePictureContainer;
    @Bind(R.id.ticket_info_riders_picked_up) Button mRidersPickedUpButton;
    @Bind(R.id.ticket_info_relative_layout_driver_info) View mDriverInfoView;
    @Bind({R.id.ticket_info_riders_picked_up}) List<View> mDriverViews;

    private TicketInfoListener mListener;
    private Dialog mDefaultProgressDialog;

    public static TicketInfoFragment newInstance(Ticket ticket) {
        TicketInfoFragment infoFragment = new TicketInfoFragment();
        infoFragment.saveTicket(ticket);
        return infoFragment;
    }

    public static TicketInfoFragment newInstance() {
        return new TicketInfoFragment();
    }

    public TicketInfoFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() != null) {
            mListener = (TicketInfoListener) getParentFragment();
        } else {
            mListener = (TicketInfoListener) context;
        }
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ticket_info, container, false);
    }

    @Override
    public void initUI() {
        if (getTicket() != null) {
            int price = getTicket().isDriving() ? getTicket().getEstimatedEarnings() : getTicket().getFixedPrice();
            mTicketPriceTextView.setText(CurrencyUtils.getFormattedDollars(price));

            Car car = getTicket().getCar();
            if (car != null) {
                String carInfo = car.getMake();
                carInfo += car.getColor() != null && !"".equals(car.getColor()) ? ", " + car.getColor() : "";
                carInfo += car.getModel() != null && !"".equals(car.getModel()) ? ", " + car.getModel() : "";

                mCarNameTextView.setText(carInfo);
                mCarLicenseNumberTextView.setText(getTicket().getCar().getLicensePlate());
            }

            if (getTicket().getDriver() != null)
                mDriverNameTextView.setText(getTicket().getDriver().getFirstName());

            if (getTicket().isDriving()) {
                mDriverInfoView.setVisibility(View.GONE);
                updateRidersPickedUpButton();
                ButterKnife.apply(mDriverViews, VISIBILITY_ACTION);
            } else {
                mDriverInfoView.setVisibility(View.VISIBLE);

                ButterKnife.apply(mDriverViews, INVISIBILITY_ACTION);
                loadProfilePicture(getTicket().getDriver().getSmallImageUrl(), mDriverProfileImageView);
            }

            initRidersUI();
            getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (getView() != null) {
                        getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        if (mListener != null)
                            mListener.onTicketInfoUIMeasured(mTicketPriceTextView.getHeight(), getView().getHeight());
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);

        if (mDefaultProgressDialog != null) {
            mDefaultProgressDialog.cancel();
            mDefaultProgressDialog = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(CommuteScheduledEvent event) {
        updateTicket(event.getActiveTicket());
        initUI();
    }

    private void initRidersUI() {
        mRiderProfilePictureContainer.removeAllViews();

        RealmList<Rider> riders = getTicket().getRiders();
        if (riders != null)
            for (Rider rider : riders)
                if (rider.getId() != getTicket().getDriver().getId())
                    addRider(rider);

    }

    private void addRider(Rider rider) {
        addRider(rider.getFirstName(), rider.getSmallImageUrl(), rider.getPhone());
    }

    private void addRider(String firstName, String profilePictureUrl, final String phoneNumber) {
        View riderInfoView = View.inflate(getActivity(), R.layout.layout_rider_information, null);
        ImageView riderProfileImageView = (ImageView) riderInfoView.findViewById(R.id.rider_information_image_view_profile);
        TextView riderNameTextView = (TextView) riderInfoView.findViewById(R.id.rider_information_text_view_name);

        riderNameTextView.setText(firstName);
        loadProfilePicture(profilePictureUrl, riderProfileImageView);
        riderProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCallTextOptions(phoneNumber);
            }
        });

        mRiderProfilePictureContainer.addView(riderInfoView);
    }

    private void loadProfilePicture(String url, ImageView imageView) {
        Picasso.with(getActivity()).load(url)
                .placeholder(R.mipmap.profile_picture_placeholder)
                .error(R.mipmap.profile_picture_placeholder)
                .fit()
                .centerCrop()
                .into(imageView);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.ticket_info_image_view_driver_profile)
    public void onDriveProfilePictureClicked() {
        showCallTextOptions(getDriverPhoneNumber());
    }

    private void showCallTextOptions(final String phoneNumber) {
        final int CALL_POS = 0, TEXT_POS = 1;
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.contact_driver)
                .setItems(R.array.contact_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case CALL_POS:
                                callNumber(phoneNumber);
                                break;
                            case TEXT_POS:
                                textNumber(phoneNumber);
                                break;
                        }
                    }
                })
                .show();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.ticket_info_riders_picked_up)
    public void onRidersPickedUpButtonClicked() {
        mDefaultProgressDialog = DialogUtils.showDefaultProgressDialog(getActivity(), false);

        if (!isRideInProgress())
            CommuteManager.getInstance().ridersPickedUp(getTicket(), ridersStatusUpdatedCallback);
        else
            CommuteManager.getInstance().ridersDroppedOff(getTicket(), ridersStatusUpdatedCallback);
    }

    private Callback ridersStatusUpdatedCallback = new Callback() {
        @Override
        public void success() {
            if (mDefaultProgressDialog != null)
                mDefaultProgressDialog.cancel();

            mListener.onRiderStateChanged();
            updateRidersPickedUpButton();
        }

        @Override
        public void failure(String message) {
            if (mDefaultProgressDialog != null)
                mDefaultProgressDialog.cancel();

            if (getActivity() != null)
                Snackbar.make(getView(), R.string.unable_rider_picked_up, Snackbar.LENGTH_SHORT).show();
        }
    };

    private void updateRidersPickedUpButton() {
        int statusText = isRideInProgress() ? R.string.riders_dropped_off : R.string.riders_picked_up;
        mRidersPickedUpButton.setText(statusText);
    }

    private boolean isRideInProgress() {
        return getTicket().getState().equals(Ticket.STATE_IN_PROGRESS) ||
                getTicket().getState().equals(Ticket.STATE_STARTED);
    }

    private void textNumber(String phoneNumber) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", phoneNumber);
        startActivity(smsIntent);
    }

    private boolean hasPhonePermission() {
        final PackageManager packageManager = getActivity().getPackageManager();
        return packageManager.checkPermission(Manifest.permission.CALL_PHONE,
                getActivity().getPackageName()) == PackageManager.PERMISSION_GRANTED;
    }

    private void callNumber(String phoneNumber) {
        if(!hasPhonePermission()){
            Snackbar.make(getView(), R.string.phone_permission_missing, Snackbar.LENGTH_SHORT).show();
            return;
        }

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
}