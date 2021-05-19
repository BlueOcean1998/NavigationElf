package com.navigation.foxizz.data

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import base.foxizz.util.NetworkUtil
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.baidu.mapapi.search.core.PoiDetailInfo
import com.baidu.mapapi.search.poi.PoiDetailSearchOption
import com.baidu.mapapi.utils.DistanceUtil
import com.navigation.foxizz.activity.fragment.MainFragment
import com.navigation.foxizz.data.DatabaseHelper.Companion.databaseHelper
import com.navigation.foxizz.mybaidumap.BaiduSearch
import java.math.BigDecimal
import java.util.*

/**
 * 搜索数据帮助类
 */
object SearchDataHelper {
    /**
     * 移动视角到最近的一条搜索记录
     *
     * @param mainFragment 地图页碎片
     */
    fun moveToLastSearchRecordLocation(mainFragment: MainFragment) {
        if (isHasSearchData) { //如果有搜索记录
            val builder = LatLngBounds.Builder().include(searchData[0].latLng)
            val msu = MapStatusUpdateFactory.newLatLngBounds(builder.build())
            mainFragment.mBaiduMap.setMapStatus(msu)
        }
    }

    /**
     * 初始化搜索记录
     *
     * @param mainFragment 地图页碎片
     */
    fun initSearchData(mainFragment: MainFragment) {
        mainFragment.run {
            if (isHasSearchData) {
                mBaiduSearch.mSearchList.clear()
                var isRefreshSearchRecord = false //是否刷新搜索记录
                //有网络连接且没有开飞行模式
                if (NetworkUtil.isNetworkConnected && !NetworkUtil.isAirplaneModeEnable) {
                    isRefreshSearchRecord = true
                    //设置为详细搜索全部
                    mBaiduSearch.mSearchType = BaiduSearch.DETAIL_SEARCH_ALL
                    mBaiduSearch.isFirstDetailSearch = true //第一次详细信息搜索
                }
                for (searchItem in searchData) {
                    //获取定位点到目标点的距离（单位：m，结果除以1000转化为km）
                    var distance =
                        DistanceUtil.getDistance(mBaiduLocation.mLatLng, searchItem.latLng) / 1000
                    //保留两位小数
                    val bd = BigDecimal(distance)
                    distance = bd.setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
                    searchItem.distance = distance
                    mBaiduSearch.mSearchList.add(searchItem)
                    if (isRefreshSearchRecord) { //通过网络重新获取搜索信息
                        mBaiduSearch.mPoiSearch.searchPoiDetail(
                            PoiDetailSearchOption().poiUids(searchItem.uid)
                        )
                    }
                }
                mSearchAdapter.updateList() //通知adapter更新
            } else {
                mBaiduSearch.mSearchList.clear()
                mSearchAdapter.updateList() //通知adapter更新
            }
        }
    }

    /**
     * 判断是否有搜索记录
     */
    val isHasSearchData
        get() = try {
            databaseHelper.readableDatabase.use { db ->
                db.rawQuery("select * from SearchData", null).use { cursor ->
                    cursor.count > 0
                }
            }
        } catch (e: Exception) {
            false
        }

    /**
     * 获取搜索记录
     */
    private val searchData: List<SearchItem>
        get() {
            var db: SQLiteDatabase? = null
            var cursor: Cursor? = null
            val searchItems = ArrayList<SearchItem>()
            try {
                db = databaseHelper.readableDatabase
                //查询所有的搜索记录，按时间降序排列
                cursor = db.rawQuery(
                    "select * from SearchData order by time desc",
                    null
                )
                cursor?.run {
                    if (cursor.moveToFirst()) {
                        do {
                            searchItems.add(SearchItem().apply {
                                uid = getString(getColumnIndex("uid"))
                                targetName = getString(getColumnIndex("target_name"))
                                address = getString(getColumnIndex("address"))
                                latLng = LatLng(
                                    getDouble(getColumnIndex("latitude")),
                                    getDouble(getColumnIndex("longitude"))
                                )
                            })
                        } while (moveToNext())
                    }
                }
            } catch (e: Exception) {
            } finally {
                cursor?.close()
                db?.close()
            }
            return searchItems
        }

    /**
     * 将详细搜索结果录入数据库或更新数据库中这条记录的内容
     *
     * @param poiDetailInfo POI详细信息
     */
    fun insertOrUpdateSearchData(poiDetailInfo: PoiDetailInfo) = try {
        databaseHelper.readableDatabase.use { db ->
            poiDetailInfo.let {
                db.rawQuery(
                    "select * from SearchData where uid = ?",
                    arrayOf(it.uid)
                ).use { cursor ->
                    if (cursor.count > 0) updateSearchData(it) //有则更新
                    else insertSearchData(it) //没有则添加
                }
            }
        }
    } catch (e: Exception) {
    }

    /**
     * 添加搜索信息
     *
     * @param poiDetailInfo POI详细信息
     */
    private fun insertSearchData(poiDetailInfo: PoiDetailInfo) = try {
        databaseHelper.writableDatabase.use { db ->
            poiDetailInfo.run {
                db.execSQL(
                    "insert into SearchData " +
                            "(uid, latitude, longitude, target_name, address, time) " +
                            "values(?, ?, ?, ?, ?, ?)",
                    arrayOf(
                        uid,
                        location.latitude,
                        location.longitude,
                        name,
                        address,
                        System.currentTimeMillis()
                    )
                )
            }
        }
    } catch (e: Exception) {
    }

    /**
     * 更新搜索信息数据库
     *
     * @param poiDetailInfo POI详细信息
     */
    private fun updateSearchData(poiDetailInfo: PoiDetailInfo) = try {
        databaseHelper.writableDatabase.use { db ->
            poiDetailInfo.run {
                db.execSQL(
                    "update SearchData set latitude = ?, longitude = ?, " +
                            "target_name = ?, address = ?, time = ? where uid = ?",
                    arrayOf(
                        location.latitude,
                        location.longitude,
                        name,
                        address,
                        System.currentTimeMillis(),
                        uid
                    )
                )
            }
        }
    } catch (e: Exception) {
    }

    /**
     * 根据uid删除某条搜索记录
     *
     * @param uid uid
     */
    fun deleteSearchData(uid: String?) = try {
        databaseHelper.writableDatabase
            .use { db ->
                db.execSQL(
                    "delete from SearchData where uid = ?",
                    arrayOf(uid)
                )
            }
    } catch (e: Exception) {
    }

    /**
     * 清空搜索记录
     */
    fun deleteSearchData() = try {
        databaseHelper.writableDatabase.use { db ->
            db.execSQL("delete from SearchData")
        }
    } catch (e: Exception) {
    }
}