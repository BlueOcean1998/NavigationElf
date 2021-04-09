package com.navigation.foxizz.util

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import com.navigation.foxizz.BaseApplication.Companion.baseApplication
import com.navigation.foxizz.R

/**
 * 布局工具类
 */
object LayoutUtil {
    /**
     * 获取控件真实尺寸
     *
     * @param view 控件
     * @param type true:宽 false:高
     * @return 尺寸
     */
    fun getViewSize(view: View, type: Boolean): Int {
        val heightOrWidth = IntArray(1)
        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (type) heightOrWidth[0] = view.width
                else heightOrWidth[0] = view.height
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        return heightOrWidth[0]
    }

    /**
     * 获取改变控件尺寸动画
     *
     * @param view        需要改变高度的view
     * @param startHeight 动画前的高度
     * @param endHeight   动画后的高度
     */
    fun getValueAnimator(view: View, startHeight: Int, endHeight: Int): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(startHeight, endHeight)
        //valueAnimator.duration = 300;//动画时间（默认就是300）
        valueAnimator.addUpdateListener { animation -> //逐渐改变view的高度
            view.layoutParams.height = animation.animatedValue as Int
            view.requestLayout()
        }
        return valueAnimator
    }

    /**
     * 获取改变控件尺寸，同时固定点击的item的动画
     *
     * @param view         需要改变高度的view
     * @param startHeight  动画前的高度
     * @param endHeight    动画后的高度
     * @param recyclerView 需要回滚的recyclerView
     * @param position     回滚的位置
     */
    fun getValueAnimator(
            view: View, startHeight: Int, endHeight: Int,
            recyclerView: RecyclerView, position: Int): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(startHeight, endHeight)
        //valueAnimator.duration = 300;//动画时间（默认就是300）
        valueAnimator.addUpdateListener { animation -> //逐渐改变view的高度
            view.layoutParams.height = animation.animatedValue as Int
            view.requestLayout()

            //不断地移动回点击的item的位置
            recyclerView.scrollToPosition(position)
        }
        return valueAnimator
    }
}

/**
 * 将px值转换为sp值
 */
fun Float.pxToSp(): Float = this / baseApplication.resources.displayMetrics.scaledDensity

/**
 * 将sp值转换为px值
 */
fun Float.spToPx(): Float = this * baseApplication.resources.displayMetrics.scaledDensity

/**
 * 获取控件宽度
 *
 * @return 宽度
 */
fun View.getRealWidth(): Int {
    return LayoutUtil.getViewSize(this, true)
}

/**
 * 获取控件高度
 *
 * @return 高度
 */
fun View.getRealHeight(): Int {
    return LayoutUtil.getViewSize(this, false)
}

/**
 * 设置控件宽度
 *
 * @param width 宽度
 */
fun View.setWidth(width: Int) {
    layoutParams.width = width
}

/**
 * 设置控件高度
 *
 * @param height 高度
 */
fun View.setHeight(height: Int) {
    layoutParams.height = height
}

/**
 * 伸缩布局
 *
 * @param flag 伸或缩
 */
fun LinearLayout.expandLayout(flag: Boolean) {
    if (flag) {
        //动画2，出现
        startAnimation(AnimationUtils.loadAnimation(baseApplication, R.anim.adapter_alpha2))
        //计算布局自适应时的高度
        var layoutHeight = 0
        for (view in this) {
            layoutHeight += view.layoutParams.height
        }
        LayoutUtil.getValueAnimator(this, 0, layoutHeight).start() //展开动画
    } else {
        //动画1，消失;
        startAnimation(AnimationUtils.loadAnimation(baseApplication, R.anim.adapter_alpha1))
        val layoutHeight = height //获取布局的高度
        LayoutUtil.getValueAnimator(this, layoutHeight, 0).start() //收起动画
    }
}

/**
 * 伸缩布局，同时固定点击的item
 *
 * @param flag         伸或缩
 * @param textView     子布局中的TextView
 * @param recyclerView 需要回滚的recyclerView
 * @param position     回滚的位置
 */
fun LinearLayout.expandLayout(
        flag: Boolean, textView: TextView, recyclerView: RecyclerView, position: Int) {
    if (flag) {
        //动画2，出现
        startAnimation(AnimationUtils.loadAnimation(baseApplication, R.anim.adapter_alpha2))
        //计算布局自适应时的高度
        val layoutHeight = textView.lineHeight * (textView.lineCount + 1)
        LayoutUtil.getValueAnimator(this, 0, layoutHeight, recyclerView, position)
                .start() //展开动画
    } else {//动画1，消失
        startAnimation(AnimationUtils.loadAnimation(baseApplication, R.anim.adapter_alpha1))
        val layoutHeight = height //获取布局的高度
        LayoutUtil.getValueAnimator(this, layoutHeight, 0, recyclerView, position)
                .start() //收起动画
    }
}

/**
 * 旋转控件动画
 *
 * @param from 动画前的旋转角度
 * @param to   动画后的旋转角度
 */
fun View.rotateExpandIcon(from: Float, to: Float) {
    val valueAnimator = ValueAnimator.ofFloat(from, to)
    valueAnimator.interpolator = DecelerateInterpolator() //先加速后减速的动画
    //valueAnimator.duration = 300;//动画时间（默认就是300）
    valueAnimator.addUpdateListener { //逐渐改变view的旋转角度
        rotation = valueAnimator.animatedValue as Float
    }
    valueAnimator.start()
}
