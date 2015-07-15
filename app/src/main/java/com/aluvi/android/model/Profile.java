package com.aluvi.android.model;

/**
 * Created by matthewxi on 7/13/15.
 */
public class Profile {

    private String firstName;
    private String lastName;
    private String phone;
    private String email;

    private int commuterRefillAmountCents;
    private int commuterBalanceCents;
    private int commuterRefillEnabled;

    private String defaultCardToken;
    private String cardLastFour;
    private String cardBrand;

    private String recipientCardBrand;
    private String recipientCardLastFour;
    private String bankAccountName;

    private String smallImageUrl;
    private String largeImageUrl;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCommuterRefillAmountCents() {
        return commuterRefillAmountCents;
    }

    public void setCommuterRefillAmountCents(int commuterRefillAmountCents) {
        this.commuterRefillAmountCents = commuterRefillAmountCents;
    }

    public int getCommuterBalanceCents() {
        return commuterBalanceCents;
    }

    public void setCommuterBalanceCents(int commuterBalanceCents) {
        this.commuterBalanceCents = commuterBalanceCents;
    }

    public int getCommuterRefillEnabled() {
        return commuterRefillEnabled;
    }

    public void setCommuterRefillEnabled(int commuterRefillEnabled) {
        this.commuterRefillEnabled = commuterRefillEnabled;
    }

    public String getDefaultCardToken() {
        return defaultCardToken;
    }

    public void setDefaultCardToken(String defaultCardToken) {
        this.defaultCardToken = defaultCardToken;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public String getRecipientCardBrand() {
        return recipientCardBrand;
    }

    public void setRecipientCardBrand(String recipientCardBrand) {
        this.recipientCardBrand = recipientCardBrand;
    }

    public String getRecipientCardLastFour() {
        return recipientCardLastFour;
    }

    public void setRecipientCardLastFour(String recipientCardLastFour) {
        this.recipientCardLastFour = recipientCardLastFour;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }

    public String getLargeImageUrl() {
        return largeImageUrl;
    }

    public void setLargeImageUrl(String largeImageUrl) {
        this.largeImageUrl = largeImageUrl;
    }



}
