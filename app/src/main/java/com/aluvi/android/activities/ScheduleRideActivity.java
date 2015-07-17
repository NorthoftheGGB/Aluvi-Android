package com.aluvi.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.aluvi.android.R;
import com.aluvi.android.fragments.LocationSelectDialogFragment;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.model.local.TicketLocation;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.TimePickerDialog;

import butterknife.Bind;
import butterknife.OnClick;

public class ScheduleRideActivity extends BaseToolBarActivity implements LocationSelectDialogFragment.OnLocationSelectedListener
{
    @Bind(R.id.schedule_ride_button_from) Button mFromButton;
    @Bind(R.id.schedule_ride_button_to) Button mToButton;
    @Bind(R.id.schedule_ride_button_start_time) Button mStartTimeButton;
    @Bind(R.id.schedule_ride_button_end_time) Button mEndTimeButton;

    public final static int RESULT_SCHEDULE_OK = 453, RESULT_CANCEL = 354;
    private final String FROM_LOCATION_TAG = "from_location", TO_LOCATION_TAG = "to_location";

    private int mStartHour, mEndHour, mStartMin, mEndMin;
    private TicketLocation mStartLocation, mEndLocation;
    private boolean mIsDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initLocations();
    }

    @Override
    public int getLayoutId()
    {
        return R.layout.activity_schedule_ride;
    }

    public void initLocations()
    {
        CommuteManager manager = CommuteManager.getInstance();

        String homeAddress = manager.getHomePlaceName();
        String workAddress = manager.getWorkPlaceName();

        mFromButton.setText(homeAddress);
        mToButton.setText(workAddress);
    }

    @OnClick(R.id.schedule_ride_button_from)
    public void onFromButtonClicked()
    {
        LocationSelectDialogFragment.newInstance().show(getSupportFragmentManager(), FROM_LOCATION_TAG);
    }

    @OnClick(R.id.schedule_ride_button_to)
    public void onToButtonClicked()
    {
        LocationSelectDialogFragment.newInstance().show(getSupportFragmentManager(), TO_LOCATION_TAG);
    }

    @OnClick(R.id.schedule_ride_button_start_time)
    public void onStartTimeButtonClicked()
    {
        showTimePicker(mStartHour, mStartMin, new OnTimeSetListener()
        {
            @Override
            public void onTimeSet(int hour, int min)
            {
                mStartHour = hour;
                mStartMin = min;
                mStartTimeButton.setText(getString(R.string.at) + " " + getAmPm(mStartHour, mStartMin));
            }
        });
    }

    @OnClick(R.id.schedule_ride_button_end_time)
    public void onEndTimeButtonClicked()
    {
        showTimePicker(mEndHour, mEndMin, new OnTimeSetListener()
        {
            @Override
            public void onTimeSet(int hour, int min)
            {
                mEndHour = hour;
                mEndMin = min;
                mEndTimeButton.setText(getString(R.string.return_home) + " " + getAmPm(mEndHour, mEndMin));
            }
        });
    }

    @OnClick(R.id.schedule_ride_button_commute_tomorrow)
    public void onConfirmCommuteButtonClicked()
    {
        setResult(RESULT_SCHEDULE_OK, new Intent());
        finish();
    }

    @Override
    public void onLocationSelected(TicketLocation address, LocationSelectDialogFragment fragment)
    {
        if (fragment.getTag().equals(FROM_LOCATION_TAG))
        {
            mFromButton.setText(address.getPlaceName());
            mStartLocation = address;
        }
        else
        {
            mToButton.setText(address.getPlaceName());
            mEndLocation = address;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_schedule_ride, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_cancel_schedule_ride:
                setResult(RESULT_CANCEL);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showTimePicker(int startHour, int startMin, final OnTimeSetListener listener)
    {
        final TimePickerDialog.Builder builder = new TimePickerDialog.Builder(startHour, startMin)
        {
            @Override
            public void onPositiveActionClicked(DialogFragment fragment)
            {
                listener.onTimeSet(getHour(), getMinute());
                super.onPositiveActionClicked(fragment);
            }
        };

        builder.positiveAction(getString(android.R.string.ok))
                .negativeAction(getString(android.R.string.cancel));

        DialogFragment.newInstance(builder)
                .show(getSupportFragmentManager(), "time_picker");
    }

    public String getAmPm(int hour, int min)
    {
        String modifier = hour >= 12 ? "PM" : "AM";
        hour = hour > 12 ? hour - 12 : hour;
        hour = hour == 0 ? 12 : hour;

        String formattedHour = hour < 10 ? "0" + hour : Integer.toString(hour);
        String formattedMin = min < 10 ? "0" + min : Integer.toString(min);
        return formattedHour + ":" + formattedMin + modifier;
    }

    private interface OnTimeSetListener
    {
        void onTimeSet(int hour, int min);
    }
}
