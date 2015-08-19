package com.aluvi.android.fragments.onboarding;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluvi.android.R;
import com.aluvi.android.api.users.models.DriverProfileData;
import com.aluvi.android.fragments.base.BaseButterFragment;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by usama on 8/8/15.
 */
public class DriverRegistrationFragment extends BaseButterFragment {
    public interface DriverRegistrationListener {
        void onDriverRegistrationComplete(DriverProfileData data);
    }

    @Bind(R.id.onboaring_driver_registration_root_view) View mDriverRegistrationRootView;

    private DriverInfoUIHelper mRegistrationHelper;
    private DriverRegistrationListener mListener;

    public static DriverRegistrationFragment newInstance() {
        return new DriverRegistrationFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (DriverRegistrationListener) activity;
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_driver, container, false);
    }

    @Override
    public void initUI() {
        mRegistrationHelper = new DriverInfoUIHelper(mDriverRegistrationRootView);
    }

    @OnClick(R.id.onboarding_register_driver_button_next)
    public void onDriverRegistrationNextButtonClicked() {
        if (mRegistrationHelper.validateForm())
            mListener.onDriverRegistrationComplete(mRegistrationHelper.initData());
    }

    @Override
    public void onDestroyView() {
        mRegistrationHelper.destroy();
        super.onDestroyView();
    }
}
