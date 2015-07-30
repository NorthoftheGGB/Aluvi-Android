package com.aluvi.android.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.aluvi.android.R;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.managers.UserStateManager;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by usama on 7/24/15.
 */
public class LoginActivity extends BaseToolBarActivity {
    @Bind(R.id.log_in_edit_text_username) EditText mUsernameEditText;
    @Bind(R.id.log_in_edit_text_password) EditText mPasswordEditText;

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

    @OnClick(R.id.login_button)
    public void onLogInButtonClicked() {
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        if (!"".equals(username) && !"".equals(password)) {
            final Dialog progressDialog = DialogUtils.getDefaultProgressDialog(this, false);
            UserStateManager.getInstance().login(username, password, new UserStateManager.Callback() {
                @Override
                public void success() {
                    if (progressDialog != null)
                        progressDialog.cancel();

                    onLoggedIn();
                }

                @Override
                public void failure(String message) {
                    if (progressDialog != null)
                        progressDialog.cancel();

                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, R.string.all_fields_required, Toast.LENGTH_SHORT).show();
        }
    }

    private void onLoggedIn() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}

