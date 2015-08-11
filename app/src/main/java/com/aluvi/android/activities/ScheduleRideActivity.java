package com.aluvi.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.activities.base.AluviAuthActivity;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.exceptions.UserRecoverableSystemError;
import com.aluvi.android.fragments.LocationSelectDialogFragment;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.PaymentManager;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.packages.Callback;
import com.aluvi.android.managers.packages.DataCallback;
import com.aluvi.android.model.local.TicketLocation;
import com.aluvi.android.model.realm.LocationWrapper;
import com.aluvi.android.model.realm.Profile;
import com.aluvi.android.model.realm.Route;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.TimePickerDialog;

import org.joda.time.LocalDateTime;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import io.realm.Realm;
import io.realm.RealmResults;

public class ScheduleRideActivity extends AluviAuthActivity implements LocationSelectDialogFragment.OnLocationSelectedListener {

    @Bind(R.id.schedule_ride_root_view) View mRootView;
    @Bind(R.id.schedule_ride_button_from) Button mFromButton;
    @Bind(R.id.schedule_ride_button_to) Button mToButton;
    @Bind(R.id.schedule_ride_button_start_time) Button mStartTimeButton;
    @Bind(R.id.schedule_ride_button_end_time) Button mEndTimeButton;
    @Bind(R.id.schedule_ride_button_drive_there) CheckBox mDriveThereCheckbox;
    @Bind(R.id.schedule_ride_button_commute_tomorrow) Button mCommuteTomorrowButton;

    @Bind({R.id.schedule_ride_button_from, R.id.schedule_ride_button_to, R.id.schedule_ride_button_start_time,
            R.id.schedule_ride_button_end_time, R.id.schedule_ride_button_drive_there, R.id.schedule_ride_button_commute_tomorrow})
    List<View> mScheduleEditViews;

    public final static int RESULT_SCHEDULE_OK = 453, RESULT_CANCEL = 354, RESULT_ERROR = 431,
            CARD_IO_REQUEST_CODE = 1241;

    public final static String COMMUTE_TO_VIEW_ID_KEY = "commute_view__id_key";

    private final String TAG = "ScheduleRideActivity",
            FROM_LOCATION_TAG = "from_location",
            TO_LOCATION_TAG = "to_location";

    private final int MIN_HOME_LEAVE_HOUR = 7, MAX_HOME_LEAVE_HOUR = 9,
            MIN_WORK_LEAVE_HOUR = 16, MAX_WORK_LEAVE_HOUR = 19;

    private int mStartHour, mEndHour, mStartMin, mEndMin;
    private TicketLocation mStartLocation, mEndLocation;
    private boolean mIsInViewMode;

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
        if (!initUISavedTrip())
            initUICommuteManager();

        if (mStartLocation != null && mEndLocation != null) {
            String homeAddress = mStartLocation.getPlaceName();
            String workAddress = mEndLocation.getPlaceName();

            if (homeAddress != null && !"".equals(homeAddress))
                mFromButton.setText(homeAddress);
            if (workAddress != null && !"".equals(workAddress))
                mToButton.setText(workAddress);
        }

        updateStartTimeButton();
        updateEndTimeButton();
        checkCreditCardDetails();
    }

    private boolean initUISavedTrip() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            int tripId = getIntent().getExtras().getInt(COMMUTE_TO_VIEW_ID_KEY);
            RealmResults<Ticket> tickets = getTicketsForTripId(tripId);
            if (tickets.size() == 2) {
                Ticket homeTicket = tickets.get(0);
                Ticket workTicket = tickets.get(1);

                mStartLocation = new TicketLocation(homeTicket.getOriginLatitude(),
                        homeTicket.getOriginLatitude(), homeTicket.getOriginPlaceName());

                mEndLocation = new TicketLocation(homeTicket.getDestinationLatitude(),
                        homeTicket.getDestinationLongitude(), homeTicket.getDestinationPlaceName());

                LocalDateTime pickupTime = LocalDateTime.fromDateFields(homeTicket.getPickupTime());
                mStartHour = pickupTime.getHourOfDay();
                mStartMin = pickupTime.getMinuteOfHour();

                LocalDateTime pickupTimeWork = LocalDateTime.fromDateFields(workTicket.getPickupTime());
                mEndHour = pickupTimeWork.getHourOfDay();
                mEndMin = pickupTimeWork.getMinuteOfHour();

                mDriveThereCheckbox.setChecked(homeTicket.isDriving());
                ButterKnife.apply(mScheduleEditViews, new ButterKnife.Action<View>() {
                    @Override
                    public void apply(View view, int index) {
                        view.setEnabled(false);
                    }
                });

                mIsInViewMode = true;
                mCommuteTomorrowButton.setText(R.string.action_commute_pending);
                getToolbar().setTitle(R.string.scheduled_commute);
                supportInvalidateOptionsMenu();
                return true;
            }
        }

        return false;
    }

    @Nullable
    private RealmResults<Ticket> getTicketsForTripId(int tripId) {
        Trip trip = AluviRealm.getDefaultRealm().where(Trip.class)
                .equalTo("tripId", tripId)
                .findFirst();
        if (trip != null) {
            return trip.getTickets().where()
                    .findAllSorted("pickupTime");

        }

        return null;
    }

    private void initUICommuteManager() {
        if (CommuteManager.getInstance().isMinViableRouteAvailable()) {
            Route route = CommuteManager.getInstance().getRoute();
            mStartLocation = Route.getOriginTicketLocation(route);
            mEndLocation = Route.getDestinationTicketLocation(route);

            mStartHour = Route.getHour(route.getPickupTime());
            mStartMin = Route.getMinute(route.getPickupTime());

            mEndHour = Route.getHour(route.getReturnTime());
            mEndMin = Route.getMinute(route.getReturnTime());

            mDriveThereCheckbox.setChecked(route.isDriving());
        }
    }

    private void checkCreditCardDetails() {
        Intent scanIntent = new Intent(this, CardIOActivity.class);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true);
        startActivityForResult(scanIntent, CARD_IO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CARD_IO_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                onCreditCardFetched(scanResult);
            } else {
                onCreditCardProcessingError();
            }
        }
    }

    private void onCreditCardFetched(CreditCard fetchedCard) {
        final MaterialDialog progressDialog = DialogUtils.getDefaultProgressDialog(this, false);
        PaymentManager.getInstance().requestToken(fetchedCard, new DataCallback<String>() {
            @Override
            public void success(String result) {
                if (progressDialog != null)
                    progressDialog.cancel();

                updateUserPaymentToken(result);
            }

            @Override
            public void failure(String message) {
                if (progressDialog != null)
                    progressDialog.cancel();

                if (mRootView != null)
                    Snackbar.make(mRootView, message, Snackbar.LENGTH_SHORT).show();

                onCreditCardProcessingError();
            }
        });
    }

    private void updateUserPaymentToken(final String token) {
        AluviRealm.getDefaultRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Profile profile = UserStateManager.getInstance().getProfile();
                profile.setDefaultCardToken(token);
            }
        });

        final MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                .title(R.string.loading)
                .content(R.string.updating_payment_info)
                .progress(true, 0)
                .cancelable(false)
                .show();

        UserStateManager.getInstance().saveProfile(new Callback() {
            @Override
            public void success() {
                if (progressDialog != null)
                    progressDialog.cancel();
            }

            @Override
            public void failure(String message) {
                if (progressDialog != null)
                    progressDialog.cancel();

                if (mRootView != null) {
                    Snackbar.make(mRootView, message, Snackbar.LENGTH_SHORT).show();
                }

                onCreditCardProcessingError();
            }
        });
    }

    private void onCreditCardProcessingError() {
        finish();
    }

    @OnClick(R.id.schedule_ride_button_from)
    public void onFromButtonClicked() {
        LocationSelectDialogFragment.newInstance(mStartLocation)
                .show(getSupportFragmentManager(), FROM_LOCATION_TAG);
    }

    @OnClick(R.id.schedule_ride_button_to)
    public void onToButtonClicked() {
        LocationSelectDialogFragment.newInstance(mEndLocation)
                .show(getSupportFragmentManager(), TO_LOCATION_TAG);
    }

    @OnClick(R.id.schedule_ride_button_start_time)
    public void onStartTimeButtonClicked() {
        showTimePicker(mStartHour, mStartMin, MIN_HOME_LEAVE_HOUR, MAX_HOME_LEAVE_HOUR,
                new OnTimeSetListener() {
                    @Override
                    public void onTimeSet(int hour, int min) {
                        mStartHour = hour;
                        mStartMin = min == CommuteManager.INVALID_TIME ? 0 : min;
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
                mEndMin = min == CommuteManager.INVALID_TIME ? 0 : min;
                updateEndTimeButton();
            }
        });
    }

    public void updateEndTimeButton() {
        if (mEndHour != CommuteManager.INVALID_TIME && mEndMin != CommuteManager.INVALID_TIME)
            mEndTimeButton.setText(getString(R.string.return_home) + " " + getAmPm(mEndHour, mEndMin));
    }

    public void updateStartTimeButton() {
        if (mStartHour != CommuteManager.INVALID_TIME && mStartMin != CommuteManager.INVALID_TIME)
            mStartTimeButton.setText(getString(R.string.at) + " " + getAmPm(mStartHour, mStartMin));
    }

    @OnClick(R.id.schedule_ride_button_commute_tomorrow)
    public void onConfirmCommuteButtonClicked() {
        if (isCommuteReady()) {
            updateSavedRoute();
            try {
                CommuteManager.getInstance()
                        .requestRidesForTomorrow(new Callback() {
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
        } else {
            Snackbar.make(mRootView, R.string.please_fill_fields, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void updateSavedRoute() {
        final CommuteManager manager = CommuteManager.getInstance();

        Realm realm = AluviRealm.getDefaultRealm();
        realm.beginTransaction();

        Route route = manager.getRoute();
        route.setDriving(mDriveThereCheckbox.isChecked());

        LocationWrapper origin = route.getOrigin();
        origin = origin == null ? realm.createObject(LocationWrapper.class) : origin;
        origin.setLatitude(mStartLocation.getLatitude());
        origin.setLongitude(mStartLocation.getLongitude());
        route.setOriginPlaceName(mStartLocation.getPlaceName());
        route.setOrigin(origin);

        LocationWrapper destination = route.getDestination();
        destination = destination == null ? realm.createObject(LocationWrapper.class) : destination;
        destination.setLatitude(mEndLocation.getLatitude());
        destination.setLongitude(mEndLocation.getLongitude());
        route.setDestinationPlaceName(mEndLocation.getPlaceName());
        route.setDestination(destination);

        route.setPickupTime(Route.getTime(mStartHour, mStartMin));
        route.setReturnTime(Route.getTime(mEndHour, mEndMin));

        realm.commitTransaction();
        manager.saveRoute(null);
    }

    private boolean isCommuteReady() {
        return mStartHour != CommuteManager.INVALID_TIME && mStartMin != CommuteManager.INVALID_TIME
                && mEndHour != CommuteManager.INVALID_TIME && mEndMin != CommuteManager.INVALID_TIME
                && mStartLocation != null && mStartLocation.getPlaceName() != null && !mStartLocation.getPlaceName().equals("")
                && mEndLocation != null && mEndLocation.getPlaceName() != null && !mEndLocation.getPlaceName().equals("");
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mIsInViewMode)
            menu.findItem(R.id.action_cancel_schedule_ride)
                    .setTitle(R.string.close);

        return super.onPrepareOptionsMenu(menu);
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
