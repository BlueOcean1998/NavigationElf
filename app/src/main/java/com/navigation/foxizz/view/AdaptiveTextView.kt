package com.navigation.foxizz.view

import android.content.Context
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.navigation.foxizz.R

class AdaptiveTextView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {
    init {
        initialise(context, attrs)
    }

    private var mMaxTextSize = 0f //默认字体大小
    private var mMinTextSize = 0f //最小字体大小
    private lateinit var mTextPaint: Paint //文本描述对象
    private var adaptiveType = false //自适应类型

    //默认设置
    private fun initialise(context: Context, attrs: AttributeSet) {
        gravity = gravity or Gravity.CENTER_VERTICAL //水平居中
        setLines(1) //一行

        //最大字体大小为默认大小，最小为8
        mMaxTextSize = this.textSize
        mMinTextSize = 8f

        //获取文本描述对象
        mTextPaint = TextPaint()
        (mTextPaint as TextPaint).set(this.paint)

        //获取自定义属性，默认为宽度自适应
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AdaptiveTextView)
        adaptiveType = typedArray.getInt(
                R.styleable.AdaptiveTextView_adaptive_type, 0) == 0
        typedArray.recycle()
    }

    /**
     * 设置自适应类型
     *
     * @param adaptiveType 高度或宽度
     */
    fun setAdaptiveType(adaptiveType: Boolean) {
        this.adaptiveType = adaptiveType
    }

    //内容改变时
    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        if (adaptiveType) refitTextWidth(text.toString(), width) //textView视图的宽度
        else refitTextHeight(height) //textView视图的高度
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }

    //高度改变时
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (adaptiveType)
            if (w != oldw) refitTextWidth(this.text.toString(), w)
            else if (h != oldh) refitTextHeight(h)
    }

    //调整字体大小，使其适应文本框的宽度
    private fun refitTextWidth(text: String, textWidth: Int) {
        if (textWidth > 0) {
            //减去边距计算字体的实际宽度
            val availableWidth = textWidth - this.paddingLeft - this.paddingRight
            var trySize = mMaxTextSize
            mTextPaint.textSize = trySize
            //测量的字体宽度过大，不断地缩放
            while (mTextPaint.measureText(text) > availableWidth) {
                trySize-- //字体不断地减小来适应
                if (trySize <= mMinTextSize) {
                    trySize = mMinTextSize
                    break
                }
                mTextPaint.textSize = trySize
            }

            //setTextSize参数值为sp值
            textSize = px2sp(context, trySize)
        }
    }

    //调整字体大小，使其适应文本框的高度
    private fun refitTextHeight(height: Int) {
        if (height > 0) {
            //减去边距计算字体的实际高度
            val availableHeight = height - this.paddingTop - this.paddingBottom
            var trySize = mMaxTextSize
            mTextPaint.textSize = trySize
            //测量的字体高度过大，不断地缩放
            while (mTextPaint.descent() - mTextPaint.ascent() > availableHeight) {
                trySize-- //字体不断地减小来适应
                if (trySize <= mMinTextSize) {
                    trySize = mMinTextSize //最小为这个
                    break
                }
                mTextPaint.textSize = trySize
            }

            //setTextSize参数值为sp值
            textSize = px2sp(context, trySize)
        }
    }

    //将px值转换为sp值，保证文字大小不变
    private fun px2sp(context: Context, pxValue: Float): Float {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return pxValue / fontScale
    }
}