package com.aluvi.android.model.realm;

import com.aluvi.android.api.tickets.model.CarData;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/13/15.
 */
public class Car extends RealmObject {

    private RealmList<Ticket> tickets;

    @JsonProperty("id")
    private int id;

    @JsonProperty("make")
    private String make;

    @JsonProperty("model")
    private String model;

    @JsonProperty("license_plate")
    private String licensePlate;

    @JsonProperty("state")
    private String state;

    @JsonProperty("year")
    private int year;

    @JsonProperty("car_photo")
    private String carPhotoUrl;

    public static void updateCarWithCarData(Car car, CarData data) {
        car.setId(data.getId());
        car.setMake(data.getMake());
        car.setModel(data.getModel());
        car.setLicensePlate(data.getLicensePlate());
        car.setState(data.getState());
        car.setYear(data.getYear());
        car.setCarPhotoUrl(data.getCarPhoto());
    }

    public static HashMap<String, String> toMap(Car car) {
        HashMap<String, String> out = new HashMap<>();
        out.put("id", Integer.toString(car.getId()));
        out.put("make", car.getMake());
        out.put("model", car.getModel());
        out.put("license_plate", car.getLicensePlate());
        out.put("color", "silver");
        return out;
    }

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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
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
