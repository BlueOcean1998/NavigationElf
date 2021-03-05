package com.navigation.foxizz.util;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import static com.navigation.foxizz.BaseApplication.getBaseApplication;

/**
 * Toast工具类
 */
public class ToastUtil {

    private static Toast mToast;//Toast对象

    private final static Handler mainHandler = new Handler(Looper.getMainLooper());//主线程Handler

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
    public static void showToast(final String text, final int duration) {
        //转移至主线程，防止在子线程更新UI
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) mToast.cancel();//销毁上一个
                mToast = Toast.makeText(getBaseApplication(), null, duration);//弹出下一个
                mToast.setText(text);//重设内容，去除小米自带的"appName:"
                mToast.show();
            }
        });
    }

    /**
     * 弹出提示信息
     *
     * @param resId    提示信息的resId
     * @param duration 延时（short/long）
     */
    public static void showToast(final int resId, final int duration) {
        //转移至主线程，防止在子线程更新UI
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) mToast.cancel();//销毁上一个
                mToast = Toast.makeText(getBaseApplication(), null, duration);//弹出下一个
                mToast.setText(resId);//重设内容，去除小米自带的"appName:"
                mToast.show();
            }
        });
    }

}
