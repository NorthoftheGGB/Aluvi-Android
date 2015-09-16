package com.aluvi.android.fragments.onboarding;


import android.app.Activity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.aluvi.android.R;
import com.aluvi.android.api.users.models.ProfileData;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.ProfileUtils;
import com.aluvi.android.helpers.views.FormUtils;
import com.aluvi.android.helpers.views.FormValidator;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by usama on 8/06/15.
 */
public class RegisterFragment extends BaseButterFragment {
    public interface RegistrationListener {
        void onRegistered(ProfileData profileData);
    }

    @Bind(R.id.register_edit_text_first_last_name) EditText mFullNameEditText;
    @Bind(R.id.register_edit_text_phone_number) EditText mPhoneNumberEditText;
    @Bind(R.id.register_edit_text_work_email) EditText mWorkEmailEditText;

    private final static String DATA_KEY = "profile_data";
    private ProfileData mProfileData;
    private RegistrationListener mRegistrationListener;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    public static RegisterFragment newInstance(ProfileData data) {
        Bundle args = new Bundle();
        args.putParcelable(DATA_KEY, data);

        RegisterFragment fragment = new RegisterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public RegisterFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mRegistrationListener = (RegistrationListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            mProfileData = getArguments().getParcelable(DATA_KEY);
    }

    @Override
    public View getRootView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void initUI() {
        mPhoneNumberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        mPhoneNumberEditText.setText(ProfileUtils.getUSUserPhoneNumber(getActivity()));
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.register_button_sign_up)
    public void onSignUpButtonClicked() {
        if (validateForm())
            mRegistrationListener.onRegistered(initRegistrationData());
    }

    private boolean validateForm() {
        return new FormValidator(getString(R.string.field_required_error))
                .addField(mFullNameEditText, getString(R.string.full_name),
                        new FormValidator.Validator() {
                            @Override
                            public boolean isValid(String input) {
                                return input.split(" ").length > 1;
                            }
                        })
                .addField(mPhoneNumberEditText, getString(R.string.error_invalid_phone), FormUtils.getPhoneValidator())
                .addField(mWorkEmailEditText, FormUtils.getEmailValidator())
                .validate();
    }

    private ProfileData initRegistrationData() {
        String fullName = mFullNameEditText.getText().toString();
        String firstName = fullName.split(" ")[0];
        String lastName = fullName.split(" ")[1];

        ProfileData data = mProfileData == null ? new ProfileData() : mProfileData;
        data.setFirstName(firstName);
        data.setLastName(lastName);
        data.setPhoneNumber(mPhoneNumberEditText.getText().toString());
        data.setWorkEmail(mWorkEmailEditText.getText().toString());
        return data;
    }
}
