package com.aluvi.android.helpers.views;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by usama on 7/14/15.
 */
public class ViewUtils {
    public static void hideKeyboard(Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void hideKeyboardFragment(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressWarnings("deprecated")
    public static void registerOnLayoutCallback(final View panelRootView, final OnLayoutListener listener) {
        panelRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                panelRootView.getViewTreeObserver().removeGlobalOnLayoutListener(this); // Not REALLY deprecated (check the Android src)
                listener.onDimensReady(panelRootView.getWidth(), panelRootView.getHeight());
            }
        });
    }

    public interface OnLayoutListener {
        void onDimensReady(int width, int height);
    }
}
