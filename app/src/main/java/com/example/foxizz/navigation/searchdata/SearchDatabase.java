package com.example.foxizz.navigation.searchdata;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.example.foxizz.navigation.activity.MainActivity;

import static com.example.foxizz.navigation.demo.Tools.isAirplaneModeOn;
import static com.example.foxizz.navigation.demo.Tools.isNetworkConnected;

/**
 * 搜索到的信息的数据库
 */
public class SearchDatabase extends SQLiteOpenHelper {

    private static final String CREATE_SEARCH = "create table SearchData ("
            + "uid text primary key, "//uid
            + "latitude double, "//纬度
            + "longitude double, "//经度
            + "targetName text, "//目标名
            + "address text, "//目标地址
            + "time long)";//记录时间

    private Context mContext;
    public SearchDatabase(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    private SQLiteDatabase db;
    private Cursor cursor;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SEARCH);//建表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists SearchData");
        onCreate(db);//升级数据库
    }

    //初始化搜索记录
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void initSearchData() {
        MainActivity mainActivity = (MainActivity) mContext;
        mainActivity.searchList.clear();

        boolean flag = false;//是否刷新搜索记录
        if(isNetworkConnected(mContext)
                && !isAirplaneModeOn(mContext)
                && mainActivity.permissionFlag == MainActivity.READY_TO_LOCATION
                && mainActivity.mCity != null) {
            flag = true;
            mainActivity.myPoiSearch.detailPoiSearch();//设置为直接详细搜索
        }

        db = this.getReadableDatabase();
        //查询所有的搜索记录，按时间降序排列
        cursor = db.rawQuery("select * from SearchData order by time desc", null);
        if(cursor != null && cursor.moveToFirst()) {
            do {
                SearchItem searchItem = new SearchItem();
                searchItem.setUid(cursor.getString(cursor.getColumnIndex("uid")));

                if(flag) {
                    mainActivity.searchList.add(searchItem);
                    mainActivity.mPoiSearch.searchPoiDetail(
                            (new PoiDetailSearchOption()).poiUids(searchItem.getUid()));

                } else {
                    searchItem.setLatLng(new LatLng(
                            cursor.getDouble(cursor.getColumnIndex("latitude")),
                            cursor.getDouble(cursor.getColumnIndex("longitude"))));
                    searchItem.setTargetName(cursor.getString(cursor.getColumnIndex("targetName")));
                    searchItem.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    searchItem.setDistance(0.0);

                    mainActivity.searchList.add(searchItem);
                    mainActivity.searchAdapter.notifyDataSetChanged();
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
    }

    //根据uid获取某条搜索记录
    public Cursor getSearchData(String uid) {
        db = this.getReadableDatabase();
        return db.rawQuery("select * from SearchData where uid = ?", new String[] { uid });
    }

    //录入搜索信息数据库
    public void insertSearchDatabase(PoiDetailInfo info) {
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
    public void updateSearchDatabase(PoiDetailInfo info) {
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
        MainActivity mainActivity = (MainActivity) mContext;
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
