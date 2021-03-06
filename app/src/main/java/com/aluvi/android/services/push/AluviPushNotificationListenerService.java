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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.aluvi.android.R;
import com.aluvi.android.activities.MainActivity;

import de.greenrobot.event.EventBus;

public class AluviPushNotificationListenerService extends com.google.android.gms.gcm.GcmListenerService {

    private static final String TAG = "AluviGcmListenerService";
    private final static String TRIP_STATE_UNFULFILLED = "trip_unfulfilled",
            TRIP_STATE_FULFILLED = "trip_fulfilled",
            TRIP_STATE_CANCELLED_BY_RIDER = "fare_cancelled_by_rider",
            TRIP_STATE_CANCELLED_BY_DRIVER = "fare_cancelled_by_driver",
            TRIP_STATE_COMMUTE_REMINDER = "commute_reminder",
            TRIP_STATE_RIDE_PAYMENT_PROBLEM = "ride_payment_problem",
            TRIP_STATE_USER_STATE_CHANGED = "user_state_change",
            TRIP_STATE_GENERIC = "generic";

    private final int NOTIFICATION_ID = 2412;

    private Handler mMainThreadHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mMainThreadHandler = new Handler();
    }

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        final String type = data.getString("type");
        final String message = type.equals(TRIP_STATE_GENERIC) ? data.getString("message")
                : getString(getNotificationMessageForType(type));

        Log.e(TAG, "Received message: " + message);

        mMainThreadHandler.post(new MessageRunnable(type, message));
        sendNotification(message);
    }

    private static class MessageRunnable implements Runnable {
        private String type, message;

        public MessageRunnable(String type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public void run() {
            EventBus.getDefault().post(new PushNotificationEvent(type, message));
        }
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private int getNotificationMessageForType(String type) {
        switch (type) {
            case TRIP_STATE_FULFILLED:
                return R.string.trip_fulfilled;
            case TRIP_STATE_UNFULFILLED:
                return R.string.trip_unfulfilled;
            case TRIP_STATE_CANCELLED_BY_RIDER:
                return R.string.fare_cancelled_by_rider;
            case TRIP_STATE_CANCELLED_BY_DRIVER:
                return R.string.fare_cancelled_by_driver;
            case TRIP_STATE_COMMUTE_REMINDER:
                return R.string.commute_reminder;
            case TRIP_STATE_RIDE_PAYMENT_PROBLEM:
                return R.string.ride_payment_problem;
            case TRIP_STATE_USER_STATE_CHANGED:
                return R.string.user_state_change;
            default:
                return R.string.aluvi_default;
        }
    }

    public static class PushNotificationEvent {
        private String pushType, pushData;

        public PushNotificationEvent(String pushType, String pushData) {
            this.pushType = pushType;
            this.pushData = pushData;
        }

        public String getPushType() {
            return pushType;
        }

        public void setPushType(String pushType) {
            this.pushType = pushType;
        }

        public String getPushData() {
            return pushData;
        }

        public void setPushData(String pushData) {
            this.pushData = pushData;
        }
    }
}
