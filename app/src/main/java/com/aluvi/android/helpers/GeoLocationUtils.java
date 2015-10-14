package com.aluvi.android.helpers;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by usama on 8/21/15.
 */
public class GeoLocationUtils {
    public final static float INVALID_LOCATION = 360;

    public static class BoundingBox {
        private LatLng mNorthEast, mSouthWest;

        public BoundingBox(LatLng mNorthEast, LatLng mSouthWest) {
            this.mNorthEast = mNorthEast;
            this.mSouthWest = mSouthWest;
        }

        public LatLng getNorthEast() {
            return mNorthEast;
        }

        public void setNorthEast(LatLng mNorthEast) {
            this.mNorthEast = mNorthEast;
        }

        public LatLng getSouthWest() {
            return mSouthWest;
        }

        public void setSouthWest(LatLng mSouthWest) {
            this.mSouthWest = mSouthWest;
        }

        public boolean contains(LatLng point) {
            return point.latitude < mNorthEast.latitude && point.latitude > mSouthWest.latitude
                    && point.longitude < mNorthEast.longitude && point.longitude > mSouthWest.longitude;
        }
    }

    public static String getFormattedAddress(Address address) {
        StringBuilder out = new StringBuilder();
        int addressLines = address.getMaxAddressLineIndex() + 1;
        for (int i = 0; i < addressLines; i++) {
            String line = address.getAddressLine(i);
            if (line != null && !line.trim().equals("")) {
                out.append(address.getAddressLine(i));
                if (i + 1 < addressLines) {
                    String nextLine = address.getAddressLine(i + 1);
                    if (nextLine != null && !nextLine.equals(""))
                        out.append(", ");
                }
            }
        }

        return out.toString();
    }
}
