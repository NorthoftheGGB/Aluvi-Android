package com.aluvi.android.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.managers.CommuteManager;

/**
 * Initialize any data we need before the user can use the app. Having this activity is useful because we avoid potential race conditions when using the app
 * when we want to provide the most up-to-date data but also have local caches that can also be used. This entry activity, then, lets us
 * easily maintain an up-to-date cache.
 * <p/>
 * Created by usama on 7/30/15
 */
public class InitActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Dialog progressDialog = DialogUtils.getDefaultProgressDialog(this, false);
        CommuteManager.getInstance().sync(new CommuteManager.Callback() {
            @Override
            public void success() {
                if (progressDialog != null)
                    progressDialog.cancel();

                onInitializationFinished();
            }

            @Override
            public void failure(String message) {
                if (progressDialog != null)
                    progressDialog.cancel();

                Toast.makeText(InitActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void onInitializationFinished() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
