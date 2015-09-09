package com.aluvi.android.helpers.views;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;

/**
 * Created by usama on 7/21/15.
 */
public class DialogUtils {
    public static MaterialDialog showDefaultProgressDialog(Context context) {
        return showDefaultProgressDialog(context, true);
    }

    public static MaterialDialog showDefaultProgressDialog(Context context, boolean canceleable) {
        return new MaterialDialog.Builder(context)
                .progress(true, 0)
                .title(R.string.loading)
                .content(R.string.please_wait)
                .cancelable(canceleable)
                .show();
    }

    public static MaterialDialog showCustomProgressDialog(Context context, String title, String message, boolean cancelable) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(message)
                .progress(true, 0)
                .cancelable(cancelable)
                .show();
    }
}
