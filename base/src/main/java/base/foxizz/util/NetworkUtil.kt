package base.foxizz.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.provider.Settings
import base.foxizz.BaseApplication.Companion.baseApplication

/**
 * 网络工具类
 */
object NetworkUtil {
    /**
     * 判断是否有网络连接
     */
    val isNetworkConnected
        @SuppressLint("MissingPermission")
        get() = (baseApplication.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager).activeNetworkInfo.run {
            this != null && isConnected
        }

    /**
     * 获取网络类型
     */
    val networkType: String
        @SuppressLint("MissingPermission")
        get() {
            (baseApplication.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager).activeNetworkInfo.run {
                if (this != null && isConnected) {
                    when (type) {
                        ConnectivityManager.TYPE_WIFI -> return "wifi"
                        ConnectivityManager.TYPE_MOBILE -> return "mobile"
                    }
                }
            }
            return ""
        }

    /**
     * 判断是否开启了飞行模式
     */
    val isAirplaneModeEnable
        get() = Settings.Global.getInt(baseApplication.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0
}