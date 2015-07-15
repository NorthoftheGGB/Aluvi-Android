package com.aluvi.android.activities;

import android.location.Address;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.aluvi.aluvi.R;
import com.aluvi.android.fragments.LocationSelectDialogFragment;
import com.aluvi.android.helpers.GeocoderUtils;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.TimePickerDialog;

import butterknife.InjectView;
import butterknife.OnClick;

public class ScheduleRideActivity extends BaseToolBarActivity implements LocationSelectDialogFragment.OnLocationSelectedListener
{
    @InjectView(R.id.schedule_ride_button_from) Button mFromButton;
    @InjectView(R.id.schedule_ride_button_to) Button mToButton;
    @InjectView(R.id.schedule_ride_button_start_time) Button mStartTimeButton;
    @InjectView(R.id.schedule_ride_button_end_time) Button mEndTimeButton;

    private final String FROM_LOCATION_TAG = "from_location", TO_LOCATION_TAG = "to_location";

    private int mStartHour, mEndHour, mStartMin, mEndMin;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId()
    {
        return R.layout.activity_schedule_ride;
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

    }

    @Override
    public void onLocationSelected(Address address, LocationSelectDialogFragment fragment)
    {
        String formattedAddress = GeocoderUtils.getFormattedAddress(address);
        if (fragment.getTag().equals(FROM_LOCATION_TAG))
        {
            mFromButton.setText(formattedAddress);
        }
        else
        {
            mToButton.setText(formattedAddress);
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
