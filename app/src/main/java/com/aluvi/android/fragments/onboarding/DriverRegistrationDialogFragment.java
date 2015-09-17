package com.aluvi.android.fragments.onboarding;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.callbacks.Callback;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by usama on 8/15/15.
 */
public class DriverRegistrationDialogFragment extends DialogFragment {
    @Bind(R.id.dialog_register_driver_root_view) View mRegisterDriverRootView;
    private Dialog mDefaultProgressDialog;

    public static DriverRegistrationDialogFragment newInstance() {
        return new DriverRegistrationDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = View.inflate(getActivity(), R.layout.dialog_fragment_register_driver, null);
        ButterKnife.bind(this, rootView);

        final DriverInfoUIHelper helper = new DriverInfoUIHelper(mRegisterDriverRootView);
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.car_details)
                .customView(rootView, true)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.no)
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(final MaterialDialog dialog) {
                        if (helper.validateForm()) {
                            mDefaultProgressDialog = DialogUtils.showDefaultProgressDialog(getActivity(), false);
                            UserStateManager.getInstance().registerDriver(helper.initData(), new Callback() {
                                @Override
                                public void success() {
                                    if (mDefaultProgressDialog != null)
                                        mDefaultProgressDialog.cancel();

                                    if (!isDetached())
                                        dismiss();
                                }

                                @Override
                                public void failure(String message) {
                                    if (mDefaultProgressDialog != null)
                                        mDefaultProgressDialog.cancel();

                                    if (getView() != null)
                                        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
                                }
                            });

                            /*
                            UserStateManager.getInstance().saveCarInfo(helper.initCarData(), new Callback() {
                                @Override
                                public void success() {
                                    if (mDefaultProgressDialog != null)
                                        mDefaultProgressDialog.cancel();

                                    if (!isDetached())
                                        dismiss();
                                }

                                @Override
                                public void failure(String message) {
                                    if (mDefaultProgressDialog != null)
                                        mDefaultProgressDialog.cancel();

                                    if (getView() != null)
                                        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
                                }
                            }); */
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dismiss();
                    }
                }).build();
    }

    @Override
    public void onDestroyView() {
        if (mDefaultProgressDialog != null)
            mDefaultProgressDialog.cancel();

        ButterKnife.unbind(this);
        super.onDestroyView();
    }
}
