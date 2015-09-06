package com.aluvi.android.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.activities.base.AluviAuthActivity;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.helpers.RequestQueue;
import com.aluvi.android.managers.UserStateManager;

import java.util.Arrays;

/**
 * Initialize any data we need before the user can use the app. Having this activity is useful because we avoid potential race conditions when using the app
 * when we want to provide the most up-to-date data but also have local caches that can also be used. This entry activity, then, lets us
 * easily maintain an up-to-date cache.
 * <p/>
 * Created by usama on 7/30/15
 */
public class InitActivity extends AluviAuthActivity {
    private Dialog mDefaultProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAluvi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDefaultProgressDialog != null) {
            mDefaultProgressDialog.dismiss();
            mDefaultProgressDialog = null;
        }
    }

    private void initAluvi() {
        mDefaultProgressDialog = DialogUtils.getDefaultProgressDialog(this, false);

        RequestQueue q1 = UserStateManager.getInstance().buildSyncQueue(null);
        RequestQueue q2 = CommuteManager.getInstance().buildSyncQueue(null);
        RequestQueue.mergeQueues(Arrays.asList(q1, q2),
                new RequestQueue.RequestQueueListener() {
                    @Override
                    public void onRequestsFinished() {
                        if (mDefaultProgressDialog != null)
                            mDefaultProgressDialog.cancel();

                        onInitializationFinished();
                    }

                    @Override
                    public void onError(String message) {
                        if (mDefaultProgressDialog != null)
                            mDefaultProgressDialog.cancel();

                        showInitErrorMessage();
                    }
                }).execute();
    }

    @Override
    public int getLayoutId() {
        return -1;
    }

    public void showInitErrorMessage() {
        new MaterialDialog.Builder(this)
                .title(R.string.error_initializing)
                .content(R.string.check_network)
                .cancelable(false)
                .positiveText(R.string.retry)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        initAluvi();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        finish();
                    }
                })
                .show();
    }

    @Override
    public void showForceLogoutDialog() {
        super.logOut();
    }

    private void onInitializationFinished() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
