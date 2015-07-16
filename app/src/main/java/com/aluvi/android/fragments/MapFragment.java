package com.aluvi.android.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aluvi.android.R;
import com.aluvi.android.helpers.eventBus.CommuteScheduledEvent;
import com.aluvi.android.helpers.views.MapBoxStateSaver;
import com.aluvi.android.models.EasyILatLang;
import com.mapbox.mapboxsdk.views.MapView;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

public class MapFragment extends BaseButterFragment
{
    public interface OnMapEventListener
    {
        void onScheduleRideRequested();
    }

    @Bind(R.id.mapview) MapView mMapView;
    @Bind(R.id.map_text_view_commute_pending) TextView mCommutePendingTextView;

    private final String MAP_STATE_KEY = "map_fragment_main";

    private EventBus mBus = EventBus.getDefault();
    private boolean mIsCommutePending;
    private OnMapEventListener mEventListener;

    public MapFragment()
    {
    }

    public static MapFragment newInstance()
    {
        return new MapFragment();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mEventListener = (OnMapEventListener) activity;
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void initUI()
    {
        mMapView.setUserLocationEnabled(true);

        if (!MapBoxStateSaver.restoreMapState(mMapView, MAP_STATE_KEY))
            mMapView.setCenter(new EasyILatLang(mMapView.getUserLocation()), false);

        mBus.register(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(CommuteScheduledEvent event)
    {
        mIsCommutePending = true;
        mCommutePendingTextView.setVisibility(View.VISIBLE);

        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_main_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        if (mIsCommutePending)
            menu.findItem(R.id.action_schedule_ride)
                    .setEnabled(false)
                    .setTitle(R.string.action_commute_pending);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_schedule_ride:
                mEventListener.onScheduleRideRequested();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        MapBoxStateSaver.saveMapState(mMapView, MAP_STATE_KEY);
    }

    @Override
    public void onDestroyView()
    {
        mBus.unregister(this);
        super.onDestroyView();
    }

    public EventBus getBus()
    {
        return mBus;
    }

    public void setBus(EventBus mBus)
    {
        this.mBus = mBus;
    }
}
