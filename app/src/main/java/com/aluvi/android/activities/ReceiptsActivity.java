package com.aluvi.android.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.aluvi.android.R;
import com.aluvi.android.activities.base.AluviAuthActivity;

import butterknife.Bind;

/**
 * Created by usama on 9/8/15.
 */
public class ReceiptsActivity extends AluviAuthActivity {
    @Bind(R.id.receipts_root_view) View mReceiptsRootView;

    @Override
    public int getLayoutId() {
        return R.layout.activity_receipts;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_receipts, menu);
        return super.onCreateOptionsMenu(menu);
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
