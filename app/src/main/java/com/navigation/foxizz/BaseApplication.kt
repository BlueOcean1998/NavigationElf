package com.navigation.foxizz

import android.app.Application
import cn.zerokirby.api.ZerokirbyApi
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.navigation.foxizz.data.DatabaseHelper
import com.navigation.foxizz.data.SPHelper

/**
 * App name: NavigationElf
 * Author: Foxizz
 * Accomplish date: 2020-04-30
 * Last modify date: 2021-04-02
 */
class BaseApplication : Application() {
    companion object {
        /**
         * 获取Application
         *
         * @return application
         */
        lateinit var baseApplication: BaseApplication
    }

    override fun onCreate() {
        super.onCreate()
        baseApplication = this
        SPHelper.initSharedPreferences(this) //初始化SharedPreferences
        DatabaseHelper.initDatabaseHelper(this) //初始化搜索数据库

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(this)
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL)
        ZerokirbyApi.initialize(this) //初始化天天API
    }
}