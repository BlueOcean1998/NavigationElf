package com.navigation.foxizz.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.navigation.foxizz.activity.MainActivity
import com.navigation.foxizz.data.Constants
import com.navigation.foxizz.util.NetworkUtil

class SystemReceiver(private val mContext: Context) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (mContext is MainActivity) {
            val mainFragment = mContext.mainFragment
            if (Constants.CONNECTIVITY_CHANGE == intent.action) {
                if (NetworkUtil.isNetworkConnected) { //有网络连接
                    //初始化驾车导航引擎
                    mainFragment.mBaiduNavigation.initDriveNavigateHelper()
                }
            }
        }
    }
}