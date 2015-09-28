package com.aluvi.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.aluvi.android.R;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.callbacks.Callback;

import butterknife.Bind;

/**
 * Created by usama on 8/22/15.
 */
public class AluviSupportFragment extends BaseButterFragment {
    @Bind(R.id.support_edit_text_message) EditText mSupportEditText;

    public static AluviSupportFragment newInstance() {
        return new AluviSupportFragment();
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_support, container, false);
    }

    @Override
    public void initUI() {
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelProgressDialogs();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_support, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_request_support:
                onSubmitButtonClicked();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSubmitButtonClicked() {
        showDefaultProgressDialog();
        String supportMessage = mSupportEditText.getText().toString();
        UserStateManager.getInstance().sendSupportMessage(supportMessage, new Callback() {
            @Override
            public void success() {
                cancelProgressDialogs();
                if (getView() != null)
                    Snackbar.make(getView(), R.string.successfully_sent_request, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void failure(String message) {
                cancelProgressDialogs();
                if (getView() != null)
                    Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
