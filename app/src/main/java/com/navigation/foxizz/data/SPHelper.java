package com.navigation.foxizz.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences帮助类
 */
public class SPHelper {

    private static SharedPreferences sp;

    /**
     * 初始化SharedPreferences
     */
    public static void initSharedPreferences(Context context) {
        sp = context.getSharedPreferences(Constants.SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static void putBoolean(String key, boolean value) {
        try {
            sp.edit().putBoolean(key, value).apply();
        } catch (Exception e) {
            sp.edit().remove(key).putBoolean(key, value).apply();
        }
    }

    public static boolean getBoolean(String key, boolean defValue) {
        try {
            return sp.getBoolean(key, defValue);
        } catch (Exception e) {
            sp.edit().remove(key).putBoolean(key, defValue).apply();
        }
        return defValue;
    }

    public static void putString(String key, String value) {
        try {
            sp.edit().putString(key, value).apply();
        } catch (Exception e) {
            sp.edit().remove(key).putString(key, value).apply();
        }
    }

    public static String getString(String key, String defValue) {
        try {
            return sp.getString(key, defValue);
        } catch (Exception e) {
            sp.edit().remove(key).putString(key, defValue).apply();
        }
        return defValue;
    }

    public static void putInt(String key, int value) {
        try {
            sp.edit().putInt(key, value).apply();
        } catch (Exception e) {
            sp.edit().remove(key).putInt(key, value).apply();
        }
    }

    public static int getInt(String key, int defValue) {
        try {
            return sp.getInt(key, defValue);
        } catch (Exception e) {
            sp.edit().remove(key).putInt(key, defValue).apply();
        }
        return defValue;
    }

}
