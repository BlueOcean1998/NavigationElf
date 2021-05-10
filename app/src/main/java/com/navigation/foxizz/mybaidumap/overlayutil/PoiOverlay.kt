package com.navigation.foxizz.mybaidumap.overlayutil

import android.os.Bundle
import android.util.Log
import com.baidu.mapapi.map.*
import com.baidu.mapapi.search.poi.PoiResult
import java.util.*

/**
 * 用于显示poi的overly
 */
/**
 * 构造函数
 *
 * @param baiduMap 该 PoiOverlay 引用的 BaiduMap 对象
 */
class PoiOverlay(baiduMap: BaiduMap) : OverlayManager(baiduMap) {
    companion object {
        private const val MAX_POI_SIZE = 16
    }

    /**
     * 获取该PoiOverlay的poi数据
     *
     * @return POI数据
     */
    private lateinit var mPoiResult: PoiResult

    /**
     * 设置POI数据
     *
     * @param poiResult 设置POI数据
     */
    fun setData(poiResult: PoiResult) {
        this.mPoiResult = poiResult
    }

    override val overlayOptions: List<OverlayOptions>
        get() {
            val markerList = ArrayList<OverlayOptions>()
            var markerSize = 0
            var i = 0
            while (i < mPoiResult.allPoi.size && markerSize < MAX_POI_SIZE) {
                if (mPoiResult.allPoi[i].location == null) {
                    i++
                    continue
                }
                markerSize++
                val bundle = Bundle()
                bundle.putInt("index", i)
                markerList.add(MarkerOptions()
                    .extraInfo(bundle)
                    .position(mPoiResult.allPoi[i].location)
                    .icon(BitmapDescriptorFactory.fromAssetWithDpi("Icon_mark$markerSize.png")))
                i++
            }
            return markerList
        }

    /**
     * 覆写此方法以改变默认点击行为
     *
     * @param i 被点击的poi在 [PoiResult.getAllPoi] 中的索引
     * @return true--事件已经处理，false--事件未处理
     */
    private fun onPoiClick(i: Int): Boolean {
        if (mPoiResult.allPoi != null && mPoiResult.allPoi[i] != null) {
            Log.i("baidumapsdk", "PoiOverlay onPoiClick")
        }
        return false
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (!mOverlayList.contains(marker)) {
            return false
        }
        return if (marker.extraInfo != null) {
            onPoiClick(marker.extraInfo.getInt("index"))
        } else false
    }

    override fun onPolylineClick(polyline: Polyline): Boolean {
        return false
    }
}