package com.example.foxizz.navigation.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.MainActivity;

/**
 * 定位模块
 */
@SuppressLint("Registered")
public class MyLocation {

    private MainActivity mainActivity;
    public MyLocation(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    //初始化定位
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void initLocationOption() {
        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mainActivity.mLocationClient = new LocationClient(mainActivity);

        //定位监听
        mainActivity.mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                //mapView 销毁后不在处理新接收的位置
                if (location == null || mainActivity.mMapView == null){
                    return;
                }

                //获取定位数据
                mainActivity.latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mainActivity.mLocType = location.getLocType();
                mainActivity.mRadius = location.getRadius();
                mainActivity.mLatitude = location.getLatitude();
                mainActivity.mLongitude = location.getLongitude();
                mainActivity.mCity = location.getCity();

                //更新定位
                mainActivity.locData = new MyLocationData.Builder()
                        .accuracy(mainActivity.mRadius)
                        .direction(mainActivity.mLastX)
                        .latitude(mainActivity.mLatitude)
                        .longitude(mainActivity.mLongitude).build();
                mainActivity.mBaiduMap.setMyLocationData(mainActivity.locData);//设置定位数据

                if(mainActivity.isFirstLoc) {
                    mainActivity.isFirstLoc = false;

                    //改变地图状态
                    MapStatusUpdate msu= MapStatusUpdateFactory.newLatLng(mainActivity.latLng);
                    mainActivity.mBaiduMap.setMapStatus(msu);
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(mainActivity.latLng);
                    mainActivity.mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                    if(mainActivity.mLocType == BDLocation.TypeGpsLocation //GPS定位结果
                            || mainActivity.mLocType == BDLocation.TypeNetWorkLocation //网络定位结果
                            || mainActivity.mLocType == BDLocation.TypeOffLineLocation) {//离线定位结果
                        //Toast.makeText(MainActivity.this,
                        //location.getAddrStr(), Toast.LENGTH_SHORT).show();
                    } else if(mainActivity.mLocType == BDLocation.TypeServerError) {//服务器错误
                        Toast.makeText(mainActivity,
                                R.string.server_error, Toast.LENGTH_SHORT).show();
                    } else if(mainActivity.mLocType == BDLocation.TypeNetWorkException) {//网络错误
                        Toast.makeText(mainActivity,
                                mainActivity.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                    } else if(mainActivity.mLocType == BDLocation.TypeCriteriaException) {//手机模式错误
                        Toast.makeText(mainActivity,
                                R.string.close_airplane_mode, Toast.LENGTH_SHORT).show();
                    }

                    mainActivity.dbHelper.initSearchData();//初始化搜索记录
                }
            }
        });

        //声明LocationClient类实例并配置定位参数
        LocationClientOption option = new LocationClientOption();
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        option.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(1000);
        //可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址描述
        option.setIsNeedLocationDescribe(true);
        //可选，设置是否需要设备方向结果
        option.setNeedDeviceDirect(true);
        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setLocationNotify(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，
        //结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(true);
        //可选，默认false，设置是否开启Gps定位
        option.setOpenGps(true);
        //可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        option.setIsNeedAltitude(false);
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，
        //该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        option.setOpenAutoNotifyMode();
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        //option.setOpenAutoNotifyMode(3000, 1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        mainActivity.mLocationClient.setLocOption(option);
        //开启定位
        mainActivity.mLocationClient.start();
    }

}
