package com.aluvi.android.activities;

import android.content.Intent;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.api.AuthFailEvent;
import com.aluvi.android.managers.UserStateManager;

import de.greenrobot.event.EventBus;

/**
 * Created by usama on 7/24/15.
 * <p/>
 * This class has a singular purpose: listen for events that tell us if the user's token has gone bad. If it has, then re-direct the user to the log-in screen.
 * Subclasses should only be activities that communicate and which at some point send out authenticated requests to the Aluvi API.
 */
public abstract class AluviAuthActivity extends BaseToolBarActivity {

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
        UserStateManager.getInstance().logout(null);
        new MaterialDialog.Builder(this)
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
        Intent logInIntent = new Intent(this, LoginActivity.class);
        logInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logInIntent);
        finish();
    }
}
