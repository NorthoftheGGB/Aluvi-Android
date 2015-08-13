package com.aluvi.android.model.realm;

import com.aluvi.android.api.users.models.ProfileData;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/13/15.
 */
public class Profile extends RealmObject {
    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("commuter_refill_amount_cents")
    private int commuterRefillAmountCents;

    @JsonProperty("commuter_balance_cents")
    private int commuterBalanceCents;

    @JsonProperty("commuter_refill_enabled")
    private int commuterRefillEnabled;

    @JsonProperty("default_card_token")
    private String defaultCardToken;

    @JsonProperty("card_last_four")
    private String cardLastFour;

    @JsonProperty("card_brand")
    private String cardBrand;

    @JsonProperty("recipient_card_brand")
    private String recipientCardBrand;

    @JsonProperty("recipient_card_last_four")
    private String recipientCardLastFour;

    private String bankAccountName;

    @JsonProperty("image_small")
    private String smallImageUrl;

    @JsonProperty("image_large")
    private String largeImageUrl;

    public static ProfileData getProfileData(Profile profile) {
        ProfileData out = new ProfileData();
        out.setFirstName(profile.getFirstName());
        out.setLastName(profile.getLastName());
        out.setEmail(profile.getEmail());
        out.setPhoneNumber(profile.getPhone());
        out.setDefaultCardToken(profile.getDefaultCardToken());
        return out;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
