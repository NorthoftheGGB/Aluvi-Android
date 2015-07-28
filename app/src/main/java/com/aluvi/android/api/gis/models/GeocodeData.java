package com.aluvi.android.api.gis.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by usama on 7/24/15.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GeocodeData {

    @JsonProperty("results")
    private Results[] results;

    private static class Results {
        @JsonProperty("locations")
        private GeocodedLocation[] locations;
    }

    public static class GeocodedLocation {
        @JsonProperty("street")
        private String street;

        @JsonProperty("adminArea5")
        private String city;

        @JsonProperty("adminArea3")
        private String state;

        @JsonProperty("adminArea1")
        private String country;

        @JsonProperty("postalCode")
        private String postalCode;

        @JsonProperty("latLng")
        private GeocodeLatLng latLng;

        public static class GeocodeLatLng {
            @JsonProperty("lat")
            private double lat;

            @JsonProperty("lng")
            private double lng;

            public double getLat() {
                return lat;
            }

            public void setLat(double lat) {
                this.lat = lat;
            }

            public double getLng() {
                return lng;
            }

            public void setLng(double lng) {
                this.lng = lng;
            }
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public GeocodeLatLng getLatLng() {
            return latLng;
        }

        public void setLatLng(GeocodeLatLng latLng) {
            this.latLng = latLng;
        }
    }

    public GeocodedLocation[] getLocations() {
        if (results != null && results.length > 0 && results[0] != null) {
            GeocodedLocation[] locations = results[0].locations;
            return locations;
        }

        return null;
    }
}
