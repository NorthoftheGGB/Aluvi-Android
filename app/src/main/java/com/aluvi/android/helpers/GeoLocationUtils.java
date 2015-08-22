package com.aluvi.android.helpers;

import android.location.Address;

/**
 * Created by usama on 8/21/15.
 */
public class GeoLocationUtils {
    public final static float INVALID_LOCATION = 360;

    public static String getFormattedAddress(Address address) {
        StringBuilder out = new StringBuilder();
        int addressLines = address.getMaxAddressLineIndex() + 1;
        for (int i = 0; i < addressLines; i++) {
            String line = address.getAddressLine(i);
            if (line != null && !line.trim().equals("")) {
                out.append(address.getAddressLine(i));
                if (i + 1 < addressLines)
                    out.append(", ");
            }
        }

        return out.toString();
    }
}
