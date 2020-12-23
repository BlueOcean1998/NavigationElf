package com.example.foxizz.navigation.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;

import com.example.foxizz.navigation.BuildConfig;

import java.util.Objects;

import static com.example.foxizz.navigation.MyApplication.getContext;

public class AppUtil {

    /**
     * 获取应用程序名称
     *
     * @return String
     */
    public static synchronized String getAppName() {
        try {
            PackageManager packageManager = getContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getContext().getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return getContext().getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用版本名称信息
     *
     * @return 当前应用的版本名称
     */
    public static synchronized String getAppVersionName() {
        try {
            PackageManager packageManager = getContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getContext().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用版本名称信息
     *
     * @return 当前应用的版本名称
     */
    public static synchronized int getAppVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    /**
     * 获取应用程序版本名称信息
     *
     * @return 当前应用的版本名称
     */
    public static synchronized String getPackageName() {
        try {
            PackageManager packageManager = getContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getContext().getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用图标
     *
     * @return Bitmap
     */
    public static synchronized Bitmap getAppBitmap() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo;
        try {
            packageManager = getContext().getApplicationContext()
                    .getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(
                    getContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        Drawable d = packageManager.getApplicationIcon(Objects.requireNonNull(applicationInfo)); //xxx根据自己的情况获取drawable
        BitmapDrawable bd = (BitmapDrawable) d;
        return bd.getBitmap();
    }

    /**
     * 获取渠道名称 channel
     *
     * @return String
     */
    public static synchronized String getAppChannel() {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = packageInfo.applicationInfo.metaData;
            return metaData.getString("CHANNEL");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取SD卡路径
     *
     * @return String
     */
    public static String getSDCardDir() {
        return Environment.getExternalStorageDirectory().toString();
    }

    /**
     * 获取app文件夹名
     *
     * @return String
     */
    public static String getAppFolderName() {
        return getContext().getExternalCacheDir().getPath();
    }

}