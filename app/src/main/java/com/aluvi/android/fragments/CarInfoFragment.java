package com.aluvi.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aluvi.android.R;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.fragments.onboarding.DriverInfoUIHelper;
import com.aluvi.android.model.realm.Profile;

import butterknife.Bind;

/**
 * Created by usama on 8/18/15.
 */
public class CarInfoFragment extends BaseButterFragment {
    public interface CarInfoListener {
        void onInfoSaved();
    }

    @Bind(R.id.car_profile_image_view) ImageView mCarProfileImageView;

    private DriverInfoUIHelper mInfoUIHelper;
    private CarInfoListener mListener;

    public static CarInfoFragment newInstance() {
        return new CarInfoFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (CarInfoListener) activity;
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_car_info, container, false);
    }

    @Override
    public void initUI() {
        Profile userProfile = AluviRealm.getDefaultRealm().where(Profile.class).findFirst();

        mInfoUIHelper = new DriverInfoUIHelper(getView());
        mInfoUIHelper.updateData(userProfile);
    }

    @Override
    public void onDestroyView() {
        mInfoUIHelper.destroy();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_car_info, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveCarInfo();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveCarInfo() {
        mListener.onInfoSaved();
    }
}
