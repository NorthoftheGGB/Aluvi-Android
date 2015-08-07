package com.aluvi.android.activities.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * Created by usama on 8/6/15.
 */
public abstract class BaseButterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getLayoutId();
        if (layoutId != -1) {
            setContentView(getLayoutId());
            ButterKnife.bind(this);
        }
    }

    public abstract int getLayoutId();
}
