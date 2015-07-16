package com.aluvi.android.model.realm;

import io.realm.RealmObject;
import io.realm.RealmList;

/**
 * Created by matthewxi on 7/13/15.
 */
public class Rider extends RealmObject {

    private RealmList<Fare> fares;

    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private double latitude;
    private double longitude;
    private String smallImageUrl;
    private String largeImageUrl;

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

    public RealmList<Fare> getFares() {
        return fares;
    }

    public void setFares(RealmList<Fare> fares) {
        this.fares = fares;
    }
}
