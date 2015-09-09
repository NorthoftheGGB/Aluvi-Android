package com.aluvi.android.activities.base;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.aluvi.android.R;
import com.aluvi.android.helpers.views.DialogUtils;

import java.util.ArrayList;

import butterknife.Bind;

/**
 * Created by usama on 7/13/15.
 */
public abstract class BaseToolBarActivity extends BaseButterActivity {
    @Nullable
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private ArrayList<Dialog> mProgressDialogs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mToolbar != null) {
            mToolbar.setTitle(getTitle());
            setSupportActionBar(mToolbar);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelProgressDialogs();
    }

    public void showDefaultProgressDialog() {
        mProgressDialogs.add(DialogUtils.showDefaultProgressDialog(this, false));
    }

    public void showCustomProgressDialog(int title, int message, boolean cancelable) {
        showCustomProgressDialog(getString(title), getString(message), cancelable);
    }

    public void showCustomProgressDialog(String title, String message, boolean cancelable) {
        mProgressDialogs.add(DialogUtils.showCustomProgressDialog(this, title, message, cancelable));
    }

    public void cancelProgressDialogs() {
        for (Dialog dialog : mProgressDialogs)
            dialog.cancel();

        mProgressDialogs.clear();
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
