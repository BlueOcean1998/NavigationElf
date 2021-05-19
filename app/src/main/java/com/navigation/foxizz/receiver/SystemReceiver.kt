package com.navigation.foxizz.receiver

import Constants
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import base.foxizz.util.NetworkUtil
import com.navigation.foxizz.activity.MainActivity
import com.navigation.foxizz.data.SearchDataHelper

/**
 * 系统接收器
 *
 * @param mContext 上下文
 */
class SystemReceiver(private val mContext: Context) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (mContext is MainActivity) {
            mContext.mainFragment.run {
                if (Constants.CONNECTIVITY_CHANGE == intent.action) {
                    if (NetworkUtil.isNetworkConnected) { //有网络连接
                        if (isHistorySearchResult) {
                            //初始化搜索记录
                            SearchDataHelper.initSearchData(this)
                        }
                        //初始化驾车导航引擎
                        mBaiduNavigation.initDriveNavigateHelper()
                    }
                }
            }
        }
    }
}