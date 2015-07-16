package com.aluvi.android.api;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by matthewxi on 7/15/15.
 */
public class AluviPayload {

    public Map<String, String> toMap (){
        HashMap map = new HashMap<String, String>();
        Gson gson = new Gson();
        String json = gson.toJson(this);
        map = gson.fromJson(json, map.getClass());
        return map;
    }

}
