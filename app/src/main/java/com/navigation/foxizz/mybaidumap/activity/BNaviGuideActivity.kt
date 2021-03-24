/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.navigation.foxizz.mybaidumap.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import com.baidu.mapapi.bikenavi.BikeNavigateHelper
import com.baidu.mapapi.bikenavi.adapter.IBRouteGuidanceListener
import com.baidu.mapapi.bikenavi.model.BikeRouteDetailInfo
import com.baidu.mapapi.walknavi.model.RouteGuideKind
import com.baidu.tts.client.SpeechSynthesizer
import com.navigation.foxizz.util.SettingUtil

/**
 * 骑行导航诱导活动
 * 不能继承AppCompatActivity
 */
class BNaviGuideActivity : Activity() {
    companion object {
        private val TAG = BNaviGuideActivity::class.java.simpleName

        /**
         * 启动骑行导航诱导活动
         *
         * @param context 上下文
         */
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, BNaviGuideActivity::class.java))
        }
    }

    private lateinit var mNaviHelper: BikeNavigateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNaviHelper = BikeNavigateHelper.getInstance()
        val view = mNaviHelper.onCreate(this)
        view?.let { setContentView(it) }
        mNaviHelper.setBikeNaviStatusListener {
            Log.d(TAG, "onNaviExit")
        }
        mNaviHelper.setTTsPlayer { s, _ ->
            Log.d("tts", s)
            SpeechSynthesizer.getInstance().speak(s) //语音播报
            0
        }
        mNaviHelper.startBikeNavi(this)
        mNaviHelper.setRouteGuidanceListener(this, object : IBRouteGuidanceListener {
            override fun onRouteGuideIconUpdate(icon: Drawable) {}
            override fun onRouteGuideKind(routeGuideKind: RouteGuideKind) {}
            override fun onRoadGuideTextUpdate(charSequence: CharSequence, charSequence1: CharSequence) {}
            override fun onRemainDistanceUpdate(charSequence: CharSequence) {}
            override fun onRemainTimeUpdate(charSequence: CharSequence) {}
            override fun onGpsStatusChange(charSequence: CharSequence, drawable: Drawable) {}
            override fun onRouteFarAway(charSequence: CharSequence, drawable: Drawable) {}
            override fun onRoutePlanYawing(charSequence: CharSequence, drawable: Drawable) {}
            override fun onReRouteComplete() {}
            override fun onArriveDest() {}
            override fun onVibrate() {}
            override fun onGetRouteDetailInfo(bikeRouteDetailInfo: BikeRouteDetailInfo) {}
        })

        //初始化设置
        SettingUtil.initSettings(this)
    }

    override fun onResume() {
        super.onResume()
        mNaviHelper.resume()
    }

    override fun onPause() {
        super.onPause()
        mNaviHelper.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mNaviHelper.quit()
    }
}