package com.example.foxizz.navigation.activity;

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

    private static List<Activity> activities = new ArrayList<>();

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

    //退出程序
    public static void finishAll() {
        for(Activity activity: activities) {
            if(!activity.isFinishing()) {
                activity.finish();
            }
        }
        activities.clear();
    }

}
