package com.aluvi.android.fragments.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by usama on 7/13/15.
 */
public abstract class BaseButterFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = getRootView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        propagateActivityResult(requestCode, resultCode, data, this);
    }

    public void propagateActivityResult(int requestCode, int resultCode, Intent data, Fragment root) {
        List<Fragment> childFragments = root.getChildFragmentManager().getFragments();
        if (childFragments != null) {
            for (Fragment fragment : childFragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
                propagateActivityResult(requestCode, resultCode, data, fragment);
            }
        }
    }

    public abstract View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    public abstract void initUI();
}
