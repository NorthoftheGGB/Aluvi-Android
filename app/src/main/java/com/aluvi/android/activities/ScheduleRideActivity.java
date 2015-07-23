package com.aluvi.android.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.aluvi.android.R;
import com.aluvi.android.exceptions.UserRecoverableSystemError;
import com.aluvi.android.fragments.LocationSelectDialogFragment;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.model.local.TicketLocation;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.TimePickerDialog;

import butterknife.Bind;
import butterknife.OnClick;

public class ScheduleRideActivity extends BaseToolBarActivity implements LocationSelectDialogFragment.OnLocationSelectedListener {

    @Bind(R.id.schedule_ride_root_view) View mRootView;
    @Bind(R.id.schedule_ride_button_from) Button mFromButton;
    @Bind(R.id.schedule_ride_button_to) Button mToButton;
    @Bind(R.id.schedule_ride_button_start_time) Button mStartTimeButton;
    @Bind(R.id.schedule_ride_button_end_time) Button mEndTimeButton;
    @Bind(R.id.schedule_ride_button_drive_there) CheckBox mDriveThereCheckbox;

    public final static int RESULT_SCHEDULE_OK = 453, RESULT_CANCEL = 354, RESULT_ERROR = 431;
    private final String TAG = "ScheduleRideActivity",
            FROM_LOCATION_TAG = "from_location",
            TO_LOCATION_TAG = "to_location";

    private final int MIN_HOME_LEAVE_HOUR = 7, MAX_HOME_LEAVE_HOUR = 9,
            MIN_WORK_LEAVE_HOUR = 16, MAX_WORK_LEAVE_HOUR = 19;

    private int mStartHour, mEndHour, mStartMin, mEndMin;
    private TicketLocation mStartLocation, mEndLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_schedule_ride;
    }

    public void initUI() {
        CommuteManager manager = CommuteManager.getInstance();

        mStartLocation = manager.getHomeLocation();
        mEndLocation = manager.getWorkLocation();

        String homeAddress = mStartLocation.getPlaceName();
        String workAddress = mEndLocation.getPlaceName();

        mStartHour = manager.getPickupTimeHour();
        mStartMin = manager.getPickupTimeMinute();
        mEndHour = manager.getReturnTimeHour();
        mEndMin = manager.getReturnTimeMinute();

        updateStartTimeButton();
        updateEndTimeButton();

        if (homeAddress != null && !"".equals(homeAddress))
            mFromButton.setText(homeAddress);
        if (workAddress != null && !"".equals(workAddress))
            mToButton.setText(workAddress);

        mDriveThereCheckbox.setChecked(manager.isDriving());
    }

    @OnClick(R.id.schedule_ride_button_from)
    public void onFromButtonClicked() {
        LocationSelectDialogFragment.newInstance(CommuteManager.getInstance().getHomeLocation())
                .show(getSupportFragmentManager(), FROM_LOCATION_TAG);
    }

    @OnClick(R.id.schedule_ride_button_to)
    public void onToButtonClicked() {
        LocationSelectDialogFragment.newInstance(CommuteManager.getInstance().getWorkLocation())
                .show(getSupportFragmentManager(), TO_LOCATION_TAG);
    }

    @OnClick(R.id.schedule_ride_button_start_time)
    public void onStartTimeButtonClicked() {
        showTimePicker(mStartHour, mStartMin, MIN_HOME_LEAVE_HOUR, MAX_HOME_LEAVE_HOUR,
                new OnTimeSetListener() {
                    @Override
                    public void onTimeSet(int hour, int min) {
                        mStartHour = hour;
                        mStartMin = min;
                        updateStartTimeButton();
                    }
                });
    }

    @OnClick(R.id.schedule_ride_button_end_time)
    public void onEndTimeButtonClicked() {
        showTimePicker(mEndHour, mEndMin, MIN_WORK_LEAVE_HOUR, MAX_WORK_LEAVE_HOUR, new OnTimeSetListener() {
            @Override
            public void onTimeSet(int hour, int min) {
                mEndHour = hour;
                mEndMin = min;
                updateEndTimeButton();
            }
        });
    }

    public void updateEndTimeButton() {
        if (mEndHour != -1 && mEndMin != -1)
            mEndTimeButton.setText(getString(R.string.return_home) + " " + getAmPm(mEndHour, mEndMin));
    }

    public void updateStartTimeButton() {
        if (mStartHour != -1 && mStartMin != -1)
            mStartTimeButton.setText(getString(R.string.at) + " " + getAmPm(mStartHour, mStartMin));
    }

    @OnClick(R.id.schedule_ride_button_commute_tomorrow)
    public void onConfirmCommuteButtonClicked() {
        final CommuteManager manager = CommuteManager.getInstance();
        manager.setDriving(mDriveThereCheckbox.isChecked());

        manager.setHomeLocation(mStartLocation);
        manager.setWorkLocation(mEndLocation);

        manager.setPickupTimeHour(mStartHour);
        manager.setPickupTimeMinute(mStartMin);

        manager.setReturnTimeHour(mEndHour);
        manager.setReturnTimeMinute(mEndMin);
        manager.save(null);

        try {
            manager.requestRidesForTomorrow(new CommuteManager.Callback() {
                @Override
                public void success() {
                    onCommuteRequestSuccess();
                }

                @Override
                public void failure(String message) {
                    Log.e(TAG, message);
                    onCommuteRequestFail();
                }
            });
        } catch (UserRecoverableSystemError error) {
            error.printStackTrace();
            onCommuteRequestFail();
        }
    }

    public void onCommuteRequestFail() {
        Snackbar.make(mRootView, R.string.unable_schedule_commute, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onConfirmCommuteButtonClicked();
                    }
                })
                .show();
    }

    public void onCommuteRequestSuccess() {
        setResult(RESULT_SCHEDULE_OK);
        finish();
    }

    @Override
    public void onLocationSelected(TicketLocation address, LocationSelectDialogFragment fragment) {
        if (fragment.getTag().equals(FROM_LOCATION_TAG)) {
            mFromButton.setText(address.getPlaceName());
            mStartLocation = address;
        } else {
            mToButton.setText(address.getPlaceName());
            mEndLocation = address;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule_ride, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel_schedule_ride:
                setResult(RESULT_CANCEL);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showTimePicker(int startHour, int startMin, final OnTimeSetListener listener) {
        showTimePicker(startHour, startMin, 0, 24, listener);
    }

    public void showTimePicker(int startHour, int startMin, final int minHour, final int maxHour, final OnTimeSetListener listener) {
        final TimePickerDialog.Builder builder = new TimePickerDialog.Builder(startHour, startMin) {
            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                if (getHour() >= minHour && getHour() < maxHour) {
                    listener.onTimeSet(getHour(), getMinute());
                    super.onPositiveActionClicked(fragment);
                } else {
                    Toast.makeText(ScheduleRideActivity.this, "Time must be within " + getAmPm(minHour, 0)
                            + " and " + getAmPm(maxHour, 0), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        };

        builder.positiveAction(getString(android.R.string.ok))
                .negativeAction(getString(android.R.string.cancel));

        DialogFragment.newInstance(builder)
                .show(getSupportFragmentManager(), "time_picker");
    }

    public String getAmPm(int hour, int min) {
        String modifier = hour >= 12 ? "PM" : "AM";
        hour = hour > 12 ? hour - 12 : hour;
        hour = hour == 0 ? 12 : hour;

        String formattedHour = hour < 10 ? "0" + hour : Integer.toString(hour);
        String formattedMin = min < 10 ? "0" + min : Integer.toString(min);
        return formattedHour + ":" + formattedMin + modifier;
    }

    private interface OnTimeSetListener {
        void onTimeSet(int hour, int min);
    }
}
