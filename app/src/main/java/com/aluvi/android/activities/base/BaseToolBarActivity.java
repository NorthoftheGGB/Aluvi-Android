package com.aluvi.android.activities.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.aluvi.android.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by usama on 7/13/15.
 */
public abstract class BaseToolBarActivity extends AppCompatActivity {
    @Nullable
    @Bind(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int layoutId = getLayoutId();
        if (layoutId != -1) {
            setContentView(getLayoutId());
            ButterKnife.bind(this);

            if (mToolbar != null) {
                mToolbar.setTitle(getTitle());
                setSupportActionBar(mToolbar);
            }
        }
    }

    public abstract int getLayoutId();

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
