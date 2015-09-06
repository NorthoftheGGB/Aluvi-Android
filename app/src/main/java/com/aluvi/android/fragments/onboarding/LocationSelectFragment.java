package com.aluvi.android.fragments.onboarding;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.aluvi.android.R;
import com.aluvi.android.api.users.models.ProfileData;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.fragments.gis.LocationSelectDialogFragment;
import com.aluvi.android.model.local.TicketLocation;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by usama on 8/6/15.
 */
public class LocationSelectFragment extends BaseButterFragment
        implements LocationSelectDialogFragment.OnLocationSelectedListener {

    public interface LocationSelectedListener {
        void onLocationSelected(TicketLocation start, TicketLocation end, ProfileData data);
    }

    @Bind(R.id.onboarding_checkbox_interested_driver) CheckBox mInterestedInDrivingCheckbox;
    @Bind(R.id.onboarding_button_home_location) Button mHomeButton;
    @Bind(R.id.onboarding_button_work_location) Button mWorkButton;

    private final static String HOME_LOC = "home_location", WORK_LOC = "work_location";

    private TicketLocation mHomeLocation, mWorkLocation;
    private LocationSelectedListener mListener;

    public static LocationSelectFragment newInstance(TicketLocation homeLoc, TicketLocation workLoc) {
        Bundle args = new Bundle();
        args.putParcelable(HOME_LOC, homeLoc);
        args.putParcelable(WORK_LOC, workLoc);

        LocationSelectFragment locationSelectFragment = new LocationSelectFragment();
        locationSelectFragment.setArguments(args);
        return locationSelectFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (LocationSelectedListener) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mHomeLocation = getArguments().getParcelable(HOME_LOC);
            mWorkLocation = getArguments().getParcelable(WORK_LOC);
        }

        if (savedInstanceState != null) {
            mHomeLocation = savedInstanceState.getParcelable(HOME_LOC);
            mWorkLocation = savedInstanceState.getParcelable(WORK_LOC);
        }
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_location_select, container, false);
    }

    @Override
    public void initUI() {
        updateHomeButton();
        updateWorkButton();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(HOME_LOC, mHomeLocation);
        outState.putParcelable(WORK_LOC, mWorkLocation);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.onboarding_button_home_location)
    public void onHomeLocationButtonClicked() {
        LocationSelectDialogFragment.newInstance(false).show(getChildFragmentManager(), HOME_LOC);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.onboarding_button_work_location)
    public void onWorkLocationButtonClicked() {
        LocationSelectDialogFragment.newInstance(false).show(getChildFragmentManager(), WORK_LOC);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.onboarding_location_next_button)
    public void nextButtonClicked() {
        if (mHomeLocation != null && mWorkLocation != null) {
            ProfileData data = new ProfileData();
            data.setIsInterestedDriver(mInterestedInDrivingCheckbox.isChecked());
            mListener.onLocationSelected(mHomeLocation, mWorkLocation, data);
        } else {
            Snackbar.make(getView(), R.string.location_select_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationSelected(TicketLocation address, LocationSelectDialogFragment fragment) {
        if (address != null) {
            if (fragment.getTag().equals(HOME_LOC)) {
                mHomeLocation = address;
                updateHomeButton();
            } else {
                mWorkLocation = address;
                updateWorkButton();
            }
        }
    }

    private void updateHomeButton() {
        if (mHomeLocation != null)
            mHomeButton.setText(mHomeLocation.getPlaceName());
    }

    private void updateWorkButton() {
        if (mWorkLocation != null)
            mWorkButton.setText(mWorkLocation.getPlaceName());
    }
}
