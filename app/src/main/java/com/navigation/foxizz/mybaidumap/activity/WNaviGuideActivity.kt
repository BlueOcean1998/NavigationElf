/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.navigation.foxizz.mybaidumap.activity

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.*
import android.util.Log
import base.foxizz.util.SettingUtil
import base.foxizz.util.showToast
import com.baidu.mapapi.walknavi.WalkNavigateHelper
import com.baidu.mapapi.walknavi.adapter.IWNaviStatusListener
import com.baidu.mapapi.walknavi.adapter.IWRouteGuidanceListener
import com.baidu.mapapi.walknavi.model.RouteGuideKind
import com.baidu.platform.comapi.walknavi.WalkNaviModeSwitchListener
import com.baidu.platform.comapi.walknavi.widget.ArCameraView
import com.baidu.tts.client.SpeechSynthesizer
import com.navigation.foxizz.R

/**
 * 步行导航诱导活动
 * 不能继承AppCompatActivity
 */
class WNaviGuideActivity : Activity() {
    companion object {
        private val TAG = WNaviGuideActivity::class.java.simpleName

        /**
         * 启动步行导航诱导活动
         *
         * @param context 上下文
         */
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, WNaviGuideActivity::class.java))
        }
    }

    private lateinit var mNaviHelper: WalkNavigateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNaviHelper = WalkNavigateHelper.getInstance()
        try {
            val view = mNaviHelper.onCreate(this)
            view?.let { setContentView(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mNaviHelper.setWalkNaviStatusListener(object : IWNaviStatusListener {
            override fun onWalkNaviModeChange(mode: Int, listener: WalkNaviModeSwitchListener) {
                Log.d(TAG, "onWalkNaviModeChange : $mode")
                mNaviHelper.switchWalkNaviMode(this@WNaviGuideActivity, mode, listener)
            }

            override fun onNaviExit() {
                Log.d(TAG, "onNaviExit")
            }
        })
        mNaviHelper.setTTsPlayer { s, _ ->
            Log.d(TAG, "tts: $s")
            SpeechSynthesizer.getInstance().speak(s) //语音播报
        }
        val startResult = mNaviHelper.startWalkNavi(this)
        Log.e(TAG, "startWalkNavi result : $startResult")
        mNaviHelper.setRouteGuidanceListener(this, object : IWRouteGuidanceListener {
            override fun onRouteGuideIconUpdate(icon: Drawable) {}
            override fun onRouteGuideKind(routeGuideKind: RouteGuideKind) {
                Log.d(TAG, "onRouteGuideKind: $routeGuideKind")
            }

            override fun onRoadGuideTextUpdate(charSequence: CharSequence, charSequence1: CharSequence) {
                Log.d(TAG, "onRoadGuideTextUpdate   charSequence=: " + charSequence + "   charSequence1 = : " +
                        charSequence1)
            }

            override fun onRemainDistanceUpdate(charSequence: CharSequence) {
                Log.d(TAG, "onRemainDistanceUpdate: charSequence = :$charSequence")
            }

            override fun onRemainTimeUpdate(charSequence: CharSequence) {
                Log.d(TAG, "onRemainTimeUpdate: charSequence = :$charSequence")
            }

            override fun onGpsStatusChange(charSequence: CharSequence, drawable: Drawable) {
                Log.d(TAG, "onGpsStatusChange: charSequence = :$charSequence")
            }

            override fun onRouteFarAway(charSequence: CharSequence, drawable: Drawable) {
                Log.d(TAG, "onRouteFarAway: charSequence = :$charSequence")
            }

            override fun onRoutePlanYawing(charSequence: CharSequence, drawable: Drawable) {
                Log.d(TAG, "onRoutePlanYawing: charSequence = :$charSequence")
            }

            override fun onReRouteComplete() {}
            override fun onArriveDest() {}
            override fun onIndoorEnd(msg: Message) {}
            override fun onFinalEnd(msg: Message) {}
            override fun onVibrate() {}
        })

        SettingUtil.initSettings(this) //初始化设置
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ArCameraView.WALK_AR_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
                showToast(R.string.no_camera_permissions)
            else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                mNaviHelper.startCameraAndSetMapView(this)
        }
    }
}