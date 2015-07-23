package com.aluvi.android.api.tickets.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by matthewxi on 7/18/15.
 */
public class RiderData {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("first_name")
    private Integer firstName;

    @JsonProperty("last_name")
    private Integer lastName;

    @JsonProperty("phone")
    private Integer phone;

    @JsonProperty("large_image")
    private Integer large_image;

    @JsonProperty("small_image")
    private Integer smallImage;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFirstName() {
        return firstName;
    }

    public void setFirstName(Integer firstName) {
        this.firstName = firstName;
    }

    public Integer getLastName() {
        return lastName;
    }

    public void setLastName(Integer lastName) {
        this.lastName = lastName;
    }

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
    }

    public Integer getLarge_image() {
        return large_image;
    }

    public void setLarge_image(Integer large_image) {
        this.large_image = large_image;
    }

    public Integer getSmallImage() {
        return smallImage;
    }

    public void setSmallImage(Integer smallImage) {
        this.smallImage = smallImage;
    }
}
