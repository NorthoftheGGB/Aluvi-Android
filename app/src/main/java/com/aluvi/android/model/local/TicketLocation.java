package com.aluvi.android.model.local;

import android.location.Address;

import com.aluvi.android.helpers.GeocoderUtils;

/**
 * Created by matthewxi on 7/16/15.
 */
public class TicketLocation
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
                GeocoderUtils.getFormattedAddress(address));
    }

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
