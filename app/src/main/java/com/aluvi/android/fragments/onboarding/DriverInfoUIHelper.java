package com.aluvi.android.fragments.onboarding;

import android.view.View;
import android.widget.EditText;

import com.aluvi.android.R;
import com.aluvi.android.api.users.models.DriverProfileData;
import com.aluvi.android.helpers.views.FormValidator;
import com.aluvi.android.model.realm.Car;
import com.aluvi.android.model.realm.Profile;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by usama on 8/15/15.
 */
public class DriverInfoUIHelper {

    @Bind(R.id.onboarding_register_driver_edit_text_license_plate_number) EditText mLicensePlateNumberEditText;
    @Bind(R.id.onboarding_register_driver_edit_text_car_make) EditText mCarMakeEditText;
    @Bind(R.id.onboarding_register_driver_edit_text_car_model) EditText mCarModelEditText;
    @Bind(R.id.onboarding_register_driver_edit_text_car_color) EditText mCarColorEditText;

    private View mRootView;

    public DriverInfoUIHelper(View registrationRootView) {
        mRootView = registrationRootView;
        ButterKnife.bind(this, registrationRootView);
    }

    public void destroy() {
        ButterKnife.unbind(this);
    }

    public DriverProfileData initData() {
        DriverProfileData data = new DriverProfileData();
        data.setCarLicensePlate(mLicensePlateNumberEditText.getText().toString());
        data.setCarBrand(mCarMakeEditText.getText().toString());
        data.setCarModel(mCarModelEditText.getText().toString());
        return data;
    }

    public Car initCarData() {
        Car car = new Car();
        car.setLicensePlate(mLicensePlateNumberEditText.getText().toString());
        car.setMake(mCarMakeEditText.getText().toString());
        car.setModel(mCarModelEditText.getText().toString());
        car.setColor(mCarColorEditText.getText().toString());
        return car;
    }

    public void updateData(Profile profile) {
        Car car = profile.getCar();
        if (car != null) {
            mLicensePlateNumberEditText.setText(car.getLicensePlate());
            mCarMakeEditText.setText(car.getMake());
            mCarModelEditText.setText(car.getModel());
            mCarColorEditText.setText(car.getColor());
        }
    }

    public boolean validateForm() {
        return new FormValidator(mRootView.getContext().getString(R.string.field_required_error))
                .addField(mLicensePlateNumberEditText)
                .addField(mCarMakeEditText)
                .addField(mCarModelEditText)
                .addField(mCarColorEditText)
                .validate();
    }
}
