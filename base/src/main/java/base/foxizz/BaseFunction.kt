package base.foxizz

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import base.foxizz.BaseApplication.Companion.baseApplication

/**
 * 获取字符串资源
 *
 * @param resId StringRes
 */
@JvmName("baseGetString")
fun getString(@StringRes resId: Int): String = baseApplication.getString(resId)

/**
 * 获取颜色资源
 *
 * @param resId ColorRes
 */
@JvmName("baseGetColor")
fun getColor(@ColorRes resId: Int) = baseApplication.getColor(resId)
