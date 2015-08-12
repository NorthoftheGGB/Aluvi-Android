package com.aluvi.android.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import com.aluvi.android.helpers.views.CameraImageRotationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by usama on 8/11/15.
 */
public class CameraHelper {
    private final int PICTURE_CAPTURE_REQ_CODE = 981,
            GALLERY_REQ_CODE = 189;
    private final String IMAGE_PATH_INST_SAVE = "img_path_save",
            IMAGE_URI_SAVE = "img_uri_save";

    private String mCurrentPhotoPath, mId;
    private Uri mCurrentPhotoUri;
    private Context mContext;

    public CameraHelper(String id, Context context) {
        mId = id;
        mContext = context;
    }

    public void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentPhotoPath = savedInstanceState.getString(mId + IMAGE_PATH_INST_SAVE);
            mCurrentPhotoUri = savedInstanceState.getParcelable(mId + IMAGE_URI_SAVE);
        }
    }

    public void restoreSavedBitmaps(final AsyncCallback<Bitmap> savedBitmapCallback) {
        if (mCurrentPhotoPath != null)
            loadBitmap(mCurrentPhotoPath, savedBitmapCallback);
        else if (mCurrentPhotoUri != null)
            loadBitmap(mCurrentPhotoUri, mContext, savedBitmapCallback);
    }

    public void save(Bundle outState) {
        if (mCurrentPhotoPath != null)
            outState.putString(mId + IMAGE_PATH_INST_SAVE, mCurrentPhotoPath);
        else if (mCurrentPhotoUri != null)
            outState.putParcelable(mId + IMAGE_URI_SAVE, mCurrentPhotoUri);
    }

    public boolean takePictureCamera(Activity activity) {
        Intent pictureIntent = getTakePictureIntent();
        if (pictureIntent != null) {
            activity.startActivityForResult(pictureIntent, PICTURE_CAPTURE_REQ_CODE);
            return true;
        }

        return false;
    }

    public boolean takePictureCamera(Fragment fragment) {
        Intent pictureIntent = getTakePictureIntent();
        if (pictureIntent != null) {
            fragment.startActivityForResult(pictureIntent, PICTURE_CAPTURE_REQ_CODE);
            return true;
        }

        return false;
    }

    private Intent getTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            File photoFile = createImageFile();
            mCurrentPhotoPath = photoFile.getAbsolutePath();

            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                return takePictureIntent;
            }
        }

        return null;
    }

    public void takePictureGallery(Activity activity) {
        activity.startActivityForResult(getGalleryPictureIntent(), GALLERY_REQ_CODE);
    }

    public void takePictureGallery(Fragment fragment) {
        fragment.startActivityForResult(getGalleryPictureIntent(), GALLERY_REQ_CODE);
    }

    private Intent getGalleryPictureIntent() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        return photoPickerIntent;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data, final AsyncCallback<Bitmap> pictureLoadCallback) {
        if (requestCode == PICTURE_CAPTURE_REQ_CODE) {
            mCurrentPhotoUri = null; // If the user selected a gallery image before, don't show that one again, especially not when restoring the app's state
            loadBitmap(mCurrentPhotoPath, pictureLoadCallback);
        } else if (requestCode == GALLERY_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                mCurrentPhotoPath = null; // If the user took a picture before, don't show that one again, especially not when restoring the app's state
                mCurrentPhotoUri = data.getData();

                /**
                 * TODO Fix URI image rotation
                 * Horrible, crappy way to copy the image from gallery to external file directory and then
                 * performing image rotation crap.
                 */
                loadBitmap(mCurrentPhotoUri, mContext, new AsyncCallback<Bitmap>() {
                    @Override
                    public void onOperationCompleted(Bitmap result) {
                        final File saveLoc = createImageFile();
                        asyncSaveBitmap(saveLoc, result, new AsyncCallback<Boolean>() {
                            @Override
                            public void onOperationCompleted(Boolean result) {
                                if (result) {
                                    loadBitmap(saveLoc.getAbsolutePath(), new AsyncCallback<Bitmap>() {
                                        @Override
                                        public void onOperationCompleted(Bitmap result) {
                                            pictureLoadCallback.onOperationCompleted(result);
                                        }
                                    });
                                } else {
                                    pictureLoadCallback.onOperationCompleted(null);
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    private static void loadBitmap(final Uri pictureUri, final Context context, final AsyncCallback<Bitmap> callback) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    InputStream imageStream = context.getContentResolver().openInputStream(pictureUri);
                    return BitmapFactory.decodeStream(imageStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                callback.onOperationCompleted(bitmap);
            }
        }.execute();
    }

    private static void loadBitmap(final String picturePath, final AsyncCallback<Bitmap> callback) {
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
                callback.onOperationCompleted(bitmap);
            }
        }.execute();
    }

    private static void asyncSaveBitmap(final File file, final Bitmap bitmap, final AsyncCallback<Boolean> onCompletedCallback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return saveBitmap(file, bitmap);
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                onCompletedCallback.onOperationCompleted(aBoolean);
            }
        };
    }

    private static boolean saveBitmap(File file, Bitmap bitmap) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private static File createImageFile() {
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

            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
