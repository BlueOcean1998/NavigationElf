package com.navigation.foxizz.mybaidumap

import android.app.ProgressDialog
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.baidu.mapapi.bikenavi.BikeNavigateHelper
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam
import com.baidu.mapapi.bikenavi.params.BikeRouteNodeInfo
import com.baidu.mapapi.utils.DistanceUtil
import com.baidu.mapapi.walknavi.WalkNavigateHelper
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo
import com.baidu.navisdk.adapter.BNRoutePlanNode
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory
import com.baidu.navisdk.adapter.IBNRoutePlanManager
import com.baidu.navisdk.adapter.IBaiduNaviManager.INaviInitListener
import com.baidu.navisdk.adapter.struct.BNTTsInitConfig
import com.navigation.foxizz.BaseApplication.Companion.baseApplication
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.fragment.MainFragment
import com.navigation.foxizz.mybaidumap.activity.BNaviGuideActivity
import com.navigation.foxizz.mybaidumap.activity.DNaviGuideActivity
import com.navigation.foxizz.mybaidumap.activity.WNaviGuideActivity
import com.navigation.foxizz.util.AppUtil
import com.navigation.foxizz.util.NetworkUtil
import com.navigation.foxizz.util.SettingUtil
import com.navigation.foxizz.util.showToast
import java.util.*

/**
 * 导航模块
 */
class BaiduNavigation(private val mainFragment: MainFragment) {
    init {
        initProgressDialog() //初始化加载弹窗

        //初始化驾车导航引擎
        if (NetworkUtil.isNetworkConnected) { //有网络连接
            initDriveNavigateHelper()
        }
    }

    companion object {
        private var hasInitDriveNavigate = false //驾车导航是否已初始化
        private var enableDriveNavigate = false //是否可以进行驾车导航
    }

    private lateinit var mLoadingProgress: ProgressDialog //加载弹窗

    /**
     * 初始化驾车导航引擎
     */
    fun initDriveNavigateHelper() {
        if (!hasInitDriveNavigate) {
            BaiduNaviManagerFactory.getBaiduNaviManager().init(baseApplication,
                    AppUtil.sdCardDir,
                    AppUtil.appFolderName,
                    object : INaviInitListener {
                        override fun onAuthResult(status: Int, msg: String) {
                            if (status != 0) {
                                (mainFragment.getString(R.string.key_checkout_fail) + msg).showToast()
                            }
                        }

                        override fun initStart() {}
                        override fun initSuccess() {
                            enableDriveNavigate = true

                            //初始化语音合成模块
                            initTTS()
                            hasInitDriveNavigate = true
                        }

                        override fun initFailed(errCode: Int) {
                            (R.string.drive_navigate_init_fail + errCode).showToast()
                        }
                    })
        }
    }

    //初始化加载弹窗
    private fun initProgressDialog() {
        mLoadingProgress = ProgressDialog(mainFragment.requireActivity())
        mLoadingProgress.setTitle(R.string.hint)
        mLoadingProgress.setMessage(mainFragment.getString(R.string.route_plan_please_wait))
        mLoadingProgress.setCancelable(false)
    }

    //初始化语音合成模块
    private fun initTTS() {
        BaiduNaviManagerFactory.getTTSManager().initTTS(BNTTsInitConfig.Builder()
                .context(baseApplication)
                .sdcardRootPath(AppUtil.sdCardDir)
                .appFolderName(AppUtil.appFolderName)
                .appId(mainFragment.getString(R.string.app_id))
                .appKey(mainFragment.getString(R.string.api_key))
                .secretKey(mainFragment.getString(R.string.secret_key))
                .build()
        )
    }

    /**
     * 初始化步行导航引擎
     */
    private fun initWalkNavigateHelper() {
        //步行引擎初始化
        WalkNavigateHelper.getInstance().initNaviEngine(
                mainFragment.requireActivity(), object : IWEngineInitListener {
            override fun engineInitSuccess() {
                routeWalkPlanWithParam()
            }

            override fun engineInitFail() {
                R.string.walk_navigate_init_fail.showToast()
            }
        })
    }

    /**
     * 初始化骑行导航引擎
     */
    private fun initBikeNavigateHelper() {
        //骑行引擎初始化
        BikeNavigateHelper.getInstance().initNaviEngine(
                mainFragment.requireActivity(), object : IBEngineInitListener {
            override fun engineInitSuccess() {
                routeBikePlanWithParam()
            }

            override fun engineInitFail() {
                R.string.bike_navigate_init_fail.showToast()
            }
        })
    }

    /**
     * 开始导航
     */
    fun startNavigate() {
        if (!NetworkUtil.isNetworkConnected) { //没有网络连接
            R.string.network_error.showToast()
            return
        }
        if (NetworkUtil.isAirplaneModeOn) { //没有关飞行模式
            R.string.close_airplane_mode.showToast()
            return
        }
        if (SettingUtil.haveReadWriteAndLocationPermissions()) { //权限不足
            mainFragment.requestPermission() //申请权限，获得权限后定位
            return
        }
        if (mainFragment.mBaiduLocation.mLatLng == null) { //还没有得到定位
            R.string.wait_for_location_result.showToast()
            return
        }
        if (mainFragment.mBaiduRoutePlan.mEndLocation == null) {
            R.string.end_location_is_null.showToast()
            return
        }
        when (mainFragment.mRoutePlanSelect) {
            BaiduRoutePlan.DRIVING -> routeDrivePlanWithParam() //开始驾车导航
            BaiduRoutePlan.WALKING, BaiduRoutePlan.TRANSIT -> initWalkNavigateHelper() //开始步行导航
            BaiduRoutePlan.BIKING -> initBikeNavigateHelper() //开始骑行导航
        }
    }

    //初始化驾车路线规划
    private fun routeDrivePlanWithParam() {
        if (!enableDriveNavigate) return

        //设置驾车导航的起点和终点
        val startNode = BNRoutePlanNode.Builder()
                .latitude(mainFragment.mBaiduLocation.mLatLng?.latitude!!)
                .longitude(mainFragment.mBaiduLocation.mLatLng?.longitude!!)
                .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
                .build()
        val endNode = BNRoutePlanNode.Builder()
                .latitude(mainFragment.mBaiduRoutePlan.mEndLocation?.latitude!!)
                .longitude(mainFragment.mBaiduRoutePlan.mEndLocation?.longitude!!)
                .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
                .build()
        val mBNRoutePlanNodes = ArrayList<BNRoutePlanNode>()
        mBNRoutePlanNodes.add(startNode)
        mBNRoutePlanNodes.add(endNode)
        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                mBNRoutePlanNodes,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                null,
                object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        when (msg.what) {
                            IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START ->
                                mLoadingProgress.show()
                            IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS ->
                                mLoadingProgress.dismiss()
                            IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED -> {
                                mLoadingProgress.dismiss()
                                R.string.drive_route_plan_fail.showToast()
                            }
                            IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI ->
                                DNaviGuideActivity.startActivity(mainFragment.requireActivity())
                        }
                    }
                }
        )
    }

    //初始化步行路线规划
    private fun routeWalkPlanWithParam() {
        val walkStartNode = WalkRouteNodeInfo()
        val walkEndNode = WalkRouteNodeInfo()

        //设置起点
        walkStartNode.location = mainFragment.mBaiduLocation.mLatLng

        //设置步行导航的终点
        if (mainFragment.mRoutePlanSelect == BaiduRoutePlan.WALKING) {
            walkEndNode.location = mainFragment.mBaiduRoutePlan.mEndLocation

            //计算公交导航的步行导航的终点
        } else if (mainFragment.mRoutePlanSelect == BaiduRoutePlan.TRANSIT) {
            if (mainFragment.mBaiduRoutePlan.mBusStationLocations.size == 0) {
                R.string.wait_for_route_plan_result.showToast()
                return
            }

            //设置目的地
            var minDistance = DistanceUtil.getDistance(
                    mainFragment.mBaiduLocation.mLatLng, mainFragment.mBaiduRoutePlan.mEndLocation
            )
            walkEndNode.location = mainFragment.mBaiduRoutePlan.mEndLocation
            for (i in mainFragment.mBaiduRoutePlan.mBusStationLocations.indices) {
                val busStationDistance = DistanceUtil.getDistance(
                        mainFragment.mBaiduLocation.mLatLng, mainFragment.mBaiduRoutePlan.mBusStationLocations[i]
                )
                if (busStationDistance < minDistance) {
                    minDistance = busStationDistance
                    //最近的站点距离大于100m则将目的地设置为最近的站点
                    if (minDistance > 100) {
                        walkEndNode.location = mainFragment.mBaiduRoutePlan.mBusStationLocations[i]
                        //否则设置为最近的站点的下一个站点
                    } else if (i != mainFragment.mBaiduRoutePlan.mBusStationLocations.size - 1) {
                        walkEndNode.location = mainFragment.mBaiduRoutePlan.mBusStationLocations[i + 1]
                    }
                }
            }
        }
        val walkParam = WalkNaviLaunchParam()
                .startNodeInfo(walkStartNode)
                .endNodeInfo(walkEndNode)
        walkParam.extraNaviMode(0) //普通步行导航
        WalkNavigateHelper.getInstance().routePlanWithRouteNode(walkParam, object : IWRoutePlanListener {
            override fun onRoutePlanStart() {
                mLoadingProgress.show()
            }

            override fun onRoutePlanSuccess() {
                mLoadingProgress.dismiss()
                WNaviGuideActivity.startActivity(mainFragment.requireActivity())
            }

            override fun onRoutePlanFail(error: WalkRoutePlanError) {
                mLoadingProgress.dismiss()
                R.string.walk_route_plan_fail.showToast()
            }
        })
    }

    //初始化骑行路线规划
    private fun routeBikePlanWithParam() {
        //获取定位点和目标点坐标
        val bikeStartNode = BikeRouteNodeInfo()
        bikeStartNode.location = mainFragment.mBaiduLocation.mLatLng
        val bikeEndNode = BikeRouteNodeInfo()
        bikeEndNode.location = mainFragment.mBaiduSearch.mSearchList[0].latLng
        val bikeParam = BikeNaviLaunchParam()
                .startNodeInfo(bikeStartNode)
                .endNodeInfo(bikeEndNode)
        BikeNavigateHelper.getInstance().routePlanWithRouteNode(bikeParam, object : IBRoutePlanListener {
            override fun onRoutePlanStart() {
                mLoadingProgress.show()
            }

            override fun onRoutePlanSuccess() {
                mLoadingProgress.dismiss()
                BNaviGuideActivity.startActivity(mainFragment.requireActivity())
            }

            override fun onRoutePlanFail(bikeRoutePlanError: BikeRoutePlanError) {
                mLoadingProgress.dismiss()
                R.string.bike_route_plan_fail.showToast()
            }
        })
    }
}