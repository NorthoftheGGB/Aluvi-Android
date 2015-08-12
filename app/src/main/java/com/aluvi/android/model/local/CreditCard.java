package com.aluvi.android.model.local;

/**
 * Created by usama on 8/11/15.
 */
public class CreditCard {
    private String cardNumber, cvv;
    private int expireMonth, expireYear;

    public CreditCard(String cardNumber, int expireMonth, int expireYear, String cvv) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expireMonth = expireMonth;
        this.expireYear = expireYear;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getExpirationMonth() {
        return expireMonth;
    }

    public void setExpireMonth(int expireMonth) {
        this.expireMonth = expireMonth;
    }

    public int getExpirationYear() {
        return expireYear;
    }

    public void setExpireYear(int expireYear) {
        this.expireYear = expireYear;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}
