package com.navigation.foxizz.mybaidumap

import Constants
import base.foxizz.BaseApplication.Companion.baseApplication
import base.foxizz.util.SPUtil
import base.foxizz.util.showToast
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.fragment.MainFragment
import com.navigation.foxizz.data.SearchDataHelper

/**
 * 定位模块
 */
class BaiduLocation(private val mainFragment: MainFragment) {
    companion object {
        private const val MAX_TIME = 8 //最大请求次数
    }

    //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
    var mLocationClient = LocationClient(baseApplication)
    lateinit var mLocData: MyLocationData //地址信息
    var mLatLng: LatLng? = null //坐标
    var mLocType = 0 //定位结果
    var mRadius = 0f //精度半径
    var mLatitude = 0.0 //纬度
    var mLongitude = 0.0 //经度
    var mCity = "" //所在城市

    var requestLocationTime = 0//请求定位的次数
    var refreshSearchList = false//是否刷新搜索列表
    private var isFirstLoc = false//是否是首次定位

    /**
     * 初始化定位
     */
    fun initLocationOption() {
        isFirstLoc = true //首次定位

        //停止定位服务
        if (mLocationClient.isStarted) mLocationClient.stop()

        //定位监听
        mLocationClient.registerLocationListener(object : BDAbstractLocationListener() {
            override fun onReceiveLocation(location: BDLocation) {
                //获取定位数据
                mLatLng = LatLng(location.latitude, location.longitude)
                mLocType = location.locType
                mRadius = location.radius
                mLatitude = location.latitude
                mLongitude = location.longitude
                mCity = location.city

                //更新定位
                mLocData = MyLocationData.Builder()
                    .accuracy(mRadius)
                    .direction(mainFragment.mOrientationListener.mLastX)
                    .latitude(mLatitude)
                    .longitude(mLongitude).build()
                mainFragment.mBaiduMap.setMyLocationData(mLocData) //设置定位数据
                if (mLocType == BDLocation.TypeGpsLocation //GPS定位结果
                    || mLocType == BDLocation.TypeNetWorkLocation //网络定位结果
                    || mLocType == BDLocation.TypeOffLineLocation
                ) { //离线定位结果
                    //location.addrStr.showToast()

                    //到新城市时
                    if (mCity.isNotEmpty() && mCity != SPUtil.getString(Constants.MY_CITY)) {
                        /*
                        //启动下载离线地图服务
                        OfflineMapService.startService(
                                mainFragment.baseActivity, mainFragment.mBaiduLocation.mCity
                        )
                        */
                        SPUtil.put(Constants.MY_CITY, mCity) //保存新城市
                    }

                    if (isFirstLoc) {
                        isFirstLoc = false
                        if (refreshSearchList) {
                            refreshSearchList = false
                            if (mainFragment.isHistorySearchResult)
                                SearchDataHelper.initSearchData(mainFragment) //初始化搜索记录                    ）
                        }

                        //移动视角并改变缩放等级
                        val msu = MapStatusUpdateFactory.newLatLng(mLatLng)
                        mainFragment.mBaiduMap.setMapStatus(msu)
                        val builder = MapStatus.Builder()
                        builder.zoom(18.0f).target(mLatLng)
                        mainFragment.mBaiduMap.animateMapStatus(
                            MapStatusUpdateFactory.newMapStatus(builder.build()))
                    }
                } else {
                    if (requestLocationTime < MAX_TIME) {
                        requestLocationTime++ //请求次数+1
                    } else {
                        //弹出错误提示
                        when (mLocType) {
                            BDLocation.TypeServerError -> showToast(R.string.server_error)
                            BDLocation.TypeNetWorkException -> showToast(R.string.network_error)
                            BDLocation.TypeCriteriaException -> showToast(R.string.close_airplane_mode)
                            else -> showToast(R.string.unknown_error)
                        }
                    }
                }
            }
        })

        //声明LocationClient类实例并配置定位参数
        LocationClientOption().run {
            //默认高精度，设置定位模式，高精度，低功耗，仅设备
            locationMode = LocationClientOption.LocationMode.Hight_Accuracy
            //默认false，设置是否开启GPS定位
            isOpenGps = true
            //默认false，设置是否当GPS有效时按照1S1次频率输出GPS结果
            isLocationNotify = false
            //默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
            setCoorType("bd09ll")
            //默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
            setScanSpan(1000)
            //设置是否需要地址信息，默认不需要
            setIsNeedAddress(true)
            //设置是否需要设备方向结果
            setNeedDeviceDirect(true)
            //默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
            setIgnoreKillProcess(true)
            //默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，
            //结果类似于“在北京天安门附近”
            setIsNeedLocationDescribe(true)
            //默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
            setIsNeedLocationPoiList(true)
            //默认false，设置是否收集CRASH信息，默认收集
            SetIgnoreCacheException(true)
            //默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
            setIsNeedAltitude(false)
            //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，
            //该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
            setOpenAutoNotifyMode()
            //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
            //setOpenAutoNotifyMode(3000, 1, LocationClientOption.LOC_SENSITIVITY_HIGHT)
            //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
            mLocationClient.locOption = this
        }
        //启动定位服务
        mLocationClient.start()
    }
}