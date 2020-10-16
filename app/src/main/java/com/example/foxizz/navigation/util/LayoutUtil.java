package com.example.foxizz.navigation.util;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foxizz.navigation.R;

import static com.example.foxizz.navigation.mybaidumap.MyApplication.getContext;

/**
 * 布局工具类
 */
public class LayoutUtil {

    /**
     * 伸缩布局
     *
     * @param linearLayout 需要伸缩的linearLayout
     * @param flag         伸或缩
     */
    public static void expandLayout(LinearLayout linearLayout, boolean flag) {
        if (flag) {
            linearLayout.startAnimation(
                    AnimationUtils.loadAnimation(getContext(), R.anim.adapter_alpha2)
            );//动画2，出现;

            //计算布局自适应时的高度
            int layoutHeight = 0;
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                layoutHeight += linearLayout.getChildAt(i).getLayoutParams().height;
            }

            getValueAnimator(linearLayout, 0, layoutHeight).start();//展开动画
        } else {
            linearLayout.startAnimation(
                    AnimationUtils.loadAnimation(getContext(), R.anim.adapter_alpha1)
            );//动画1，消失;

            int layoutHeight = linearLayout.getHeight();//获取布局的高度

            getValueAnimator(linearLayout, layoutHeight, 0).start();//收起动画
        }
    }

    /**
     * 获取改变控件尺寸动画
     *
     * @param view        需要改变高度的view
     * @param startHeight 动画前的高度
     * @param endHeight   动画后的高度
     */
    public static ValueAnimator getValueAnimator(final View view, int startHeight, int endHeight) {
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(startHeight, endHeight);
        //valueAnimator.setDuration(300);//动画时间（默认就是300）
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //逐渐改变view的高度
                view.getLayoutParams().height = (int) animation.getAnimatedValue();
                view.requestLayout();
            }
        });
        return valueAnimator;
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
    public static void expandLayout(
            LinearLayout linearLayout, boolean flag, final TextView textView,
            final RecyclerView recyclerView, final int position) {
        if (flag) {
            linearLayout.startAnimation(
                    AnimationUtils.loadAnimation(getContext(), R.anim.adapter_alpha2)
            );//动画2，出现;

            //计算布局自适应时的高度
            int layoutHeight = textView.getLineHeight() * (textView.getLineCount() + 1);

            getValueAnimator(linearLayout, 0, layoutHeight, recyclerView, position)
                    .start();//展开动画
        } else {
            linearLayout.startAnimation(
                    AnimationUtils.loadAnimation(getContext(), R.anim.adapter_alpha1)
            );//动画1，消失;

            int layoutHeight = linearLayout.getHeight();//获取布局的高度

            getValueAnimator(linearLayout, layoutHeight, 0, recyclerView, position)
                    .start();//收起动画
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
    public static ValueAnimator getValueAnimator(
            final View view, int startHeight, int endHeight,
            final RecyclerView recyclerView, final int position) {
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(startHeight, endHeight);
        //valueAnimator.setDuration(300);//动画时间（默认就是300）
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //逐渐改变view的高度
                view.getLayoutParams().height = (int) animation.getAnimatedValue();
                view.requestLayout();

                //不断地移动回点击的item的位置
                recyclerView.scrollToPosition(position);
            }
        });
        return valueAnimator;
    }

    /**
     * 伸展按钮的旋转动画
     *
     * @param view 需要旋转的view
     * @param from 动画前的旋转角度
     * @param to   动画后的旋转角度
     */
    public static void rotateExpandIcon(final View view, float from, float to) {
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);
        valueAnimator.setInterpolator(new DecelerateInterpolator());//先加速后减速的动画
        //valueAnimator.setDuration(300);//动画时间（默认就是300）
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //逐渐改变view的旋转角度
                view.setRotation((float) valueAnimator.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

}
