package com.navigation.foxizz.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import static com.navigation.foxizz.BaseApplication.getContext;

/**
 * 网络工具类
 */
public class NetworkUtil {//获取网络状态

    /**
     * 判断是否有网络连接
     *
     * @return boolean
     */
    public static boolean isNetworkConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = null;
        if (mConnectivityManager != null) {
            mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        }
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    /**
     * 判断网络类型
     *
     * @return String
     */
    public static String getNetworkType() {
        ConnectivityManager connectivity = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    return "wifi";
                } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return "mobile";
                }
            }
        }
        return null;
    }

    /**
     * 判断是否开启了飞行模式
     *
     * @return boolean
     */
    public static boolean isAirplaneModeOn() {
        return Settings.Global.getInt(getContext().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

}
