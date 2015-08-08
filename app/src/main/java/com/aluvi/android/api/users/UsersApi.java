package com.aluvi.android.api.users;

import android.util.Log;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.AluviApiKeys;
import com.aluvi.android.api.request.AluviAuthenticatedRequest;
import com.aluvi.android.api.request.AluviUnauthenticatedRequest;
import com.aluvi.android.api.users.models.DriverRegistrationData;
import com.aluvi.android.api.users.models.UserRegistrationData;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.spothero.volley.JacksonRequestListener;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.HashMap;

/**
 * Created by matthewxi on 7/14/15.
 */
public class UsersApi {
    public interface LoginCallback {
        void success(String token);

        void failure(int statusCode);
    }

    public interface RegistrationCallback {
        void success();

        void failure(int statusCode);
    }

    public static void login(String email, String password, final LoginCallback loginCallback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(AluviApiKeys.EMAIL_KEY, email);
        params.put(AluviApiKeys.PASSWORD_KEY, password);
        AluviUnauthenticatedRequest request = new AluviUnauthenticatedRequest(
                Request.Method.POST,
                AluviApi.API_LOGIN,
                params,
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

    public static void registerUser(UserRegistrationData riderData, final RegistrationCallback callback) {
        AluviUnauthenticatedRequest userRegistrationRequest = new AluviUnauthenticatedRequest(
                Request.Method.POST,
                AluviApi.API_USERS,
                riderData,
                new JacksonRequestListener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_CREATED) {
                            callback.success();
                        } else {
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(JSONObject.class);
                    }
                }
        );

        userRegistrationRequest.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_CREATED,
                HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(userRegistrationRequest);
    }

    public static void registerDriver(DriverRegistrationData driverData, final RegistrationCallback callback) {
        AluviAuthenticatedRequest registerDriverRequest = new AluviAuthenticatedRequest(
                Request.Method.POST,
                AluviApi.API_DRIVER_REGISTRATION,
                driverData,
                new JacksonRequestListener() {
                    @Override
                    public void onResponse(Object response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_CREATED) {
                            callback.success();
                        } else {
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(JSONObject.class);
                    }
                }
        );

        registerDriverRequest.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_CREATED,
                HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(registerDriverRequest);
    }
}
