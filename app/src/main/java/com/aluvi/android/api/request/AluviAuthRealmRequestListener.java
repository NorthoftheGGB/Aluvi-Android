package com.aluvi.android.api.request;

import com.aluvi.android.application.AluviRealm;
import com.android.volley.VolleyError;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by usama on 7/29/15.
 */
public abstract class AluviAuthRealmRequestListener<T extends RealmObject> extends AluviAuthRequestListener<T> {
    private Realm realm;

    public AluviAuthRealmRequestListener() {
        realm = AluviRealm.getDefaultRealm();
    }

    @Override
    public void onAuthenticatedResponse(T response, int statusCode, VolleyError error) {
        if (error == null && response != null) {
            realm.beginTransaction();
            realm.copyToRealm(response);
            realm.commitTransaction();
        }

        onAuthRealmResponse(response, statusCode, error);
    }

    public abstract void onAuthRealmResponse(T response, int statusCode, VolleyError error);
}
