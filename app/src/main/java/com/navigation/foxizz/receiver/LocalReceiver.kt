package com.navigation.foxizz.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.zerokirby.api.data.AvatarDataHelper
import cn.zerokirby.api.data.UserDataHelper
import com.navigation.foxizz.activity.MainActivity
import com.navigation.foxizz.data.Constants
import com.navigation.foxizz.util.SettingUtil
import kotlinx.android.synthetic.main.fragment_user.*

/**
 * 本地接收器
 */
class LocalReceiver(private val mContext: Context) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (mContext is MainActivity) {
            val mainActivity = mContext

            //地图页接收设置变化
            if (intent.action == Constants.SETTINGS_BROADCAST) {
                val mainFragment = mainActivity.mainFragment
                when (intent.getIntExtra(Constants.SETTINGS_TYPE, 0)) {
                    Constants.SET_MAP_TYPE -> mainFragment.setMapType()
                    Constants.SET_LANDSCAPE -> SettingUtil.initSettings(mainActivity)
                    Constants.SET_ANGLE_3D -> mainFragment.setAngle3D()
                    Constants.SET_MAP_ROTATION -> mainFragment.setMapRotation()
                    Constants.SET_SCALE_CONTROL -> mainFragment.setScaleControl()
                    Constants.SET_ZOOM_CONTROLS -> mainFragment.setZoomControls()
                    Constants.SET_COMPASS -> mainFragment.setCompass()
                    Constants.CLEAN_RECORD -> {
                        mainFragment.mBaiduSearch.mSearchList.clear() //清空搜索列表
                        mainFragment.mSearchAdapter.updateList() //通知adapter更新
                    }
                }
                //用户页接收登录变化
            } else if (intent.action == Constants.LOGIN_BROADCAST) {
                val userFragment = mainActivity.userFragment
                when (intent.getIntExtra(Constants.LOGIN_TYPE, 0)) {
                    Constants.SET_USERNAME ->
                        userFragment.tv_user_name.text =
                                UserDataHelper.getUser(UserDataHelper.loginUserId).username
                    Constants.SET_AVATAR ->
                        userFragment.iv_avatar_image.setImageBitmap(
                                AvatarDataHelper.getBitmapAvatar(UserDataHelper.loginUserId))
                }
            }
        }
    }
}