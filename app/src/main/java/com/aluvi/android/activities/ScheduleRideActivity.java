package com.aluvi.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.activities.base.AluviAuthActivity;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.exceptions.UserRecoverableSystemError;
import com.aluvi.android.fragments.CreditCardInfoDialogFragment;
import com.aluvi.android.fragments.LocationSelectDialogFragment;
import com.aluvi.android.fragments.onboarding.DriverRegistrationDialogFragment;
import com.aluvi.android.helpers.views.BaseSpinnerArrayAdapter;
import com.aluvi.android.helpers.views.ViewHolder;
import com.aluvi.android.managers.CommuteManager;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.callbacks.Callback;
import com.aluvi.android.model.local.TicketLocation;
import com.aluvi.android.model.realm.Profile;
import com.aluvi.android.model.realm.RealmLatLng;
import com.aluvi.android.model.realm.Route;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class ScheduleRideActivity extends AluviAuthActivity implements
        LocationSelectDialogFragment.OnLocationSelectedListener,
        CreditCardInfoDialogFragment.CreditCardListener {

    @Bind(R.id.schedule_ride_root_view) View mRootView;
    @Bind(R.id.schedule_ride_text_view_home) TextView mFromButton;
    @Bind(R.id.schedule_ride_text_view_work) TextView mToButton;
    @Bind(R.id.schedule_ride_spinner_start_time) Spinner mStartTimeSpinner;
    @Bind(R.id.schedule_ride_spinner_end_time) Spinner mEndTimeSpinner;
    @Bind(R.id.schedule_ride_checkbox_drive_there) CheckBox mDriveThereCheckbox;

    @Bind({R.id.schedule_ride_home_button_container, R.id.schedule_ride_work_button_container, R.id.schedule_ride_start_time_container,
            R.id.schedule_ride_end_time_container, R.id.schedule_ride_checkbox_drive_there, R.id.schedule_ride_commute_tomorrow_button})
    List<View> mScheduleEditViews;

    public final static String COMMUTE_TO_VIEW_ID_KEY = "commute_view__id_key";
    private final String TAG = "ScheduleRideActivity",
            FROM_LOCATION_TAG = "from_location",
            TO_LOCATION_TAG = "to_location";

    public final static int RESULT_SCHEDULE_OK = 453, RESULT_CANCEL = 354;
    private final int MIN_HOME_LEAVE_HOUR = 7, MAX_HOME_LEAVE_HOUR = 9,
            MIN_WORK_LEAVE_HOUR = 16, MAX_WORK_LEAVE_HOUR = 19;

    private int mStartHour, mEndHour, mStartMin, mEndMin;
    private TicketLocation mStartLocation, mEndLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        boolean successfulInit = false, isInViewMode = false;
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

                isInViewMode = true;
                supportInvalidateOptionsMenu();
                successfulInit = true;
            }
        }

        getToolbar().setTitle(isInViewMode ? R.string.action_commute_pending : R.string.my_commute);
        return successfulInit;
    }

    @Nullable
    private RealmResults<Ticket> getTicketsForTripId(int tripId) {
        Trip trip = AluviRealm.getDefaultRealm().where(Trip.class)
                .equalTo("tripId", tripId)
                .findFirst();
        return trip != null ? trip.getTickets().where().findAllSorted("pickupTime") : null;
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
        String lastFourCreditCard = UserStateManager.getInstance().getProfile().getCardLastFour();
        if (lastFourCreditCard == null || lastFourCreditCard.equals(""))
            CreditCardInfoDialogFragment.newInstance().show(getSupportFragmentManager(), "credit_card_fragment");
    }

    @Override
    public void onStripeTokenReceived(String token) {
        updateUserPaymentToken(token);
    }

    @Override
    public void onCreditCardProcessingError(String message) {
        Snackbar.make(mRootView, message, Snackbar.LENGTH_SHORT).show();
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

                if (mRootView != null)
                    Snackbar.make(mRootView, message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.schedule_ride_home_button_container)
    public void onFromButtonClicked() {
        LocationSelectDialogFragment.newInstance(mStartLocation)
                .show(getSupportFragmentManager(), FROM_LOCATION_TAG);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.schedule_ride_work_button_container)
    public void onToButtonClicked() {
        LocationSelectDialogFragment.newInstance(mEndLocation)
                .show(getSupportFragmentManager(), TO_LOCATION_TAG);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.schedule_ride_commute_tomorrow_button)
    public void onConfirmCommuteButtonClicked() {
        if (isCommuteReady()) {
            if (mDriveThereCheckbox.isChecked() && !UserStateManager.getInstance().isUserDriver()) {
                DriverRegistrationDialogFragment.newInstance().show(getSupportFragmentManager(), "driver_registration");
            } else {
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
            }
        } else {
            Snackbar.make(mRootView, R.string.please_fill_fields, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void updateEndTimeButton() {
        if (mEndHour != CommuteManager.INVALID_TIME && mEndMin != CommuteManager.INVALID_TIME) {
            ArrayList<TimeHolder> pmTimes = getTimes(MIN_WORK_LEAVE_HOUR, MAX_WORK_LEAVE_HOUR, 15);
            mEndTimeSpinner.setAdapter(new TimesAdapter(this, pmTimes));
        }
    }

    private void updateStartTimeButton() {
        if (mStartHour != CommuteManager.INVALID_TIME && mStartMin != CommuteManager.INVALID_TIME) {
            ArrayList<TimeHolder> amTimes = getTimes(MIN_HOME_LEAVE_HOUR, MAX_HOME_LEAVE_HOUR, 15);
            mStartTimeSpinner.setAdapter(new TimesAdapter(this, amTimes));
        }
    }

    private ArrayList<TimeHolder> getTimes(int start, int end, int interval) {
        ArrayList<TimeHolder> times = new ArrayList<>();
        int iters = 60 / interval;
        for (int i = start; i < end; i++) {
            for (int j = 0; j < iters; j++) {
                TimeHolder holder = new TimeHolder();
                holder.hour = i;
                holder.minute = j * interval;
                times.add(holder);
            }
        }

        return times;
    }

    private void updateSavedRoute() {
        final CommuteManager manager = CommuteManager.getInstance();

        Realm realm = AluviRealm.getDefaultRealm();
        realm.beginTransaction();

        Route route = manager.getRoute();
        route.setDriving(mDriveThereCheckbox.isChecked());

        RealmLatLng origin = route.getOrigin();
        origin = origin == null ? realm.createObject(RealmLatLng.class) : origin;
        origin.setLatitude(mStartLocation.getLatitude());
        origin.setLongitude(mStartLocation.getLongitude());
        route.setOriginPlaceName(mStartLocation.getPlaceName());
        route.setOrigin(origin);

        RealmLatLng destination = route.getDestination();
        destination = destination == null ? realm.createObject(RealmLatLng.class) : destination;
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
            case android.R.id.home:
                setResult(RESULT_CANCEL);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class TimeHolder {
        private int hour, minute;

        private String formattedTime() {
            return getAmPm(hour, minute);
        }

        public static String getAmPm(int hour, int min) {
            String modifier = hour >= 12 ? "PM" : "AM";
            hour = hour > 12 ? hour - 12 : hour;
            hour = hour == 0 ? 12 : hour;

            String formattedMin = min < 10 ? "0" + min : Integer.toString(min);
            return hour + ":" + formattedMin + " " + modifier;
        }
    }

    private static class TimesAdapter extends BaseSpinnerArrayAdapter<TimeHolder> {
        public TimesAdapter(Context context, ArrayList<TimeHolder> data) {
            super(context, R.layout.row_layout_spinner, R.layout.row_layout_spinner, data);
        }

        @Override
        protected void initView(ViewHolder holder, int position) {
            initTimesRow(holder, position);
        }

        @Override
        protected void initDropDownBiew(ViewHolder holder, int position) {
            initTimesRow(holder, position);
        }

        private void initTimesRow(ViewHolder holder, int position) {
            TextView contentView = (TextView) holder.getView(R.id.spinner_text_view);
            contentView.setText(getItem(position).formattedTime());
        }
    }
}
