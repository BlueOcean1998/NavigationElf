package com.navigation.foxizz.util

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.navigation.foxizz.BaseApplication
import com.navigation.foxizz.R

/**
 * 布局工具类
 */
object LayoutUtil {
    /**
     * 获取视图尺寸
     *
     * @param view 视图
     * @param type true:宽 false:高
     * @return 尺寸
     */
    fun getViewSize(view: View, type: Boolean): Int {
        val heightOrWidth = IntArray(1)
        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (type) heightOrWidth[0] = view.width else heightOrWidth[0] = view.height
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        return heightOrWidth[0]
    }

    /**
     * 获取视图宽度
     *
     * @param view 视图
     * @return 宽度
     */
    fun getViewWidth(view: View): Int {
        return getViewSize(view, true)
    }

    /**
     * 获取视图高度
     *
     * @param view 视图
     * @return 高度
     */
    fun getViewHeight(view: View): Int {
        return getViewSize(view, false)
    }

    /**
     * 设置视图宽度
     *
     * @param view  视图
     * @param width 宽度
     */
    fun setViewWidth(view: View, width: Int) {
        val params = view.layoutParams
        params.width = width
        view.layoutParams = params
    }

    /**
     * 设置视图高度
     *
     * @param view   视图
     * @param height 高度
     */
    fun setViewHeight(view: View, height: Int) {
        val params = view.layoutParams
        params.height = height
        view.layoutParams = params
    }

    /**
     * 伸缩布局
     *
     * @param linearLayout 需要伸缩的linearLayout
     * @param flag         伸或缩
     */
    fun expandLayout(linearLayout: LinearLayout, flag: Boolean) {
        if (flag) {
            linearLayout.startAnimation(
                    AnimationUtils.loadAnimation(BaseApplication.instance, R.anim.adapter_alpha2)
            ) //动画2，出现;

            //计算布局自适应时的高度
            var layoutHeight = 0
            for (i in 0 until linearLayout.childCount) {
                layoutHeight += linearLayout.getChildAt(i).layoutParams.height
            }
            getValueAnimator(linearLayout, 0, layoutHeight).start() //展开动画
        } else {
            linearLayout.startAnimation(
                    AnimationUtils.loadAnimation(BaseApplication.instance, R.anim.adapter_alpha1)
            ) //动画1，消失;
            val layoutHeight = linearLayout.height //获取布局的高度
            getValueAnimator(linearLayout, layoutHeight, 0).start() //收起动画
        }
    }

    /**
     * 获取改变视图尺寸动画
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
     * 伸缩布局，同时固定点击的item
     *
     * @param linearLayout 需要伸缩的linearLayout
     * @param flag         伸或缩
     * @param textView     子布局中的TextView
     * @param recyclerView 需要回滚的recyclerView
     * @param position     回滚的位置
     */
    fun expandLayout(
            linearLayout: LinearLayout, flag: Boolean, textView: TextView,
            recyclerView: RecyclerView, position: Int) {
        if (flag) {
            linearLayout.startAnimation(
                    AnimationUtils.loadAnimation(BaseApplication.instance, R.anim.adapter_alpha2)
            ) //动画2，出现;

            //计算布局自适应时的高度
            val layoutHeight = textView.lineHeight * (textView.lineCount + 1)
            getValueAnimator(linearLayout, 0, layoutHeight, recyclerView, position)
                    .start() //展开动画
        } else {
            linearLayout.startAnimation(
                    AnimationUtils.loadAnimation(BaseApplication.instance, R.anim.adapter_alpha1)
            ) //动画1，消失;
            val layoutHeight = linearLayout.height //获取布局的高度
            getValueAnimator(linearLayout, layoutHeight, 0, recyclerView, position)
                    .start() //收起动画
        }
    }

    /**
     * 获取改变视图尺寸，同时固定点击的item的动画
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

    /**
     * 旋转视图动画
     *
     * @param view 需要旋转的view
     * @param from 动画前的旋转角度
     * @param to   动画后的旋转角度
     */
    fun rotateExpandIcon(view: View, from: Float, to: Float) {
        val valueAnimator = ValueAnimator.ofFloat(from, to)
        valueAnimator.interpolator = DecelerateInterpolator() //先加速后减速的动画
        //valueAnimator.duration = 300;//动画时间（默认就是300）
        valueAnimator.addUpdateListener { //逐渐改变view的旋转角度
            view.rotation = valueAnimator.animatedValue as Float
        }
        valueAnimator.start()
    }
}