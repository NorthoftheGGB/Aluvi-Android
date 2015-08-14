package com.aluvi.android.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.AsyncCallback;
import com.aluvi.android.helpers.CameraHelper;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.helpers.views.FormUtils;
import com.aluvi.android.helpers.views.FormValidator;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.packages.Callback;
import com.aluvi.android.model.realm.Profile;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

/**
 * Created by usama on 8/8/15.
 */
public class ProfileFragment extends BaseButterFragment {
    public interface ProfileListener {
        void onProfileSavedListener();
    }

    @Bind(R.id.profile_image_view) CircleImageView mProfileImageView;
    @Bind(R.id.profile_text_view_name) TextView mNameTextView;
    @Bind(R.id.profile_edit_text_email) EditText mEmailEditText;
    @Bind(R.id.profile_edit_text_phone_number) EditText mPhoneNumberEditText;
    @Bind(R.id.profile_edit_text_work_email) EditText mWorkEmailEditText;

    private CameraHelper mCameraHelper;
    private ProfileListener mListener;
    private Dialog mDefaultProgressDialog;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (ProfileListener) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraHelper = new CameraHelper(getActivity());
        mCameraHelper.restore(savedInstanceState);
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void initUI() {
        final Profile profile = UserStateManager.getInstance().getProfile();
        mNameTextView.setText(profile.getFirstName() + " " + profile.getLastName());
        mEmailEditText.setText(profile.getEmail());
        mPhoneNumberEditText.setText(profile.getPhone());

        mCameraHelper.restoreSavedBitmaps(new AsyncCallback<Bitmap>() {
            @Override
            public void onOperationCompleted(Bitmap result) {
                if (mProfileImageView != null) {
                    if (result != null)
                        mProfileImageView.setImageBitmap(result);
                    else
                        loadProfilePhoto(profile);
                }
            }
        });
    }

    private void loadProfilePhoto(Profile profile) {
        Picasso.with(getActivity()).load(profile.getSmallImageUrl())
                .fit().centerCrop()
                .placeholder(R.mipmap.test_profile_pic)
                .error(R.mipmap.test_profile_pic)
                .into(mProfileImageView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mCameraHelper.save(outState);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mDefaultProgressDialog != null) {
            mDefaultProgressDialog.dismiss();
            mDefaultProgressDialog = null;
        }
    }

    @SuppressWarnings("unused")
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

            mDefaultProgressDialog = DialogUtils.getDefaultProgressDialog(getActivity(), true);
            UserStateManager.getInstance().saveProfile(new Callback() {
                @Override
                public void success() {
                    if (getView() != null)
                        Snackbar.make(getView(), R.string.profile_saved, Snackbar.LENGTH_SHORT).show();

                    if (mDefaultProgressDialog != null)
                        mDefaultProgressDialog.dismiss();

                    mListener.onProfileSavedListener();
                }

                @Override
                public void failure(String message) {
                    if (getView() != null)
                        Snackbar.make(getView(), R.string.save_error, Snackbar.LENGTH_SHORT).show();

                    if (mDefaultProgressDialog != null)
                        mDefaultProgressDialog.dismiss();
                }
            });
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.profile_image_view)
    public void onProfilePictureClicked() {
        mCameraHelper.takePictureGallery(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCameraHelper.onActivityResult(requestCode, resultCode, data, new AsyncCallback<Bitmap>() {
            @Override
            public void onOperationCompleted(Bitmap result) {
                if (mProfileImageView != null && result != null) {
                    mProfileImageView.setImageBitmap(result);
                    String currentPhotoPath = mCameraHelper.getCurrentPhotoPath();
                    updateSavedProfilePhoto(currentPhotoPath);
                }
            }
        });
    }

    private void updateSavedProfilePhoto(String profilePicturePath) {
        Profile profileToEdit = UserStateManager.getInstance().getProfile();
        profileToEdit.setProfilePicturePath(profilePicturePath);
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
