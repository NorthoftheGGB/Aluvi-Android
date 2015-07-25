package com.aluvi.android.model.local;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import com.aluvi.android.api.gis.GeocodingApi;

/**
 * Created by matthewxi on 7/16/15.
 */
public class TicketLocation implements Parcelable
{
    private float latitude, longitude;
    private String placeName;

    public TicketLocation(float latitude, float longitude, String placeName)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
    }

    public TicketLocation(Address address)
    {
        this((float) address.getLatitude(), (float) address.getLongitude(),
                GeocodingApi.getFormattedAddress(address));
    }

    public TicketLocation(Parcel source)
    {
        latitude = source.readFloat();
        longitude = source.readFloat();
        placeName = source.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeFloat(latitude);
        dest.writeFloat(longitude);
        dest.writeString(placeName);
    }

    public static final Parcelable.Creator<TicketLocation> CREATOR = new Parcelable.Creator<TicketLocation>()
    {
        @Override
        public TicketLocation createFromParcel(Parcel source)
        {
            return new TicketLocation(source);
        }

        @Override
        public TicketLocation[] newArray(int size)
        {
            return new TicketLocation[size];
        }
    };

    public float getLatitude()
    {
        return latitude;
    }

    public void setLatitude(float latitude)
    {
        this.latitude = latitude;
    }

    public float getLongitude()
    {
        return longitude;
    }

    public void setLongitude(float longitude)
    {
        this.longitude = longitude;
    }

    public String getPlaceName()
    {
        return placeName;
    }

    public void setPlaceName(String placeName)
    {
        this.placeName = placeName;
    }
}
