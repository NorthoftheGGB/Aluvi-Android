package com.aluvi.android.helpers.views;

import android.app.Dialog;
import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;

/**
 * Created by usama on 7/21/15.
 */
public class DialogUtils {
    public static Dialog getDefaultProgressDialog(Context context) {
        return getDefaultProgressDialog(context, true);
    }

    public static Dialog getDefaultProgressDialog(Context context, boolean canceleable) {
        return new MaterialDialog.Builder(context)
                .progress(true, 0)
                .title(R.string.loading)
                .content(R.string.please_wait)
                .cancelable(canceleable)
                .show();
    }
}
