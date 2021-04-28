package base.foxizz.util

import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import base.foxizz.BaseApplication.Companion.baseApplication

/**
 * 应用工具类
 */
object AppUtil {
    /**
     * 获取应用包名
     */
    val packageName = baseApplication.packageName

    /**
     * 获取应用程序名称
     */
    val appName: String = baseApplication.run {
        resources.getString(packageManager.getPackageInfo(packageName, 0)
                .applicationInfo.labelRes)
    }

    /**
     * 获取应用版本号
     */
    val appVersionCode = baseApplication.packageManager.getPackageInfo(
            baseApplication.packageName, 0).run {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            longVersionCode
        } else {
            versionCode
        }
    }

    /**
     * 获取应用版本名
     */
    val appVersionName = baseApplication.run {
        packageManager.getPackageInfo(packageName, 0).versionName
    }

    /**
     * 获取应用图标
     */
    val appBitmap = baseApplication.packageManager.run {
        (getApplicationIcon(getApplicationInfo(baseApplication.packageName, 0))
                as BitmapDrawable).bitmap
    }


    /**
     * 获取渠道名称channel
     */
    val appChannel = baseApplication.run {
        packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
                .applicationInfo.metaData.getString("CHANNEL") ?: ""
    }

    /**
     * 获取SD卡路径
     */
    val sdCardDir = Environment.getExternalStorageDirectory().toString()

    /**
     * 获取应用文件夹路径
     */
    val appFolderName = baseApplication.externalCacheDir?.path ?: ""
}