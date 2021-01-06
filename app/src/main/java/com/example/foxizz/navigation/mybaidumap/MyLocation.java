package com.example.foxizz.navigation.mybaidumap;

import android.annotation.SuppressLint;

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
import com.example.foxizz.navigation.activity.fragment.MainFragment;
import com.example.foxizz.navigation.data.SearchDataHelper;
import com.example.foxizz.navigation.util.ToastUtil;

import static com.example.foxizz.navigation.BaseApplication.getContext;

/**
 * 定位模块
 */
@SuppressLint("Registered")
public class MyLocation {

    private final static int MAX_TIME = 10;//最大请求次数
    private int requestLocationTime;//请求定位的次数
    private boolean isFirstLoc;//是否是首次定位
    public boolean refreshSearchList;//是否刷新搜索列表

    private final MainFragment mainFragment;
    public MyLocation(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    /**
     * 初始化定位
     */
    public void initLocationOption() {
        requestLocationTime = 0;//请求次数置0
        isFirstLoc = true;//首次定位

        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mainFragment.mLocationClient = new LocationClient(getContext());

        //定位监听
        mainFragment.mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                //mapView销毁后不再处理新接收的位置
                if (location == null || mainFragment.mMapView == null) return;

                //获取定位数据
                mainFragment.latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mainFragment.mLocType = location.getLocType();
                mainFragment.mRadius = location.getRadius();
                mainFragment.mLatitude = location.getLatitude();
                mainFragment.mLongitude = location.getLongitude();
                mainFragment.mCity = location.getCity();

                //更新定位
                mainFragment.locData = new MyLocationData.Builder()
                        .accuracy(mainFragment.mRadius)
                        .direction(mainFragment.mLastX)
                        .latitude(mainFragment.mLatitude)
                        .longitude(mainFragment.mLongitude).build();
                mainFragment.mBaiduMap.setMyLocationData(mainFragment.locData);//设置定位数据

                if (mainFragment.mLocType == BDLocation.TypeGpsLocation //GPS定位结果
                        || mainFragment.mLocType == BDLocation.TypeNetWorkLocation //网络定位结果
                        || mainFragment.mLocType == BDLocation.TypeOffLineLocation) {//离线定位结果
                    //ToastUtil.showToast(location.getAddrStr());
                    if (isFirstLoc) {
                        isFirstLoc = false;

                        if (refreshSearchList) {
                            refreshSearchList = false;
                            if (mainFragment.isHistorySearchResult)
                                SearchDataHelper.initSearchData(mainFragment);//初始化搜索记录
                            else
                                mainFragment.startSearch();//开始搜索（有bug，暂时不作此操作）                      ）
                        }

                        mainFragment.myNavigateHelper.initDriveNavigateHelper();//初始化驾车导航引擎

                        //移动视角并改变缩放等级
                        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(mainFragment.latLng);
                        mainFragment.mBaiduMap.setMapStatus(msu);
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.zoom(18.0f).target(mainFragment.latLng);
                        mainFragment.mBaiduMap.animateMapStatus(
                                MapStatusUpdateFactory.newMapStatus(builder.build())
                        );
                    }
                } else {
                    if (requestLocationTime < MAX_TIME) {
                        initLocationOption();//再次请求定位
                        requestLocationTime++;//请求次数+1
                    } else {
                        //弹出错误提示
                        switch (mainFragment.mLocType) {
                            case BDLocation.TypeServerError://服务器错误
                                ToastUtil.showToast(R.string.server_error);
                                break;
                            case BDLocation.TypeNetWorkException://网络错误
                                ToastUtil.showToast(R.string.network_error);
                                break;
                            case BDLocation.TypeCriteriaException://手机模式错误
                                ToastUtil.showToast(R.string.close_airplane_mode);
                                break;
                            default:
                                ToastUtil.showToast(R.string.unknown_error);
                                break;
                        }
                    }
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
        mainFragment.mLocationClient.setLocOption(option);
        //开启定位
        mainFragment.mLocationClient.start();
    }

}
