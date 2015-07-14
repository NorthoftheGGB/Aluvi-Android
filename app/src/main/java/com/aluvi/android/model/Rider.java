package com.aluvi.android.model;

import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/13/15.
 */
public class Rider extends RealmObject {

    int id;
    String firstName;
    String lastName;
    String phone;
    double latitude;
    double longitude;
    String smallImageUrl;
    String largeImageUrl;
    List<Fare> fares;

}
