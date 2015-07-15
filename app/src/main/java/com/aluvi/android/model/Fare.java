package com.aluvi.android.model;

import com.aluvi.android.model.base.Transit;

import io.realm.RealmList;

/**
 * Created by matthewxi on 7/13/15.
 */
public class Fare extends Transit {

    RealmList<Rider> riders;

    private int car_id;
    private int driveTime;
    private float distance;

}
