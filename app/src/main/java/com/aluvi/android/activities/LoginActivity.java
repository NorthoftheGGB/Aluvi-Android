package com.aluvi.android.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.activities.base.BaseToolBarActivity;
import com.aluvi.android.application.push.PushManager;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.helpers.views.FormUtils;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.callbacks.Callback;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by usama on 7/24/15.
 */
public class LoginActivity extends BaseToolBarActivity {
    @Bind(R.id.log_in_root_view) View mRootView;
    @Bind(R.id.log_in_edit_text_username) EditText mUsernameEditText;
    @Bind(R.id.log_in_edit_text_password) EditText mPasswordEditText;
    @Bind(R.id.log_in_button) Button mLoginButton;

    private final int REGISTER_REQ_CODE = 4223;
    private Dialog mDefaultProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PushManager.updateGooglePlayServicesIfNeeded(this)) {
            if (UserStateManager.getInstance().getApiToken() != null) {
                onLoggedIn();
            }
        }

        super.onCreate(savedInstanceState);
        mLoginButton.setEnabled(false);
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

    @SuppressWarnings("unused")
    @OnTextChanged(R.id.log_in_edit_text_password)
    public void onPasswordTextChanged(CharSequence newText) {
        mLoginButton.setEnabled(newText.length() > 0 && mUsernameEditText.getText().length() > 0);
    }

    @SuppressWarnings("unused")
    @OnTextChanged(R.id.log_in_edit_text_username)
    public void onEmailTextChanged(CharSequence newText) {
        mLoginButton.setEnabled(newText.length() > 0 && mPasswordEditText.getText().length() > 0);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.log_in_button)
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

    @SuppressWarnings("unused")
    @OnClick(R.id.login_button_register)
    public void onRegisterButtonClicked() {
        startActivityForResult(new Intent(this, OnboardingActivity.class), REGISTER_REQ_CODE);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.log_in_text_view_forgot_password)
    public void onForgotPasswordButtonClicked() {
        View resetPasswordView = View.inflate(this, R.layout.dialog_reset_password, null);
        final EditText passwordResetEmailEditText = (EditText) resetPasswordView.findViewById(R.id.reset_password_edit_text_email);
        new MaterialDialog.Builder(this)
                .title(R.string.reset_password)
                .customView(resetPasswordView, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.no)
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        String providedEmail = passwordResetEmailEditText.getText().toString();
                        if (FormUtils.isValidEmail(providedEmail)) {
                            dialog.dismiss();
                            sendPasswordResetEmail(providedEmail);
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.error_invalid_email, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void sendPasswordResetEmail(final String email) {
        mDefaultProgressDialog = DialogUtils.getDefaultProgressDialog(this, false);
        UserStateManager.getInstance().sendPasswordResetEmail(email, new Callback() {
            @Override
            public void success() {
                if (mDefaultProgressDialog != null)
                    mDefaultProgressDialog.cancel();

                if (mRootView != null)
                    Snackbar.make(mRootView, "Sent password reset link to " + email, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void failure(String message) {
                if (mDefaultProgressDialog != null)
                    mDefaultProgressDialog.cancel();

                if (mRootView != null)
                    Snackbar.make(mRootView, message, Snackbar.LENGTH_SHORT).show();
            }
        });
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

