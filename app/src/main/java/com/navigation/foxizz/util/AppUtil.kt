package com.navigation.foxizz.util

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import com.navigation.foxizz.BaseApplication
import com.navigation.foxizz.BuildConfig
import com.navigation.foxizz.R

/**
 * 应用工具类
 */
object AppUtil {
    /**
     * 获取应用包名
     *
     * @return 应用包名
     */
    val packageName = BuildConfig.APPLICATION_ID

    /**
     * 获取应用程序名称
     *
     * @return 应用程序名称
     */
    val appName = BaseApplication.instance.getString(R.string.app_name)

    /**
     * 获取应用版本号
     *
     * @return 应用的版本号
     */
    val appVersionCode = BuildConfig.VERSION_CODE

    /**
     * 获取应用版本名
     *
     * @return 应用的版本名
     */
    val appVersionName = BuildConfig.VERSION_NAME

    /**
     * 获取应用图标
     *
     * @return 应用图标Bitmap
     */
    val appBitmap: Bitmap
        get() {
            val packageManager = BaseApplication.instance.packageManager
            val applicationInfo = packageManager.getApplicationInfo(
                    BaseApplication.instance.packageName, 0
            )
            return (packageManager.getApplicationIcon(applicationInfo) as BitmapDrawable).bitmap
        }

    /**
     * 获取渠道名称channel
     *
     * @return 渠道名称channel
     */
    val appChannel: String
        get() {
            val packageManager = BaseApplication.instance.packageManager
            val packageInfo = packageManager.getPackageInfo(
                    BaseApplication.instance.packageName, PackageManager.GET_META_DATA
            )
            val metaData = packageInfo.applicationInfo.metaData
            val channel = metaData.getString("CHANNEL")
            if (channel != null)
                return channel
            else return ""
        }

    /**
     * 获取SD卡路径
     *
     * @return SD卡路径
     */
    val sdCardDir = Environment.getExternalStorageDirectory().toString()

    /**
     * 获取应用文件夹路径
     *
     * @return 应用文件夹路径
     */
    val appFolderName = BaseApplication.instance.externalCacheDir?.path
}