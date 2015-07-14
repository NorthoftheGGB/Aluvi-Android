package com.aluvi.android.fragments;

import android.app.Dialog;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.aluvi.R;
import com.aluvi.android.helpers.AsyncCallback;
import com.aluvi.android.helpers.GeocoderUtils;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by usama on 7/13/15.
 */
public class LocationSelectDialogFragment extends DialogFragment
{
    @InjectView(R.id.location_select_edit_text_search) EditText mLocationSearchEditText;

    public static LocationSelectDialogFragment newInstance()
    {
        return new LocationSelectDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View rootView = View.inflate(getActivity(), R.layout.fragment_location_select, null);
        ButterKnife.inject(this, rootView);

        return new MaterialDialog.Builder(getActivity())
                .customView(rootView, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .build();
    }

    @OnClick(R.id.location_select_image_button_search)
    public void onLocationSearchClicked()
    {
        String enteredLocation = mLocationSearchEditText.getText().toString();
        if (!"".equals(enteredLocation))
        {
           GeocoderUtils.getAddressesForName(enteredLocation, 8, getActivity(), new AsyncCallback<List<Address>>()
           {
               @Override
               public void onOperationCompleted(List<Address> result)
               {

               }
           });
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
