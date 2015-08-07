package com.aluvi.android.fragments.onboarding;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.aluvi.android.R;
import com.aluvi.android.fragments.BaseButterFragment;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by usama on 8/06/15.
 */
public class RegisterFragment extends BaseButterFragment {

    public interface RegistrationListener {
        void onRegistered();
    }

    @Bind(R.id.register_edit_text_email) EditText mEmailEditText;
    @Bind(R.id.register_edit_text_password) EditText mPasswordEditText;
    @Bind(R.id.register_edit_text_confirm_password) EditText mConfirmPasswordEditText;
    @Bind(R.id.register_button_sign_up) Button mSignUpButton;

    private RegistrationListener mRegistrationListener;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    public RegisterFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mRegistrationListener = (RegistrationListener) activity;
    }

    @Override
    public View getRootView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void initUI() {
    }

    @OnClick(R.id.register_button_sign_up)
    public void onSignUpButtonClicked() {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String confirmationPassword = mConfirmPasswordEditText.getText().toString();

        if (showRegistrationErrors(email, password, confirmationPassword))
            mRegistrationListener.onRegistered();
    }

    private boolean showRegistrationErrors(String email, String password, String confirmPassword) {
        boolean errorFree = true;

        if (email.equals("")) {
            mEmailEditText.setError(getString(R.string.email_error));
            errorFree = false;
        }

        if (password.equals("")) {
            mPasswordEditText.setError(getString(R.string.password_error));
            errorFree = false;
        }

        if (!confirmPassword.equals(password)) {
            mConfirmPasswordEditText.setError(getString(R.string.confirm_password_error));
            errorFree = false;
        }

        return errorFree;
    }
}
