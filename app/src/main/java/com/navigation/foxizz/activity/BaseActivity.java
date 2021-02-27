package com.navigation.foxizz.activity;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础活动
 * 管理除3个百度导航诱导活动外的所有活动
 */
public class BaseActivity extends AppCompatActivity {

    private static final List<Activity> activities = new ArrayList<>();

    /**
     * 获取指定活动
     * 若该活动不在返回栈中，则返回MainActivity
     *
     * @param cls 活动类
     * @return 活动
     */
    public static Activity findActivity(Class<?> cls) {
        for (Activity activity : activities) {
            if (activity.getClass() == cls) {
                return activity;
            }
        }
        return activities.get(0);
    }

    /**
     * 退出程序
     */
    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activities.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activities.add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activities.remove(this);
    }

}
