package com.aluvi.android.api.users;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.AluviApiKeys;
import com.aluvi.android.api.ApiCallback;
import com.aluvi.android.api.request.AluviAuthMultipartRequest;
import com.aluvi.android.api.request.AluviAuthRealmRequestListener;
import com.aluvi.android.api.request.AluviAuthRequestListener;
import com.aluvi.android.api.request.AluviAuthenticatedRequest;
import com.aluvi.android.api.request.AluviUnauthenticatedRequest;
import com.aluvi.android.api.users.models.DriverProfileData;
import com.aluvi.android.api.users.models.ProfileData;
import com.aluvi.android.api.users.requests.EmailResetRequest;
import com.aluvi.android.model.realm.Car;
import com.aluvi.android.model.realm.Profile;
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
    public interface FailureCallback {
        void failure(int statusCode);
    }

    public interface LoginCallback extends FailureCallback {
        void success(LoginResponse response);
    }

    public interface RegisteredCheckCallback {
        void success();

        void failure();
    }

    public interface RegistrationCallback extends FailureCallback {
        void success(LoginResponse response);
    }

    public interface DriverRegistrationCallback extends FailureCallback {
        void success();
    }

    public interface ProfileCallback extends FailureCallback {
        void success(Profile profile);
    }

    public interface EmailResetCallback extends FailureCallback {
        void success();
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
                        if (statusCode == HttpURLConnection.HTTP_OK && response.getToken() != null)
                            loginCallback.success(response);
                        else
                            loginCallback.failure(statusCode);
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(LoginResponse.class);
                    }
                }
        );

        request.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_CREATED,
                HttpURLConnection.HTTP_FORBIDDEN, HttpURLConnection.HTTP_NOT_FOUND});
        AluviApi.getInstance().getRequestQueue().add(request);
    }

    public static void isUserAlreadyRegistered(String email, final RegisteredCheckCallback callback) {
        login(email, "a", new LoginCallback() {
            @Override
            public void success(LoginResponse response) {
                callback.success();
            }

            @Override
            public void failure(int statusCode) {
                if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED)
                    callback.success();
                else
                    callback.failure();
            }
        });
    }

    public static void sendPasswordResetEmail(String email, final EmailResetCallback callback) {
        AluviUnauthenticatedRequest<JSONObject> passwordResetRequest = new AluviUnauthenticatedRequest<JSONObject>(
                Request.Method.POST,
                AluviApi.API_FORGOT_PASSWORD,
                new EmailResetRequest(email),
                new JacksonRequestListener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK)
                            callback.success();
                        else
                            callback.failure(statusCode);
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(JSONObject.class);
                    }
                });

        AluviApi.getInstance().getRequestQueue().add(passwordResetRequest);
    }

    public static void registerUser(ProfileData riderData, final RegistrationCallback callback) {
        AluviUnauthenticatedRequest<LoginResponse> userRegistrationRequest = new AluviUnauthenticatedRequest(
                Request.Method.POST,
                AluviApi.API_USERS,
                riderData,
                new JacksonRequestListener<LoginResponse>() {
                    @Override
                    public void onResponse(LoginResponse response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_CREATED || statusCode == HttpURLConnection.HTTP_OK) {
                            callback.success(response);
                        } else {
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(LoginResponse.class);
                    }
                }
        );

        userRegistrationRequest.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_CREATED,
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(userRegistrationRequest);
    }

    public static void registerDriver(DriverProfileData driverData, final DriverRegistrationCallback callback) {
        AluviAuthenticatedRequest registerDriverRequest = new AluviAuthenticatedRequest(
                Request.Method.POST,
                AluviApi.API_DRIVER_REGISTRATION,
                driverData,
                new JacksonRequestListener() {
                    @Override
                    public void onResponse(Object response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_CREATED || statusCode == HttpURLConnection.HTTP_OK) {
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
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(registerDriverRequest);
    }

    public static void refreshProfile(final ProfileCallback callback) {
        AluviAuthenticatedRequest profileRequest = new AluviAuthenticatedRequest(
                Request.Method.GET,
                AluviApi.API_USER_PROFILE,
                new AluviAuthRealmRequestListener<Profile>(false) {
                    @Override
                    public void onAuthRealmResponse(Profile response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK)
                            callback.success(response);
                        else
                            callback.failure(statusCode);
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(Profile.class);
                    }
                }
        );

        profileRequest.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK,
                HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(profileRequest);
    }

    public static void saveProfile(Profile profile, final ProfileCallback callback) {
        AluviAuthMultipartRequest<Profile> profileRequest = new AluviAuthMultipartRequest<>(
                Request.Method.POST,
                AluviApi.API_USER_PROFILE,
                Profile.toMap(profile),
                Profile.toFileMap(profile),
                new AluviAuthRealmRequestListener<Profile>(false) {
                    @Override
                    public void onAuthRealmResponse(Profile response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK)
                            callback.success(response);
                        else
                            callback.failure(statusCode);
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(Profile.class);
                    }
                }
        );

        profileRequest.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(profileRequest);
    }

    public static void saveCarInfo(Car car, final ApiCallback callback) {
        AluviAuthenticatedRequest carRequest = new AluviAuthenticatedRequest<>(
                Request.Method.POST,
                AluviApi.API_CAR,
                Car.toMap(car),
                new AluviAuthRequestListener<Void>() {
                    @Override
                    public void onAuthenticatedResponse(Void response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_CREATED)
                            callback.success();
                        else
                            callback.failure(statusCode);
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(Void.class);
                    }
                }
        );

        carRequest.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK,
                HttpURLConnection.HTTP_CREATED, HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(carRequest);
    }
}
