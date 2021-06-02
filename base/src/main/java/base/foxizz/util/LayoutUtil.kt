package base.foxizz.util

import android.animation.ValueAnimator
import android.content.res.Resources
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import base.foxizz.BaseApplication.Companion.baseApplication
import base.foxizz.R

/**
 * 布局工具类
 */
object LayoutUtil {
    /**
     * 获取改变控件尺寸动画
     *
     * @param view        需要改变高度的view
     * @param startHeight 动画前的高度
     * @param endHeight   动画后的高度
     */
    fun getValueAnimator(view: View, startHeight: Int, endHeight: Int) =
        ValueAnimator.ofInt(startHeight, endHeight).apply {
            //duration = 300;//动画时间（默认就是300）
            addUpdateListener { animation -> //逐渐改变view的高度
                view.layoutParams.height = animation.animatedValue as Int
                view.requestLayout()
            }
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
        view: View, startHeight: Int, endHeight: Int, recyclerView: RecyclerView, position: Int
    ) = ValueAnimator.ofInt(startHeight, endHeight).apply {
        //duration = 300;//动画时间（默认就是300）
        addUpdateListener { animation -> //逐渐改变view的高度
            view.layoutParams.height = animation.animatedValue as Int
            view.requestLayout()
            //不断地移动回点击的item的位置
            recyclerView.scrollToPosition(position)
        }
    }
}

//将dp转换为px
val Int.dpToPx get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Float.dpToPx get() = this * Resources.getSystem().displayMetrics.density

//将px转换为dp
val Int.pxToDp get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Float.pxToDp get() = this / Resources.getSystem().displayMetrics.density

//将sp转换为px
val Int.spToPx get() = (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()
val Float.spToPx get() = this * Resources.getSystem().displayMetrics.scaledDensity

//将px转换为sp
val Int.pxToSp get() = (this / Resources.getSystem().displayMetrics.scaledDensity).toInt()
val Float.pxToSp get() = this / Resources.getSystem().displayMetrics.scaledDensity

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
fun LinearLayout.expandLayout(flag: Boolean) = if (flag) {
    //动画2，出现
    startAnimation(AnimationUtils.loadAnimation(baseApplication, R.anim.adapter_alpha2))
    //计算布局自适应时的高度
    var layoutHeight = 0
    forEach { layoutHeight += it.layoutParams.height }
    LayoutUtil.getValueAnimator(this, 0, layoutHeight).start() //展开动画
} else {
    //动画1，消失;
    startAnimation(AnimationUtils.loadAnimation(baseApplication, R.anim.adapter_alpha1))
    val layoutHeight = height //获取布局的高度
    LayoutUtil.getValueAnimator(this, layoutHeight, 0).start() //收起动画
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
    flag: Boolean, textView: TextView, recyclerView: RecyclerView, position: Int
) = if (flag) {
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

/**
 * 旋转控件动画
 *
 * @param from 动画前的旋转角度
 * @param to   动画后的旋转角度
 */
fun View.rotateExpandIcon(from: Float, to: Float) = ValueAnimator.ofFloat(from, to).run {
    interpolator = DecelerateInterpolator() //先加速后减速的动画
    //duration = 300;//动画时间（默认就是300）
    addUpdateListener { //逐渐改变view的旋转角度
        rotation = animatedValue as Float
    }
    start()
}
