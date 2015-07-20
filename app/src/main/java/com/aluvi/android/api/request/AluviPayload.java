package com.aluvi.android.api.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by matthewxi on 7/15/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AluviPayload {

    private final String TAG = "AluviPayload";

    // Convert class fields to a Map<String, String>
    public Map<String, String> toMap() {
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();

        Map<String, String> out = null;
        try {
            // TODO
            // not a great way to handle this
            // need to get into internals
            // would be good to use Jackson to go bytes
            // and then translate from bytes to Map
            // problem with Map is getting Class type for readValue
            // need to avoid Type Erasure problems

            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            String json = mapper.writeValueAsString(this);
            Gson gson = new Gson();
            out = gson.fromJson(json, type);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return out;
    }
}
