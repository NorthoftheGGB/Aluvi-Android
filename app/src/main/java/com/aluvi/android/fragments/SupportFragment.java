package com.aluvi.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluvi.android.R;
import com.aluvi.android.fragments.base.BaseButterFragment;

/**
 * Created by usama on 8/22/15.
 */
public class SupportFragment extends BaseButterFragment {
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
}
