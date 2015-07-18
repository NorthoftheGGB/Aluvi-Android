package com.aluvi.android.model;

import com.aluvi.android.application.AluviRealm;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/17/15.
 */
public class RealmHelper {
    public static void removeFromRealm(RealmObject object){
        Realm realm = AluviRealm.getDefaultRealm();
        realm.beginTransaction();
        object.removeFromRealm();
        realm.commitTransaction();
    }

}
