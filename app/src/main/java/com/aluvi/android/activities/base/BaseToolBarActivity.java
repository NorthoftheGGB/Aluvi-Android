package com.aluvi.android.activities.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.aluvi.android.R;

import butterknife.Bind;

/**
 * Created by usama on 7/13/15.
 */
public abstract class BaseToolBarActivity extends BaseButterActivity {
    @Nullable
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mToolbar != null) {
            mToolbar.setTitle(getTitle());
            setSupportActionBar(mToolbar);
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
