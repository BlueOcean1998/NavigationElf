package com.navigation.foxizz.mybaidumap

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.OverlayOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.*
import com.baidu.mapapi.search.route.MassTransitRouteLine.TransitStep.StepVehicleInfoType
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.fragment.MainFragment
import com.navigation.foxizz.data.SchemeItem
import com.navigation.foxizz.mybaidumap.overlayutil.*
import com.navigation.foxizz.util.*
import java.util.*

/**
 * 路线规划模块
 */
class BaiduRoutePlan(private val mainFragment: MainFragment) {
    //交通选择
    companion object {
        const val DRIVING = 1 //驾车
        const val WALKING = 2 //步行
        const val BIKING = 3 //骑行
        const val TRANSIT = 4 //公交
    }

    lateinit var mRoutePlanSearch: RoutePlanSearch
    var mBusStationLocations = ArrayList<LatLng>() //公交导航所有站点的坐标
    var mEndLocation: LatLng? = null //终点

    /**
     * 开始路线规划
     */
    fun startRoutePlanSearch() {
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
        if (mEndLocation == null) {
            R.string.end_location_is_null.showToast()
            return
        }

        //获取定位点和目标点
        val startNode = PlanNode.withLocation(mainFragment.mBaiduLocation.mLatLng)
        val endNode = PlanNode.withLocation(mEndLocation)
        when (mainFragment.mRoutePlanSelect) {
            DRIVING -> mRoutePlanSearch
                    .drivingSearch(DrivingRoutePlanOption()
                            .from(startNode)
                            .to(endNode))
            WALKING -> mRoutePlanSearch
                    .walkingSearch(WalkingRoutePlanOption()
                            .from(startNode)
                            .to(endNode))
            BIKING -> mRoutePlanSearch
                    .bikingSearch(BikingRoutePlanOption()
                            .from(startNode)
                            .to(endNode))
            TRANSIT -> {
                //加载路线方案
                mainFragment.llSchemeLoading.visibility = View.VISIBLE
                mainFragment.recyclerSchemeResult.visibility = View.GONE

                //收回所有展开的方案
                var i = 0
                while (i < mainFragment.mSchemeList.size) {
                    //遍历所有item
                    if (mainFragment.mSchemeList[i].expandFlag) { //如果是展开状态
                        //用layoutManager找到相应的item
                        val view = mainFragment.mSchemeLayoutManager.findViewByPosition(i)
                        if (view != null) {
                            val infoDrawer = view.findViewById<LinearLayout>(R.id.ll_info_drawer)
                            val schemeExpand = view.findViewById<ImageButton>(R.id.ib_scheme_expand)
                            LayoutUtil.expandLayout(infoDrawer, false)
                            LayoutUtil.rotateExpandIcon(schemeExpand, 180f, 0f) //旋转伸展按钮
                            mainFragment.mSchemeList[i].expandFlag = false
                            mainFragment.mSchemeAdapter.updateList() //通知adapter更新
                        }
                    }
                    i++
                }
                mainFragment.mSchemeList.clear() //清空方案列表
                mainFragment.mSchemeAdapter.updateList() //通知adapter更新
                mRoutePlanSearch
                        .masstransitSearch(MassTransitRoutePlanOption()
                                .from(startNode)
                                .to(endNode))

                mainFragment.infoFlag = 2 //设置信息状态为交通选择
                mainFragment.btMiddle.setText(R.string.middle_button3) //设置按钮为交通选择
                LayoutUtil.expandLayout(mainFragment.llSelectLayout, false) //收起选择布局
                mainFragment.schemeExpandFlag = 1 //设置方案布局为方案列表
                LayoutUtil.setViewHeight(mainFragment.llSchemeInfoLayout, 0)//设置方案信息布局的高度为0
                LayoutUtil.expandLayout(mainFragment.llSchemeDrawer, true) //展开方案抽屉
            }
        }
    }

    /**
     * 初始化路线规划
     */
    fun initRoutePlanSearch() {
        //创建路线规划检索实例
        mRoutePlanSearch = RoutePlanSearch.newInstance()

        //创建路线规划检索结果监听器
        val listener: OnGetRoutePlanResultListener = object : OnGetRoutePlanResultListener {
            override fun onGetWalkingRouteResult(walkingRouteResult: WalkingRouteResult) {
                if (walkingRouteResult.routeLines == null
                        || walkingRouteResult.routeLines.size == 0) {
                    R.string.suggest_not_to_walk.showToast()
                    return
                }

                //创建WalkingRouteOverlay实例
                val overlay = WalkingRouteOverlay(mainFragment.mBaiduMap)
                //清空地图上的标记
                mainFragment.mBaiduMap.clear()
                //获取路径规划数据,(以返回的第一条数据为例)
                //为WalkingRouteOverlay实例设置路径数据
                overlay.setData(walkingRouteResult.routeLines[0])
                //在地图上绘制WalkingRouteOverlay
                overlay.addToMap()
                //将路线放在最佳视野位置
                overlay.zoomToSpan()
            }

            override fun onGetTransitRouteResult(transitRouteResult: TransitRouteResult) {
                if (transitRouteResult.routeLines == null
                        || transitRouteResult.routeLines.size == 0) {
                    R.string.suggest_to_walk.showToast()
                    return
                }

                //创建TransitRouteOverlay实例
                val overlay = TransitRouteOverlay(mainFragment.mBaiduMap)
                //清空地图上的标记
                mainFragment.mBaiduMap.clear()
                //获取路径规划数据,(以返回的第一条数据为例)
                //为TransitRouteOverlay实例设置路径数据
                overlay.setData(transitRouteResult.routeLines[0])
                //在地图上绘制TransitRouteOverlay
                overlay.addToMap()
                //将路线放在最佳视野位置
                overlay.zoomToSpan()
            }

            override fun onGetMassTransitRouteResult(massTransitRouteResult: MassTransitRouteResult) {
                //路线方案加载完成
                mainFragment.llSchemeLoading.visibility = View.GONE
                mainFragment.recyclerSchemeResult.visibility = View.VISIBLE
                if (massTransitRouteResult.routeLines == null
                        || massTransitRouteResult.routeLines.size == 0) {
                    R.string.suggest_to_walk.showToast()
                    return
                }
                ThreadUtil.execute {
                    //所有的路线
                    for (massTransitRouteLine in massTransitRouteResult.routeLines) {
                        val schemeItem = SchemeItem()
                        schemeItem.routeLine = massTransitRouteLine

                        //获取公交路线信息
                        val allStationInfo = StringBuilder()
                        var simpleInfo = StringBuilder()
                        //每条路线的所有段
                        for (transitSteps in massTransitRouteLine.newSteps) {
                            //每一段的所有信息
                            for (transitStep in transitSteps) {
                                //只收集巴士和长途巴士的信息
                                if (transitStep.vehileType ==  //巴士
                                        StepVehicleInfoType.ESTEP_BUS
                                        || transitStep.vehileType ==  //长途巴士
                                        StepVehicleInfoType.ESTEP_COACH) {
                                    if (transitStep.busInfo != null) { //巴士
                                        simpleInfo.append("—").append(transitStep.busInfo.name)
                                        allStationInfo.append(transitStep.busInfo.name)
                                    }
                                    if (transitStep.coachInfo != null) { //长途巴士
                                        simpleInfo.append("—").append(transitStep.coachInfo.name)
                                        allStationInfo.append(transitStep.coachInfo.name)
                                    }
                                    //终点站
                                    allStationInfo.append("—终点站：").append(
                                            transitStep.busInfo.arriveStation).append("\n")
                                }
                            }
                        }
                        simpleInfo = StringBuilder(simpleInfo.substring(1))
                        schemeItem.simpleInfo = simpleInfo.toString()
                        schemeItem.allStationInfo = allStationInfo.toString()

                        //获取详细信息
                        val detailInfo = StringBuilder()
                        var spendTime: Long
                        val nowTime = System.currentTimeMillis()
                        val arriveTime = TimeUtil.parse(massTransitRouteLine.arriveTime,
                                TimeUtil.FORMATION_yMdHms).time
                        spendTime = arriveTime - nowTime
                        if (spendTime < 3 * 60 * 60 * 1000) { //小于3小时
                            detailInfo.append(mainFragment.getString(R.string.spend_time))
                                    .append(spendTime / 1000 / 60).append(mainFragment.getString(R.string.minute))
                        } else {
                            detailInfo.append(mainFragment.getString(R.string.spend_time))
                                    .append(spendTime / 1000 / 60 / 60).append(mainFragment.getString(R.string.hour))
                                    .append(spendTime / 1000 / 60 % 60).append(mainFragment.getString(R.string.minute))
                        }
                        if (massTransitRouteLine.price > 10) {
                            detailInfo.append("\n")
                                    .append(mainFragment.getString(R.string.budget))
                                    .append(massTransitRouteLine.price.toInt()).append(mainFragment.getString(R.string.yuan))
                        }
                        schemeItem.detailInfo = detailInfo.toString()
                        mainFragment.mSchemeList.add(schemeItem) //添加到列表中
                        mainFragment.mSchemeAdapter.updateList() //通知adapter更新
                        mainFragment.requireActivity().runOnUiThread {
                            startMassTransitRoutePlan(0) //默认选择第一个方案
                        }
                    }
                }
            }

            override fun onGetDrivingRouteResult(drivingRouteResult: DrivingRouteResult) {
                if (drivingRouteResult.routeLines == null
                        || drivingRouteResult.routeLines.size == 0) return

                //创建DrivingRouteOverlay实例
                val overlay = DrivingRouteOverlay(mainFragment.mBaiduMap)
                //清空地图上的标记
                mainFragment.mBaiduMap.clear()
                //获取路径规划数据,(以返回的第一条路线为例）
                //为DrivingRouteOverlay实例设置数据
                overlay.setData(drivingRouteResult.routeLines[0])
                //在地图上绘制DrivingRouteOverlay
                overlay.addToMap()
                //将路线放在最佳视野位置
                overlay.zoomToSpan()
            }

            override fun onGetIndoorRouteResult(indoorRouteResult: IndoorRouteResult) {
                if (indoorRouteResult.routeLines == null
                        || indoorRouteResult.routeLines.size == 0) return

                //创建IndoorRouteOverlay实例
                val overlay = IndoorRouteOverlay(mainFragment.mBaiduMap)
                //清空地图上的标记
                mainFragment.mBaiduMap.clear()
                //获取室内路径规划数据（以返回的第一条路线为例）
                //为IndoorRouteOverlay实例设置数据
                overlay.setData(indoorRouteResult.routeLines[0])
                //在地图上绘制IndoorRouteOverlay
                overlay.addToMap()
                //将路线放在最佳视野位置
                overlay.zoomToSpan()
            }

            override fun onGetBikingRouteResult(bikingRouteResult: BikingRouteResult) {
                if (bikingRouteResult.routeLines == null
                        || bikingRouteResult.routeLines.size == 0) {
                    R.string.suggest_not_to_bike.showToast()
                    return
                }

                //创建BikingRouteOverlay实例
                val overlay = BikingRouteOverlay(mainFragment.mBaiduMap)
                //清空地图上的标记
                mainFragment.mBaiduMap.clear()
                //获取路径规划数据,(以返回的第一条路线为例）
                //为BikingRouteOverlay实例设置数据
                overlay.setData(bikingRouteResult.routeLines[0])
                //在地图上绘制BikingRouteOverlay
                overlay.addToMap()
                //将路线放在最佳视野位置
                overlay.zoomToSpan()
            }
        }

        //设置路线规划检索监听器
        mRoutePlanSearch.setOnGetRoutePlanResultListener(listener)
    }

    /**
     * 开始跨城公交路线规划
     */
    @SuppressLint("SetTextI18n")
    fun startMassTransitRoutePlan(index: Int) {
        val schemeItem = mainFragment.mSchemeList[index]

        //设置方案信息
        mainFragment.tvSchemeInfo.text = schemeItem.allStationInfo + schemeItem.detailInfo

        //创建MassTransitRouteOverlay实例
        val overlay = MassTransitRouteOverlay(mainFragment.mBaiduMap)

        //清空地图上的所有标记点和绘制的路线
        mainFragment.mBaiduMap.clear()
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
        for (transitSteps in schemeItem.routeLine?.newSteps!!) {
            for (transitStep in transitSteps) {
                //将获取到的站点信息临时保存
                mBusStationLocations.add(transitStep.endLocation)

                //构建MarkerOption，用于在地图上添加Marker
                val option: OverlayOptions = MarkerOptions()
                        .position(transitStep.endLocation)
                        .icon(bitmap)

                //在地图上添加Marker，并显示
                mainFragment.mBaiduMap.addOverlay(option)
            }
        }

        try {
            //获取路线规划数据
            //为MassTransitRouteOverlay设置数据
            overlay.setData(schemeItem.routeLine!!)
            //在地图上绘制Overlay
            overlay.addToMap()
            //将路线放在最佳视野位置
            overlay.zoomToSpan()
        } catch (ignored: Exception) {
            R.string.draw_route_fail.showToast()
        }
    }
}