package com.navigation.foxizz.mybaidumap.overlayutil

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.baidu.mapapi.map.*
import com.baidu.mapapi.search.route.TransitRouteLine
import com.baidu.mapapi.search.route.TransitRouteLine.TransitStep.TransitRouteStepType
import java.util.*

/**
 * 用于显示换乘路线的Overlay，
 * 自3.4.0版本起可实例化多个添加在地图中显示
 *
 * @param baiduMap 百度地图
 */
class TransitRouteOverlay(baiduMap: BaiduMap) : OverlayManager(baiduMap) {
    private lateinit var mRouteLine: TransitRouteLine

    /**
     * 设置路线数据
     *
     * @param routeOverlay 路线数据
     */
    fun setData(routeOverlay: TransitRouteLine) {
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

    /**
     * 覆写此方法以改变默认终点图标
     *
     * @return 终点图标
     */
    private val terminalMarker: BitmapDescriptor =
        BitmapDescriptorFactory.fromAssetWithDpi("Icon_end.png")

    //polyline
    //step node
    override val overlayOptions: List<OverlayOptions>
        get() {
            val overlayOptionses = ArrayList<OverlayOptions>()

            //step node
            if (mRouteLine.allStep != null && mRouteLine.allStep.size > 0) {
                for (step in mRouteLine.allStep) {
                    val b = Bundle()
                    b.putInt("index", mRouteLine.allStep.indexOf(step))
                    if (step.entrance != null) {
                        overlayOptionses.add(
                            MarkerOptions()
                                .position(step.entrance.location)
                                .anchor(0.5f, 0.5f)
                                .zIndex(10)
                                .extraInfo(b)
                                .icon(getIconForStep(step))
                        )
                    }

                    //最后路段绘制出口点
                    if (mRouteLine.allStep.indexOf(step) == mRouteLine
                            .allStep.size - 1 && step.exit != null
                    ) {
                        overlayOptionses.add(
                            MarkerOptions()
                                .position(step.exit.location)
                                .anchor(0.5f, 0.5f)
                                .zIndex(10)
                                .icon(getIconForStep(step))
                        )
                    }
                }
            }
            if (mRouteLine.starting != null) {
                overlayOptionses.add(
                    MarkerOptions()
                        .position(mRouteLine.starting.location)
                        .icon(startMarker)
                        .zIndex(10)
                )
            }
            if (mRouteLine.terminal != null) {
                overlayOptionses.add(
                    MarkerOptions()
                        .position(mRouteLine.terminal.location)
                        .icon(terminalMarker)
                        .zIndex(10)
                )
            }

            //polyline
            if (mRouteLine.allStep != null && mRouteLine.allStep.size > 0) {
                for (step in mRouteLine.allStep) {
                    if (step.wayPoints == null) {
                        continue
                    }
                    val color: Int = if (step.stepType != TransitRouteStepType.WAKLING) {
                        if (lineColor != 0) lineColor else Color.argb(178, 0, 78, 255)
                    } else {
                        if (lineColor != 0) lineColor else Color.argb(178, 88, 208, 0)
                    }
                    overlayOptionses.add(
                        PolylineOptions()
                            .points(step.wayPoints)
                            .width(10)
                            .color(color)
                            .zIndex(0)
                    )
                }
            }
            return overlayOptionses
        }

    private fun getIconForStep(step: TransitRouteLine.TransitStep): BitmapDescriptor? {
        return when (step.stepType) {
            TransitRouteStepType.BUSLINE ->
                BitmapDescriptorFactory.fromAssetWithDpi("Icon_bus_station.png")
            TransitRouteStepType.SUBWAY ->
                BitmapDescriptorFactory.fromAssetWithDpi("Icon_subway_station.png")
            TransitRouteStepType.WAKLING ->
                BitmapDescriptorFactory.fromAssetWithDpi("Icon_walk_route.png")
            else -> null
        }
    }

    /**
     * 覆写此方法以改变起默认点击行为
     *
     * @param i 被点击的step在
     * [com.baidu.mapapi.search.route.TransitRouteLine.getAllStep]
     * 中的索引
     * @return 是否处理了该点击事件
     */
    private fun onRouteNodeClick(i: Int): Boolean {
        if (mRouteLine.allStep != null && mRouteLine.allStep[i] != null) {
            Log.i("baidumapsdk", "TransitRouteOverlay onRouteNodeClick")
        }
        return false
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        for (mMarker in mOverlayList) {
            if (mMarker is Marker && mMarker == marker) {
                if (marker.extraInfo != null) {
                    onRouteNodeClick(marker.extraInfo.getInt("index"))
                }
            }
        }
        return true
    }

    override fun onPolylineClick(polyline: Polyline): Boolean {
        return false
    }
}