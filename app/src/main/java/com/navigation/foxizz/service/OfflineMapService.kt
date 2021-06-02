package com.navigation.foxizz.service

import Constants
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import base.foxizz.util.AppUtil
import base.foxizz.util.NetworkUtil
import base.foxizz.util.SPUtil
import com.baidu.mapapi.map.offline.MKOLSearchRecord
import com.baidu.mapapi.map.offline.MKOfflineMap
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.MainActivity
import com.navigation.foxizz.util.CityUtil

/**
 * 下载离线地图服务
 */
class OfflineMapService : Service() {
    companion object {
        private const val MAX_CITY_NUM = 3 //最大保存城市数量

        /**
         * 启动下载离线地图服务
         *
         * @param context 上下文
         * @param cityName 城市名
         */
        fun startService(context: Context, cityName: String) {
            val intent = Intent(context, OfflineMapService::class.java)
            intent.putExtra(Constants.MY_CITY, cityName)
            context.startService(intent)
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent);
            else context.startService(intent);
            */
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        //initForeground();
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        downloadOfflineMap(intent) //下载离线地图
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        //stopForeground(true);//关闭前台服务
    }

    //使用前台服务
    private fun initForeground() {
        //使用前台服务
        val channelId = AppUtil.appChannel
        val appName = AppUtil.appName
        Log.d("Foxizz_Test", "channelId=$channelId,appName=$appName")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                channelId, appName, NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        //设置点击通知返回主活动
        val appIntent = Intent(this, MainActivity::class.java)
        appIntent.action = Intent.ACTION_MAIN
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pendingIntent = PendingIntent.getActivity(this, 0, appIntent, 0)

        //设置通知信息
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.dolphizz_cartoon)
            .setContentTitle(getString(R.string.downloading_offline_map))
            .setContentText(getString(R.string.click_to_see_more))
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
    }

    //下载离线地图
    private fun downloadOfflineMap(intent: Intent) {
        val mCity = intent.getStringExtra(Constants.MY_CITY)
        if (!CityUtil.isCityName(mCity)) return
        //没有有网络连接或网络类型不为wifi
        if (!NetworkUtil.isNetworkConnected || NetworkUtil.networkType != "wifi") return

        //初始化离线地图下载器
        val mkOfflineMap = MKOfflineMap()
        mkOfflineMap.init { result, cityID ->
            Log.d("Foxizz_Test", "result=$result,cityID=$cityID")
            //stopSelf();
        }

        //根据城市名获取城市id
        var cityID = 0
        val searchRecords: List<MKOLSearchRecord> = mkOfflineMap.searchCity(mCity)
        if (searchRecords.size == 1) cityID = searchRecords[0].cityID
        Log.d("Foxizz_Test", "cityID=$cityID")
        if (cityID != 0) {
            //mkOfflineMap.remove(cityID);
            //SPUtil.put(Constants.OFFLINE_CITIES, "");
            if (mkOfflineMap.start(cityID)) { //开始下载
                //限制离线地图城市数量
                var offlineCities = SPUtil.getString(Constants.OFFLINE_CITIES, "")
                Log.d("Foxizz_Test", "oldOfflineCities=$offlineCities")
                if (offlineCities.isNotEmpty()) {
                    offlineCities.split(" ").forEach {
                        if (it.toInt() == cityID) return
                    }
                }
                offlineCities = "$offlineCities $cityID".trim()
                val offlineCitiesSplit = offlineCities.split(" ")
                if (offlineCitiesSplit.size > MAX_CITY_NUM) {
                    val offlineCity = offlineCitiesSplit[0]
                    mkOfflineMap.remove(offlineCity.toInt())
                    offlineCities = offlineCities.replaceFirst(offlineCity, "").trim()
                }
                SPUtil.put(Constants.OFFLINE_CITIES, offlineCities)
                Log.d("Foxizz_Test", "newOfflineCities=$offlineCities")
            } else mkOfflineMap.update(cityID) //更新离线地图
        }
    }
}