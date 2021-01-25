package com.navigation.foxizz.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.navigation.foxizz.activity.MainActivity;
import com.navigation.foxizz.activity.fragment.MainFragment;
import com.navigation.foxizz.activity.fragment.UserFragment;
import com.navigation.foxizz.data.Constants;
import com.navigation.foxizz.util.SettingUtil;

import cn.zerokirby.api.data.AvatarDataHelper;

/**
 * 本地接收器
 */
public class LocalReceiver extends BroadcastReceiver {

    private final Context mContext;
    public LocalReceiver(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mContext instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) mContext;

            //地图页接收设置变化
            if (TextUtils.equals(intent.getAction(), Constants.SETTINGS_BROADCAST)) {
                MainFragment mainFragment = mainActivity.getMainFragment();
                switch (intent.getIntExtra(Constants.SETTINGS_TYPE, 0)) {
                    case Constants.SET_MAP_TYPE:
                        mainFragment.setMapType();
                        break;
                    case Constants.SET_LANDSCAPE:
                        SettingUtil.initSettings(mainActivity);
                        break;
                    case Constants.SET_ANGLE_3D:
                        mainFragment.setAngle3D();
                        break;
                    case Constants.SET_MAP_ROTATION:
                        mainFragment.setMapRotation();
                        break;
                    case Constants.SET_SCALE_CONTROL:
                        mainFragment.setScaleControl();
                        break;
                    case Constants.SET_ZOOM_CONTROLS:
                        mainFragment.setZoomControls();
                        break;
                    case Constants.SET_COMPASS:
                        mainFragment.setCompass();
                        break;
                    case Constants.CLEAN_RECORD:
                        mainFragment.searchList.clear();//清空搜索列表
                        mainFragment.searchAdapter.updateList();//通知adapter更新
                        break;
                    default:
                        break;
                }
                //用户页接收登录变化
            } else if (TextUtils.equals(intent.getAction(), Constants.LOGIN_BROADCAST)) {
                UserFragment userFragment = mainActivity.getUserFragment();
                switch (intent.getIntExtra(Constants.LOGIN_TYPE, 0)) {
                    case Constants.SET_AVATAR:
                        userFragment.avatarImage.setImageBitmap(AvatarDataHelper.getBitmapAvatar());
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
