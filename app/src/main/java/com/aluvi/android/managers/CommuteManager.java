package com.aluvi.android.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.aluvi.android.api.tickets.CommuterTicketsResponse;
import com.aluvi.android.api.tickets.RequestCommuterTicketsCallback;
import com.aluvi.android.api.tickets.TicketsApi;
import com.aluvi.android.application.AluviPreferences;
import com.aluvi.android.exceptions.UserRecoverableSystemError;
import com.aluvi.android.model.local.TicketLocation;
import com.aluvi.android.model.realm.Ticket;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by matthewxi on 7/15/15.
 */
public class CommuteManager
{
    public interface Callback
    {
        void success();

        void failure(String message);
    }

    private static CommuteManager mInstance;

    private SharedPreferences preferences;
    private Context ctx;

    private float homeLatitude, homeLongitude, workLatitude, workLongitude;
    private String homePlaceName, workPlaceName;
    private int pickupTimeHour, pickupTimeMinute;
    private int returnTimeHour, returnTimeMinute;
    private boolean driving;

    public static synchronized void initialize(Context context)
    {
        if (mInstance == null)
        {
            mInstance = new CommuteManager(context);
        }
    }

    public static synchronized CommuteManager getInstance()
    {
        return mInstance;
    }

    public CommuteManager(Context context)
    {
        ctx = context;
        preferences = context.getSharedPreferences(AluviPreferences.COMMUTER_PREFERENCES_FILE, 0);
        load();
    }

    private void load()
    {
        homeLatitude = preferences.getFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, 0);
        homeLongitude = preferences.getFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, 0);

        workLatitude = preferences.getFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, 0);
        workLongitude = preferences.getFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, 0);

        homePlaceName = preferences.getString(AluviPreferences.COMMUTER_HOME_PLACENAME_KEY, "");
        workPlaceName = preferences.getString(AluviPreferences.COMMUTER_WORK_PLACENAME_KEY, "");

        pickupTimeHour = preferences.getInt(AluviPreferences.COMMUTER_PICKUP_TIME_HOUR_KEY, -1);
        returnTimeHour = preferences.getInt(AluviPreferences.COMMUTER_RETURN_TIME_HOUR_KEY, -1);

        pickupTimeMinute = preferences.getInt(AluviPreferences.COMMUTER_PICKUP_TIME_MINUTE_KEY, -1);
        returnTimeMinute = preferences.getInt(AluviPreferences.COMMUTER_RETURN_TIME_MINUTE_KEY, -1);

        driving = preferences.getBoolean(AluviPreferences.COMMUTER_IS_DRIVER_KEY, false);
    }

    public void loadFromServer()
    {
        // routes API
        // TODO: implement RoutesApi library file
    }

    private void store()
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, homeLatitude);
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, homeLongitude);
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, workLatitude);
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, workLongitude);
        editor.putString(AluviPreferences.COMMUTER_HOME_PLACENAME_KEY, homePlaceName);
        editor.putString(AluviPreferences.COMMUTER_WORK_PLACENAME_KEY, workPlaceName);
        editor.putInt(AluviPreferences.COMMUTER_PICKUP_TIME_HOUR_KEY, pickupTimeHour);
        editor.putInt(AluviPreferences.COMMUTER_RETURN_TIME_HOUR_KEY, returnTimeHour);
        editor.putInt(AluviPreferences.COMMUTER_PICKUP_TIME_MINUTE_KEY, pickupTimeMinute);
        editor.putInt(AluviPreferences.COMMUTER_RETURN_TIME_MINUTE_KEY, returnTimeMinute);
        editor.putBoolean(AluviPreferences.COMMUTER_IS_DRIVER_KEY, driving);
        editor.commit();
    }

    public void save(Callback callback)
    {
        // TODO: Implement Routes API
        store();
    }

    public void clear()
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LATITUDE_KEY, 0);
        editor.putFloat(AluviPreferences.COMMUTER_HOME_LONGITUDE_KEY, 0);
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LATITUDE_KEY, 0);
        editor.putFloat(AluviPreferences.COMMUTER_WORK_LONGITUDE_KEY, 0);
        editor.putString(AluviPreferences.COMMUTER_HOME_PLACENAME_KEY, "");
        editor.putString(AluviPreferences.COMMUTER_WORK_PLACENAME_KEY, "");
        editor.putInt(AluviPreferences.COMMUTER_PICKUP_TIME_HOUR_KEY, -1);
        editor.putInt(AluviPreferences.COMMUTER_RETURN_TIME_HOUR_KEY, -1);
        editor.putInt(AluviPreferences.COMMUTER_PICKUP_TIME_MINUTE_KEY, -1);
        editor.putInt(AluviPreferences.COMMUTER_RETURN_TIME_MINUTE_KEY, -1);
        editor.putBoolean(AluviPreferences.COMMUTER_IS_DRIVER_KEY, false);
        editor.commit();

        load(); // Reset by pulling default values from shared prefs
    }

    public boolean routeIsSet()
    {
        return homeLatitude == 0 || homeLongitude == 0 || workLatitude == 0 || workLongitude == 0
                || pickupTimeHour == -1 || pickupTimeMinute == -1 || returnTimeHour == -1 || returnTimeMinute == -1;
    }

    public void requestRidesForTomorrow(final Callback callback) throws UserRecoverableSystemError
    {
        LocalDate today = new LocalDate();
        LocalDate tomorrow = today.plus(Period.days(1));
        Date rideDate = tomorrow.toDateTimeAtStartOfDay().toDate();

        Realm realm = Realm.getInstance(ctx);

        // Lood for a prexisting request for tomorrow
        RealmQuery<Ticket> query = realm.where(Ticket.class);
        query.equalTo("rideDate", rideDate);

        RealmResults<Ticket> results = query.findAll();
        int count = 0;
        Ticket orphan = null;
        if (results.size() != 0)
        {
            // since we can't do an IN query, we check here for statuses
            for (Ticket ticket : results)
            {
                String state = ticket.getState();
                if (state.equals(Ticket.StateCreated) || state.equals(Ticket.StateRequested) || state.equals(Ticket.StateScheduled))
                {
                    // already have a ticket in there for tomorrow
                    count++;
                    orphan = ticket;
                }
            }
        }

        if (count == 2)
        {
            throw new UserRecoverableSystemError("There are already rides requested or scheduled for tomorrow, this is a system error but can be recovered by canceling your commuter rides and requesting again");
            // AluviStrings.commuter_rides_already_in_database);
        }

        if (count == 1 && orphan != null)
        {
            // Orphaned request, delete it
            realm.beginTransaction();
            orphan.removeFromRealm();
            realm.commitTransaction();
        }

        // go ahead and create the tickets, then request with the server
        realm.beginTransaction();
        Ticket toWorkTicket = realm.createObject(Ticket.class);
        Ticket.buildNewTicket(toWorkTicket, rideDate, getHomeLocation(), getWorkLocation(),
                driving, pickupTimeHour, pickupTimeMinute);
        Ticket fromWorkTicket = realm.createObject(Ticket.class);
        Ticket.buildNewTicket(fromWorkTicket, rideDate, getWorkLocation(), getHomeLocation(),
                driving, returnTimeHour, returnTimeMinute);
        realm.commitTransaction();

        class Callback extends RequestCommuterTicketsCallback
        {
            public Callback(Ticket toWorkTicket, Ticket fromWorkTicket)
            {
                super(toWorkTicket, fromWorkTicket);
            }

            @Override
            public void success(CommuterTicketsResponse response)
            {
                callback.success();
            }

            @Override
            public void failure(int statusCode)
            {
                callback.failure("Scheduling failure message");
            }
        }

        TicketsApi.requestCommuterTickets(toWorkTicket, fromWorkTicket, new Callback(toWorkTicket, fromWorkTicket));
    }

    private TicketLocation getHomeLocation()
    {
        TicketLocation ticketLocation = new TicketLocation(homeLatitude, homeLongitude, homePlaceName);
        return ticketLocation;
    }

    private TicketLocation getWorkLocation()
    {
        TicketLocation ticketLocation = new TicketLocation(workLatitude, workLongitude, workPlaceName);
        return ticketLocation;
    }

    public double getHomeLatitude()
    {
        return homeLatitude;
    }

    public void setHomeLatitude(float homeLatitude)
    {
        this.homeLatitude = homeLatitude;
    }

    public double getHomeLongitude()
    {
        return homeLongitude;
    }

    public void setHomeLongitude(float homeLongitude)
    {
        this.homeLongitude = homeLongitude;
    }

    public double getWorkLatitude()
    {
        return workLatitude;
    }

    public void setWorkLatitude(float workLatitude)
    {
        this.workLatitude = workLatitude;
    }

    public double getWorkLongitude()
    {
        return workLongitude;
    }

    public void setWorkLongitude(float workLongitude)
    {
        this.workLongitude = workLongitude;
    }

    public String getHomePlaceName()
    {
        return homePlaceName;
    }

    public void setHomePlaceName(String homePlaceName)
    {
        this.homePlaceName = homePlaceName;
    }

    public String getWorkPlaceName()
    {
        return workPlaceName;
    }

    public void setWorkPlaceName(String workPlaceName)
    {
        this.workPlaceName = workPlaceName;
    }

    public boolean isDriving()
    {
        return driving;
    }

    public void setDriving(boolean driving)
    {
        this.driving = driving;
    }

    public void setHomeLocation(TicketLocation homeLocation)
    {
        homeLatitude = homeLocation.getLatitude();
        homeLongitude = homeLocation.getLongitude();
        homePlaceName = homeLocation.getPlaceName();
    }

    public void setWorkLocation(TicketLocation workLocation)
    {
        workLatitude = workLocation.getLatitude();
        workLongitude = workLocation.getLongitude();
        workPlaceName = workLocation.getPlaceName();
    }

    public int getPickupTimeHour()
    {
        return pickupTimeHour;
    }

    public void setPickupTimeHour(int pickupTimeHour)
    {
        this.pickupTimeHour = pickupTimeHour;
    }

    public int getPickupTimeMinute()
    {
        return pickupTimeMinute;
    }

    public void setPickupTimeMinute(int pickupTimeMinute)
    {
        this.pickupTimeMinute = pickupTimeMinute;
    }

    public int getReturnTimeHour()
    {
        return returnTimeHour;
    }

    public void setReturnTimeHour(int returnTimeHour)
    {
        this.returnTimeHour = returnTimeHour;
    }

    public int getReturnTimeMinute()
    {
        return returnTimeMinute;
    }

    public void setReturnTimeMinute(int returnTimeMinute)
    {
        this.returnTimeMinute = returnTimeMinute;
    }
}
