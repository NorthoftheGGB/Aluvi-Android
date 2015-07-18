package com.aluvi.android.application;

import android.content.Context;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by matthewxi on 7/17/15.
 */
public class AluviRealm {

    private static AluviRealm instance;
    private Realm realm;
    private RealmConfiguration realmConfig;

    public static void initialize(Context context) {
        // Configure Realm
        Log.w("Aluvi", "Migrations have not been implemented for Realm");
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context)
                .name("aluvi")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()  // not running migrations yet
                .build();
        instance = new AluviRealm(realmConfig);
    }

    public AluviRealm(RealmConfiguration realmConfig ) {
        this.realmConfig = realmConfig;
        this.realm = Realm.getInstance(realmConfig);
    }

    public static Realm getDefaultRealm(){
        return instance.getRealm();
    }

    public Realm getRealm(){
        return this.realm;
    }

}
