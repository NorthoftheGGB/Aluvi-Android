package com.aluvi.android.api.users;

import android.util.Log;
import android.widget.Toast;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.AluviApiKeys;
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

import java.util.HashMap;

import static android.util.Log.*;

/**
 * Created by matthewxi on 7/14/15.
 */
public class UsersApi {

    public interface Callback {
        public void success();
        public void failure();
    }

    static public void login(String email, String password, final Callback callback ){

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(AluviApiKeys.EMAIL_KEY, email);
        params.put(AluviApiKeys.PASSWORD_KEY, password);
        JacksonRequest request =  new JacksonRequest<LoginResponse>(
                Request.Method.POST, AluviApi.API_BASE_URL,  AluviApi.API_LOGIN, params,
                new JacksonRequestListener<LoginResponse>(){

                    @Override
                    public void onResponse(LoginResponse response, int statusCode, VolleyError error) {
                        if(response != null) {
                            Log.d("JSON", response.token);
                            callback.success();
                        } else {
                            Log.d("JSON", "Did not work " + error.getMessage());
                            callback.failure();
                        }
                     }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(LoginResponse.class);
                    }


                }

        );
        request.addAcceptedStatusCodes(new int[]{201, 403, 404});

        AluviApi.getInstance().getRequestQueue().add(request);


    }


}
