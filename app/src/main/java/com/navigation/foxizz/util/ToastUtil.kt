package com.navigation.foxizz.util

import android.widget.Toast
import com.navigation.foxizz.BaseApplication.Companion.baseApplication
import com.navigation.foxizz.mlh

//下一个Toast弹出时立刻覆盖掉上一个Toast
private var mToast: Toast? = null

/**
 * 弹出提示信息
 *
 * @param duration 延时（short/long），默认为short
 */
fun String.showToast(duration: Int = Toast.LENGTH_SHORT) {
    mlh.post {
        mToast?.cancel()
        mToast = Toast.makeText(baseApplication, null, duration)
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
    mlh.post {
        mToast?.cancel()
        mToast = Toast.makeText(baseApplication, null, duration)
        mToast!!.setText(this)
        mToast!!.show()
    }
}