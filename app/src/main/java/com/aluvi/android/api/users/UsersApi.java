package com.aluvi.android.api.users;

import android.util.Log;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.AluviApiKeys;
import com.aluvi.android.api.request.AluviUnauthenticatedRequest;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.spothero.volley.JacksonRequestListener;

import java.util.HashMap;

/**
 * Created by matthewxi on 7/14/15.
 */
public class UsersApi {
    public interface LoginCallback {
        void success(String token);

        void failure(int statusCode);
    }

    public static void login(String email, String password, final LoginCallback loginCallback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(AluviApiKeys.EMAIL_KEY, email);
        params.put(AluviApiKeys.PASSWORD_KEY, password);
        AluviUnauthenticatedRequest request = new AluviUnauthenticatedRequest(
                Request.Method.POST, AluviApi.API_LOGIN, params,
                new JacksonRequestListener<LoginResponse>() {
                    @Override
                    public void onResponse(LoginResponse response, int statusCode, VolleyError error) {
                        if (statusCode == 200 && response.getToken() != null) {
                            Log.d("Login Success", "Received token: " + response.getToken());
                            loginCallback.success(response.getToken());
                        } else {
                            if (error != null) {
                                Log.d("JSON", "Did not work " + error.getMessage());
                            }
                            loginCallback.failure(statusCode);
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
