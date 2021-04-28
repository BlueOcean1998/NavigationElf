package com.navigation.foxizz.receiver

import Constants
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import base.foxizz.util.SettingUtil
import cn.zerokirby.api.data.AvatarDataHelper
import cn.zerokirby.api.data.UserDataHelper
import com.navigation.foxizz.activity.MainActivity
import kotlinx.android.synthetic.main.fragment_user.*

/**
 * 本地接收器
 */
class LocalReceiver(private val mContext: Context) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (mContext is MainActivity) {
            //地图页接收设置变化
            if (intent.action == Constants.SETTINGS_BROADCAST) {
                mContext.mainFragment.run {
                    when (intent.getIntExtra(Constants.SETTINGS_TYPE, 0)) {
                        Constants.SET_MAP_TYPE -> setMapType()
                        Constants.SET_LANDSCAPE -> SettingUtil.initSettings(mContext)
                        Constants.SET_ANGLE_3D -> setAngle3D()
                        Constants.SET_MAP_ROTATION -> setMapRotation()
                        Constants.SET_SCALE_CONTROL -> setScaleControl()
                        Constants.SET_ZOOM_CONTROLS -> setZoomControls()
                        Constants.SET_COMPASS -> setCompass()
                        Constants.CLEAN_RECORD -> {
                            mBaiduSearch.mSearchList.clear() //清空搜索列表
                            mSearchAdapter.updateList() //通知adapter更新
                        }
                        else -> null
                    }
                }
                //用户页接收登录变化
            } else if (intent.action == Constants.LOGIN_BROADCAST) {
                mContext.userFragment.run {
                    when (intent.getIntExtra(Constants.LOGIN_TYPE, 0)) {
                        Constants.SET_USERNAME ->
                            tv_user_name.text =
                                    UserDataHelper.getUser(UserDataHelper.loginUserId).username
                        Constants.SET_AVATAR ->
                            iv_avatar_image.setImageBitmap(
                                    AvatarDataHelper.getBitmapAvatar(UserDataHelper.loginUserId))
                    }
                }
            }
        }
    }
}