package com.navigation.foxizz.data

import com.baidu.mapapi.model.LatLng

/**
 * 搜索目标信息类
 */
data class SearchItem(
        var uid: String = "", //唯一地址标识
        var latLng: LatLng? = null, //坐标
        var targetName: String = "", //目标名
        var address: String = "", //目标地址
        var distance: Double = 0.0 //与目标的距离
)