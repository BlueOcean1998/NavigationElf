/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.navigation.foxizz.mybaidumap.overlayutil

import android.graphics.Color
import android.os.Bundle
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.IndoorRouteLine
import java.util.*

class IndoorRouteOverlay(baiduMap: BaiduMap) : OverlayManager(baiduMap) {
    private lateinit var mRouteLine: IndoorRouteLine

    /**
     * 设置路线数据
     *
     * @param line 路线数据
     */
    fun setData(line: IndoorRouteLine) {
        mRouteLine = line
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
    private var startMarker: BitmapDescriptor =
            BitmapDescriptorFactory.fromAssetWithDpi("Icon_start.png")

    /**
     * 覆写此方法以改变默认终点图标
     *
     * @return 终点图标
     */
    private var terminalMarker: BitmapDescriptor =
            BitmapDescriptorFactory.fromAssetWithDpi("Icon_end.png")

    private var colorInfo: IntArray = intArrayOf(
            Color.argb(178, 0, 78, 255),
            Color.argb(178, 88, 208, 0),
            Color.argb(178, 88, 78, 255))

    // 添加线poly line list
    // 添加step的节点
    override val overlayOptions: List<OverlayOptions>
        get() {
            val overlayList = ArrayList<OverlayOptions>()

            // 添加step的节点
            if (mRouteLine.allStep != null && mRouteLine.allStep.size > 0) {
                for (step in mRouteLine.allStep) {
                    val b = Bundle()
                    b.putInt("index", mRouteLine.allStep.indexOf(step))
                    if (step.entrace != null) {
                        overlayList.add(MarkerOptions()
                                .position(step.entrace.location)
                                .zIndex(10)
                                .anchor(0.5f, 0.5f)
                                .extraInfo(b)
                                .icon(BitmapDescriptorFactory
                                        .fromAssetWithDpi("Icon_walk_route.png")))
                    }

                    // 最后路段绘制出口点
                    if (mRouteLine.allStep.indexOf(step) == mRouteLine.allStep.size - 1
                            && step.exit != null) {
                        overlayList.add(MarkerOptions()
                                .position(step.exit.location)
                                .anchor(0.5f, 0.5f)
                                .zIndex(10)
                                .icon(BitmapDescriptorFactory
                                        .fromAssetWithDpi("Icon_walk_route.png")))
                    }
                }
            }

            // 添加起点starting
            if (mRouteLine.starting != null) {
                overlayList.add(MarkerOptions()
                        .position(mRouteLine.starting.location)
                        .icon(startMarker)
                        .zIndex(10))
            }

            // 添加终点terminal
            if (mRouteLine.terminal != null) {
                overlayList.add(MarkerOptions()
                        .position(mRouteLine.terminal.location)
                        .icon(terminalMarker)
                        .zIndex(10))
            }

            // 添加线poly line list
            if (mRouteLine.allStep != null && mRouteLine.allStep.size > 0) {
                var lastStepLastPoint: LatLng? = null
                var idex = 0
                for (step in mRouteLine.allStep) {
                    val watPoints = step.wayPoints
                    if (watPoints != null) {
                        val points = ArrayList<LatLng>()
                        if (lastStepLastPoint != null) {
                            points.add(lastStepLastPoint)
                        }
                        points.addAll(watPoints)
                        overlayList.add(PolylineOptions()
                                .points(points)
                                .width(10)
                                .color(if (lineColor != 0) lineColor else colorInfo[idex++ % 3]).zIndex(0))
                        lastStepLastPoint = watPoints[watPoints.size - 1]
                    }
                }
            }
            return overlayList
        }

    override fun onMarkerClick(marker: Marker): Boolean {
        return false
    }

    override fun onPolylineClick(polyline: Polyline): Boolean {
        return false
    }

    /*
    fun getIconForStep(step: IndoorRouteLine.TransitStep): BitmapDescriptor? {
        return when (step.getVehileType()) {
            ESTEP_WALK ->
                BitmapDescriptorFactory.fromAssetWithDpi("Icon_walk_route.png")
            ESTEP_TRAIN ->
                BitmapDescriptorFactory.fromAssetWithDpi("Icon_subway_station.png")
            ESTEP_DRIVING, ESTEP_COACH, ESTEP_PLANE, ESTEP_BUS ->
                BitmapDescriptorFactory.fromAssetWithDpi("Icon_walk_route.png")
            else -> null
        }
    }
    */

}