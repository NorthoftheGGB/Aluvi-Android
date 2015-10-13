package com.aluvi.android.fragments.onboarding;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluvi.android.R;
import com.aluvi.android.fragments.base.BaseButterFragment;

import butterknife.OnClick;

/**
 * Created by usama on 8/7/15.
 */
public class WelcomeFragment extends BaseButterFragment {
    public interface WelcomeListener {
        void onTutorialComplete();
    }

    private WelcomeListener mWelcomeListener;

    public static WelcomeFragment newInstance() {
        return new WelcomeFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mWelcomeListener = (WelcomeListener) context;
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void initUI() {
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.onboarding_welcome_next_button)
    public void onNextButtonClicked() {
        mWelcomeListener.onTutorialComplete();
    }
}
