package com.example.foxizz.navigation.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.activity.SettingsActivity;
import com.example.foxizz.navigation.activity.fragment.MainFragment;
import com.example.foxizz.navigation.activity.fragment.UserFragment;
import com.example.foxizz.navigation.mybaidumap.MyPoiSearch;

import static com.example.foxizz.navigation.util.Tools.isAirplaneModeOn;
import static com.example.foxizz.navigation.util.Tools.isNetworkConnected;

//搜索数据帮助类
public class SearchDataHelper {

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    public SearchDataHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    //移动视角到最近的一条搜索记录
    public void moveToLastSearchRecordLocation(MainFragment mainFragment) {
        try {
            if(isHasSearchData()) {//如果有搜索记录
                db = databaseHelper.getReadableDatabase();
                //查询所有的搜索记录，按时间降序排列
                cursor = db.rawQuery("select * from SearchData order by time desc", null);
                if(cursor != null && cursor.moveToFirst()) {
                    //移动视角
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(new LatLng(
                            cursor.getDouble(cursor.getColumnIndex("latitude")),
                            cursor.getDouble(cursor.getColumnIndex("longitude"))));
                    MapStatusUpdate msu= MapStatusUpdateFactory.newLatLngBounds(builder.build());
                    mainFragment.mBaiduMap.setMapStatus(msu);
                }
            }
        } finally {
            if(cursor !=null) cursor.close();
            if(db != null) db.close();
        }

    }

    //初始化搜索记录
    public void initSearchData(MainFragment mainFragment) {
        try {
            mainFragment.searchList.clear();

            boolean flag = false;//是否刷新搜索记录
            //有网络连接且没有开飞行模式
            if(isNetworkConnected() && !isAirplaneModeOn()) {
                flag = true;
                //设置为详细搜索全部
                mainFragment.myPoiSearch.poiSearchType = MyPoiSearch.DETAIL_SEARCH_ALL;
            }

            db = databaseHelper.getReadableDatabase();
            //查询所有的搜索记录，按时间降序排列
            cursor = db.rawQuery("select * from SearchData order by time desc", null);
            if(cursor != null && cursor.moveToFirst()) {
                do {
                    SearchItem searchItem = new SearchItem();

                    searchItem.setUid(cursor.getString(cursor.getColumnIndex("uid")));
                    searchItem.setLatLng(new LatLng(
                            cursor.getDouble(cursor.getColumnIndex("latitude")),
                            cursor.getDouble(cursor.getColumnIndex("longitude"))));

                    searchItem.setTargetName(cursor.getString(cursor.getColumnIndex("target_name")));
                    searchItem.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    searchItem.setDistance(0.0);

                    mainFragment.searchList.add(searchItem);

                    if(flag) {
                        //通过网络重新获取搜索信息
                        mainFragment.mPoiSearch.searchPoiDetail(
                                (new PoiDetailSearchOption()).poiUids(searchItem.getUid()));
                    }
                } while (cursor.moveToNext());
            }
            mainFragment.searchAdapter.notifyDataSetChanged();
        } finally {
            if(cursor != null) cursor.close();
            if(db != null) db.close();
        }
    }

    //是否有搜索记录
    public boolean isHasSearchData() {
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.rawQuery("select * from SearchData", null);
            return cursor.getCount() > 0;
        } finally {
            if(cursor != null) cursor.close();
            if(db != null) db.close();
        }
    }

    //录入搜索信息数据库
    public void insertSearchData(PoiDetailInfo info) {
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("insert into SearchData (uid, latitude, longitude, target_name, address, time) " +
                            "values(?, ?, ?, ?, ?, ?)",
                    new String[] { info.getUid(),
                            String.valueOf(info.getLocation().latitude),
                            String.valueOf(info.getLocation().longitude),
                            info.getName(),
                            info.getAddress(),
                            String.valueOf(System.currentTimeMillis()) });
        } finally {
            if(db != null) db.close();
        }
    }

    //更新搜索信息数据库
    public void updateSearchData(PoiDetailInfo info) {
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("update SearchData set latitude = ?, longitude = ?, " +
                            "target_name = ?, address = ?, time = ? where uid = ?",
                    new String[] {
                            String.valueOf(info.getLocation().latitude),
                            String.valueOf(info.getLocation().longitude),
                            info.getName(),
                            info.getAddress(),
                            String.valueOf(System.currentTimeMillis()),
                            info.getUid() });
        } finally {
            if(db != null) db.close();
        }
    }

    //将详细搜索结果录入数据库或更新数据库中这条记录的内容
    public void insertOrUpdateSearchDatabase(PoiDetailInfo info) {
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.rawQuery("select * from SearchData where uid = ?", new String[] { info.getUid() });
            if(cursor.getCount() > 0) updateSearchData(info);//有则更新
            else insertSearchData(info);//没有则添加
        } finally {
            if(cursor != null) cursor.close();
            if(db != null) db.close();
        }
    }

    //清空搜索记录
    public void deleteAllSearchData(MainFragment mainFragment) {
        try {
            mainFragment.searchList.clear();//清空搜索列表
            mainFragment.searchAdapter.notifyDataSetChanged();//通知adapter更新

            db = databaseHelper.getWritableDatabase();
            db.execSQL("delete from SearchData");
        } finally {
            if(db != null) db.close();
        }
    }

    //根据uid删除某条搜索记录
    public void deleteSearchData(String uid) {
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("delete from SearchData where uid = ?", new String[] { uid });
        } finally {
            if(db != null) db.close();
        }
    }

}
