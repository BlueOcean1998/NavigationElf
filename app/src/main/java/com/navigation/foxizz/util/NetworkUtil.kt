package com.navigation.foxizz.util

import android.content.Context
import android.net.ConnectivityManager
import android.provider.Settings
import com.navigation.foxizz.BaseApplication

/**
 * 网络工具类
 */
object NetworkUtil {
    //获取网络状态
    /**
     * 判断是否有网络连接
     *
     * @return boolean
     */
    val isNetworkConnected: Boolean
        get() {
            val connectivityManager = BaseApplication.instance
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    /**
     * 获取网络类型
     *
     * @return wifi或mobile
     */
    val networkType: String
        get() {
            val connectivity = BaseApplication.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivity.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) {
                if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                    return "wifi"
                } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                    return "mobile"
                }
            }
            return ""
        }

    /**
     * 判断是否开启了飞行模式
     *
     * @return boolean
     */
    val isAirplaneModeOn: Boolean
        get() = Settings.Global.getInt(BaseApplication.instance.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0
}