package com.aluvi.android.api.tickets.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by matthewxi on 7/18/15.
 */
public class RiderData {

    @JsonProperty("id")
    Integer id;

    @JsonProperty("first_name")
    Integer firstName;

    @JsonProperty("last_name")
    Integer lastName;

    @JsonProperty("phone")
    Integer phone;

    @JsonProperty("large_image")
    Integer large_image;

    @JsonProperty("small_image")
    Integer smallImage;

}
