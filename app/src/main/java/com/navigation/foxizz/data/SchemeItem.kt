package com.navigation.foxizz.data

import com.baidu.mapapi.search.route.MassTransitRouteLine

/**
 * 路线规划信息类
 */
data class SchemeItem(
    var routeLine: MassTransitRouteLine? = null, //路线
    var allStationInfo: String = "", //所有站点信息
    var simpleInfo: String = "", //简要信息
    var detailInfo: String = "", //详细信息
    var expandFlag: Boolean = false,//伸展状态
)