package com.aluvi.android.api.tickets.model;

import com.aluvi.android.api.request.AluviPayload;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by matthewxi on 7/18/15.
 */
public class RiderData extends AluviPayload{

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("first_name")
    private String firstName = "";

    @JsonProperty("last_name")
    private String lastName = "";

    @JsonProperty("phone")
    private String phone = "";

    @JsonProperty("large_image")
    private String largeImage = "";

    @JsonProperty("small_image")
    private String smallImage = "";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getLargeImage() {
        return largeImage;
    }

    public void setLarge_image(String largeImage) {
        this.largeImage = largeImage;
    }

    public String getSmallImage() {
        return smallImage;
    }

    public void setSmallImage(String smallImage) {
        this.smallImage = smallImage;
    }
}
