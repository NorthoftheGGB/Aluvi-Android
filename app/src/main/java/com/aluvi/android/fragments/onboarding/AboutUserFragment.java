package com.aluvi.android.fragments.onboarding;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aluvi.android.R;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.views.CameraImageRotationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    private final int PICTURE_CAPTURE_REQ_CODE = 981,
            GALLERY_REQ_CODE = 189;
    private final String IMAGE_PATH_INST_SAVE = "img_path_save",
            IMAGE_URI_SAVE = "img_uri_save";

    private String mCurrentPhotoPath;
    private Uri mCurrentPhotoUri;
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
        if (savedInstanceState != null) {
            mCurrentPhotoPath = savedInstanceState.getString(IMAGE_PATH_INST_SAVE);
            mCurrentPhotoUri = savedInstanceState.getParcelable(IMAGE_URI_SAVE);
        }

        return inflater.inflate(R.layout.fragment_about_user, container, false);
    }

    @Override
    public void initUI() {
        if (mCurrentPhotoPath != null)
            updateProfilePicture(mCurrentPhotoPath);
        else if (mCurrentPhotoUri != null)
            updateProfilePicture(mCurrentPhotoUri);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCurrentPhotoPath != null)
            outState.putString(IMAGE_PATH_INST_SAVE, mCurrentPhotoPath);
        else if (mCurrentPhotoUri != null)
            outState.putParcelable(IMAGE_URI_SAVE, mCurrentPhotoUri);
    }

    @OnClick(R.id.onboarding_about_button_next)
    public void onNextClicked() {
        mListener.onUserDetailsPopulated();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.onboarding_about_button_take_photo)
    public void onTakePhotoButtonClicked() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, PICTURE_CAPTURE_REQ_CODE);
            }
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.onboarding_about_button_choose_existing_photo)
    public void onExistingPhotoButtonClicked() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQ_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_CAPTURE_REQ_CODE) {
            mCurrentPhotoUri = null; // If the user selected a gallery image before, don't show that one again, especially not when restoring the app's state
            updateProfilePicture(mCurrentPhotoPath);
        } else if (requestCode == GALLERY_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                mCurrentPhotoPath = null; // If the user took a picture before, don't show that one again, especially not when restoring the app's state
                mCurrentPhotoUri = data.getData();
                updateProfilePicture(mCurrentPhotoUri);
            }
        }
    }

    public void updateProfilePicture(final Uri pictureUri) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    InputStream imageStream = getActivity().getContentResolver().openInputStream(pictureUri);
                    return BitmapFactory.decodeStream(imageStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                updateProfilePicture(bitmap);
            }
        }.execute();
    }

    public void updateProfilePicture(final String picturePath) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    return CameraImageRotationUtils.handleSamplingAndRotationBitmap(picturePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                updateProfilePicture(bitmap);
            }
        }.execute();
    }

    public void updateProfilePicture(Bitmap bitmap) {
        if (bitmap != null)
            mProfilePictureView.setImageBitmap(bitmap);
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + "/Aluvi");
        storageDir.mkdirs();

        try {
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            mCurrentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
