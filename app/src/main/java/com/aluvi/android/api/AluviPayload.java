package com.aluvi.android.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matthewxi on 7/15/15.
 */
public class AluviPayload {

    public Map<String, String> toMap (){
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Gson gson = new Gson();
        String json = gson.toJson(this);
        HashMap map = gson.fromJson(json, type);
        return map;
    }

}
