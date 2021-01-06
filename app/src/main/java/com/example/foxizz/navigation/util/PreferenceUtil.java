package com.example.foxizz.navigation.util;

import android.content.Context;
import android.content.SharedPreferences;

import static com.example.foxizz.navigation.BaseApplication.getContext;

/**
 * Preference工具类
 */
public class PreferenceUtil {

    private static final String SETTINGS = "settings";

    //写入布尔值
    public static void putBoolean(String key, boolean value) {
        SharedPreferences sp = getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, value).apply();
    }

    //读取布尔值
    public static boolean getBoolean(String key, boolean defValue) {
        SharedPreferences sp = getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defValue);
    }

    //写入字符串
    public static void putString(String key, String value) {
        SharedPreferences sp = getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    //读取字符串
    public static String getString(String key, String defValue) {
        SharedPreferences sp = getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    //写入整数
    public static void putInt(String key, int value) {
        SharedPreferences sp = getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        sp.edit().putInt(key, value).apply();
    }

    //读取整数
    public static int getInt(String key, int defValue) {
        SharedPreferences sp = getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.getInt(key, defValue);
    }

}
