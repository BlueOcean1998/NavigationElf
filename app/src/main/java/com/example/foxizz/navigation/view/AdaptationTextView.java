package com.example.foxizz.navigation.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;

import com.example.foxizz.navigation.R;

public class AdaptationTextView extends androidx.appcompat.widget.AppCompatTextView {

    private float mMaxTextSize;//默认字体大小
    private float mMinTextSize;//最小字体大小

    private Paint mTextPaint;//文本描述对象

    private boolean adaptiveType;//自适应类型
    public final static boolean widthAdaptive = true;//宽度自适应
    public final static boolean heightAdaptive = false;//高度自适应

    public AdaptationTextView(Context context) {
        super(context);
    }

    public AdaptationTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context, attrs);
    }

    //默认设置
    private void initialise(Context context, AttributeSet attrs) {
        setGravity(getGravity() | Gravity.CENTER_VERTICAL);//水平居中
        setLines(1);//一行

        //最大字体大小为默认大小，最小为8
        mMaxTextSize = this.getTextSize();
        mMinTextSize = 8;

        //获取文本描述对象
        mTextPaint = new TextPaint();
        mTextPaint.set(this.getPaint());

        //获取自定义属性，默认为宽度自适应
        @SuppressLint("Recycle") TypedArray typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.AdaptationTextView);
        if(typedArray != null) {
            adaptiveType = typedArray.getInt(
                    R.styleable.AdaptationTextView_adaptive_type, 0) == 0;
        }
    }

    /**
     * 设置自适应类型
     * @param adaptiveType 高度或宽度
     */
    public void setAdaptiveType(boolean adaptiveType) {
        this.adaptiveType = adaptiveType;
    }

    //内容改变时
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if(adaptiveType) refitTextWidth(text.toString(), this.getWidth());//textView视图的宽度
        else refitTextHeight(this.getHeight());//textView视图的高度

        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    //高度改变时
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if(adaptiveType) {
            if(w != oldw) refitTextWidth(this.getText().toString(), w);
        } else {
            if(h != oldh) refitTextHeight(h);
        }
    }

    //调整字体大小，使其适应文本框的宽度
    private void refitTextWidth(String text, int textWidth) {
        if(textWidth > 0) {
            //减去边距计算字体的实际宽度
            int availableWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
            float trySize = mMaxTextSize;
            mTextPaint.setTextSize(trySize);
            //测量的字体宽度过大，不断地缩放
            while(mTextPaint.measureText(text) > availableWidth) {
                trySize--;//字体不断地减小来适应
                if (trySize <= mMinTextSize) {
                    trySize = mMinTextSize;
                    break;
                }
                mTextPaint.setTextSize(trySize);
            }

            //setTextSize参数值为sp值
            setTextSize(px2sp(getContext(), trySize));
        }
    }

    //调整字体大小，使其适应文本框的高度
    private void refitTextHeight(int height) {
        if(height > 0) {
            //减去边距计算字体的实际高度
            int availableHeight = height - this.getPaddingTop() - this.getPaddingBottom();
            float trySize = mMaxTextSize;
            mTextPaint.setTextSize(trySize);
            //测量的字体高度过大，不断地缩放
            while(mTextPaint.descent() - mTextPaint.ascent() > availableHeight) {
                trySize--;//字体不断地减小来适应
                if (trySize <= mMinTextSize) {
                    trySize = mMinTextSize;//最小为这个
                    break;
                }
                mTextPaint.setTextSize(trySize);
            }

            //setTextSize参数值为sp值
            setTextSize(px2sp(getContext(), trySize));
        }
    }

    //将px值转换为sp值，保证文字大小不变
    private float px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (pxValue / fontScale);
    }

}