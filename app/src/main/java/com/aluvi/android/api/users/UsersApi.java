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
public class UsersApi
{
    public interface Callback
    {
        void success(String token);

        void failure(int statusCode);
    }

    static public void login(String email, String password, final Callback callback)
    {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(AluviApiKeys.EMAIL_KEY, email);
        params.put(AluviApiKeys.PASSWORD_KEY, password);
        AluviUnauthenticatedRequest request = new AluviUnauthenticatedRequest(
                Request.Method.POST, AluviApi.API_LOGIN, params,
                new JacksonRequestListener<LoginResponse>()
                {

                    @Override
                    public void onResponse(LoginResponse response, int statusCode, VolleyError error)
                    {
                        if (response != null)
                        {
                            Log.d("Login Success", response.token);
                            callback.success(response.token);
                        }
                        else
                        {
                            Log.d("JSON", "Did not work " + error.getMessage());
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType()
                    {
                        return SimpleType.construct(LoginResponse.class);
                    }
                }
        );

        request.addAcceptedStatusCodes(new int[]{201, 403, 404});
        AluviApi.getInstance().getRequestQueue().add(request);
    }
}
