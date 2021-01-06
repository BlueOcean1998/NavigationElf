package com.example.foxizz.navigation.util;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foxizz.navigation.R;

import static com.example.foxizz.navigation.BaseApplication.getContext;

/**
 * 布局工具类
 */
public class LayoutUtil {

    /**
     * 获取视图尺寸
     *
     * @param view 视图
     * @param type true:宽 false:高
     * @return 尺寸
     */
    public static int getViewSize(final View view, final boolean type) {
        final int[] heightOrWidth = new int[1];
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (type) heightOrWidth[0] = view.getWidth();
                else heightOrWidth[0] = view.getHeight();
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        return heightOrWidth[0];
    }

    /**
     * 获取视图宽度
     *
     * @param view 视图
     * @return 宽度
     */
    public static int getViewWidth(View view) {
        return getViewSize(view, true);
    }

    /**
     * 获取视图高度
     *
     * @param view 视图
     * @return 高度
     */
    public static int getViewHeight(View view) {
        return getViewSize(view, false);
    }

    /**
     * 设置视图宽度
     *
     * @param view  视图
     * @param width 宽度
     */
    public static void setViewWidth(View view, int width) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        view.setLayoutParams(params);
    }

    /**
     * 设置视图高度
     *
     * @param view   视图
     * @param height 高度
     */
    public static void setViewHeight(View view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }

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
     * 获取改变视图尺寸动画
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
     * 获取改变视图尺寸，同时固定点击的item的动画
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
     * 旋转视图动画
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
