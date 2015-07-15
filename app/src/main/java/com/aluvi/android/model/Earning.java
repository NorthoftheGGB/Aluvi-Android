package com.aluvi.android.model;

import java.util.Date;

/**
 * Created by matthewxi on 7/14/15.
 */
public class Earning {
    private int amountCents;
    private Date timestamp;
    private int fare_id;
    private String motive;
    private Fare fare;

    public int getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(int amountCents) {
        this.amountCents = amountCents;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getFare_id() {
        return fare_id;
    }

    public void setFare_id(int fare_id) {
        this.fare_id = fare_id;
    }

    public String getMotive() {
        return motive;
    }

    public void setMotive(String motive) {
        this.motive = motive;
    }

    public Fare getFare() {
        return fare;
    }

    public void setFare(Fare fare) {
        this.fare = fare;
    }
}
