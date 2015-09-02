package com.aluvi.android.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.aluvi.android.R;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.callbacks.Callback;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by usama on 8/22/15.
 */
public class SupportFragment extends BaseButterFragment {
    @Bind(R.id.support_edit_text_message) EditText mSupportEditText;
    @Bind(R.id.support_button_submit) Button mSubmitButton;

    private Dialog mDefaultProgressDialog;

    public static SupportFragment newInstance() {
        return new SupportFragment();
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_support, container, false);
    }

    @Override
    public void initUI() {
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDefaultProgressDialog != null) {
            mDefaultProgressDialog.cancel();
            mDefaultProgressDialog = null;
        }
    }

    @SuppressWarnings("unused")
    @OnTextChanged(R.id.support_edit_text_message)
    public void onSupportTextChanged(CharSequence s, int start, int before, int count) {
        mSubmitButton.setEnabled(s.length() > 0);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.support_button_submit)
    public void onSubmitButtonClicked() {
        mDefaultProgressDialog = DialogUtils.getDefaultProgressDialog(getActivity(), false);

        String supportMessage = mSupportEditText.getText().toString();
        UserStateManager.getInstance().sendSupportMessage(supportMessage, new Callback() {
            @Override
            public void success() {
                if (mDefaultProgressDialog != null)
                    mDefaultProgressDialog.cancel();

                if (getView() != null)
                    Snackbar.make(getView(), R.string.successfully_sent_request, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void failure(String message) {
                if (mDefaultProgressDialog != null)
                    mDefaultProgressDialog.cancel();

                if (getView() != null)
                    Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
