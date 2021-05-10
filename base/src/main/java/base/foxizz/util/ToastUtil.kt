package base.foxizz.util

import android.widget.Toast
import androidx.annotation.StringRes
import base.foxizz.BaseApplication.Companion.baseApplication
import base.foxizz.mlh

/**
 * Toast工具类
 * 下一个Toast弹出时立刻覆盖掉上一个Toast
 */

private var mToast: Toast? = null

/**
 * 弹出提示信息
 *
 * @param string 字符串
 * @param duration 延时（short/long），默认为short
 */
@JvmName("baseShowToast")
fun showToast(string: String, duration: Int = Toast.LENGTH_SHORT) {
    mlh.post {
        mToast?.cancel()
        mToast = Toast.makeText(baseApplication, null, duration)
        mToast!!.setText(string)
        mToast!!.show()
    }
}

/**
 * 弹出提示信息
 *
 * @param resId StringRes
 * @param duration 延时（short/long），默认为short
 */
@JvmName("baseShowToast")
fun showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    mlh.post {
        mToast?.cancel()
        mToast = Toast.makeText(baseApplication, null, duration)
        mToast!!.setText(resId)
        mToast!!.show()
    }
}
