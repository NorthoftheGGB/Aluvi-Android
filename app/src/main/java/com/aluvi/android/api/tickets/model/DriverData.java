package com.aluvi.android.api.tickets.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by matthewxi on 7/18/15.
 */
public class DriverData {

    @JsonProperty("id")
    public Integer id;

    @JsonProperty("id")
    public String firstName;

    @JsonProperty("last_name")
    public String lastName;

    @JsonProperty("phone")
    public String phone;

    @JsonProperty("drivers_license_number")
    public String driversLicenseNumber;

    @JsonProperty("large_image")
    public String largeImage;

    @JsonProperty("small_image")
    public String smallImage;
}
