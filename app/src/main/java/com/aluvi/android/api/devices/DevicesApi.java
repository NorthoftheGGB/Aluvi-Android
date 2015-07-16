package com.aluvi.android.api.devices;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.request.AluviAuthenticatedRequest;
import com.aluvi.android.application.GlobalIdentifiers;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.spothero.volley.JacksonRequestListener;

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

    // Send an empty device, the API token will identify and update the user of this device
    public static void updateUser(Callback callback){
        Device device = new Device();
        patchDevice(device, callback);
    }

    public static void patchDevice(Device device, final Callback callback){
        AluviAuthenticatedRequest request = new AluviAuthenticatedRequest<Device>(
                Request.Method.POST,
                AluviApi.API_DEVICES + GlobalIdentifiers.getInstance().getAndroidId(),
                device,
                new JacksonRequestListener<Device>() {

                    @Override
                    public void onResponse(Device response, int statusCode, VolleyError error) {
                        if (response != null) {
                            callback.success();
                        } else {
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(Device.class);
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                headers.put("X-HTTP-Method-Override", "PATCH");
                return headers;
            }
        };

        request.addAcceptedStatusCodes(new int[]{201});

        AluviApi.getInstance().getRequestQueue().add(request);

    }
}
