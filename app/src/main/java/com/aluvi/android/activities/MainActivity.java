package com.aluvi.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.fragments.AluviMapFragment;
import com.aluvi.android.fragments.TicketAluviMapFragment;
import com.aluvi.android.helpers.eventBus.CommuteScheduledEvent;
import com.aluvi.android.helpers.views.BaseArrayAdapter;
import com.aluvi.android.helpers.views.ViewHolder;
import com.aluvi.android.managers.UserStateManager;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

public class MainActivity extends BaseToolBarActivity implements AluviMapFragment.OnMapEventListener {
    @Bind(R.id.main_navigation_view) NavigationView mNavigationView;
    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    private final String TAG = "MainActivity";

    private final int SCHEDULE_RIDE_REQUEST_CODE = 982;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavigationView();
        onHomeClicked();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    public void initNavigationView() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                getToolbar(), R.string.drawer_open, R.string.drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_drawer_home:
                        onHomeClicked();
                        break;
                    case R.id.action_debug_user_log_in:
                        debugLogInSelected();
                        break;
                }

                return false;
            }
        });
    }

    @Override
    public void onScheduleRideRequested() {
        if (UserStateManager.getInstance().getApiToken() != null)
            startActivityForResult(new Intent(this, ScheduleRideActivity.class), SCHEDULE_RIDE_REQUEST_CODE);
        else
            Snackbar.make(mDrawerLayout.getRootView(), R.string.log_in_required, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            debugLogInSelected();
                        }
                    })
                    .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCHEDULE_RIDE_REQUEST_CODE) {
            if (resultCode == ScheduleRideActivity.RESULT_SCHEDULE_OK) {
                onCommuteScheduled();
            }
        }
    }

    public void onHomeClicked() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, AluviMapFragment.newInstance()).commit();
    }

    public void onCommuteScheduled() {
        supportInvalidateOptionsMenu();
        EventBus.getDefault().post(new CommuteScheduledEvent());
    }

    public void onRiderTicketSelected() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, TicketAluviMapFragment.newInstance()).commit();
    }

    public void onDriverTicketSelected() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, TicketAluviMapFragment.newInstance()).commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    class DebugUser {
        String username, password;

        public DebugUser(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    private void debugLogInSelected() {
        DebugUser[] testUsers = {
                new DebugUser("test@test.com", "tiny123"),
                new DebugUser("joe@joe.com", "tiny123")
        };

        class DebugAdapter extends BaseArrayAdapter<DebugUser> {
            public DebugAdapter(Context context, DebugUser[] data) {
                super(context, android.R.layout.simple_list_item_1, data);
            }

            @Override
            protected void initView(ViewHolder holder, int position) {
                ((TextView) holder.getView(android.R.id.text1)).setText(getItem(position).username);
            }
        }

        final DebugAdapter adapter = new DebugAdapter(this, testUsers);
        new MaterialDialog.Builder(this)
                .adapter(adapter, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        logUserIn(adapter.getItem(i));
                        materialDialog.dismiss();
                    }
                })
                .show();
    }

    private void logUserIn(DebugUser user) {
        final MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                .progress(true, 0)
                .title(R.string.loading)
                .content(R.string.please_wait)
                .cancelable(false)
                .show();

        UserStateManager.getInstance().login(user.username, user.password, new UserStateManager.Callback() {
            @Override
            public void success() {
                if (progressDialog != null)
                    progressDialog.cancel();

                Toast.makeText(getApplicationContext(), R.string.logged_in, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(String message) {
                if (progressDialog != null)
                    progressDialog.cancel();

                Log.e(TAG, message);
            }
        });
    }
}
