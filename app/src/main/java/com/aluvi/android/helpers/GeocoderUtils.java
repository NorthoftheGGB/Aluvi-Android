package com.aluvi.android.helpers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by usama on 7/13/15.
 */
public class GeocoderUtils
{
    public static void getAddressesForName(final String name, final int maxResults, final Context context,
                                           final AsyncCallback<List<Address>> addressCallback)
    {
        new AsyncTask<Void, Void, List<Address>>()
        {
            @Override
            protected List<Address> doInBackground(Void... voids)
            {
                Geocoder coder = new Geocoder(context, Locale.getDefault());
                try
                {
                    return coder.getFromLocationName(name, maxResults);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(List<Address> addresses)
            {
                super.onPostExecute(addresses);
                if (addressCallback != null)
                    addressCallback.onOperationCompleted(addresses);
            }
        }.execute();
    }

    public static void getAddressesForLocation(final double lat, final double lon, final int maxResults,
                                               final Context context, final AsyncCallback<List<Address>> addressCallback)
    {
        new AsyncTask<Void, Void, List<Address>>()
        {
            @Override
            protected List<Address> doInBackground(Void... voids)
            {
                Geocoder coder = new Geocoder(context, Locale.getDefault());
                try
                {
                    return coder.getFromLocation(lat, lon, maxResults);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(List<Address> addresses)
            {
                super.onPostExecute(addresses);
                if (addressCallback != null)
                    addressCallback.onOperationCompleted(addresses);
            }
        }.execute();
    }

    public static String getFormattedAddress(Address address)
    {
        StringBuilder out = new StringBuilder();
        int addressLines = Math.min(2, address.getMaxAddressLineIndex());
        for (int i = 0; i < addressLines; i++)
        {
            out.append(address.getAddressLine(i));
            if (i + 1 < addressLines)
                out.append(", ");
        }

        return out.toString();
    }
}

