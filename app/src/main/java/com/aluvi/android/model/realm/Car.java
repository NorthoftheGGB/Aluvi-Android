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

    @JsonProperty("color")
    private String color;

    @JsonProperty("car_photo")
    private String carPhotoUrl;

    public static void updateCarWithCarData(Car car, CarData data) {
        car.setId(data.getId());
        if (data.getMake() != null)
            car.setMake(data.getMake());
        if (data.getModel() != null)
            car.setModel(data.getModel());
        if (data.getLicensePlate() != null)
            car.setLicensePlate(data.getLicensePlate());
        if (data.getState() != null)
            car.setState(data.getState());
        if (data.getColor() != null)
            car.setColor(data.getColor());
        if (data.getCarPhoto() != null)
            car.setCarPhotoUrl(data.getCarPhoto());
    }

    public static HashMap<String, String> toMap(Car car) {
        HashMap<String, String> out = new HashMap<>();
        out.put("make", car.getMake());
        out.put("model", car.getModel());
        out.put("license_plate", car.getLicensePlate());
        out.put("color", car.getColor());
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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
}
