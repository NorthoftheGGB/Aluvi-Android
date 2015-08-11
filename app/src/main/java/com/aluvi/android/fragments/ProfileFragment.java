package com.aluvi.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.aluvi.android.R;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.helpers.views.FormUtils;
import com.aluvi.android.helpers.views.FormValidator;
import com.aluvi.android.managers.packages.Callback;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.model.realm.Profile;

import butterknife.Bind;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

/**
 * Created by usama on 8/8/15.
 */
public class ProfileFragment extends BaseButterFragment {

    @Bind(R.id.profile_image_view) CircleImageView mProfileImageView;
    @Bind(R.id.profile_text_view_name) TextView mNameTextView;
    @Bind(R.id.profile_edit_text_email) EditText mEmailEditText;
    @Bind(R.id.profile_edit_text_phone_number) EditText mPhoneNumberEditText;
    @Bind(R.id.profile_edit_text_work_email) EditText mWorkEmailEditText;

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void initUI() {
        Profile profile = UserStateManager.getInstance().getProfile();
        mNameTextView.setText(profile.getFirstName() + " " + profile.getLastName());
        mEmailEditText.setText(profile.getEmail());
        mPhoneNumberEditText.setText(profile.getPhone());
    }

    @OnClick(R.id.profile_button_save)
    public void saveButtonClicked() {
        if (isFormValid()) {
            AluviRealm.getDefaultRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Profile profile = UserStateManager.getInstance().getProfile();
                    profile.setEmail(mEmailEditText.getText().toString());
                    profile.setPhone(mPhoneNumberEditText.getText().toString());
                }
            });

            UserStateManager.getInstance().saveProfile(new Callback() {
                @Override
                public void success() {
                    if (getView() != null)
                        Snackbar.make(getView(), R.string.profile_saved, Snackbar.LENGTH_SHORT).show();
                }

                @Override
                public void failure(String message) {
                    if (getView() != null)
                        Snackbar.make(getView(), R.string.save_error, Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

    public boolean isFormValid() {
        return new FormValidator(getString(R.string.all_fields_required))
                .addField(mEmailEditText, getString(R.string.error_invalid_email),
                        new FormValidator.Validator() {
                            @Override
                            public boolean isValid(String input) {
                                return FormUtils.isValidEmail(input);
                            }
                        })
                .addField(mPhoneNumberEditText, getString(R.string.error_invalid_phone), FormUtils.getPhoneValidator())
                .validate();
    }
}
