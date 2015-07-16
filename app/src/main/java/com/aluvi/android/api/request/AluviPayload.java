package com.aluvi.android.api.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matthewxi on 7/15/15.
 */
public class AluviPayload {

    // Convert class fields to a Map<String, String>
    public Map<String, String> toMap (){
        Type type = new TypeToken<Map<String, String>>(){}.getType();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            // not a great way to handle this
            // need to get into internals
            // would be good to use Jackson to go bytes
            // and then translate from bytes to Map
            // problem with Map is getting Class type for readValue
            // need to avoid Type Erasure problems
            String json = mapper.writeValueAsString(this);
            Gson gson = new Gson();
            HashMap map = gson.fromJson(json, type);
            return map;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

    }

}
