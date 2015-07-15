package com.aluvi.android.managers;

import android.view.View;
import android.widget.Button;

import com.aluvi.android.api.users.UsersApi;

/**
 * Created by matthewxi on 7/14/15.
 */

public class UserStateManager {

    private static UserStateManager mInstance;

    public interface Callback {
         public void success();
         public void failure();
    }

    public static synchronized UserStateManager getInstance(){
        if(mInstance == null){
            mInstance = new UserStateManager();
        }
        return mInstance;
    }


    public void login(String email, String password, final Callback callback) {
        UsersApi.login(email, password, new UsersApi.Callback(){
            @Override
            public void success() {
                callback.success();
            }

            @Override
            public void failure() {
                callback.failure();
            }
        });
    }
}
