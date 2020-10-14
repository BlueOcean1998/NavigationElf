package com.example.foxizz.navigation.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.activity.fragment.MainFragment;
import com.example.foxizz.navigation.util.Tools;

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
                    case SettingsConstants.SET_MAP_TYPE:
                        mainFragment.setMapType();
                        break;
                    case SettingsConstants.SET_LANDSCAPE:
                        Tools.initSettings(mainActivity);
                        break;
                    case SettingsConstants.SET_ANGLE_3D:
                        mainFragment.setAngle3D();
                        break;
                    case SettingsConstants.SET_MAP_ROTATION:
                        mainFragment.setMapRotation();
                        break;
                    case SettingsConstants.SET_SCALE_CONTROL:
                        mainFragment.setScaleControl();
                        break;
                    case SettingsConstants.SET_ZOOM_CONTROLS:
                        mainFragment.setZoomControls();
                        break;
                    case SettingsConstants.SET_COMPASS:
                        mainFragment.setCompass();
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
