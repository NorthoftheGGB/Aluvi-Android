package com.aluvi.android.api.gis.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by usama on 7/24/15.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GeocodeData {
    @JsonProperty("features")
    private Feature[] features;

    public Feature[] getFeatures() {
        return features;
    }

    public void setFeatures(Feature[] features) {
        this.features = features;
    }

    public static class Feature {
        @JsonProperty("id")
        private String id;

        @JsonProperty("type")
        private String type;

        @JsonProperty("address")
        private String address;

        @JsonProperty("text")
        private String streetName;

        @JsonProperty("place_name")
        private String placeName;

        @JsonProperty("center")
        private double[] center;

        @JsonProperty("context")
        private Context[] contexts;

        public double getLat() {
            return center[1];
        }

        public double getLon() {
            return center[0];
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStreetName() {
            return streetName;
        }

        public void setStreetName(String streetName) {
            this.streetName = streetName;
        }

        public String getPlaceName() {
            return placeName;
        }

        public void setPlaceName(String placeName) {
            this.placeName = placeName;
        }

        public String getStreet() {
            return address + " " + streetName;
        }

        public String getCity() {
            return getContextValue("place");
        }

        public String getState() {
            return getContextValue("region");
        }

        public String getPostalCode() {
            return getContextValue("postcode");
        }

        public String getCountry() {
            return getContextValue("country");
        }

        private String getContextValue(String id) {
            Context stateContext = getContext(id);
            return stateContext != null ? stateContext.getText() : "";
        }

        private Context getContext(String name) {
            if (contexts != null)
                for (Context context : contexts)
                    if (context.getId().contains(name))
                        return context;
            return null;
        }

        private static class Context {
            @JsonProperty("id")
            private String id;

            @JsonProperty("text")
            private String text;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }
        }
    }
}
