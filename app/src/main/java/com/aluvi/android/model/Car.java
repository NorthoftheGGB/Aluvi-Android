package com.aluvi.android.model;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/13/15.
 */
public class Car extends RealmObject {

    private RealmList<Ticket> tickets;

    private int id;
    private String make;
    private String model;
    private String licensePlate;
    private String state;
    private String year;
    private String carPhotoUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCarPhotoUrl() {
        return carPhotoUrl;
    }

    public void setCarPhotoUrl(String carPhotoUrl) {
        this.carPhotoUrl = carPhotoUrl;
    }

    public RealmList<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(RealmList<Ticket> tickets) {
        this.tickets = tickets;
    }

    public static String summary(Car car) {
        return car.getYear() + ' ' + car.getMake() + ' ' + car.getModel();
    }
}
