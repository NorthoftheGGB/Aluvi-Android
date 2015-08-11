package com.aluvi.android.fragments.onboarding;

import android.app.Activity;
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
public class TutorialFragment extends BaseButterFragment {
    public interface TutorialListener {
        void onTutorialRequested();
    }

    private TutorialListener mTutorialListener;

    public static TutorialFragment newInstance() {
        return new TutorialFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTutorialListener = (TutorialListener) activity;
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tutorial, container, false);
    }

    @Override
    public void initUI() {
    }

    @OnClick(R.id.onboarding_tutorial_next_button)
    public void onNextButtonClicked() {
        mTutorialListener.onTutorialRequested();
    }
}
