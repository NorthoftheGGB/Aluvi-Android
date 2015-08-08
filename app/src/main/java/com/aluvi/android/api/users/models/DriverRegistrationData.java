package com.aluvi.android.api.users.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.aluvi.android.api.request.AluviPayload;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by usama on 8/8/15.
 */
public class DriverRegistrationData extends AluviPayload implements Parcelable {
    @JsonProperty("drivers_license_number")
    private String licenseNumber;

    @JsonProperty("car_brand")
    private String carBrand;

    @JsonProperty("car_model")
    private String carModel;

    @JsonProperty("car_year")
    private String carYear;

    @JsonProperty("car_license_plate")
    private String carLicensePlate;

    @JsonProperty("referral_code")
    private String referralCode;

    public DriverRegistrationData() {
    }

    private DriverRegistrationData(Parcel in) {
        licenseNumber = in.readString();
        carBrand = in.readString();
        carModel = in.readString();
        carYear = in.readString();
        carLicensePlate = in.readString();
        referralCode = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(licenseNumber);
        dest.writeString(carBrand);
        dest.writeString(carModel);
        dest.writeString(carYear);
        dest.writeString(carLicensePlate);
        dest.writeString(referralCode);
    }

    public static final Parcelable.Creator<DriverRegistrationData> CREATOR =
            new Parcelable.Creator<DriverRegistrationData>() {
                @Override
                public DriverRegistrationData createFromParcel(Parcel source) {
                    return new DriverRegistrationData(source);
                }

                @Override
                public DriverRegistrationData[] newArray(int size) {
                    return new DriverRegistrationData[size];
                }
            };

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCarYear() {
        return carYear;
    }

    public void setCarYear(String carYear) {
        this.carYear = carYear;
    }

    public String getCarLicensePlate() {
        return carLicensePlate;
    }

    public void setCarLicensePlate(String carLicensePlate) {
        this.carLicensePlate = carLicensePlate;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
}
