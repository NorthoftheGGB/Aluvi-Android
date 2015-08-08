package com.aluvi.android.fragments.onboarding;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.aluvi.android.R;
import com.aluvi.android.api.users.models.DriverRegistrationData;
import com.aluvi.android.fragments.BaseButterFragment;
import com.aluvi.android.helpers.views.FormValidator;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by usama on 8/8/15.
 */
public class DriverRegistrationFragment extends BaseButterFragment {
    public interface DriverRegistrationListener {
        void onDriverRegistrationComplete(DriverRegistrationData data);
    }

    @Bind(R.id.onboarding_register_driver_edit_text_license_number) EditText mLicenseNumberEditText;
    @Bind(R.id.onboarding_register_driver_edit_text_license_plate_number) EditText mLicensePlateNumberEditText;
    @Bind(R.id.onboarding_register_driver_edit_text_car_make) EditText mCarMakeEditText;
    @Bind(R.id.onboarding_register_driver_edit_text_car_model) EditText mCarModelEditText;
    @Bind(R.id.onboarding_register_driver_edit_text_car_year) EditText mCarYearEditText;

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
    }

    @OnClick(R.id.onboarding_register_driver_button_next)
    public void onDriverRegistrationNextButtonClicked() {
        if (validateForm())
            mListener.onDriverRegistrationComplete(initData());
    }

    private DriverRegistrationData initData() {
        DriverRegistrationData data = new DriverRegistrationData();
        data.setLicenseNumber(mLicenseNumberEditText.getText().toString());
        data.setCarLicensePlate(mLicensePlateNumberEditText.getText().toString());
        data.setCarBrand(mCarMakeEditText.getText().toString());
        data.setCarModel(mCarModelEditText.getText().toString());
        data.setCarYear(mCarYearEditText.getText().toString());
        return data;
    }

    private boolean validateForm() {
        return new FormValidator(getString(R.string.field_required_error))
                .addField(mLicenseNumberEditText)
                .addField(mLicensePlateNumberEditText)
                .addField(mCarMakeEditText)
                .addField(mCarModelEditText)
                .addField(mCarYearEditText)
                .validate();
    }
}
