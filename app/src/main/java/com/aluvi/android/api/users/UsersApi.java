package com.aluvi.android.api.users;

import android.util.Log;

import com.aluvi.android.api.AluviApi;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.spothero.volley.JacksonNetwork;
import com.spothero.volley.JacksonRequest;
import com.spothero.volley.JacksonRequestListener;

import static android.util.Log.*;

/**
 * Created by matthewxi on 7/14/15.
 */
public class UsersApi {

    static public void login(String email, String password ){

        AluviApi.getInstance().getRequestQueue().add(new JacksonRequest<LoginResponse>(),
                Request.Method.GET, AluviApi.API_LOGIN,
                new JacksonRequestListener<LoginResponse>(){

                    @Override
                    public void onResponse(LoginResponse response, int statusCode, VolleyError error) {
                        if(response != null) {
                            Log.d("JSON", response.getToken());
                        } else {
                            Log.d("JSON", "Did not work");
                        }
                     }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(LoginResponse.class);
                    }
                }
        );


    }


}
