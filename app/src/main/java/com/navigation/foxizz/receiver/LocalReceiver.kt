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
 *
 * @param mContext 上下文
 */
class LocalReceiver(private val mContext: Context) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (mContext is MainActivity) {
            Constants.run {
                when (intent.action) {
                    //地图页接收设置变化
                    SETTINGS_BROADCAST -> {
                        mContext.mainFragment.run {
                            when (intent.getIntExtra(SETTINGS_TYPE, 0)) {
                                SET_MAP_TYPE -> setMapType()
                                SET_LANDSCAPE -> SettingUtil.initSettings(mContext)
                                SET_ANGLE_3D -> setAngle3D()
                                SET_MAP_ROTATION -> setMapRotation()
                                SET_SCALE_CONTROL -> setScaleControl()
                                SET_ZOOM_CONTROLS -> setZoomControls()
                                SET_COMPASS -> setCompass()
                                CLEAN_RECORD -> {
                                    mBaiduSearch.mSearchList.clear() //清空搜索列表
                                    mSearchAdapter.updateList() //通知adapter更新
                                }
                                else -> {
                                }
                            }
                        }
                    }
                    //用户页接收登录变化
                    LOGIN_BROADCAST -> {
                        mContext.userFragment.run {
                            when (intent.getIntExtra(LOGIN_TYPE, 0)) {
                                SET_USERNAME -> tv_user_name.text =
                                    UserDataHelper.getUser(UserDataHelper.loginUserId).username
                                SET_AVATAR -> iv_avatar_image.setImageBitmap(
                                    AvatarDataHelper.getBitmapAvatar(UserDataHelper.loginUserId)
                                )
                            }
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }
}