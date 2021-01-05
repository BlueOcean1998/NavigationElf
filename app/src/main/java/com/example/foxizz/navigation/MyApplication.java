package com.example.foxizz.navigation;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.example.foxizz.navigation.data.SearchDataHelper;

/**
 * app name: NavigationElf
 * author: Foxizz
 * accomplish date: 2020-04-30
 * last modify date: 2021-01-05
 */
public class MyApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    /**
     * 获取全局context
     *
     * @return context
     */
    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(context);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);

        SearchDataHelper.initSearchDataHelper();//初始化搜索数据库
    }

}