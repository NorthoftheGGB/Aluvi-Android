package com.aluvi.android.activities;

import android.os.Bundle;
import android.view.MenuItem;

import com.aluvi.android.R;
import com.aluvi.android.activities.base.BaseToolBarActivity;

/**
 * Created by usama on 9/27/15.
 */
public class PaymentInfoActivity extends BaseToolBarActivity {

    @Override
    public int getLayoutId() {
        return R.layout.activity_payment_info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
}
