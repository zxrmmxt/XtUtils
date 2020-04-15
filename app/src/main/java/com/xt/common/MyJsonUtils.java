package com.xt.common;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * created by XuTi on 2019/6/4 11:10
 */
public class MyJsonUtils {

    private static final String TAG = MyJsonUtils.class.getSimpleName();

    public static JsonElement parseJson(String json) {
        try {
            return JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "bad json:" + json);
            throw e;
        }
    }

    /**
     * 传入的类型要和json一致
     *
     * @param json   It could either be a {@link JsonObject}, a {@link JsonArray}, a {@link JsonPrimitive} or a {@link JsonNull}.
     * @param tClass It could either be a {@link JsonObject}, a {@link JsonArray}, a {@link JsonPrimitive} or a {@link JsonNull}.
     *               可以传例如：对象类型{@link Object}, 对象数组类型{@link Object[]}, 字符串类型{@link String}, 基本类型{@link Integer}
     */
    public static <T> T getJsonElementFromJson(String json, Class<T> tClass) {
        JsonElement jsonElement = parseJson(json);
        return new Gson().fromJson(jsonElement, tClass);
    }

    /**
     * 传入的类型要和key对应的值一致
     *
     * @param json   一定是{@link JsonObject}类型
     * @param key    json字段名
     * @param tClass It could either be a {@link JsonObject}, a {@link JsonArray}, a {@link JsonPrimitive} or a {@link JsonNull}.
     *               可以传例如：对象类型{@link Object}, 对象数组类型{@link Object[]}, 字符串类型{@link String}, 基本类型{@link Integer}
     */
    public static <T> T getJsonElementFromJsonField(String json, String key, Class<T> tClass) {
        JsonElement jsonElement = parseJson(json);
        if (jsonElement.isJsonObject()) {
            JsonObject  jsonObject            = jsonElement.getAsJsonObject();
            JsonElement fieldValueJsonElement = jsonObject.get(key);
            return new Gson().fromJson(fieldValueJsonElement, tClass);
        } else {
            throw new IllegalStateException("Not a JSON Object: " + json);
        }
    }

    public static String getAsStringFromJsonField(String json, String fieldName) {
        return getJsonElementFromJsonField(json, fieldName, String.class);
    }

    /**
     * @param json
     * @param key
     * @return
     */
    public static String getJsonStrfromJsonField(String json, String key) {
        return getJsonElementFromJsonField(json, key, Object.class).toString();
    }


    public static boolean hasFieldName(String json, String fieldName) {
        JsonElement jsonElement = parseJson(json);
        if (jsonElement.isJsonObject()) {
            return jsonElement.getAsJsonObject().has(fieldName);
        } else {
            throw new IllegalStateException("Not a JSON Object: " + json);
        }
    }

    public static boolean isGoodJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "bad json:" + json);
            return false;
        }
    }

    public static boolean isJsonObject(String json) {
        try {
            JsonParser.parseString(json).isJsonObject();
            return true;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "bad json: " + json);
            return false;
        }
    }

    public static boolean isJsonArray(String json) {
        try {
            JsonParser.parseString(json).isJsonArray();
            return true;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "bad json: " + json);
            return false;
        }
    }

    public static <T> T fromJsonWithTypeToken(String json) {
        return new Gson().fromJson(json, new TypeToken<T>() {
        }.getType());
    }

    /**
     * 不能用泛型的时候，可以将Object类型的对象转成json字符串
     * 1、javabean对象转成json字符串
     * 2、javabean对象数组转成json数组
     */
    public static String objectToJson(Object object) {
        return new Gson().toJson(object);
    }

    /**
     * 对象转成json字符串
     *
     * @param t
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> String toJson(T t, Class<T> tClass) {
        return new Gson().toJson(t, tClass);
    }
}
