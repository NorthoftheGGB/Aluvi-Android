package com.aluvi.android.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.aluvi.android.application.AluviPreferences;
import com.aluvi.android.model.Profile;
import com.google.gson.Gson;

/**
 * Created by matthewxi on 7/15/15.
 */
public class CommuteManager {

    private static CommuteManager mInstance;

    private SharedPreferences preferences;

    private double homeLatitude;
    private double homeLongitude;
    private double workLatitude;
    private double workLongitude;
    private String homePlaceName;
    private String workPlaceName;
    private String pickupTime;
    private String returnTime;
    private boolean driving;

    public static synchronized void initialize(Context context){
        if(mInstance == null){
            mInstance = new CommuteManager(context);
        }
    }

    public static synchronized CommuteManager getInstance(){
        return mInstance;
    }

    public CommuteManager(Context context) {
        preferences = context.getSharedPreferences(AluviPreferences.COMMUTER_PREFERENCES_FILE, 0);
        load();
    }


    public double getHomeLatitude() {
        return homeLatitude;
    }

    public void setHomeLatitude(double homeLatitude) {
        this.homeLatitude = homeLatitude;
    }

    public double getHomeLongitude() {
        return homeLongitude;
    }

    public void setHomeLongitude(double homeLongitude) {
        this.homeLongitude = homeLongitude;
    }

    public double getWorkLatitude() {
        return workLatitude;
    }

    public void setWorkLatitude(double workLatitude) {
        this.workLatitude = workLatitude;
    }

    public double getWorkLongitude() {
        return workLongitude;
    }

    public void setWorkLongitude(double workLongitude) {
        this.workLongitude = workLongitude;
    }

    public String getHomePlaceName() {
        return homePlaceName;
    }

    public void setHomePlaceName(String homePlaceName) {
        this.homePlaceName = homePlaceName;
    }

    public String getWorkPlaceName() {
        return workPlaceName;
    }

    public void setWorkPlaceName(String workPlaceName) {
        this.workPlaceName = workPlaceName;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    public String getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(String returnTime) {
        this.returnTime = returnTime;
    }

    public boolean isDriving() {
        return driving;
    }

    public void setDriving(boolean driving) {
        this.driving = driving;
    }

    private void load(){
        homeLatitude = preferences.getFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, 0);
        homeLongitude = preferences.getFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, 0);
        workLatitude = preferences.getFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, 0);
        workLongitude = preferences.getFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, 0);
        homePlaceName = preferences.getString(AluviPreferences.COMMUTER_HOME_PLACENAME_KEY, "");
        workPlaceName = preferences.getString(AluviPreferences.COMMUTER_WORK_PLACENAME_KEY, "");
        pickupTime = preferences.getString(AluviPreferences.COMMUTER_PICKUP_TIME_KEY, "");
        returnTime = preferences.getString(AluviPreferences.COMMUTER_RETURN_TIME_KEY, "");
        driving = preferences.getBoolean(AluviPreferences.COMMUTER_IS_DRIVER_KEY, false);
    }

    public void loadFromServer(){

    }
}
