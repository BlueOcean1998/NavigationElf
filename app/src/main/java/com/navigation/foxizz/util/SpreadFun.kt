package com.navigation.foxizz.util

import com.navigation.foxizz.BaseApplication.Companion.baseApplication

/**
 * 将px值转换为sp值
 */
fun Float.pxToSp(): Float = this / baseApplication.resources.displayMetrics.scaledDensity

/**
 * 将sp值转换为px值
 */
fun Float.spToPx(): Float = this * baseApplication.resources.displayMetrics.scaledDensity
