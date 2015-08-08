package com.aluvi.android.fragments.onboarding;


import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.aluvi.android.R;
import com.aluvi.android.api.users.models.UserRegistrationData;
import com.aluvi.android.fragments.BaseButterFragment;
import com.aluvi.android.helpers.views.FormValidator;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by usama on 8/06/15.
 */
public class RegisterFragment extends BaseButterFragment {
    public interface RegistrationListener {
        void onRegistered(UserRegistrationData userRegistrationData);
    }

    @Bind(R.id.register_edit_text_first_name) EditText mFirstNameEditText;
    @Bind(R.id.register_edit_text_last_name) EditText mLastNameEditText;
    @Bind(R.id.register_edit_text_email) EditText mEmailEditText;
    @Bind(R.id.register_edit_text_password) EditText mPasswordEditText;
    @Bind(R.id.register_edit_text_confirm_password) EditText mConfirmPasswordEditText;
    @Bind(R.id.register_check_box_interested_driver) CheckBox mInterestedDriverCheckBox;
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
        if (validateForm()) {
            mRegistrationListener.onRegistered(initRegistrationData());
        }
    }

    private boolean validateForm() {
        return new FormValidator(getString(R.string.field_required_error))
                .addField(mFirstNameEditText)
                .addField(mLastNameEditText)
                .addField(mEmailEditText, getString(R.string.error_invalid_email),
                        new FormValidator.Validator() {
                            @Override
                            public boolean isValid(String input) {
                                return !"".equals(input) && isValidEmail(input);
                            }
                        })
                .addField(mPasswordEditText)
                .addField(mConfirmPasswordEditText, getString(R.string.confirm_password_error),
                        new FormValidator.Validator() {
                            @Override
                            public boolean isValid(String input) {
                                return input.equals(mPasswordEditText.getText().toString());
                            }
                        })
                .validate();
    }

    private UserRegistrationData initRegistrationData() {
        UserRegistrationData data = new UserRegistrationData();
        data.setFirstName(mFirstNameEditText.getText().toString());
        data.setLastName(mLastNameEditText.getText().toString());
        data.setEmail(mEmailEditText.getText().toString());
        data.setPassword(mPasswordEditText.getText().toString());
        data.setIsInterestedDriver(mInterestedDriverCheckBox.isChecked());
        return data;
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
