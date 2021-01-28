package com.navigation.foxizz.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.navigation.foxizz.data.Constants;

import static com.navigation.foxizz.BaseApplication.getContext;

/**
 * 设置工具类
 */
public class SettingUtil {

    /**
     * 判断是否已经获取了读取存储和定位权限
     *
     * @return boolean
     */
    public static boolean haveReadWriteAndLocationPermissions() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission)
                    != PackageManager.PERMISSION_GRANTED)
                return true;
        }

        return false;
    }

    //判断是否是手机模式
    public static boolean isMobile() {
        return (getContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * 初始化设置（目前只有一个设置）
     *
     * @param context 上下文
     */
    public static void initSettings(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            //设置是否允许横屏
            if (PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(Constants.KEY_LANDSCAPE, false))
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//自动旋转
            else activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//只允许竖屏
        }
    }

}
