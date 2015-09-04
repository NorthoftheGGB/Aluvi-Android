package com.aluvi.android.helpers.eventBus;

/**
 * Created by usama on 9/3/15.
 */
public class SlidingPanelEvent {
    private float mPanelHeight;

    public SlidingPanelEvent(float mPanelHeight) {
        this.mPanelHeight = mPanelHeight;
    }

    public float getPanelHeight() {
        return mPanelHeight;
    }

    public void setPanelHeight(float mPanelHeight) {
        this.mPanelHeight = mPanelHeight;
    }
}
