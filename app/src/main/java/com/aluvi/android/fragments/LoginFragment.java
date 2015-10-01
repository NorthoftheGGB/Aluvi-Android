package com.aluvi.android.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.activities.OnboardingActivity;
import com.aluvi.android.application.push.PushManager;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.ProfileUtils;
import com.aluvi.android.helpers.views.FormUtils;
import com.aluvi.android.helpers.views.FormValidator;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.callbacks.Callback;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by usama on 9/30/15.
 */
public class LoginFragment extends BaseButterFragment {
    public interface LoginListener {
        void onLoggedIn();
    }

    @Bind(R.id.log_in_root_view) View mRootView;
    @Bind(R.id.log_in_edit_text_username) EditText mUsernameEditText;
    @Bind(R.id.log_in_edit_text_password) EditText mPasswordEditText;

    private LoginListener mListener;
    private final int REGISTER_REQ_CODE = 4223;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (LoginListener) activity;
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void initUI() {
        if (PushManager.updateGooglePlayServicesIfNeeded(getActivity()))
            if (UserStateManager.getInstance().getApiToken() != null)
                mListener.onLoggedIn();

        mUsernameEditText.setText(ProfileUtils.getUserEmailNumber(getActivity()));
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.log_in_register_button)
    public void onLogInButtonClicked() {
        boolean isFormValid = new FormValidator(getString(R.string.all_fields_required))
                .addField(mUsernameEditText, getString(R.string.email_error), FormUtils.getEmailValidator())
                .addField(mPasswordEditText)
                .validate();

        if (isFormValid) {
            String username = mUsernameEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();

            showDefaultProgressDialog();
            UserStateManager.getInstance().login(username, password, new UserStateManager.LoginCallback() {
                @Override
                public void onUserNotFound() {
                    cancelProgressDialogs();
                    showNewUserDialog();
                }

                @Override
                public void success() {
                    cancelProgressDialogs();
                    mListener.onLoggedIn();
                }

                @Override
                public void failure(String message) {
                    cancelProgressDialogs();
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Snackbar.make(mRootView, R.string.all_fields_required, Snackbar.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.log_in_text_view_forgot_password)
    public void onForgotPasswordButtonClicked() {
        View resetPasswordView = View.inflate(getActivity(), R.layout.dialog_reset_password, null);
        final EditText passwordResetEmailEditText = (EditText) resetPasswordView.findViewById(R.id.reset_password_edit_text_email);
        new MaterialDialog.Builder(getActivity())
                .title(R.string.forgot_password)
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
                            Toast.makeText(getActivity(), R.string.error_invalid_email, Toast.LENGTH_LONG).show();
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
        showDefaultProgressDialog();
        UserStateManager.getInstance().sendPasswordResetEmail(email, new Callback() {
            @Override
            public void success() {
                if (mRootView != null)
                    Snackbar.make(mRootView, "Sent password reset link to " + email, Snackbar.LENGTH_SHORT).show();
                cancelProgressDialogs();
            }

            @Override
            public void failure(String message) {
                if (mRootView != null)
                    Snackbar.make(mRootView, message, Snackbar.LENGTH_SHORT).show();
                cancelProgressDialogs();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REGISTER_REQ_CODE)
            if (resultCode == Activity.RESULT_OK)
                mListener.onLoggedIn();
    }

    private void showNewUserDialog() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.new_user)
                .content(R.string.email_not_found)
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.register)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        onRegisterClicked();
                    }
                })
                .show();
    }

    private void onRegisterClicked() {
        Intent onboardingIntent = new Intent(getActivity(), OnboardingActivity.class);
        onboardingIntent.putExtra(OnboardingActivity.EMAIL_KEY, mUsernameEditText.getText().toString());
        onboardingIntent.putExtra(OnboardingActivity.PASSWORD_KEY, mPasswordEditText.getText().toString());
        startActivityForResult(onboardingIntent, REGISTER_REQ_CODE);
    }
}
