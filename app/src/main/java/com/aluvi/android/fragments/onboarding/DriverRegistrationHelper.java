package com.aluvi.android.fragments.onboarding;

import android.view.View;
import android.widget.EditText;

import com.aluvi.android.R;
import com.aluvi.android.api.users.models.DriverProfileData;
import com.aluvi.android.helpers.views.FormValidator;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by usama on 8/15/15.
 */
public class DriverRegistrationHelper {

    @Bind(R.id.onboarding_register_driver_edit_text_license_number) EditText mLicenseNumberEditText;
    @Bind(R.id.onboarding_register_driver_edit_text_license_plate_number) EditText mLicensePlateNumberEditText;
    @Bind(R.id.onboarding_register_driver_edit_text_car_make) EditText mCarMakeEditText;
    @Bind(R.id.onboarding_register_driver_edit_text_car_model) EditText mCarModelEditText;
    @Bind(R.id.onboarding_register_driver_edit_text_car_year) EditText mCarYearEditText;

    private View mRootView;

    public DriverRegistrationHelper(View registrationRootView) {
        mRootView = registrationRootView;
        ButterKnife.bind(this, registrationRootView);
    }

    public void destroy() {
        ButterKnife.unbind(this);
    }

    public DriverProfileData initData() {
        DriverProfileData data = new DriverProfileData();
        data.setLicenseNumber(mLicenseNumberEditText.getText().toString());
        data.setCarLicensePlate(mLicensePlateNumberEditText.getText().toString());
        data.setCarBrand(mCarMakeEditText.getText().toString());
        data.setCarModel(mCarModelEditText.getText().toString());
        data.setCarYear(mCarYearEditText.getText().toString());
        return data;
    }

    public boolean validateForm() {
        return new FormValidator(mRootView.getContext().getString(R.string.field_required_error))
                .addField(mLicenseNumberEditText)
                .addField(mLicensePlateNumberEditText)
                .addField(mCarMakeEditText)
                .addField(mCarModelEditText)
                .addField(mCarYearEditText)
                .validate();
    }
}
