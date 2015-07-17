package com.aluvi.android.helpers.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Helper class that makes it easier to work with ListViews. Also optimizes list view management via the View Holder pattern.
 * Simply subclass {@link #initView(ViewHolder holder, int position)} and instantiate your views using the provided {@link ViewHolder}.
 *
 * @param <T>
 * @author Usama
 *         <p/>
 */
public abstract class BaseArrayAdapter<T> extends ArrayAdapter<T>
{
    private int rowLayoutResource;

    public BaseArrayAdapter(Context context, int resource, ArrayList<T> data)
    {
        super(context, resource, data);
        this.rowLayoutResource = resource;
    }

    public BaseArrayAdapter(Context context, int resource, T[] data)
    {
        super(context, resource, data);
        this.rowLayoutResource = resource;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        if (row == null)
        {
            row = LayoutInflater.from(getContext()).inflate(rowLayoutResource, parent, false);

            ViewHolder holder = new ViewHolder();
            ArrayList<View> childViews = getAllChildrenForRootView(row);
            for (View view : childViews)
            {
                holder.addView(view);
            }

            row.setTag(holder);
        }

        initView((ViewHolder) row.getTag(), position);
        return row;
    }

    protected abstract void initView(ViewHolder holder, int position);

    public static ArrayList<View> getAllChildrenForRootView(View rootView)
    {
        try
        {
            ViewGroup viewGroup = (ViewGroup) rootView;

            ArrayList<View> output = new ArrayList<View>();
            int childCount = viewGroup.getChildCount();
            output.add(viewGroup); // Keep track of views that have children

            for (int i = 0; i < childCount; i++)
            {
                View child = viewGroup.getChildAt(i);
                ArrayList<View> viewsForChild = getAllChildrenForRootView(child);
                output.addAll(viewsForChild);
            }

            return output;
        }
        catch (ClassCastException e) // View does not have any children
        {
            ArrayList<View> child = new ArrayList<View>();
            child.add(rootView);
            return child;
        }
    }

    public int getRowLayoutResource()
    {
        return rowLayoutResource;
    }

    public void setRowLayoutResource(int rowLayoutResource)
    {
        this.rowLayoutResource = rowLayoutResource;
    }
}
