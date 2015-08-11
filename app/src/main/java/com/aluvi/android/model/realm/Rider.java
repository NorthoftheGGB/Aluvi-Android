package com.aluvi.android.model.realm;

import com.aluvi.android.api.tickets.model.RiderData;

import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/13/15.
 */
public class Rider extends RealmObject {
    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private double latitude;
    private double longitude;
    private String smallImageUrl;
    private String largeImageUrl;

    public static void updateWithRiderData(Rider rider, RiderData r) {
        rider.setId(r.getId());
        rider.setFirstName(r.getFirstName());
        rider.setLastName(r.getLastName());
        rider.setPhone(r.getPhone());
        rider.setSmallImageUrl(r.getSmallImage());
        rider.setLargeImageUrl(r.getLargeImage());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }

    public String getLargeImageUrl() {
        return largeImageUrl;
    }

    public void setLargeImageUrl(String largeImageUrl) {
        this.largeImageUrl = largeImageUrl;
    }
}
