package cn.zerokirby.api.util;

import android.content.res.Configuration;
import android.os.Build;

import static cn.zerokirby.api.ZerokirbyApi.getApplication;

/**
 * 系统信息工具
 */
public class SystemUtil {

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return java.util.Locale.getDefault().toString();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取用户界面版本号
     *
     * @return 手机型号
     */
    public static String getSystemDisplay() {
        return Build.DISPLAY;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    //判断是否是手机模式
    public static boolean isMobile() {
        return (getApplication().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}
