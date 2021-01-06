package com.example.foxizz.navigation.util;

import android.widget.Toast;

import static com.example.foxizz.navigation.BaseApplication.getContext;

/**
 * Toast工具类
 */
public class ToastUtil {

    private static Toast toast;

    /**
     * 弹出提示信息，默认显示时间为短
     *
     * @param text 提示信息
     */
    public static void showToast(String text) {
        showToast(text, Toast.LENGTH_SHORT);
    }

    /**
     * 弹出提示信息，默认显示时间为短
     *
     * @param resId 提示信息的resId
     */
    public static void showToast(int resId) {
        showToast(resId, Toast.LENGTH_SHORT);
    }

    /**
     * 弹出提示信息
     *
     * @param text     提示信息
     * @param duration 延时（short/long）
     */
    public static void showToast(String text, int duration) {
        if (toast != null) toast.cancel();//销毁上一个
        toast = Toast.makeText(getContext(), null, duration);//弹出下一个
        toast.setText(text);//重设内容，去除小米自带的"appName:"
        toast.show();
    }

    /**
     * 弹出提示信息
     *
     * @param resId    提示信息的resId
     * @param duration 延时（short/long）
     */
    public static void showToast(int resId, int duration) {
        if (toast != null) toast.cancel();//销毁上一个
        toast = Toast.makeText(getContext(), null, duration);//弹出下一个
        toast.setText(resId);//重设内容，去除小米自带的"appName:"
        toast.show();
    }

}
