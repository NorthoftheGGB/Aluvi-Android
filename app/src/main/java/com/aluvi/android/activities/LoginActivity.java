package com.aluvi.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.aluvi.android.R;
import com.aluvi.android.activities.base.BaseToolBarActivity;
import com.aluvi.android.application.AluviPreferences;
import com.aluvi.android.fragments.LoginFragment;

/**
 * Created by usama on 7/24/15.
 */
public class LoginActivity extends BaseToolBarActivity implements LoginFragment.LoginListener {
    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isTutorialViewed()) {
            startActivity(new Intent(this, TutorialActivity.class));
            finish();
        }
    }

    @Override
    public void onLoggedIn() {
        startActivity(new Intent(LoginActivity.this, InitActivity.class));
        finish();
    }

    private boolean isTutorialViewed() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(AluviPreferences.TUTORIAL_VIEWED_KEY, false);
    }
}

