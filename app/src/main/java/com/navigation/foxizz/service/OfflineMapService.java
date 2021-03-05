package com.navigation.foxizz.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.navigation.foxizz.R;
import com.navigation.foxizz.activity.MainActivity;
import com.navigation.foxizz.data.Constants;
import com.navigation.foxizz.data.SPHelper;
import com.navigation.foxizz.util.AppUtil;
import com.navigation.foxizz.util.CityUtil;
import com.navigation.foxizz.util.NetworkUtil;

import java.util.List;

/*
 * 下载离线地图服务
 */
public class OfflineMapService extends Service {

    private final static int MAX_CITY_NUM = 3;//最大保存城市数量

    /**
     * 启动下载离线地图服务
     *
     * @param context 上下文
     * @param cityName 城市名
     */
    public static void startService(Context context, String cityName) {
        Intent intent = new Intent(context, OfflineMapService.class);
        intent.putExtra(Constants.MY_CITY, cityName);
        context.startService(intent);
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent);
        else context.startService(intent);
        */
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //initForeground();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        downloadOfflineMap(intent);//下载离线地图
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stopForeground(true);//关闭前台服务
    }

    //使用前台服务
    private void initForeground() {
        //使用前台服务
        String channelId = AppUtil.getAppChannel();
        String appName = AppUtil.getAppName();
        Log.d("Foxizz_Test", "channelId=" + channelId + ",appName=" + appName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(
                    channelId, appName, NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel);
        }

        //设置点击通知返回主活动
        Intent appIntent = new Intent(this, MainActivity.class);
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, appIntent, 0);

        //设置通知信息
        Notification notification = new NotificationCompat
                .Builder(this, channelId)
                .setSmallIcon(R.drawable.dolphizz_cartoon)
                .setContentTitle(getString(R.string.downloading_offline_map))
                .setContentText(getString(R.string.click_to_see_more))
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    //下载离线地图
    private void downloadOfflineMap(Intent intent) {
        if(intent == null) return;

        String mCity = intent.getStringExtra(Constants.MY_CITY);
        if (!CityUtil.checkCityName(mCity)) return;

        if (!NetworkUtil.isNetworkConnected()//有网络连接
                || !NetworkUtil.getNetworkType().equals("wifi"))//网络类型为wifi
            return;

        //初始化离线地图下载器
        MKOfflineMap mkOfflineMap = new MKOfflineMap();
        mkOfflineMap.init(new MKOfflineMapListener() {
            @Override
            public void onGetOfflineMapState(int result, int cityID) {
                Log.d("Foxizz_Test", "result=" + result + ",cityID=" + cityID);
                //stopSelf();
            }
        });

        //根据城市名获取城市id
        int cityID = 0;
        List<MKOLSearchRecord> searchRecords = mkOfflineMap.searchCity(mCity);
        if(searchRecords != null && searchRecords.size() == 1) {
            cityID = searchRecords.get(0).cityID;
        }

        Log.d("Foxizz_Test", "cityID=" + cityID);

        if (cityID != 0) {
//            mkOfflineMap.remove(cityID);
//            SPHelper.putString(Constants.OFFLINE_CITIES, "");
            if (mkOfflineMap.start(cityID)) {//开始下载
                //限制离线地图城市数量
                String offlineCities = SPHelper.getString(Constants.OFFLINE_CITIES, "");
                Log.d("Foxizz_Test", "oldOfflineCities=" + offlineCities);
                if (!TextUtils.isEmpty(offlineCities)) {
                    for (String offlineCity : offlineCities.split(" ")) {
                        if (Integer.parseInt(offlineCity) == cityID) return;
                    }
                }
                offlineCities = (offlineCities + " " + cityID).trim();
                String[] offlineCitiesSplit = offlineCities.split(" ");
                if (offlineCitiesSplit.length > MAX_CITY_NUM) {
                    String offlineCity = offlineCitiesSplit[0];
                    mkOfflineMap.remove(Integer.parseInt(offlineCity));
                    offlineCities = offlineCities.replaceFirst(offlineCity, "").trim();
                }
                SPHelper.putString(Constants.OFFLINE_CITIES, offlineCities);
                Log.d("Foxizz_Test", "newOfflineCities=" + offlineCities);
            } else {
                mkOfflineMap.update(cityID);//更新离线地图
            }
        }
    }

}
