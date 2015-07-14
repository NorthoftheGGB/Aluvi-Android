package com.aluvi.android.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.aluvi.aluvi.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by usama on 7/13/15.
 */
public abstract class BaseToolBarActivity extends AppCompatActivity
{
    @Optional
    @InjectView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.inject(this);

        if (mToolbar != null)
        {
            mToolbar.setTitle(getTitle());
            setSupportActionBar(mToolbar);
        }
    }

    public abstract int getLayoutId();

    public Toolbar getToolbar()
    {
        return mToolbar;
    }
}
