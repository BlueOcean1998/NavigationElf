package com.navigation.foxizz.mybaidumap.overlayutil

import com.baidu.mapapi.map.*
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener
import com.baidu.mapapi.map.BaiduMap.OnPolylineClickListener
import com.baidu.mapapi.model.LatLngBounds

/**
 * 该类提供一个能够显示和管理多个Overlay的基类
 *
 *
 * 复写[.getOverlayOptions] 设置欲显示和管理的Overlay列表
 *
 *
 *
 * 通过
 * [BaiduMap.setOnMarkerClickListener]
 * 将覆盖物点击事件传递给OverlayManager后，OverlayManager才能响应点击事件。
 *
 *
 * 复写[.onMarkerClick] 处理Marker点击事件
 *
 */
abstract class OverlayManager(private val mBaiduMap: BaiduMap) : OnMarkerClickListener,
    OnPolylineClickListener {
    var mOverlayList = ArrayList<Overlay>()

    private var mOverlayOptionList = ArrayList<OverlayOptions>()

    /**
     * 覆写此方法设置要管理的Overlay列表
     *
     * @return 管理的Overlay列表
     */
    abstract val overlayOptions: List<OverlayOptions>

    /**
     * 将所有Overlay 添加到地图上
     */
    fun addToMap() {
        removeFromMap()
        val overlayOptions = overlayOptions
        mOverlayOptionList.addAll(overlayOptions)
        for (option in mOverlayOptionList) {
            mOverlayList.add(mBaiduMap.addOverlay(option))
        }
    }

    /**
     * 将所有Overlay 从 地图上消除
     */
    private fun removeFromMap() {
        for (marker in mOverlayList) {
            marker.remove()
        }
        mOverlayOptionList.clear()
        mOverlayList.clear()
    }

    /**
     * 缩放地图，使所有Overlay都在合适的视野内
     *
     *
     * 注： 该方法只对Marker类型的overlay有效
     *
     */
    fun zoomToSpan() {
        if (mOverlayList.size > 0) {
            val builder = LatLngBounds.Builder()
            for (overlay in mOverlayList) {
                //polyline 中的点可能太多，只按marker 缩放
                if (overlay is Marker) {
                    builder.include(overlay.position)
                }
            }
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build()))
        }
    }
}