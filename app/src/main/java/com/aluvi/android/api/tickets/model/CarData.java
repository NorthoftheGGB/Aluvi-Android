package com.aluvi.android.api.tickets.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by matthewxi on 7/18/15.
 */
public class CarData {

    @JsonProperty("id")
    public Integer id;

    @JsonProperty("state")
    public String state;

    @JsonProperty("make")
    public String make;

    @JsonProperty("model")
    public String model;

    @JsonProperty("year")
    public String year;

    @JsonProperty("license_plate")
    public String licensePlate;

    @JsonProperty("car_photo")
    public String carPhoto;
}
