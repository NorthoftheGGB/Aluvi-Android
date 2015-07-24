package com.aluvi.android.api.tickets.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by matthewxi on 7/18/15.
 */
public class CarData {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("state")
    private String state = "";

    @JsonProperty("make")
    private String make = "";

    @JsonProperty("model")
    private String model = "";

    @JsonProperty("year")
    private String year = "";

    @JsonProperty("license_plate")
    private String licensePlate = "";

    @JsonProperty("car_photo")
    private String carPhoto = "";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getCarPhoto() {
        return carPhoto;
    }

    public void setCarPhoto(String carPhoto) {
        this.carPhoto = carPhoto;
    }
}
