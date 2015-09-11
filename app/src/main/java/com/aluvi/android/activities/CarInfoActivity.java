package com.aluvi.android.activities;

import android.os.Bundle;
import android.view.MenuItem;

import com.aluvi.android.R;
import com.aluvi.android.activities.base.BaseToolBarActivity;
import com.aluvi.android.fragments.CarInfoFragment;

/**
 * Created by usama on 8/18/15.
 */
public class CarInfoActivity extends BaseToolBarActivity implements CarInfoFragment.CarInfoListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_car_info;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInfoSaved() {
        finish();
    }
}
