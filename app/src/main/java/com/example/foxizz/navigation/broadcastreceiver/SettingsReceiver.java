package com.example.foxizz.navigation.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.demo.SettingsConstants;
import com.example.foxizz.navigation.demo.Tools;

//设置接收器
public class SettingsReceiver extends BroadcastReceiver {

    private Context mContext;

    public SettingsReceiver(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //MainActivity的广播接收
        if(mContext instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) mContext;

            switch(intent.getIntExtra("settings_type", 0)) {
                case SettingsConstants.SET_MAP_TYPE: mainActivity.setMapType();
                    break;
                case SettingsConstants.SET_LANDSCAPE: Tools.initSettings(mainActivity);
                    break;
                case SettingsConstants.SET_ANGLE_3D: mainActivity.setAngle3D();
                    break;
                case SettingsConstants.SET_MAP_ROTATION: mainActivity.setMapRotation();
                    break;
                case SettingsConstants.SET_SCALE_CONTROL: mainActivity.setScaleControl();
                    break;
                case SettingsConstants.SET_ZOOM_CONTROLS: mainActivity.setZoomControls();
                    break;
                case SettingsConstants.SET_COMPASS: mainActivity.setCompass();
                    break;
                default:
                    break;
            }
        }
    }
}
