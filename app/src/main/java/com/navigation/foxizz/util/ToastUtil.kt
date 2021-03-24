package com.navigation.foxizz.util

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.navigation.foxizz.BaseApplication

//下一个Toast弹出时立刻覆盖掉上一个Toast
private var mToast: Toast? = null

//所有Toast转移至主线程，防止在子线程更新UI
private val mainHandler = Handler(Looper.getMainLooper())

/**
 * 弹出提示信息
 *
 * @param duration 延时（short/long），默认为short
 */
fun String.showToast(duration: Int = Toast.LENGTH_SHORT) {
    mainHandler.post {
        mToast?.cancel()
        mToast = Toast.makeText(BaseApplication.instance, null, duration)
        mToast!!.setText(this)
        mToast!!.show()
    }
}

/**
 * 弹出提示信息
 *
 * @param duration 延时（short/long），默认为short
 */
fun Int.showToast(duration: Int = Toast.LENGTH_SHORT) {
    mainHandler.post {
        mToast?.cancel()
        mToast = Toast.makeText(BaseApplication.instance, null, duration)
        mToast!!.setText(this)
        mToast!!.show()
    }
}