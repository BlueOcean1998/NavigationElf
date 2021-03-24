package com.navigation.foxizz.data

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences帮助类
 */
object SPHelper {
    private lateinit var sp: SharedPreferences

    /**
     * 初始化SharedPreferences
     */
    fun initSharedPreferences(context: Context) {
        sp = context.getSharedPreferences(Constants.SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
    }

    fun putBoolean(key: String, value: Boolean) {
        try {
            sp.edit().putBoolean(key, value).apply()
        } catch (e: Exception) {
            sp.edit().remove(key).putBoolean(key, value).apply()
        }
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        try {
            return sp.getBoolean(key, defValue)
        } catch (e: Exception) {
            sp.edit().remove(key).putBoolean(key, defValue).apply()
        }
        return defValue
    }

    fun putString(key: String, value: String) {
        try {
            sp.edit().putString(key, value).apply()
        } catch (e: Exception) {
            sp.edit().remove(key).putString(key, value).apply()
        }
    }

    fun getString(key: String, defValue: String): String {
        try {
            val str = sp.getString(key, defValue)
            if (str != null) return str
        } catch (e: Exception) {
            sp.edit().remove(key).putString(key, defValue).apply()
        }
        return defValue
    }

    fun putInt(key: String, value: Int) {
        try {
            sp.edit().putInt(key, value).apply()
        } catch (e: Exception) {
            sp.edit().remove(key).putInt(key, value).apply()
        }
    }

    fun getInt(key: String, defValue: Int): Int {
        try {
            return sp.getInt(key, defValue)
        } catch (e: Exception) {
            sp.edit().remove(key).putInt(key, defValue).apply()
        }
        return defValue
    }
}