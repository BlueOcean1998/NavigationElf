package com.navigation.foxizz.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.navigation.foxizz.activity.MainActivity;
import com.navigation.foxizz.activity.fragment.MainFragment;
import com.navigation.foxizz.data.Constants;
import com.navigation.foxizz.util.NetworkUtil;

public class SystemReceiver extends BroadcastReceiver {

    private final Context mContext;
    public SystemReceiver(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mContext instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) mContext;
            MainFragment mainFragment = mainActivity.getMainFragment();

            if (Constants.CONNECTIVITY_CHANGE.equals(intent.getAction())) {
                if (NetworkUtil.isNetworkConnected()) {//有网络连接
                    //初始化驾车导航引擎
                    mainFragment.myNavigation.initDriveNavigateHelper();
                }
            }
        }
    }

}
