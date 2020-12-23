package com.example.foxizz.navigation.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.foxizz.navigation.activity.fragment.MainFragment;
import com.example.foxizz.navigation.mybaidumap.MySearch;
import com.example.foxizz.navigation.util.NetworkUtil;

import java.math.BigDecimal;

/**
 * 搜索数据帮助类
 * 操作搜索数据表
 */
public class SearchDataHelper {

    private static SQLiteDatabase db;
    private static Cursor cursor;
    private static DatabaseHelper databaseHelper;

    /**
     * 初始化搜索数据库
     */
    public static void initSearchDataHelper() {
        databaseHelper = new DatabaseHelper("Navigate.db", null, 1);
    }

    /**
     * 移动视角到最近的一条搜索记录
     *
     * @param mainFragment 地图页碎片
     */
    public static void moveToLastSearchRecordLocation(MainFragment mainFragment) {
        try {
            if (isHasSearchData()) {//如果有搜索记录
                db = databaseHelper.getReadableDatabase();
                //查询所有的搜索记录，按时间降序排列
                cursor = db.rawQuery("select * from SearchData order by time desc", null);
                if (cursor != null && cursor.moveToFirst()) {
                    //移动视角
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(new LatLng(
                            cursor.getDouble(cursor.getColumnIndex("latitude")),
                            cursor.getDouble(cursor.getColumnIndex("longitude"))));
                    MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngBounds(builder.build());
                    mainFragment.mBaiduMap.setMapStatus(msu);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * 初始化搜索记录
     *
     * @param mainFragment 地图页碎片
     */
    public static void initSearchData(MainFragment mainFragment) {
        try {
            mainFragment.searchList.clear();

            boolean flag = false;//是否刷新搜索记录
            //有网络连接且没有开飞行模式
            if (NetworkUtil.isNetworkConnected() && !NetworkUtil.isAirplaneModeOn()) {
                flag = true;
                //设置为详细搜索全部
                mainFragment.mySearch.poiSearchType = MySearch.DETAIL_SEARCH_ALL;
                mainFragment.mySearch.isFirstDetailSearch = true;//第一次详细信息搜索
            }

            db = databaseHelper.getReadableDatabase();
            //查询所有的搜索记录，按时间降序排列
            cursor = db.rawQuery("select * from SearchData order by time desc", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    SearchItem searchItem = new SearchItem();

                    searchItem.setUid(cursor.getString(cursor.getColumnIndex("uid")));
                    searchItem.setTargetName(cursor.getString(cursor.getColumnIndex("target_name")));
                    searchItem.setAddress(cursor.getString(cursor.getColumnIndex("address")));

                    LatLng latLng = new LatLng(
                            cursor.getDouble(cursor.getColumnIndex("latitude")),
                            cursor.getDouble(cursor.getColumnIndex("longitude")));
                    searchItem.setLatLng(latLng);

                    //获取定位点到目标点的距离（单位：m，结果除以1000转化为km）
                    double distance = (DistanceUtil.getDistance(mainFragment.latLng, latLng) / 1000);
                    //保留两位小数
                    BigDecimal bd = new BigDecimal(distance);
                    distance = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    searchItem.setDistance(distance);

                    mainFragment.searchList.add(searchItem);

                    if (flag) {
                        //通过网络重新获取搜索信息
                        mainFragment.mPoiSearch.searchPoiDetail(//开始POI详细信息搜索
                                (new PoiDetailSearchOption()).poiUids(searchItem.getUid()));
                    }
                } while (cursor.moveToNext());
            }
            mainFragment.searchAdapter.notifyDataSetChanged();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * 判断是否有搜索记录
     *
     * @return boolean
     */
    public static boolean isHasSearchData() {
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.rawQuery("select * from SearchData", null);
            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * 添加搜索信息
     *
     * @param info POI详细信息
     */
    public static void insertSearchData(PoiDetailInfo info) {
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("insert into SearchData (uid, latitude, longitude, target_name, address, time) " +
                            "values(?, ?, ?, ?, ?, ?)",
                    new String[]{info.getUid(),
                            String.valueOf(info.getLocation().latitude),
                            String.valueOf(info.getLocation().longitude),
                            info.getName(),
                            info.getAddress(),
                            String.valueOf(System.currentTimeMillis())});
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * 更新搜索信息数据库
     *
     * @param info POI详细信息
     */
    public static void updateSearchData(PoiDetailInfo info) {
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("update SearchData set latitude = ?, longitude = ?, " +
                            "target_name = ?, address = ?, time = ? where uid = ?",
                    new String[]{
                            String.valueOf(info.getLocation().latitude),
                            String.valueOf(info.getLocation().longitude),
                            info.getName(),
                            info.getAddress(),
                            String.valueOf(System.currentTimeMillis()),
                            info.getUid()});
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * 将详细搜索结果录入数据库或更新数据库中这条记录的内容
     *
     * @param info POI详细信息
     */
    public static void insertOrUpdateSearchData(PoiDetailInfo info) {
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.rawQuery("select * from SearchData where uid = ?", new String[]{info.getUid()});
            if (cursor.getCount() > 0) updateSearchData(info);//有则更新
            else insertSearchData(info);//没有则添加
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * 清空搜索记录
     */
    public static void deleteSearchData() {
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("delete from SearchData");
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * 根据uid删除某条搜索记录
     *
     * @param uid uid
     */
    public static void deleteSearchData(String uid) {
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("delete from SearchData where uid = ?", new String[]{uid});
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * 关闭数据库，防止内存泄漏
     */
    public static void close() {
        databaseHelper.close();
    }

}
