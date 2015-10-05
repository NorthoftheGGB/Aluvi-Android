package com.aluvi.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluvi.android.R;
import com.aluvi.android.activities.base.BaseToolBarActivity;
import com.aluvi.android.application.AluviPreferences;
import com.aluvi.android.fragments.LoginFragment;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.viewpagerindicator.CirclePageIndicator;

import butterknife.Bind;

/**
 * Created by usama on 9/30/15.
 */
public class TutorialActivity extends BaseToolBarActivity implements LoginFragment.LoginListener {
    @Bind(R.id.tutorial_view_pager) ViewPager mPager;
    @Bind(R.id.tutorial_view_pager_indicator) CirclePageIndicator mTitlePageIndicator;

    private TutorialAdapter mPagerAdapter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_tutorial;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPagerAdapter = new TutorialAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mTitlePageIndicator.setViewPager(mPager);
    }

    @Override
    public void onLoggedIn() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putBoolean(AluviPreferences.TUTORIAL_VIEWED_KEY, true).commit();

        startActivity(new Intent(this, LoginActivity.class));
    }

    private static class TutorialAdapter extends FragmentPagerAdapter {
        private final int NUM_ITEMS = 5, POS_TUT0 = 0,
                POS_TUT1 = POS_TUT0 + 1, POS_TUT2 = POS_TUT1 + 1,
                POS_TUT3 = POS_TUT2 + 1, POS_TUT4 = POS_TUT3 + 1;

        public TutorialAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case POS_TUT0:
                    return Tut0Fragment.newInstance();
                case POS_TUT1:
                    return Tut1Fragment.newInstance();
                case POS_TUT2:
                    return Tut2Fragment.newInstance();
                case POS_TUT3:
                    return Tut3Fragment.newInstance();
                case POS_TUT4:
                    return LoginFragment.newInstance();
            }

            return null;
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }

    public static class Tut0Fragment extends BaseButterFragment {
        public static Tut0Fragment newInstance() {
            return new Tut0Fragment();
        }

        @Override
        public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_tut0, container, false);
        }

        @Override
        public void initUI() {
        }
    }

    public static class Tut1Fragment extends BaseButterFragment {
        public static Tut1Fragment newInstance() {
            return new Tut1Fragment();
        }

        @Override
        public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_tut1, container, false);
        }

        @Override
        public void initUI() {
        }
    }

    public static class Tut2Fragment extends BaseButterFragment {
        public static Tut2Fragment newInstance() {
            return new Tut2Fragment();
        }

        @Override
        public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_tut2, container, false);
        }

        @Override
        public void initUI() {
        }
    }

    public static class Tut3Fragment extends BaseButterFragment {
        public static Tut3Fragment newInstance() {
            return new Tut3Fragment();
        }

        @Override
        public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_tut3, container, false);
        }

        @Override
        public void initUI() {
        }
    }
}
