package com.navigation.foxizz.mybaidumap

import android.app.ProgressDialog
import android.os.Handler
import android.os.Looper
import android.os.Message
import base.foxizz.BaseApplication.Companion.baseApplication
import base.foxizz.getString
import base.foxizz.util.AppUtil
import base.foxizz.util.NetworkUtil
import base.foxizz.util.SettingUtil
import base.foxizz.util.showToast
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
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.fragment.MainFragment
import com.navigation.foxizz.mybaidumap.activity.BNaviGuideActivity
import com.navigation.foxizz.mybaidumap.activity.DNaviGuideActivity
import com.navigation.foxizz.mybaidumap.activity.WNaviGuideActivity
import java.util.*

/**
 * 导航模块
 *
 * @param mainFragment 地图页
 */
class BaiduNavigation(private val mainFragment: MainFragment) {
    companion object {
        private var hasInitDriveNavigate = false //驾车导航是否已初始化
    }

    private lateinit var mLoadingProgress: ProgressDialog //加载弹窗

    init {
        initProgressDialog() //初始化加载弹窗

        //初始化驾车导航引擎
        if (NetworkUtil.isNetworkConnected) { //有网络连接
            initDriveNavigateHelper() //初始化驾车
            initTTS() //初始化语音合成
        }
    }

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
                        if (status != 0) showToast(getString(R.string.key_checkout_fail) + msg)
                    }

                    override fun initStart() {}
                    override fun initSuccess() {
                        hasInitDriveNavigate = true
                    }

                    override fun initFailed(errCode: Int) =
                        showToast(getString(R.string.drive_navigate_init_fail) + errCode)
                })
        }
    }

    //初始化加载弹窗
    private fun initProgressDialog() {
        mLoadingProgress = ProgressDialog(mainFragment.baseActivity)
        mLoadingProgress.run {
            setTitle(R.string.hint)
            setMessage(getString(R.string.route_plan_please_wait))
            setCancelable(false)
        }
    }

    //初始化语音合成模块
    private fun initTTS() {
        BaiduNaviManagerFactory.getTTSManager().initTTS(
            BNTTsInitConfig.Builder()
                .context(baseApplication)
                .sdcardRootPath(AppUtil.sdCardDir)
                .appFolderName(AppUtil.appFolderName)
                .appId(getString(R.string.app_id))
                .appKey(getString(R.string.api_key))
                .secretKey(getString(R.string.secret_key))
                .build()
        )
    }

    /**
     * 初始化步行导航引擎
     */
    private fun initWalkNavigateHelper() {
        //步行引擎初始化
        WalkNavigateHelper.getInstance().initNaviEngine(mainFragment.baseActivity, object
            : IWEngineInitListener {
            override fun engineInitSuccess() = routeWalkPlanWithParam()
            override fun engineInitFail() = showToast(R.string.walk_navigate_init_fail)
        })
    }

    /**
     * 初始化骑行导航引擎
     */
    private fun initBikeNavigateHelper() {
        //骑行引擎初始化
        BikeNavigateHelper.getInstance().initNaviEngine(mainFragment.baseActivity, object
            : IBEngineInitListener {
            override fun engineInitSuccess() = routeBikePlanWithParam()
            override fun engineInitFail() = showToast(R.string.bike_navigate_init_fail)
        })
    }

    /**
     * 开始导航
     */
    fun startNavigate() {
        if (!NetworkUtil.isNetworkConnected) { //没有网络连接
            showToast(R.string.network_error)
            return
        }
        if (NetworkUtil.isAirplaneModeEnable) { //没有关飞行模式
            showToast(R.string.close_airplane_mode)
            return
        }

        mainFragment.run {
            if (SettingUtil.hasReadWriteAndLocationPermissions) { //权限不足
                checkPermissionAndLocate() //申请权限，获得权限后定位
                return
            }
            if (mBaiduLocation.mLatLng == null) { //还没有得到定位
                showToast(R.string.wait_for_location_result)
                return
            }
            if (mBaiduRoutePlan.mEndLocation == null) {
                showToast(R.string.end_location_is_null)
                return
            }
            when (mRoutePlanSelect) {
                BaiduRoutePlan.DRIVING -> routeDrivePlanWithParam() //开始驾车导航
                BaiduRoutePlan.WALKING, BaiduRoutePlan.TRANSIT ->
                    initWalkNavigateHelper() //开始步行导航
                BaiduRoutePlan.BIKING -> initBikeNavigateHelper() //开始骑行导航
            }
        }
    }

    //初始化驾车路线规划
    private fun routeDrivePlanWithParam() {
        if (!hasInitDriveNavigate) return

        //设置驾车导航的起点和终点
        val mBNRoutePlanNodes = ArrayList<BNRoutePlanNode>()
        mainFragment.run {
            val startNode = BNRoutePlanNode.Builder()
                .latitude(mBaiduLocation.mLatLng!!.latitude)
                .longitude(mBaiduLocation.mLatLng!!.longitude)
                .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
                .build()
            val endNode = BNRoutePlanNode.Builder()
                .latitude(mBaiduRoutePlan.mEndLocation!!.latitude)
                .longitude(mBaiduRoutePlan.mEndLocation!!.longitude)
                .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
                .build()
            mBNRoutePlanNodes.add(startNode)
            mBNRoutePlanNodes.add(endNode)
        }

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
                            showToast(R.string.drive_route_plan_fail)
                        }
                        IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI ->
                            DNaviGuideActivity.startActivity(mainFragment.baseActivity)
                    }
                }
            }
        )
    }

    //初始化步行路线规划
    private fun routeWalkPlanWithParam() {
        val walkStartNode = WalkRouteNodeInfo()
        val walkEndNode = WalkRouteNodeInfo()

        mainFragment.run {
            //设置起点
            walkStartNode.location = mBaiduLocation.mLatLng

            if (mRoutePlanSelect == BaiduRoutePlan.WALKING) {
                //设置步行导航的终点
                walkEndNode.location = mBaiduRoutePlan.mEndLocation
            } else if (mRoutePlanSelect == BaiduRoutePlan.TRANSIT) {
                //计算公交导航的步行导航的终点
                if (mBaiduRoutePlan.mBusStationLocations.size == 0) {
                    showToast(R.string.wait_for_route_plan_result)
                    return
                }

                //设置目的地
                var minDistance = DistanceUtil.getDistance(
                    mBaiduLocation.mLatLng, mBaiduRoutePlan.mEndLocation
                )
                walkEndNode.location = mBaiduRoutePlan.mEndLocation
                for (i in mBaiduRoutePlan.mBusStationLocations.indices) {
                    val busStationDistance = DistanceUtil.getDistance(
                        mBaiduLocation.mLatLng, mBaiduRoutePlan.mBusStationLocations[i]
                    )
                    if (busStationDistance < minDistance) {
                        minDistance = busStationDistance
                        //最近的站点距离大于100m则将目的地设置为最近的站点
                        if (minDistance > 100) {
                            walkEndNode.location = mBaiduRoutePlan.mBusStationLocations[i]
                            //否则设置为最近的站点的下一个站点
                        } else if (i != mBaiduRoutePlan.mBusStationLocations.size - 1) {
                            walkEndNode.location = mBaiduRoutePlan.mBusStationLocations[i + 1]
                        }
                    }
                }
            }
        }

        val walkParam = WalkNaviLaunchParam()
            .startNodeInfo(walkStartNode)
            .endNodeInfo(walkEndNode)
            .extraNaviMode(0) //普通步行导航
        WalkNavigateHelper.getInstance().routePlanWithRouteNode(walkParam, object
            : IWRoutePlanListener {
            override fun onRoutePlanStart() = mLoadingProgress.show()
            override fun onRoutePlanSuccess() {
                mLoadingProgress.dismiss()
                WNaviGuideActivity.startActivity(mainFragment.baseActivity)
            }

            override fun onRoutePlanFail(error: WalkRoutePlanError) {
                mLoadingProgress.dismiss()
                showToast(R.string.walk_route_plan_fail)
            }
        })
    }

    //初始化骑行路线规划
    private fun routeBikePlanWithParam() {
        //获取定位点和目标点坐标
        val bikeStartNode = BikeRouteNodeInfo().apply {
            location = mainFragment.mBaiduLocation.mLatLng
        }
        val bikeEndNode = BikeRouteNodeInfo().apply {
            location = mainFragment.mBaiduSearch.mSearchList[0].latLng
        }
        val bikeParam = BikeNaviLaunchParam()
            .startNodeInfo(bikeStartNode)
            .endNodeInfo(bikeEndNode)
        BikeNavigateHelper.getInstance().routePlanWithRouteNode(bikeParam, object
            : IBRoutePlanListener {
            override fun onRoutePlanStart() = mLoadingProgress.show()
            override fun onRoutePlanSuccess() {
                mLoadingProgress.dismiss()
                BNaviGuideActivity.startActivity(mainFragment.baseActivity)
            }

            override fun onRoutePlanFail(bikeRoutePlanError: BikeRoutePlanError) {
                mLoadingProgress.dismiss()
                showToast(R.string.bike_route_plan_fail)
            }
        })
    }
}