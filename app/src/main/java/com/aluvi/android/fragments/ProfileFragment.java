package com.aluvi.android.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.aluvi.android.R;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.AsyncCallback;
import com.aluvi.android.helpers.CameraHelper;
import com.aluvi.android.helpers.views.FormUtils;
import com.aluvi.android.helpers.views.FormValidator;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.callbacks.Callback;
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
    @Bind(R.id.profile_text_view_version_number) TextView mVersionNumberTextView;
    @Bind(R.id.profile_edit_text_email) EditText mEmailEditText;
    @Bind(R.id.profile_edit_text_work_email) EditText mWorkEmailEditText;
    @Bind(R.id.profile_edit_text_password) EditText mPasswordEditText;

    private CameraHelper mCameraHelper;
    private ProfileListener mListener;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (ProfileListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mCameraHelper = new CameraHelper(getActivity());
        mCameraHelper.restore(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
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
        mWorkEmailEditText.setText(profile.getWorkEmail());
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

        try {
            String versionName = "v" + getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0).versionName;
            mVersionNumberTextView.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadProfilePhoto(Profile profile) {
        Picasso.with(getActivity()).load(profile.getSmallImageUrl())
                .fit().centerCrop()
                .placeholder(R.mipmap.profile_picture_placeholder)
                .error(R.mipmap.profile_picture_placeholder)
                .into(mProfileImageView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mCameraHelper.save(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveButtonClicked();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveButtonClicked() {
        if (isFormValid()) {
            String newPassword = mPasswordEditText.getText().toString();
            if (!newPassword.equals("") && !newPassword.equals(getString(R.string.placeholder_password))) {
                showPasswordAuthDialog();
            } else {
                onPasswordVerified();
            }
        }
    }

    private void showPasswordAuthDialog() {
        View passwordView = View.inflate(getActivity(), R.layout.layout_confirm_password, null);
        final EditText passwordEditText = (EditText) passwordView.findViewById(R.id.confirm_password_edit_text);
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.verify_password)
                .setView(passwordView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        String password = passwordEditText.getText().toString();
                        String email = UserStateManager.getInstance().getProfile().getEmail();

                        showDefaultProgressDialog();
                        UserStateManager.getInstance()
                                .login(email, password, new UserStateManager.LoginCallback() {
                                    @Override
                                    public void onUserNotFound() {
                                        cancelProgressDialogs();
                                    }

                                    @Override
                                    public void success() {
                                        cancelProgressDialogs();
                                        if (getView() != null)
                                            onPasswordVerified();
                                    }

                                    @Override
                                    public void failure(String message) {
                                        cancelProgressDialogs();
                                        if (getView() != null) {
                                            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                })
                .show();
    }

    private void onPasswordVerified() {
        AluviRealm.getDefaultRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Profile profile = UserStateManager.getInstance().getProfile();
                profile.setEmail(mEmailEditText.getText().toString());
                profile.setWorkEmail(mWorkEmailEditText.getText().toString());
                profile.setPassword(mPasswordEditText.getText().toString());
            }
        });

        showDefaultProgressDialog();
        UserStateManager.getInstance().saveProfile(new Callback() {
            @Override
            public void success() {
                cancelProgressDialogs();
                if (getView() != null)
                    Snackbar.make(getView(), R.string.profile_saved, Snackbar.LENGTH_SHORT).show();

                mListener.onProfileSavedListener();
            }

            @Override
            public void failure(String message) {
                cancelProgressDialogs();

                if (getView() != null)
                    Snackbar.make(getView(), R.string.save_error, Snackbar.LENGTH_SHORT).show();
            }
        });
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
                .addField(mEmailEditText, getString(R.string.error_invalid_email), FormUtils.getEmailValidator())
                .addField(mWorkEmailEditText, getString(R.string.error_invalid_email), FormUtils.getEmailValidator())
                .addField(mPasswordEditText)
                .validate();
    }
}
