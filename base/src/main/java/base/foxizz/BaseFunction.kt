package base.foxizz

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import base.foxizz.BaseApplication.Companion.baseApplication

/**
 * 基础函数
 */

/**
 * 获取字符串资源
 *
 * @param resId 字符串资源id
 */
fun getString(@StringRes resId: Int): String = baseApplication.getString(resId)

/**
 * 获取颜色资源
 *
 * @param resId 颜色资源id
 */
fun getColor(@ColorRes resId: Int) = baseApplication.getColor(resId)
