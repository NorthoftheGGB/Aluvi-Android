package com.aluvi.android.managers;

import com.aluvi.android.api.users.UsersApi;

/**
 * Created by matthewxi on 7/14/15.
 */

public class UserStateManager {

    private static UserStateManager mInstance;

    abstract public class Callback {
        abstract public void success();
        abstract public void failure();
    }

    public static synchronized UserStateManager getInstance(){
        if(mInstance == null){
            mInstance = new UserStateManager();
        }
        return mInstance;
    }


    public void login(String email, String password, Callback callback) {
        UsersApi.login(email, password);
    }
}
