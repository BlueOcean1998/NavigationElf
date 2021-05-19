package base.foxizz.util

import android.widget.Toast
import androidx.annotation.StringRes
import base.foxizz.BaseApplication.Companion.baseApplication
import base.foxizz.mlh

/**
 * Toast工具类
 */

//全局Toast对象，在下一个Toast弹出时覆盖上一个Toast，不再排队等待
private lateinit var mToast: Toast

//获取新Toast对象
private val newToast get() = Toast.makeText(baseApplication, null, Toast.LENGTH_SHORT)

//设置Toast内容和延时并弹出
private fun showToast(toast: Toast, string: String?, resId: Int?, duration: Int) {
    toast.run {
        string?.let { setText(it) }
        resId?.let { setText(it) }
        this.duration = duration
        show()
    }
}

//确保Toast操作在主线程进行，等待Toast或覆盖Toast
private fun showToast(string: String?, resId: Int?, duration: Int, isWait: Boolean) {
    mlh.post {
        if (isWait) {
            showToast(newToast, string, resId, duration)
        } else {
            if (::mToast.isInitialized) mToast.cancel()
            mToast = newToast
            showToast(mToast, string, resId, duration)
        }
    }
}

/**
 * 弹出提示信息
 *
 * @param string   字符串资源id
 * @param duration 延时（short/long），默认为short
 * @param isWait   是否等待，若该参数设置为true，则新Toast不再会覆盖掉旧Toast，而是会排队等待弹出
 */
fun showToast(string: String, duration: Int = Toast.LENGTH_SHORT, isWait: Boolean = false) =
    if (string.isNotEmpty()) showToast(string, null, duration, isWait) else Unit

fun showToast(string: String, isWait: Boolean) =
    if (string.isNotEmpty()) showToast(string, null, Toast.LENGTH_SHORT, isWait) else Unit

/**
 * 弹出提示信息
 *
 * @param resId    字符串资源id
 * @param duration 延时（short/long），默认为short
 * @param isWait   是否等待，若该参数设置为true，则新Toast不再会覆盖掉旧Toast，而是会排队等待弹出
 */
fun showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT, isWait: Boolean = false) =
    showToast(null, resId, duration, isWait)

fun showToast(@StringRes resId: Int, isWait: Boolean) =
    showToast(null, resId, Toast.LENGTH_SHORT, isWait)
