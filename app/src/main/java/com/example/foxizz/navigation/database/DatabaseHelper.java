package com.example.foxizz.navigation.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.activity.SettingsActivity;
import com.example.foxizz.navigation.searchdata.SearchItem;
import com.example.foxizz.navigation.util.MyPoiSearch;

import static com.example.foxizz.navigation.demo.Tools.isAirplaneModeOn;
import static com.example.foxizz.navigation.demo.Tools.isNetworkConnected;

/**
 * 搜索到的信息的数据库
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String CREATE_SETTINGS = "create table Settings ("
            + "map_type text, "//地图类型
            + "destination_city text)";//目的地城市

    private static final String INIT_SETTINGS = "insert into Settings "
            + "(map_type, destination_city) values('0', '')";

    private static final String CREATE_SEARCH = "create table SearchData ("
            + "uid text primary key, "//uid
            + "latitude double, "//纬度
            + "longitude double, "//经度
            + "targetName text, "//目标名
            + "address text, "//目标地址
            + "time long)";//记录时间

    private MainActivity mainActivity;
    private SettingsActivity settingsActivity;
    public DatabaseHelper(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        if(context instanceof MainActivity) mainActivity = (MainActivity) context;
        if(context instanceof SettingsActivity) settingsActivity = (SettingsActivity) context;
    }

    private SQLiteDatabase db;
    private Cursor cursor;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SETTINGS);//建设置表
        db.execSQL(INIT_SETTINGS);//初始化设置表
        db.execSQL(CREATE_SEARCH);//建搜索记录表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {//升级数据库
        db.execSQL("drop table if exists Settings");
        db.execSQL("drop table if exists SearchData");
        onCreate(db);
    }

    //修改设置
    public void modifySettings(String column, String value) {
        db = this.getWritableDatabase();
        db.execSQL("update Settings set " + column + " = ?",
                new String[] { value });
        db.close();
    }

    //读取设置
    public String getSettings(String column) {
        db = this.getReadableDatabase();
        cursor = db.rawQuery("select * from Settings", null);
        if(cursor != null && cursor.moveToFirst())
            return cursor.getString(cursor.getColumnIndex(column));
        return null;
    }

    //初始化搜索记录
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void initSearchData() {
        mainActivity.searchList.clear();

        boolean isLastRecord = true;//是否是最后的记录

        boolean flag = false;//是否刷新搜索记录
        if(isNetworkConnected(mainActivity)
                && !isAirplaneModeOn(mainActivity)
                && mainActivity.mCity != null) {
            flag = true;
            mainActivity.myPoiSearch.poiSearchType = MyPoiSearch.DETAIL_SEARCH_ALL;//设置为详细搜索全部
        }

        db = this.getReadableDatabase();
        //查询所有的搜索记录，按时间降序排列
        cursor = db.rawQuery("select * from SearchData order by time desc", null);
        if(cursor != null && cursor.moveToFirst()) {
            do {
                SearchItem searchItem = new SearchItem();

                searchItem.setUid(cursor.getString(cursor.getColumnIndex("uid")));
                searchItem.setLatLng(new LatLng(
                        cursor.getDouble(cursor.getColumnIndex("latitude")),
                        cursor.getDouble(cursor.getColumnIndex("longitude"))));

                searchItem.setTargetName(cursor.getString(cursor.getColumnIndex("targetName")));
                searchItem.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                searchItem.setDistance(0.0);

                mainActivity.searchList.add(searchItem);

                if(flag) {
                    //通过网络重新获取搜索信息
                    mainActivity.mPoiSearch.searchPoiDetail(
                            (new PoiDetailSearchOption()).poiUids(searchItem.getUid()));

                } else if(isLastRecord) {
                    isLastRecord = false;
                    //网络不好时移动地图到上次记录的位置
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(searchItem.getLatLng());
                    MapStatusUpdate msu= MapStatusUpdateFactory.newLatLngBounds(builder.build());
                    mainActivity.mBaiduMap.setMapStatus(msu);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        mainActivity.searchAdapter.notifyDataSetChanged();
    }

    //是否有搜索记录
    public boolean ifHasSearchData() {
        db = this.getReadableDatabase();
        cursor = db.rawQuery("select * from SearchData", null);
        if(cursor != null) {
            return cursor.getCount() > 0;
        }
        return false;
    }

    //根据uid获取某条搜索记录
    public Cursor getSearchData(String uid) {
        db = this.getReadableDatabase();
        return db.rawQuery("select * from SearchData where uid = ?", new String[] { uid });
    }

    //录入搜索信息数据库
    public void insertSearchData(PoiDetailInfo info) {
        db = this.getWritableDatabase();
        db.execSQL("insert into SearchData (uid, latitude, longitude, targetName, address, time) " +
                        "values(?, ?, ?, ?, ?, ?)",
                new String[] { info.getUid(),
                        String.valueOf(info.getLocation().latitude),
                        String.valueOf(info.getLocation().longitude),
                        info.getName(),
                        info.getAddress(),
                        String.valueOf(System.currentTimeMillis()) });
        db.close();
    }

    //更新搜索信息数据库
    public void updateSearchData(PoiDetailInfo info) {
        db = this.getWritableDatabase();
        db.execSQL("update SearchData set latitude = ?, longitude = ?, " +
                        "targetName = ?, address = ?, time = ? where uid = ?",
                new String[] {
                        String.valueOf(info.getLocation().latitude),
                        String.valueOf(info.getLocation().longitude),
                        info.getName(),
                        info.getAddress(),
                        String.valueOf(System.currentTimeMillis()),
                        info.getUid() });
        db.close();
    }

    //清空搜索记录
    public void deleteAllSearchData() {
        mainActivity.searchList.clear();//清空搜索列表
        mainActivity.searchAdapter.notifyDataSetChanged();//通知adapter更新

        db = this.getWritableDatabase();
        db.execSQL("delete from SearchData");
        db.close();
    }

    //根据uid删除某条搜索记录
    public void deleteSearchData(String uid) {
        db = this.getWritableDatabase();
        db.execSQL("delete from SearchData where uid = ?", new String[] { uid });
        db.close();
    }

}
