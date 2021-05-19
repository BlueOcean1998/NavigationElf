package base.foxizz.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import base.foxizz.BaseApplication.Companion.baseApplication
import base.foxizz.BaseConstants
import base.foxizz.dsp

/**
 * 设置工具类
 */
object SettingUtil {
    /**
     * 判断是否已经获取了读取存储和定位权限
     *
     * @return Boolean
     */
    val hasReadWriteAndLocationPermissions: Boolean
        get() {
            val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(baseApplication, permission)
                    != PackageManager.PERMISSION_GRANTED
                ) return true
            }
            return false
        }

    //判断是否是手机模式
    val isMobile
        get() = (baseApplication.resources.configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE

    /**
     * 初始化设置（目前只有一个设置）
     *
     * @param context 上下文
     */
    fun initSettings(context: Context) {
        if (context is Activity) {
            //设置是否允许横屏
            context.requestedOrientation =
                if (dsp.getBoolean(BaseConstants.KEY_LANDSCAPE, false))
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //自动旋转
                else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //只允许竖屏
        }
    }
}