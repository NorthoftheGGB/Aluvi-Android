package com.aluvi.android.model.local;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import com.aluvi.android.helpers.GeoLocationUtils;

/**
 * Created by matthewxi on 7/16/15.
 */
public class TicketLocation implements Parcelable {
    private double latitude, longitude;
    private String placeName;

    public TicketLocation(double latitude, double longitude, String placeName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
    }

    public TicketLocation(Address address) {
        this(address.getLatitude(), address.getLongitude(), GeoLocationUtils.getFormattedAddress(address));
    }

    public TicketLocation(Parcel source) {
        latitude = source.readDouble();
        longitude = source.readDouble();
        placeName = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(placeName);
    }

    public static final Parcelable.Creator<TicketLocation> CREATOR = new Parcelable.Creator<TicketLocation>() {
        @Override
        public TicketLocation createFromParcel(Parcel source) {
            return new TicketLocation(source);
        }

        @Override
        public TicketLocation[] newArray(int size) {
            return new TicketLocation[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }
}
