package com.aluvi.android.model.realm;

import com.aluvi.android.api.tickets.model.DriverData;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/13/15.
 */
public class Driver extends RealmObject {

    private RealmList<Ticket> tickets;

    private int id;
    private String firstName;
    private String lastName;
    private String driversLicenseNumber;
    private String phone;
    private String smallImageUrl;
    private String largeImageUrl;

    public static void updateWithDriverData(Driver driver, DriverData data) {
        driver.setId(data.getId());

        if (data.getFirstName() != null)
            driver.setFirstName(data.getFirstName());

        if (data.getLastName() != null)
            driver.setLastName(data.getLastName());

        if (data.getDriversLicenseNumber() != null)
            driver.setDriversLicenseNumber(data.getDriversLicenseNumber());

        if (data.getPhone() != null)
            driver.setPhone(data.getPhone());

        if (data.getSmallImage() != null)
            driver.setSmallImageUrl(data.getSmallImage());

        if (data.getLargeImage() != null)
            driver.setLargeImageUrl(data.getLargeImage());
    }

    public static String fullname(Driver driver) {
        return driver.getFirstName() + ' ' + driver.getLastName();
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

    public String getDriversLicenseNumber() {
        return driversLicenseNumber;
    }

    public void setDriversLicenseNumber(String driversLicenseNumber) {
        this.driversLicenseNumber = driversLicenseNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public RealmList<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(RealmList<Ticket> tickets) {
        this.tickets = tickets;
    }


}
