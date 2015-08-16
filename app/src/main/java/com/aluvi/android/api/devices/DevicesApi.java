package com.aluvi.android.api.devices;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.request.AluviAuthRequestListener;
import com.aluvi.android.api.request.AluviAuthenticatedRequest;
import com.aluvi.android.application.GlobalIdentifiers;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;

import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Created by matthewxi on 7/15/15.
 */
public class DevicesApi {
    public static final String ANDROID_PUSH_PLATFORM_NAME = "gcm";
    public interface Callback {
        void success();

        void failure(int statusCode);
    }

    public static void updatePushToken(String pushToken, Callback callback) {
        DeviceData deviceData = new DeviceData();
        deviceData.setPushToken(pushToken);
        patchDevice(deviceData, callback);
    }

    // Send an empty device, the API token will identify and update the user of this device
    public static void updateUser(Callback callback) {
        DeviceData deviceData = new DeviceData();
        patchDevice(deviceData, callback);
    }

    public static void patchDevice(DeviceData deviceData, final Callback callback) {
        AluviAuthenticatedRequest request = new AluviAuthenticatedRequest<DeviceData>(
                Request.Method.POST,
                AluviApi.API_DEVICES + GlobalIdentifiers.getInstance().getAndroidId(),
                deviceData,
                new AluviAuthRequestListener<DeviceData>() {
                    @Override
                    public void onAuthenticatedResponse(DeviceData response, int statusCode, VolleyError error) {
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
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                headers.put("X-HTTP-Method-Override", "PATCH");
                return headers;
            }
        };

        request.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_CREATED});
        AluviApi.getInstance().getRequestQueue().add(request);
    }

    public static void disassociateUser(final Callback callback) {
        AluviAuthenticatedRequest request = new AluviAuthenticatedRequest<>(
                Request.Method.PUT,
                AluviApi.API_DEVICE_DISASSOCIATE_USER + GlobalIdentifiers.getInstance().getAndroidId(),
                new AluviAuthRequestListener<Void>() {
                    @Override
                    public void onAuthenticatedResponse(Void response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK) {
                            callback.success();
                        } else {
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(Void.class);
                    }
                }
        );

        request.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_UNAUTHORIZED});
        AluviApi.getInstance().getRequestQueue().add(request);
    }
}
