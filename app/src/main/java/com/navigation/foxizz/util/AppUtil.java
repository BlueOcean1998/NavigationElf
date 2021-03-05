package com.navigation.foxizz.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;

import com.navigation.foxizz.BuildConfig;

import java.util.Objects;

import static com.navigation.foxizz.BaseApplication.getBaseApplication;

/**
 * 应用工具类
 */
public class AppUtil {

    /**
     * 获取应用包名
     *
     * @return 应用包名
     */
    public static synchronized String getPackageName() {
        try {
            PackageManager packageManager = getBaseApplication().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getBaseApplication().getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取应用程序名称
     *
     * @return 应用程序名称
     */
    public static synchronized String getAppName() {
        try {
            PackageManager packageManager = getBaseApplication().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getBaseApplication().getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return getBaseApplication().getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取应用版本号
     *
     * @return 应用的版本号
     */
    public static synchronized int getAppVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    /**
     * 获取应用版本名
     *
     * @return 应用的版本名
     */
    public static synchronized String getAppVersionName() {
        try {
            PackageManager packageManager = getBaseApplication().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getBaseApplication().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取应用图标
     *
     * @return 应用图标Bitmap
     */
    public static synchronized Bitmap getAppBitmap() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo;
        try {
            packageManager = getBaseApplication().getApplicationContext()
                    .getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(
                    getBaseApplication().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        Drawable d = packageManager.getApplicationIcon(Objects.requireNonNull(applicationInfo)); //xxx根据自己的情况获取drawable
        BitmapDrawable bd = (BitmapDrawable) d;
        return bd.getBitmap();
    }

    /**
     * 获取渠道名称channel
     *
     * @return 渠道名称channel
     */
    public static synchronized String getAppChannel() {
        PackageManager packageManager = getBaseApplication().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getBaseApplication().getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = packageInfo.applicationInfo.metaData;
            String appChannel = metaData.getString("CHANNEL");
            if (appChannel != null) return appChannel;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取SD卡路径
     *
     * @return SD卡路径
     */
    public static String getSDCardDir() {
        return Environment.getExternalStorageDirectory().toString();
    }

    /**
     * 获取应用文件夹路径
     *
     * @return 应用文件夹路径
     */
    public static String getAppFolderName() {
        return Objects.requireNonNull(getBaseApplication().getExternalCacheDir()).getPath();
    }

}