package base.foxizz.util

import android.os.Build
import java.util.*

/**
 * 系统信息工具类
 */
object SystemUtil {
    /**
     * 获取当前手机系统语言，例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    val systemLanguage get() = Locale.getDefault().toString()

    /**
     * 获取当前手机系统版本号
     */
    val systemVersion: String = Build.VERSION.RELEASE

    /**
     * 获取用户界面版本号
     */
    val systemDisplay: String = Build.DISPLAY

    /**
     * 获取手机型号
     */
    val systemModel: String = Build.MODEL

    /**
     * 获取手机厂商
     */
    val deviceBrand: String = Build.BRAND
}