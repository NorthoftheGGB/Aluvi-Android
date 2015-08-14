package com.aluvi.android.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.aluvi.android.R;
import com.aluvi.android.activities.base.BaseToolBarActivity;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.packages.Callback;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by usama on 7/24/15.
 */
public class LoginActivity extends BaseToolBarActivity {
    @Bind(R.id.log_in_edit_text_username) EditText mUsernameEditText;
    @Bind(R.id.log_in_edit_text_password) EditText mPasswordEditText;

    private final int REGISTER_REQ_CODE = 4223;
    private Dialog mDefaultProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (UserStateManager.getInstance().getApiToken() != null) {
            onLoggedIn();
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDefaultProgressDialog != null) {
            mDefaultProgressDialog.cancel();
            mDefaultProgressDialog = null;
        }
    }

    @OnClick(R.id.login_button)
    public void onLogInButtonClicked() {
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        if (!"".equals(username) && !"".equals(password)) {
            mDefaultProgressDialog = DialogUtils.getDefaultProgressDialog(this, false);
            UserStateManager.getInstance().login(username, password, new Callback() {
                @Override
                public void success() {
                    if (mDefaultProgressDialog != null)
                        mDefaultProgressDialog.cancel();

                    onLoggedIn();
                }

                @Override
                public void failure(String message) {
                    if (mDefaultProgressDialog != null)
                        mDefaultProgressDialog.cancel();

                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, R.string.all_fields_required, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.login_button_register)
    public void onRegisterButtonClicked() {
        startActivityForResult(new Intent(this, OnboardingActivity.class), REGISTER_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REGISTER_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                onLoggedIn();
            }
        }
    }

    private void onLoggedIn() {
        startActivity(new Intent(LoginActivity.this, InitActivity.class));
        finish();
    }
}

