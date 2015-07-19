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

    public static final String ANDROID_PUSH_PLATFORM_NAME = "gcm" ;

    public interface Callback {
        public void success();
        public void failure(int statusCode);
    }

    public static void updatePushToken(String pushToken, Callback callback){
        DeviceData deviceData = new DeviceData();
        deviceData.setPushToken(pushToken);
        patchDevice(deviceData, callback);
    }

    // Send an empty device, the API token will identify and update the user of this device
    public static void updateUser(Callback callback){
        DeviceData deviceData = new DeviceData();
        patchDevice(deviceData, callback);
    }

    public static void patchDevice(DeviceData deviceData, final Callback callback){
        AluviAuthenticatedRequest request = new AluviAuthenticatedRequest<DeviceData>(
                Request.Method.POST,
                AluviApi.API_DEVICES + GlobalIdentifiers.getInstance().getAndroidId(),
                deviceData,
                new JacksonRequestListener<DeviceData>() {

                    @Override
                    public void onResponse(DeviceData response, int statusCode, VolleyError error) {
                        if (response != null) {
                            callback.success();
                        } else {
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(DeviceData.class);
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
