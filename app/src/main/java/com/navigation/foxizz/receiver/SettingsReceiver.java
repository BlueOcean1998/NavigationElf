package com.navigation.foxizz.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.navigation.foxizz.R;
import com.navigation.foxizz.activity.MainActivity;
import com.navigation.foxizz.activity.fragment.MainFragment;
import com.navigation.foxizz.data.Constants;
import com.navigation.foxizz.util.SettingUtil;

/**
 * 设置接收器
 * 用于地图页接收设置变化
 */
public class SettingsReceiver extends BroadcastReceiver {

    private final Context mContext;

    public SettingsReceiver(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //MainActivity的广播接收
        if (mContext instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) mContext;
            MainFragment mainFragment = (MainFragment) mainActivity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_layout);

            if (mainFragment != null) {
                switch (intent.getIntExtra("settings_type", 0)) {
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
                    default:
                        break;
                }
            }
        }
    }
}
