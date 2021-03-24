package com.navigation.foxizz.data

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.baidu.mapapi.search.core.PoiDetailInfo
import com.baidu.mapapi.search.poi.PoiDetailSearchOption
import com.baidu.mapapi.utils.DistanceUtil
import com.navigation.foxizz.activity.fragment.MainFragment
import com.navigation.foxizz.mybaidumap.BaiduSearch
import com.navigation.foxizz.util.NetworkUtil
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
            val builder = LatLngBounds.Builder()
            builder.include(searchData[0].latLng)
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
        if (isHasSearchData) {
            mainFragment.searchList.clear()
            var isRefreshSearchRecord = false //是否刷新搜索记录
            //有网络连接且没有开飞行模式
            if (NetworkUtil.isNetworkConnected && !NetworkUtil.isAirplaneModeOn) {
                isRefreshSearchRecord = true
                //设置为详细搜索全部
                mainFragment.mBaiduSearch.searchType = BaiduSearch.DETAIL_SEARCH_ALL
                mainFragment.mBaiduSearch.isFirstDetailSearch = true //第一次详细信息搜索
            }
            val searchItems = searchData
            for (searchItem in searchItems) {
                //获取定位点到目标点的距离（单位：m，结果除以1000转化为km）
                var distance = DistanceUtil.getDistance(mainFragment.mBaiduLocation.mLatLng, searchItem.latLng) / 1000
                //保留两位小数
                val bd = BigDecimal(distance)
                distance = bd.setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
                searchItem.distance = distance
                mainFragment.searchList.add(searchItem)
                if (isRefreshSearchRecord) {
                    //通过网络重新获取搜索信息
                    mainFragment.mBaiduSearch.mPoiSearch.searchPoiDetail( //开始POI详细信息搜索
                            PoiDetailSearchOption().poiUids(searchItem.uid))
                }
            }
            mainFragment.mSearchAdapter.updateList() //通知adapter更新
        } else {
            mainFragment.searchList.clear()
            mainFragment.mSearchAdapter.updateList() //通知adapter更新
        }
    }

    /**
     * 判断是否有搜索记录
     *
     * @return boolean
     */
    val isHasSearchData: Boolean
        get() {
            try {
                DatabaseHelper.databaseHelper.readableDatabase.use { db ->
                    db.rawQuery("select * from SearchData", null).use { cursor ->
                        return cursor.count > 0
                    }
                }
            } catch (ignored: Exception) {
                return false
            }
        }//查询所有的搜索记录，按时间降序排列

    /**
     * 获取搜索信息
     *
     * @return 搜索历史记录列表
     */
    private val searchData: List<SearchItem>
        get() {
            var db: SQLiteDatabase? = null
            var cursor: Cursor? = null
            val searchItems = ArrayList<SearchItem>()
            try {
                db = DatabaseHelper.databaseHelper.readableDatabase
                //查询所有的搜索记录，按时间降序排列
                cursor = db.rawQuery("select * from SearchData order by time desc", null)
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        val searchItem = SearchItem()
                        searchItem.uid = cursor.getString(cursor.getColumnIndex("uid"))
                        searchItem.targetName = cursor.getString(cursor.getColumnIndex("target_name"))
                        searchItem.address = cursor.getString(cursor.getColumnIndex("address"))
                        searchItem.latLng = LatLng(
                                cursor.getDouble(cursor.getColumnIndex("latitude")),
                                cursor.getDouble(cursor.getColumnIndex("longitude")))
                        searchItems.add(searchItem)
                    } while (cursor.moveToNext())
                }
            } catch (ignored: Exception) {
            } finally {
                cursor?.close()
                db?.close()
            }
            return searchItems
        }

    /**
     * 将详细搜索结果录入数据库或更新数据库中这条记录的内容
     *
     * @param info POI详细信息
     */
    fun insertOrUpdateSearchData(info: PoiDetailInfo) {
        try {
            DatabaseHelper.databaseHelper.readableDatabase.use { db ->
                db.rawQuery("select * from SearchData where uid = ?", arrayOf(info.uid))
                        .use { cursor ->
                            if (cursor.count > 0) updateSearchData(info) //有则更新
                            else insertSearchData(info) //没有则添加
                        }
            }
        } catch (ignored: Exception) {
        }
    }

    /**
     * 添加搜索信息
     *
     * @param info POI详细信息
     */
    private fun insertSearchData(info: PoiDetailInfo) {
        try {
            DatabaseHelper.databaseHelper.writableDatabase.use { db ->
                db.execSQL("insert into SearchData " +
                        "(uid, latitude, longitude, target_name, address, time) " +
                        "values(?, ?, ?, ?, ?, ?)",
                        arrayOf(info.uid,
                                info.location.latitude.toString(),
                                info.location.longitude.toString(),
                                info.name,
                                info.address, System.currentTimeMillis().toString()
                        )
                )
            }
        } catch (ignored: Exception) {
        }
    }

    /**
     * 更新搜索信息数据库
     *
     * @param info POI详细信息
     */
    private fun updateSearchData(info: PoiDetailInfo) {
        try {
            DatabaseHelper.databaseHelper.writableDatabase.use { db ->
                db.execSQL("update SearchData set latitude = ?, longitude = ?, " +
                        "target_name = ?, address = ?, time = ? where uid = ?",
                        arrayOf(info.location.latitude.toString(),
                                info.location.longitude.toString(),
                                info.name,
                                info.address,
                                System.currentTimeMillis().toString(),
                                info.uid
                        )
                )
            }
        } catch (ignored: Exception) {
        }
    }

    /**
     * 根据uid删除某条搜索记录
     *
     * @param uid uid
     */
    fun deleteSearchData(uid: String?) {
        try {
            DatabaseHelper.databaseHelper.writableDatabase
                    .use { db ->
                        db.execSQL("delete from SearchData where uid = ?",
                                arrayOf(uid))
                    }
        } catch (ignored: Exception) {
        }
    }

    /**
     * 清空搜索记录
     */
    fun deleteSearchData() {
        try {
            DatabaseHelper.databaseHelper.writableDatabase.use { db ->
                db.execSQL("delete from SearchData")
            }
        } catch (ignored: Exception) {
        }
    }
}