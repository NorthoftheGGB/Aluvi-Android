package com.aluvi.android.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.aluvi.android.R;

public class ProfileActivity extends AluviAuthActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_profile;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_log_out) {
            super.logOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
