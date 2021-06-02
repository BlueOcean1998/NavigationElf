package com.navigation.foxizz.mybaidumap.overlayutil

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.DrivingRouteLine
import java.util.*

/**
 * 用于显示一条驾车路线的overlay，
 * 自3.4.0版本起可实例化多个添加在地图中显示，当数据中包含路况数据时，则默认使用路况纹理分段绘制
 *
 * @param baiduMap 百度地图
 */
class DrivingRouteOverlay(baiduMap: BaiduMap) : OverlayManager(baiduMap) {
    private lateinit var mRouteLine: DrivingRouteLine

    /**
     * 设置路线数据
     *
     * @param routeLine 路线数据
     */
    fun setData(routeLine: DrivingRouteLine) {
        mRouteLine = routeLine
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
    private val terminalMarker: BitmapDescriptor =
        BitmapDescriptorFactory.fromAssetWithDpi("Icon_end.png")

    //poly line
    //step node
    override val overlayOptions: List<OverlayOptions>
        get() {
            val overlayOptionses = ArrayList<OverlayOptions>()

            //step node
            if (mRouteLine.allStep != null && mRouteLine.allStep.size > 0) {
                mRouteLine.allStep.forEach {
                    val bundle = Bundle()
                    bundle.putInt("index", mRouteLine.allStep.indexOf(it))
                    if (it.entrance != null) {
                        overlayOptionses.add(
                            MarkerOptions()
                                .position(it.entrance.location)
                                .anchor(0.5f, 0.5f)
                                .zIndex(10)
                                .rotate((360 - it.direction).toFloat())
                                .extraInfo(bundle)
                                .icon(BitmapDescriptorFactory.fromAssetWithDpi("Icon_line_node.png"))
                        )
                    }
                    //最后路段绘制出口点
                    if (mRouteLine.allStep.indexOf(it) == mRouteLine.allStep.size - 1
                        && it.exit != null
                    ) {
                        overlayOptionses.add(
                            MarkerOptions()
                                .position(it.exit.location)
                                .anchor(0.5f, 0.5f)
                                .zIndex(10)
                                .icon(BitmapDescriptorFactory.fromAssetWithDpi("Icon_line_node.png"))
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

            //poly line
            if (mRouteLine.allStep != null && mRouteLine.allStep.size > 0) {
                val steps = mRouteLine.allStep
                val stepNum = steps.size
                val points = ArrayList<LatLng>()
                val traffics = ArrayList<Int>()
                var totalTraffic = 0
                for (i in 0 until stepNum) {
                    if (i == stepNum - 1) {
                        points.addAll(steps[i].wayPoints)
                    } else {
                        points.addAll(
                            steps[i].wayPoints
                                .subList(0, steps[i].wayPoints.size - 1)
                        )
                    }
                    totalTraffic += steps[i].wayPoints.size - 1
                    if (steps[i].trafficList != null
                        && steps[i].trafficList.isNotEmpty()
                    ) {
                        for (j in steps[i].trafficList.indices) {
                            traffics.add(steps[i].trafficList[j])
                        }
                    }
                }
                /*
                val indexList = Bundle()
                if (traffics.size > 0) {
                    val traffic = IntArray(traffics.size)
                    var index = 0;
                    traffics.forEach {
                        traffic[index] = it
                        index++;
                    }
                    indexList.putIntArray("indexes", traffic)
                }
                */
                var isDotLine = false
                if (traffics.size > 0) {
                    isDotLine = true
                }
                val option = PolylineOptions()
                    .points(points)
                    .textureIndex(traffics)
                    .width(7)
                    .dottedLine(isDotLine)
                    .focus(true)
                    .color(
                        if (lineColor != 0) lineColor
                        else Color.argb(178, 0, 78, 255)
                    )
                    .zIndex(0)
                if (isDotLine) {
                    option.customTextureList(customTextureList)
                }
                overlayOptionses.add(option)
            }
            return overlayOptionses
        }

    private val customTextureList: List<BitmapDescriptor>
        get() {
            return ArrayList<BitmapDescriptor>().apply {
                add(BitmapDescriptorFactory.fromAsset("Icon_road_blue_arrow.png"))
                add(BitmapDescriptorFactory.fromAsset("Icon_road_green_arrow.png"))
                add(BitmapDescriptorFactory.fromAsset("Icon_road_yellow_arrow.png"))
                add(BitmapDescriptorFactory.fromAsset("Icon_road_red_arrow.png"))
                add(BitmapDescriptorFactory.fromAsset("Icon_road_nofocus.png"))
            }
        }

    /**
     * 覆写此方法以改变默认点击处理
     *
     * @param i 线路节点的 index
     * @return 是否处理了该点击事件
     */
    private fun onRouteNodeClick(i: Int): Boolean {
        if (mRouteLine.allStep != null && mRouteLine.allStep[i] != null) {
            Log.i("baidumapsdk", "DrivingRouteOverlay onRouteNodeClick")
        }
        return false
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        mOverlayList.forEach {
            if (it is Marker && it == marker) {
                if (marker.extraInfo != null) {
                    onRouteNodeClick(marker.extraInfo.getInt("index"))
                }
            }
        }
        return true
    }

    override fun onPolylineClick(polyline: Polyline): Boolean {
        var flag = false
        mOverlayList.forEach {
            if (it is Polyline && it == polyline) {
                //选中
                flag = true
                return@forEach
            }
        }
        setFocus(flag)
        return flag
    }

    private fun setFocus(flag: Boolean) {
        mOverlayList.forEach {
            if (it is Polyline) {
                //选中
                it.isFocus = flag
                return
            }
        }
    }
}