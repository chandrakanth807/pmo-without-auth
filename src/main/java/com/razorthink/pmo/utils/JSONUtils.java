package com.razorthink.pmo.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JSONUtils {

    private JSONUtils() {

    }

    private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T parse(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

}