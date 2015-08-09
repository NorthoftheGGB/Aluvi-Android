package com.aluvi.android.api.users.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.aluvi.android.api.request.AluviPayload;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by usama on 8/8/15.
 */
public class ProfileData extends AluviPayload implements Parcelable {
    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("phone")
    private String phoneNumber;

    @JsonProperty("password")
    private String password;

    @JsonProperty("email")
    private String email;

    @JsonProperty("referral_code")
    private String referralCode;

    private boolean isInterestedDriver;

    public ProfileData() {
    }

    private ProfileData(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
        phoneNumber = in.readString();
        password = in.readString();
        email = in.readString();
        referralCode = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(phoneNumber);
        dest.writeString(password);
        dest.writeString(email);
        dest.writeString(referralCode);
    }

    public static final Parcelable.Creator<ProfileData> CREATOR =
            new Parcelable.Creator<ProfileData>() {
                @Override
                public ProfileData createFromParcel(Parcel source) {
                    return new ProfileData(source);
                }

                @Override
                public ProfileData[] newArray(int size) {
                    return new ProfileData[size];
                }
            };


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public boolean isInterestedDriver() {
        return isInterestedDriver;
    }

    public void setIsInterestedDriver(boolean isInterestedDriver) {
        this.isInterestedDriver = isInterestedDriver;
    }
}
