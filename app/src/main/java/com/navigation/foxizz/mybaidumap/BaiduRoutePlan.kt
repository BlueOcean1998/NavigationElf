package com.navigation.foxizz.mybaidumap

import android.annotation.SuppressLint
import android.view.View
import base.foxizz.getString
import base.foxizz.util.*
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.*
import com.baidu.mapapi.search.route.MassTransitRouteLine.TransitStep.StepVehicleInfoType
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.fragment.MainFragment
import com.navigation.foxizz.data.SchemeItem
import com.navigation.foxizz.mybaidumap.overlayutil.*
import kotlinx.android.synthetic.main.adapter_scheme_item.view.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.util.*

/**
 * 路线规划模块
 *
 * @param mainFragment 地图页
 */
class BaiduRoutePlan(private val mainFragment: MainFragment) {
    companion object {
        const val DRIVING = 1 //驾车
        const val WALKING = 2 //步行
        const val BIKING = 3 //骑行
        const val TRANSIT = 4 //公交
    }

    lateinit var mRoutePlanSearch: RoutePlanSearch //路线规划
    var mSchemeList = ArrayList<SchemeItem>() //方案列表
    var mBusStationLocations = ArrayList<LatLng>() //公交导航所有站点的坐标
    var mEndLocation: LatLng? = null //终点

    init {
        initRoutePlanSearch()
    }

    /**
     * 开始路线规划
     */
    fun startRoutePlanSearch() {
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
            if (mEndLocation == null) {
                showToast(R.string.end_location_is_null)
                return
            }

            //获取定位点和目标点
            val startNode = PlanNode.withLocation(mBaiduLocation.mLatLng)
            val endNode = PlanNode.withLocation(mEndLocation)
            when (mRoutePlanSelect) {
                DRIVING -> mRoutePlanSearch.drivingSearch(
                    DrivingRoutePlanOption()
                        .from(startNode)
                        .to(endNode)
                )
                WALKING -> mRoutePlanSearch.walkingSearch(
                    WalkingRoutePlanOption()
                        .from(startNode)
                        .to(endNode)
                )
                BIKING -> mRoutePlanSearch.bikingSearch(
                    BikingRoutePlanOption()
                        .from(startNode)
                        .to(endNode)
                )
                TRANSIT -> {
                    //加载路线方案
                    include_scheme_loading.visibility = View.VISIBLE
                    recycler_scheme_result.visibility = View.GONE

                    //收回所有展开的方案
                    for (i in 0 until mSchemeList.size) {
                        if (mSchemeList[i].expandFlag) { //如果是展开状态
                            recycler_scheme_result.getChildAt(i)?.run {
                                ll_info_drawer.expandLayout(false)
                                ib_scheme_expand.rotateExpandIcon(180f, 0f) //旋转伸展按钮
                            }
                            mSchemeList[i].expandFlag = false
                            mSchemeAdapter.updateList() //通知adapter更新
                        }
                    }
                    mSchemeList.clear() //清空方案列表
                    mSchemeAdapter.updateList() //通知adapter更新

                    mRoutePlanSearch.masstransitSearch(
                        MassTransitRoutePlanOption()
                            .from(startNode)
                            .to(endNode)
                    )

                    infoFlag = 2 //设置信息状态为交通选择
                    bt_middle.setText(R.string.middle_button3) //设置按钮为交通选择
                    ll_select_layout.expandLayout(false) //收起选择布局
                    schemeExpandFlag = 1 //设置方案布局为方案列表
                    ll_scheme_info_layout.setHeight(0)//设置方案信息布局的高度为0
                    ll_scheme_drawer.expandLayout(true) //展开方案抽屉
                }
                else -> {
                }
            }
        }
    }

    /**
     * 初始化路线规划
     */
    private fun initRoutePlanSearch() {
        //创建路线规划检索实例
        mRoutePlanSearch = RoutePlanSearch.newInstance()

        //创建路线规划检索结果监听器
        val listener: OnGetRoutePlanResultListener = object : OnGetRoutePlanResultListener {
            override fun onGetWalkingRouteResult(walkingRouteResult: WalkingRouteResult) {
                if (walkingRouteResult.routeLines == null
                    || walkingRouteResult.routeLines.size == 0
                ) {
                    showToast(R.string.suggest_not_to_walk)
                    return
                }

                //清空地图上的标记
                mainFragment.mBaiduMap.clear()
                //创建WalkingRouteOverlay实例
                WalkingRouteOverlay(mainFragment.mBaiduMap).run {
                    //为WalkingRouteOverlay实例设置路径数据
                    setData(walkingRouteResult.routeLines[0])
                    //在地图上绘制WalkingRouteOverlay
                    addToMap()
                    //将路线放在最佳视野位置
                    zoomToSpan()
                }
            }

            override fun onGetTransitRouteResult(transitRouteResult: TransitRouteResult) {
                if (transitRouteResult.routeLines == null
                    || transitRouteResult.routeLines.size == 0
                ) {
                    showToast(R.string.suggest_to_walk)
                    return
                }

                //清空地图上的标记
                mainFragment.mBaiduMap.clear()
                //创建TransitRouteOverlay实例
                TransitRouteOverlay(mainFragment.mBaiduMap).run {
                    //为TransitRouteOverlay实例设置路径数据
                    setData(transitRouteResult.routeLines[0])
                    //在地图上绘制TransitRouteOverlay
                    addToMap()
                    //将路线放在最佳视野位置
                    zoomToSpan()
                }
            }

            override fun onGetMassTransitRouteResult(massTransitRouteResult: MassTransitRouteResult) {
                //路线方案加载完成
                mainFragment.include_scheme_loading.visibility = View.GONE
                mainFragment.recycler_scheme_result.visibility = View.VISIBLE
                if (massTransitRouteResult.routeLines == null
                    || massTransitRouteResult.routeLines.size == 0
                ) {
                    showToast(R.string.suggest_to_walk)
                    return
                }
                runOnThread {
                    //所有的路线
                    massTransitRouteResult.routeLines.forEach {
                        val schemeItem = SchemeItem()
                        schemeItem.routeLine = it

                        //获取公交路线信息
                        val allStationInfo = StringBuilder()
                        val simpleInfo = StringBuilder()
                        //每条路线的所有段
                        it.newSteps.forEach {
                            //每一段的所有信息
                            it.forEach {
                                //只收集巴士和长途巴士的信息
                                if (it.vehileType == StepVehicleInfoType.ESTEP_BUS //巴士
                                    || it.vehileType == StepVehicleInfoType.ESTEP_COACH //长途巴士
                                ) {
                                    if (it.busInfo != null) { //巴士
                                        allStationInfo.append(it.busInfo.name)
                                        simpleInfo.append("—", it.busInfo.name)
                                    }
                                    if (it.coachInfo != null) { //长途巴士
                                        allStationInfo.append(it.coachInfo.name)
                                        simpleInfo.append("—", it.coachInfo.name)
                                    }
                                    allStationInfo.append(
                                        "—终点站：",
                                        it.busInfo.arriveStation, "\n"
                                    )
                                }
                            }
                        }
                        schemeItem.simpleInfo = simpleInfo.removeFirst("-")
                        schemeItem.allStationInfo = allStationInfo.toString()

                        //获取详细信息
                        StringBuilder().run {
                            val nowTime = System.currentTimeMillis()
                            val arriveTime = TimeUtil.parse(
                                it.arriveTime,
                                TimeUtil.FORMATION_yMdHms
                            ).time
                            val spendTime = arriveTime - nowTime
                            spendTime.let {
                                if (it < 3 * 60 * 60 * 1000) //小于3小时
                                    append(
                                        getString(R.string.spend_time),
                                        it / 1000 / 60, getString(R.string.minute)
                                    )
                                else append(
                                    getString(R.string.spend_time),
                                    it / 1000 / 60 / 60, getString(R.string.hour),
                                    it / 1000 / 60 % 60, getString(R.string.minute)
                                )
                            }
                            if (it.price > 10)
                                append(
                                    "\n", getString(R.string.budget),
                                    it.price, getString(R.string.yuan)
                                )
                            schemeItem.detailInfo = this.toString()
                        }

                        mSchemeList.add(schemeItem) //添加到列表中
                        mainFragment.mSchemeAdapter.updateList() //通知adapter更新
                        runOnUiThread {
                            startMassTransitRoutePlan(0) //默认选择第一个方案
                        }
                    }
                }
            }

            override fun onGetDrivingRouteResult(drivingRouteResult: DrivingRouteResult) {
                if (drivingRouteResult.routeLines == null
                    || drivingRouteResult.routeLines.size == 0
                ) return

                //清空地图上的标记
                mainFragment.mBaiduMap.clear()
                //创建DrivingRouteOverlay实例
                DrivingRouteOverlay(mainFragment.mBaiduMap).run {
                    //为DrivingRouteOverlay实例设置数据
                    setData(drivingRouteResult.routeLines[0])
                    //在地图上绘制DrivingRouteOverlay
                    addToMap()
                    //将路线放在最佳视野位置
                    zoomToSpan()
                }
            }

            override fun onGetIndoorRouteResult(indoorRouteResult: IndoorRouteResult) {
                if (indoorRouteResult.routeLines == null
                    || indoorRouteResult.routeLines.size == 0
                ) return

                //清空地图上的标记
                mainFragment.mBaiduMap.clear()
                //创建IndoorRouteOverlay实例
                IndoorRouteOverlay(mainFragment.mBaiduMap).run {
                    //为IndoorRouteOverlay实例设置数据
                    setData(indoorRouteResult.routeLines[0])
                    //在地图上绘制IndoorRouteOverlay
                    addToMap()
                    //将路线放在最佳视野位置
                    zoomToSpan()
                }
            }

            override fun onGetBikingRouteResult(bikingRouteResult: BikingRouteResult) {
                if (bikingRouteResult.routeLines == null
                    || bikingRouteResult.routeLines.size == 0
                ) {
                    showToast(R.string.suggest_not_to_bike)
                    return
                }

                //清空地图上的标记
                mainFragment.mBaiduMap.clear()
                //创建BikingRouteOverlay实例
                BikingRouteOverlay(mainFragment.mBaiduMap).run {
                    //为BikingRouteOverlay实例设置数据
                    setData(bikingRouteResult.routeLines[0])
                    //在地图上绘制BikingRouteOverlay
                    addToMap()
                    //将路线放在最佳视野位置
                    zoomToSpan()
                }
            }
        }

        //设置路线规划检索监听器
        mRoutePlanSearch.setOnGetRoutePlanResultListener(listener)
    }

    /**
     * 开始跨城公交路线规划
     */
    fun startMassTransitRoutePlan(index: Int) {
        val schemeItem = mSchemeList[index]

        mainFragment.run {
            //设置方案信息
            @SuppressLint("SetTextI18n")
            tv_scheme_info.text = schemeItem.allStationInfo + schemeItem.detailInfo

            //构建Marker图标
            val bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_to_location)

            //清空临时保存的公交站点信息
            mBusStationLocations.clear()

            /*
             * getRouteLines(): 所有规划好的路线
             * get(0): 第1条规划好的路线
             *
             * getNewSteps():
             * 起终点为同城时，该list表示一个step中的多个方案scheme（方案1、方案2、方案3...）
             * 起终点为跨城时，该list表示一个step中多个子步骤sub_step（如：步行->公交->火车->步行）
             *
             * get(0): 方案1或第1步
             * get(0): 步行到第1站点
             * getEndLocation(): 终点站，即步行导航的终点站
             */
            //获取所有站点信息
            schemeItem.routeLine?.newSteps?.forEach {
                it.forEach {
                    //将获取到的站点信息临时保存
                    mBusStationLocations.add(it.endLocation)

                    //构建MarkerOption，用于在地图上添加Marker
                    val option = MarkerOptions()
                        .position(it.endLocation)
                        .icon(bitmap)

                    //在地图上添加Marker，并显示
                    mBaiduMap.addOverlay(option)
                }
            }

            try {
                //创建MassTransitRouteOverlay实例
                MassTransitRouteOverlay(mBaiduMap).run {
                    if (schemeItem.routeLine != null) {
                        //清空地图上的所有标记点和绘制的路线
                        mBaiduMap.clear()
                        //为MassTransitRouteOverlay设置数据
                        setData(schemeItem.routeLine!!)
                        //在地图上绘制Overlay
                        addToMap()
                        //将路线放在最佳视野位置
                        zoomToSpan()
                    }
                }
            } catch (e: Exception) {
                showToast(R.string.draw_route_fail)
            }
        }
    }
}