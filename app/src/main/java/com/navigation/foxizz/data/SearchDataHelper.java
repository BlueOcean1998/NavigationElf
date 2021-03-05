package com.navigation.foxizz.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.utils.DistanceUtil;
import com.navigation.foxizz.activity.fragment.MainFragment;
import com.navigation.foxizz.mybaidumap.MySearch;
import com.navigation.foxizz.util.NetworkUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索数据帮助类
 */
public class SearchDataHelper {

    /**
     * 移动视角到最近的一条搜索记录
     *
     * @param mainFragment 地图页碎片
     */
    public static void moveToLastSearchRecordLocation(MainFragment mainFragment) {
        if (isHasSearchData()) {//如果有搜索记录
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(getSearchData().get(0).getLatLng());
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngBounds(builder.build());
            mainFragment.mBaiduMap.setMapStatus(msu);
        }
    }

    /**
     * 初始化搜索记录
     *
     * @param mainFragment 地图页碎片
     */
    public static void initSearchData(MainFragment mainFragment) {
        if (isHasSearchData()) {
            mainFragment.searchList.clear();

            boolean flag = false;//是否刷新搜索记录
            //有网络连接且没有开飞行模式
            if (NetworkUtil.isNetworkConnected() && !NetworkUtil.isAirplaneModeOn()) {
                flag = true;
                //设置为详细搜索全部
                mainFragment.mySearch.searchType = MySearch.DETAIL_SEARCH_ALL;
                mainFragment.mySearch.isFirstDetailSearch = true;//第一次详细信息搜索
            }

            List<SearchItem> searchItems = getSearchData();
            for (SearchItem searchItem : searchItems) {
                //获取定位点到目标点的距离（单位：m，结果除以1000转化为km）
                double distance = (DistanceUtil.getDistance(mainFragment.mLatLng, searchItem.getLatLng()) / 1000);
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
            }

            mainFragment.mSearchAdapter.updateList();//通知adapter更新
        } else {
            mainFragment.searchList.clear();
            mainFragment.mSearchAdapter.updateList();//通知adapter更新
        }
    }

    /**
     * 判断是否有搜索记录
     *
     * @return boolean
     */
    public static boolean isHasSearchData() {
        try (SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getReadableDatabase();
             Cursor cursor = db.rawQuery("select * from SearchData", null)) {
            return cursor.getCount() > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * 获取搜索信息
     *
     * @return 搜索历史记录列表
     */
    public static List<SearchItem> getSearchData() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<SearchItem> searchItems = new ArrayList<>();
        try {
            db = DatabaseHelper.getDatabaseHelper().getReadableDatabase();
            //查询所有的搜索记录，按时间降序排列
            cursor = db.rawQuery("select * from SearchData order by time desc", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    SearchItem searchItem = new SearchItem();

                    searchItem.setUid(cursor.getString(cursor.getColumnIndex("uid")));
                    searchItem.setTargetName(cursor.getString(cursor.getColumnIndex("target_name")));
                    searchItem.setAddress(cursor.getString(cursor.getColumnIndex("address")));

                    searchItem.setLatLng(new LatLng(
                            cursor.getDouble(cursor.getColumnIndex("latitude")),
                            cursor.getDouble(cursor.getColumnIndex("longitude"))));

                    searchItems.add(searchItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception ignored) {

        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return searchItems;
    }

    /**
     * 将详细搜索结果录入数据库或更新数据库中这条记录的内容
     *
     * @param info POI详细信息
     */
    public static void insertOrUpdateSearchData(PoiDetailInfo info) {
        try (SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getReadableDatabase();
             Cursor cursor = db.rawQuery(
                     "select * from SearchData where uid = ?",
                     new String[]{info.getUid()})) {
            if (cursor.getCount() > 0)
                updateSearchData(info);//有则更新
            else insertSearchData(info);//没有则添加
        } catch (Exception ignored) {

        }
    }

    /**
     * 添加搜索信息
     *
     * @param info POI详细信息
     */
    public static void insertSearchData(PoiDetailInfo info) {
        try (SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getWritableDatabase()) {
            db.execSQL("insert into SearchData " +
                            "(uid, latitude, longitude, target_name, address, time) " +
                            "values(?, ?, ?, ?, ?, ?)",
                    new String[]{info.getUid(),
                            String.valueOf(info.getLocation().latitude),
                            String.valueOf(info.getLocation().longitude),
                            info.getName(),
                            info.getAddress(),
                            String.valueOf(System.currentTimeMillis())});
        } catch (Exception ignored) {

        }
    }

    /**
     * 更新搜索信息数据库
     *
     * @param info POI详细信息
     */
    public static void updateSearchData(PoiDetailInfo info) {
        try (SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getWritableDatabase()) {
            db.execSQL("update SearchData set latitude = ?, longitude = ?, " +
                            "target_name = ?, address = ?, time = ? where uid = ?",
                    new String[]{
                            String.valueOf(info.getLocation().latitude),
                            String.valueOf(info.getLocation().longitude),
                            info.getName(),
                            info.getAddress(),
                            String.valueOf(System.currentTimeMillis()),
                            info.getUid()});
        } catch (Exception ignored) {

        }
    }

    /**
     * 根据uid删除某条搜索记录
     *
     * @param uid uid
     */
    public static void deleteSearchData(String uid) {
        try (SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getWritableDatabase()) {
            db.execSQL("delete from SearchData where uid = ?", new String[]{uid});
        } catch (Exception ignored) {

        }
    }

    /**
     * 清空搜索记录
     */
    public static void deleteSearchData() {
        try (SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getWritableDatabase()) {
            db.execSQL("delete from SearchData");
        } catch (Exception ignored) {

        }
    }

}
