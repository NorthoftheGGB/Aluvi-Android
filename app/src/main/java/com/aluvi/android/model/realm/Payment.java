package com.aluvi.android.model.realm;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/14/15.
 */
public class Payment extends RealmObject {

    private int id;
    private int driver_id;
    private int fare_id;
    private int ride_id;
    private int amountCents;
    private Date capturedAt;
    private String stripeChargeStatus;
    private Date createdAt;
    private String motive;
    private Driver driver;
    private Fare fare;
    private Ticket ticket;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public int getFare_id() {
        return fare_id;
    }

    public void setFare_id(int fare_id) {
        this.fare_id = fare_id;
    }

    public int getRide_id() {
        return ride_id;
    }

    public void setRide_id(int ride_id) {
        this.ride_id = ride_id;
    }

    public int getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(int amountCents) {
        this.amountCents = amountCents;
    }

    public Date getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(Date capturedAt) {
        this.capturedAt = capturedAt;
    }

    public String getStripeChargeStatus() {
        return stripeChargeStatus;
    }

    public void setStripeChargeStatus(String stripeChargeStatus) {
        this.stripeChargeStatus = stripeChargeStatus;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getMotive() {
        return motive;
    }

    public void setMotive(String motive) {
        this.motive = motive;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Fare getFare() {
        return fare;
    }

    public void setFare(Fare fare) {
        this.fare = fare;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}
