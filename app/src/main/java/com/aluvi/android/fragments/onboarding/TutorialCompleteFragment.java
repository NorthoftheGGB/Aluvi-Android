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
public class TutorialCompleteFragment extends BaseButterFragment {
    public interface TutorialListener {
        void onTutorialComplete();
    }

    private TutorialListener mTutorialListener;

    public static TutorialCompleteFragment newInstance() {
        return new TutorialCompleteFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mTutorialListener = (TutorialListener) context;
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tutorial, container, false);
    }

    @Override
    public void initUI() {
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.onboarding_tutorial_next_button)
    public void onNextButtonClicked() {
        mTutorialListener.onTutorialComplete();
    }
}
