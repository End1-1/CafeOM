package com.cafeom.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;

public class DataParser {

    public static Gson gson() {
        GsonBuilder gb = new GsonBuilder();
        return gb.create();
    }

    public static <T> T parse(String s, Class<T> t) {
        return gson().fromJson(s, (Type) t);
    }

    public static <T> T parse(JsonObject s, Class<T> t) {
        return gson().fromJson(s, (Type) t);
    }
}
