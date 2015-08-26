package com.aluvi.android.helpers.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by usama on 8/25/15.
 */
public abstract class BaseSpinnerArrayAdapter<T> extends BaseArrayAdapter<T> {
    private int dropdownLayoutResource;

    public BaseSpinnerArrayAdapter(Context context, int resource, int dropdownLayoutResource, ArrayList<T> data) {
        super(context, resource, data);
        this.dropdownLayoutResource = dropdownLayoutResource;
    }

    public BaseSpinnerArrayAdapter(Context context, int resource, int dropdownLayoutResource, T[] data) {
        super(context, resource, data);
        this.dropdownLayoutResource = dropdownLayoutResource;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View row = initRow(dropdownLayoutResource, position, convertView, parent);
        initDropDownBiew((ViewHolder) row.getTag(), position);
        return row;
    }

    protected abstract void initDropDownBiew(ViewHolder holder, int position);
}
