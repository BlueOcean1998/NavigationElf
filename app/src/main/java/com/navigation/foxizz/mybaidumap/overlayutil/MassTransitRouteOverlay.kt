/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.navigation.foxizz.mybaidumap.overlayutil

import android.graphics.Color
import android.os.Bundle
import com.baidu.mapapi.map.*
import com.baidu.mapapi.search.route.MassTransitRouteLine
import com.baidu.mapapi.search.route.MassTransitRouteLine.TransitStep.StepVehicleInfoType
import java.util.*

/**
 * 构造函数
 *
 * @param baiduMap 该TransitRouteOverlay引用的 BaiduMap 对象
 */
class MassTransitRouteOverlay(baiduMap: BaiduMap) : OverlayManager(baiduMap) {
    private lateinit var mRouteLine: MassTransitRouteLine

    /**
     * 设置路线数据
     *
     * @param routeOverlay 路线数据
     */
    fun setData(routeOverlay: MassTransitRouteLine) {
        mRouteLine = routeOverlay
    }

    /**
     * 覆写此方法以改变默认绘制颜色
     *
     * @return 线颜色
     */
    private val lineColor = 0

    /**
     * 覆写此方法以改变默认起点图标
     *
     * @return 起点图标
     */
    private val startMarker: BitmapDescriptor =
        BitmapDescriptorFactory.fromAssetWithDpi("Icon_start.png")

    private var isSameCity = false

    fun setSameCity(sameCity: Boolean) {
        isSameCity = sameCity
    }

    /**
     * 覆写此方法以改变默认终点图标
     *
     * @return 终点图标
     */
    private val terminalMarker: BitmapDescriptor =
        BitmapDescriptorFactory.fromAssetWithDpi("Icon_end.png")

    //step node
    override val overlayOptions: List<OverlayOptions>
        get() {
            val overlayOptionses = ArrayList<OverlayOptions>()
            val steps = mRouteLine.newSteps
            if (isSameCity) {
                //同城 （同城时，每个steps的get(i)对应的List是一条step的不同方案，此处都选第一条进行绘制，即get(0)）

                //step node
                for (i in steps.indices) {
                    val step = steps[i][0]
                    val b = Bundle()
                    b.putInt("index", i + 1)
                    if (step.startLocation != null) {
                        overlayOptionses.add(MarkerOptions()
                            .position(step.startLocation)
                            .anchor(0.5f, 0.5f)
                            .zIndex(10)
                            .extraInfo(b)
                            .icon(getIconForStep(step)))
                    }

                    //最后一个终点
                    if (i == steps.size - 1 && step.endLocation != null) {
                        overlayOptionses.add(MarkerOptions()
                            .position(step.endLocation)
                            .anchor(0.5f, 0.5f)
                            .zIndex(10)
                            .icon(getIconForStep(step)))
                    }
                }

                //polyline
                for (i in steps.indices) {
                    val step = steps[i][0]
                    val color: Int = if (step.vehileType != StepVehicleInfoType.ESTEP_WALK) {
                        //color = Color.argb(178, 0, 78, 255);
                        if (lineColor != 0) lineColor
                        else Color.argb(178, 0, 78, 255)
                    } else {
                        //color = Color.argb(178, 88, 208, 0);
                        if (lineColor != 0) lineColor
                        else Color.argb(178, 88, 208, 0)
                    }
                    overlayOptionses.add(PolylineOptions()
                        .points(step.wayPoints)
                        .width(10)
                        .color(color)
                        .zIndex(0))
                }
            } else {
                //跨城 （跨城时，每个steps的get(i)对应的List是一条step的子路线sub_step，需要将它们全部拼接才是一条完整路线）
                var stepSum = 0
                for (i in steps.indices) {
                    stepSum += steps[i].size
                }

                //step node
                var k = 1
                for (i in steps.indices) {
                    for (j in steps[i].indices) {
                        val step = steps[i][j]
                        val b = Bundle()
                        b.putInt("index", k)
                        if (step.startLocation != null) {
                            overlayOptionses.add(MarkerOptions()
                                .position(step.startLocation)
                                .anchor(0.5f, 0.5f)
                                .zIndex(10)
                                .extraInfo(b)
                                .icon(getIconForStep(step)))
                        }

                        //最后一个终点
                        if (k == stepSum && step.endLocation != null) {
                            overlayOptionses.add(MarkerOptions()
                                .position(step.endLocation)
                                .anchor(0.5f, 0.5f)
                                .zIndex(10)
                                .icon(getIconForStep(step)))
                        }
                        k++
                    }
                }

                //polyline
                for (i in steps.indices) {
                    for (j in steps[i].indices) {
                        val step = steps[i][j]
                        val color: Int = if (step.vehileType != StepVehicleInfoType.ESTEP_WALK) {
                            //color = Color.argb(178, 0, 78, 255);
                            if (lineColor != 0) lineColor
                            else Color.argb(178, 0, 78, 255)
                        } else {
                            //color = Color.argb(178, 88, 208, 0);
                            if (lineColor != 0) lineColor
                            else Color.argb(178, 88, 208, 0)
                        }
                        if (step.wayPoints != null) {
                            overlayOptionses.add(PolylineOptions()
                                .points(step.wayPoints)
                                .width(10)
                                .color(color)
                                .zIndex(0))
                        }
                    }
                }
            }

            //起点
            if (mRouteLine.starting != null && mRouteLine.starting.location != null) {
                overlayOptionses.add(MarkerOptions()
                    .position(mRouteLine.starting.location)
                    .icon(startMarker)
                    .zIndex(10))
            }

            //终点
            if (mRouteLine.terminal != null && mRouteLine.terminal.location != null) {
                overlayOptionses.add(MarkerOptions()
                    .position(mRouteLine.terminal.location)
                    .icon(terminalMarker)
                    .zIndex(10))
            }
            return overlayOptionses
        }

    private fun getIconForStep(step: MassTransitRouteLine.TransitStep): BitmapDescriptor? {
        return when (step.vehileType) {
            StepVehicleInfoType.ESTEP_WALK ->
                BitmapDescriptorFactory.fromAssetWithDpi("Icon_walk_route.png")
            StepVehicleInfoType.ESTEP_TRAIN ->
                BitmapDescriptorFactory.fromAssetWithDpi("Icon_subway_station.png")
            StepVehicleInfoType.ESTEP_DRIVING,
            StepVehicleInfoType.ESTEP_COACH,
            StepVehicleInfoType.ESTEP_PLANE,
            StepVehicleInfoType.ESTEP_BUS,
            -> BitmapDescriptorFactory.fromAssetWithDpi("Icon_bus_station.png")
            else -> null
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        return false
    }

    override fun onPolylineClick(polyline: Polyline): Boolean {
        return false
    }
}