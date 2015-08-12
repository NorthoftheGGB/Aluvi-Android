package com.aluvi.android.fragments.onboarding;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aluvi.android.R;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.AsyncCallback;
import com.aluvi.android.helpers.CameraHelper;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by usama on 8/6/15.
 */
public class AboutUserFragment extends BaseButterFragment {

    public interface AboutUserListener {
        void onUserDetailsPopulated();
    }

    @Bind(R.id.onboarding_image_view_profile_picture) ImageView mProfilePictureView;

    private CameraHelper mCameraHelper;
    private AboutUserListener mListener;

    public static AboutUserFragment newInstance() {
        return new AboutUserFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (AboutUserListener) activity;
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCameraHelper = new CameraHelper("AboutUsFragment", getActivity());
        mCameraHelper.restore(savedInstanceState);
        return inflater.inflate(R.layout.fragment_about_user, container, false);
    }

    @Override
    public void initUI() {
        mCameraHelper.restoreSavedBitmaps(new AsyncCallback<Bitmap>() {
            @Override
            public void onOperationCompleted(Bitmap result) {
                updateProfilePicture(result);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mCameraHelper.save(outState);
    }

    @OnClick(R.id.onboarding_about_button_next)
    public void onNextClicked() {
        mListener.onUserDetailsPopulated();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.onboarding_about_button_take_photo)
    public void onTakePhotoButtonClicked() {
        if (!mCameraHelper.takePictureCamera(this)) {
            Snackbar.make(getView(), R.string.camera_unavailable, Snackbar.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.onboarding_about_button_choose_existing_photo)
    public void onExistingPhotoButtonClicked() {
        mCameraHelper.takePictureGallery(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCameraHelper.onActivityResult(requestCode, resultCode, data, new AsyncCallback<Bitmap>() {
            @Override
            public void onOperationCompleted(Bitmap result) {
                updateProfilePicture(result);
            }
        });
    }


    public void updateProfilePicture(Bitmap bitmap) {
        if (bitmap != null && mProfilePictureView != null)
            mProfilePictureView.setImageBitmap(bitmap);
    }
}
