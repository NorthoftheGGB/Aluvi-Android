package com.aluvi.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.aluvi.android.api.devices.DeviceData;
import com.aluvi.android.api.devices.DevicesApi;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.exceptions.UserRecoverableSystemError;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.model.local.TicketStateTransition;
import com.aluvi.android.model.realm.Route;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;


public class DebugActivity extends AppCompatActivity {

    @Bind(R.id.login_button) Button loginButton;
    @Bind(R.id.driver_login_button) Button driverLoginButton;
    @Bind(R.id.logout_button) Button logoutButton;
    @Bind(R.id.schedule_button) Button scheduleButton;
    @Bind(R.id.cancel_ticket_button) Button cancelTicketButton;
    @Bind(R.id.cancel_trip_button) Button cancelTripButton;
    @Bind(R.id.tickets_button) Button refreshTicketsButton;
    @Bind(R.id.push_token_button) Button pushTokenButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.login_button) public void login(){
        UserStateManager.getInstance().login("paypal@fromthegut.org", "martian", new UserStateManager.Callback() {
            @Override
            public void success() {
                Toast.makeText(getApplicationContext(), "Logged In", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void failure(String message) {
                Toast.makeText(getApplicationContext(), "Failed to Log In", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @OnClick(R.id.driver_login_button) public void driverLogin(){
        UserStateManager.getInstance().login("bartle@b.com", "bartle", new UserStateManager.Callback() {
            @Override
            public void success() {
                Toast.makeText(getApplicationContext(), "Logged In", Toast.LENGTH_SHORT).show();

                CommuteManager.initialize(new CommuteManager.Callback() {
                    @Override
                    public void success() {
                        //onInitializationFinished();
                        CommuteManager.setUserRoute(new Route())
                    }

                    @Override
                    public void failure(String message) {
                        Toast.makeText(DebugActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void failure(String message) {
                Toast.makeText(getApplicationContext(), "Failed to Log In", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @OnClick(R.id.logout_button) public void logout(){
        UserStateManager.getInstance().logout(new UserStateManager.Callback() {
            @Override
            public void success() {
                Toast.makeText(getApplicationContext(), "Logged Out", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(String message) {
                Toast.makeText(getApplicationContext(), "Failed to Log Out", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.schedule_button) public void schedule(){
        try {
            CommuteManager.getInstance().requestRidesForTomorrow(new CommuteManager.Callback(){
                @Override
                public void success() {
                    Toast.makeText(getApplicationContext(), "Scheduled!", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void failure(String message) {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                }
            });
        } catch (UserRecoverableSystemError userRecoverableSystemError) {
            userRecoverableSystemError.printStackTrace();
            // This should be handled by a dialog, not a Toast
            Toast.makeText(this, userRecoverableSystemError.getMessage(), Toast.LENGTH_LONG);
        }

    }

    @OnClick(R.id.cancel_ticket_button) public void cancelTicket(){
        Realm realm = AluviRealm.getDefaultRealm();
        Ticket ticket = realm.where(Ticket.class).findFirst();
        if(ticket == null){
            Toast.makeText(getApplicationContext(), "Null", Toast.LENGTH_SHORT).show();
            return;
        }
        CommuteManager.getInstance().cancelTicket(ticket, new CommuteManager.Callback() {
            @Override
            public void success() {
                Toast.makeText(getApplicationContext(), "Cancelled!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void failure(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @OnClick(R.id.cancel_trip_button) public void cancelTrip(){
        Realm realm = AluviRealm.getDefaultRealm();
        Trip trip = realm.where(Trip.class).findFirst();
        if(trip == null){
            Toast.makeText(getApplicationContext(), "Null", Toast.LENGTH_SHORT).show();
            return;
        }
        CommuteManager.getInstance().cancelTrip(trip, new CommuteManager.Callback() {
            @Override
            public void success() {
                Toast.makeText(getApplicationContext(), "Cancelled!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void failure(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @OnClick(R.id.tickets_button) public void refreshTickets(){
        CommuteManager.getInstance().refreshTickets(new CommuteManager.DataCallback<List<TicketStateTransition>>() {
            @Override
            public void success(List<TicketStateTransition> result) {
                Toast.makeText(getApplicationContext(), "Got them!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void failure(String message) {
                Toast.makeText(getApplicationContext(), "Did not work out!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @OnClick(R.id.push_token_button) public void pushToken() {
        DeviceData device = new DeviceData();
        device.setPushToken("yeh a push token");
        DevicesApi.patchDevice(device, new DevicesApi.Callback() {
            @Override
            public void success() {
                Toast.makeText(getApplicationContext(), "Did!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void failure(int statusCode) {
                Toast.makeText(getApplicationContext(), "Did not!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    /*
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
    */
}
