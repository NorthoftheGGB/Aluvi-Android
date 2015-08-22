package com.aluvi.android.activities.base;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.activities.LoginActivity;
import com.aluvi.android.api.AuthFailEvent;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.callbacks.Callback;

import de.greenrobot.event.EventBus;

/**
 * Created by usama on 7/24/15.
 * <p/>
 * This class has a singular purpose: listen for events that tell us if the user's token has gone bad. If it has, then re-direct the user to the log-in screen.
 * Subclasses should only be activities that communicate and which at some point send out authenticated requests to the Aluvi API.
 */
public abstract class AluviAuthActivity extends BaseToolBarActivity {
    private String TAG = "AluviAuthActivity";
    private Dialog authDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @SuppressWarnings("unused")
    public void onEvent(AuthFailEvent event) {
        showForceLogoutDialog();
    }

    public void showForceLogoutDialog() {
        if (authDialog == null)
            authDialog = new MaterialDialog.Builder(this)
                    .title(R.string.auth_error)
                    .content(R.string.sign_back_in)
                    .positiveText(android.R.string.ok)
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            logOut();
                        }
                    })
                    .show();
    }

    protected void logOut() {
        UserStateManager.getInstance().logout(new Callback() {
            @Override
            public void success() {
                onLoggedOut();
            }

            @Override
            public void failure(String message) {
                Log.e(TAG, message);
                Toast.makeText(AluviAuthActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onLoggedOut() {
        Intent logInIntent = new Intent(AluviAuthActivity.this, LoginActivity.class);
        logInIntent.addFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logInIntent);
        finish();
    }
}
