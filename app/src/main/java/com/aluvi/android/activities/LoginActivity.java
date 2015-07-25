package com.aluvi.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import com.aluvi.android.R;
import com.aluvi.android.managers.UserStateManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by usama on 7/24/15.
 */
public class LoginActivity extends AppCompatActivity {
    @Bind(R.id.login_edit_text_username) EditText mUsernameEditText;
    @Bind(R.id.log_in_edit_text_password) EditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UserStateManager.getInstance().getApiToken() != null) {
            onLoggedIn();
        } else {
            setContentView(R.layout.activity_login);
            ButterKnife.bind(this);
        }
    }

    @OnClick(R.id.login_button)
    public void onLogInButtonClicked() {
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        if (!"".equals(username) && !"".equals(password)) {
            UserStateManager.getInstance().login(username, password, new UserStateManager.Callback() {
                @Override
                public void success() {
                    onLoggedIn();
                }

                @Override
                public void failure(String message) {
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

