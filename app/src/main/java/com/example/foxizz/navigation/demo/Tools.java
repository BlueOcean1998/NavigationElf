package com.example.foxizz.navigation.demo;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foxizz.navigation.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * 这是一个工具类，专门存放各种方法
 */
public class Tools {

    //伸缩布局
    //参数：上下文，需要伸缩的linearLayout，伸或缩
    public static void expandLayout(Context context, LinearLayout linearLayout, boolean flag) {
        if(flag) {
            linearLayout.startAnimation(
                    AnimationUtils.loadAnimation(context, R.anim.adapter_alpha2)
            );//动画2，出现;

            //计算布局自适应时的高度
            int layoutHeight = 0;

            for(int i = 0; i < linearLayout.getChildCount(); i++) {
                layoutHeight += linearLayout.getChildAt(i).getLayoutParams().height;
            }

            getValueAnimator(linearLayout, 0, layoutHeight).start();//展开动画
        } else {
            linearLayout.startAnimation(
                    AnimationUtils.loadAnimation(context, R.anim.adapter_alpha1)
            );//动画1，消失;

            int layoutHeight = linearLayout.getHeight();//获取布局的高度

            getValueAnimator(linearLayout, layoutHeight, 0).start();//收起动画
        }
    }

    //获取改变控件尺寸动画
    //参数：需要改变高度的view，动画前的高度，动画后的高度
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

    //伸缩布局，同时固定点击的item
    //参数：上下文，需要伸缩的linearLayout，布局中的textView，伸或缩，需要回滚的recyclerView，回滚的位置
    public static void expandLayout(
            Context context, LinearLayout linearLayout, TextView textView, boolean flag,
            final RecyclerView recyclerView, final int position) {
        if(flag) {
            linearLayout.startAnimation(
                    AnimationUtils.loadAnimation(context, R.anim.adapter_alpha2)
            );//动画2，出现;

            //计算布局自适应时的高度
            int layoutHeight = textView.getLineHeight() * (textView.getLineCount() + 1);

            getValueAnimator(linearLayout, 0, layoutHeight, recyclerView, position)
                    .start();//展开动画
        } else {
            linearLayout.startAnimation(
                    AnimationUtils.loadAnimation(context, R.anim.adapter_alpha1)
            );//动画1，消失;

            int layoutHeight = linearLayout.getHeight();//获取布局的高度

            getValueAnimator(linearLayout, layoutHeight, 0, recyclerView, position)
                    .start();//收起动画
        }
    }

    //获取改变控件尺寸，同时固定点击的item的动画
    //参数：需要改变高度的view，动画前的高度，动画后的高度，需要回滚的recyclerView，回滚的位置
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

    //伸展按钮的旋转动画
    //参数：需要旋转的spreadButton（当然也可以是其它view），动画前的旋转角度，动画后的旋转角度
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

    //判断是否已经获取了读取存储和定位权限
    public static boolean haveReadWriteAndLocationPermissions(Context context) {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };

        for(String permission: permissions) {
            if(ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;
    }

    //获取SD卡路径
    public static String getSDCardDir() {
        return Environment.getExternalStorageDirectory().toString();
    }

    //获取app文件夹名
    public static String getAppFolderName(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.requireNonNull(context.getExternalCacheDir()).getPath();
        }
        return null;
    }

    //获取网络连接状态，有则返回true，没有则返回false
    public static boolean isNetworkConnected(Context context) {
        if(context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = null;
            if(mConnectivityManager != null) {
                mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            }
            if(mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //获取飞行模式状态，有开启则返回true，没有则返回false
    public static boolean isAirplaneModeOn(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
        return true;
    }

    //判断是否在时间内
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if(nowTime != null && startTime != null && endTime != null) {
            if (nowTime.getTime() == startTime.getTime()
                    || nowTime.getTime() == endTime.getTime()) {
                return true;
            }

            Calendar date = Calendar.getInstance();
            date.setTime(nowTime);

            Calendar begin = Calendar.getInstance();
            begin.setTime(startTime);

            Calendar end = Calendar.getInstance();
            end.setTime(endTime);

            return date.after(begin) && date.before(end);
        }

        return false;
    }

    //初始化设置（目前只有一个设置）
    public static void initSettings(Context context) {
        if(context instanceof Activity) {
            Activity activity = (Activity) context;

            //设置是否允许横屏
            if(PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("landscape", false))
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//自动旋转
            else activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//只允许竖屏
        }
    }

}
