/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.navigation.foxizz.mybaidumap.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import base.foxizz.util.SettingUtil
import com.baidu.navisdk.adapter.BNaviCommonParams
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory
import com.baidu.navisdk.adapter.IBNRouteGuideManager
import com.baidu.navisdk.adapter.IBNTTSManager.IOnTTSPlayStateChangedListener
import com.baidu.navisdk.adapter.IBNaviListener

/**
 * 驾车导航诱导活动
 * 可以继承AppCompatActivity
 */
class DNaviGuideActivity : Activity() {
    companion object {
        private val TAG = DNaviGuideActivity::class.java.name

        /**
         * 启动驾车导航诱导活动
         *
         * @param context 上下文
         */
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, DNaviGuideActivity::class.java))
        }
    }

    private lateinit var mRouteGuideManager: IBNRouteGuideManager
    private val mMode = IBNaviListener.DayNightMode.DAY

    private val mOnNavigationListener: IBNRouteGuideManager.OnNavigationListener =
            object : IBNRouteGuideManager.OnNavigationListener {
                override fun onNaviGuideEnd() {
                    // 退出导航
                    finish()
                }

                override fun notifyOtherAction(actionType: Int, arg2: Int, p2: Int, obj: Any?) {
                    if (actionType == 0) {
                        // 导航到达目的地 自动退出
                        Log.i(TAG, "notifyOtherAction actionType = $actionType,导航到达目的地！")
                        mRouteGuideManager.forceQuitNaviWithoutDialog()
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fullScreen = supportFullScreen()
        val params = Bundle()
        params.putBoolean(BNaviCommonParams.ProGuideKey.IS_SUPPORT_FULL_SCREEN, fullScreen)
        mRouteGuideManager = BaiduNaviManagerFactory.getRouteGuideManager()
        val view = mRouteGuideManager.onCreate(this, mOnNavigationListener, null, params)
        view?.let { setContentView(it) }
        BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .setShowMainAuxiliaryOrBridge(true)
        initTTSListener()
    }

    private fun initTTSListener() {
        // 注册同步内置tts状态回调
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedListener(
                object : IOnTTSPlayStateChangedListener {
                    override fun onPlayStart() {
                        Log.e(TAG, "ttsCallback.onPlayStart")
                    }

                    override fun onPlayEnd(speechId: String) {
                        Log.e(TAG, "ttsCallback.onPlayEnd")
                    }

                    override fun onPlayError(code: Int, message: String) {
                        Log.e(TAG, "ttsCallback.onPlayError")
                    }
                }
        )

        // 注册内置tts 异步状态消息
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedHandler(
                object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        Log.e(TAG, "ttsHandler.msg.what=" + msg.what)
                    }
                }
        )
    }

    override fun onStart() {
        super.onStart()
        mRouteGuideManager.onStart()
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mRouteGuideManager.onResume()

        SettingUtil.initSettings(this) //初始化设置
    }

    override fun onPause() {
        super.onPause()
        mRouteGuideManager.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()
        mRouteGuideManager.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRouteGuideManager.onDestroy(false)
        unInitTTSListener()
    }

    private fun unInitTTSListener() {
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedListener(null)
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedHandler(null)
    }

    override fun onBackPressed() {
        mRouteGuideManager.onBackPressed(false, true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mRouteGuideManager.onConfigurationChanged(newConfig)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (!mRouteGuideManager.onKeyDown(keyCode, event)) {
            super.onKeyDown(keyCode, event)
        } else true
    }

    private fun supportFullScreen(): Boolean {
        val window = window
        val color: Int = if (Build.VERSION.SDK_INT >= 23)
            Color.TRANSPARENT
        else 0x2d000000
        window.statusBarColor = color
        if (Build.VERSION.SDK_INT >= 23) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            var uiVisibility = window.decorView.systemUiVisibility
            if (mMode == IBNaviListener.DayNightMode.DAY) {
                uiVisibility = uiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            window.decorView.systemUiVisibility = uiVisibility
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        return true
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        mRouteGuideManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        mRouteGuideManager.onActivityResult(requestCode, resultCode, data)
    }
}