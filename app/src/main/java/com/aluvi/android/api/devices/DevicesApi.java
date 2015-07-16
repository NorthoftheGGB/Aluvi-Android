package com.aluvi.android.api.devices;

import android.util.Log;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.AluviApiKeys;
import com.aluvi.android.api.users.LoginResponse;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.spothero.volley.JacksonRequest;
import com.spothero.volley.JacksonRequestListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by matthewxi on 7/15/15.
 */
public class DevicesApi {

    public interface Callback {
        public void success();
        public void failure(int statusCode);
    }

    public static void updatePushToken(String pushToken, Callback callback){
        Device device = new Device();
        device.setPushToken(pushToken);
        patchDevice(device, callback);
    }

    public static void patchDevice(Device device, final Callback callback){
        JacksonRequest request =  new JacksonRequest<LoginResponse>(
                Request.Method.POST,
                AluviApi.API_BASE_URL,
                AluviApi.API_LOGIN,
                device,
                new JacksonRequestListener<LoginResponse>(){

                    @Override
                    public void onResponse(LoginResponse response, int statusCode, VolleyError error) {
                        if(response != null) {
                            Log.d("Login Success", response.token);
                            callback.success();
                        } else {
                            Log.d("JSON", "Did not work " + error.getMessage());
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(LoginResponse.class);
                    }


                }

        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers =  super.getHeaders();
                headers.put("X-HTTP-Method-Override", "PATCH");
                return headers;
            }
        };

        request.addAcceptedStatusCodes(new int[]{201, 403, 404});
    }
}
