/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aluvi.android.services.push;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.aluvi.android.R;
import com.aluvi.android.api.devices.DevicesApi;
import com.aluvi.android.application.push.PushPreferences;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                String senderId = getString(R.string.gcm_sender_id);
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Log.i(TAG, "GCM Registration Token: " + token);
                sendRegistrationToServer(token);
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);

            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(PushPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        DevicesApi.updatePushToken(token, new DevicesApi.Callback() {
            @Override
            public void success() {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RegistrationIntentService.this);
                sharedPreferences.edit().putBoolean(PushPreferences.SENT_TOKEN_TO_SERVER, true).apply();

                // Notify UI that registration has completed, so the progress indicator can be hidden.
                Intent registrationComplete = new Intent(PushPreferences.REGISTRATION_COMPLETE);
                LocalBroadcastManager.getInstance(RegistrationIntentService.this).sendBroadcast(registrationComplete);
                Toast.makeText(getApplicationContext(), "Finished registering for push", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(int statusCode) {
                Toast.makeText(getApplicationContext(), "Failed to put push token on server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
