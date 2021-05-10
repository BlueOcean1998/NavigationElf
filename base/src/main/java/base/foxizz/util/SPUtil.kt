package base.foxizz.util

import android.content.Context
import androidx.core.content.edit
import base.foxizz.BaseApplication.Companion.baseApplication
import base.foxizz.BaseConstants

/**
 * SharedPreferences工具类
 */
object SPUtil {
    private val sp = baseApplication.getSharedPreferences(
        BaseConstants.SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)

    /**
     * 设置键值
     *
     * @param key 键
     * @param value 值
     */
    fun put(key: String, value: Any) {
        sp.edit {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
            }
        }
    }

    /**
     * 移除键
     *
     * @param key 键
     */
    fun remove(key: String) {
        sp.edit {
            remove(key)
        }
    }

    /**
     * 获取Boolean值
     *
     * @param key 键
     * @param defValue 默认值
     */
    fun getBoolean(key: String, defValue: Boolean = false) = sp.getBoolean(key, defValue)

    /**
     * 获取Int值
     *
     * @param key 键
     * @param defValue 默认值
     */
    fun getInt(key: String, defValue: Int = 0) = sp.getInt(key, defValue)

    /**
     * 获取Float值
     *
     * @param key 键
     * @param defValue 默认值
     */
    fun getFloat(key: String, defValue: Float = 0F) = sp.getFloat(key, defValue)

    /**
     * 获取Long值
     *
     * @param key 键
     * @param defValue 默认值
     */
    fun getLong(key: String, defValue: Long = 0) = sp.getLong(key, defValue)

    /**
     * 获取String值
     *
     * @param key 键
     * @param defValue 默认值
     */
    fun getString(key: String, defValue: String = "") = sp.getString(key, defValue) ?: ""
}