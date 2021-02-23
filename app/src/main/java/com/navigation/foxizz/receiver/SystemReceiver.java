package com.navigation.foxizz.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

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

            if (TextUtils.equals(intent.getAction(), Constants.CONNECTIVITY_CHANGE)) {
                if (NetworkUtil.isNetworkConnected()) {
                    //初始化驾车导航引擎
                    mainFragment.myNavigateHelper.initDriveNavigateHelper();
                }
            }
        }
    }

}
