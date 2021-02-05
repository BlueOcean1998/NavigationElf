package com.navigation.foxizz.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.navigation.foxizz.BaseApplication.getApplication;

/**
 * 数据库帮助类
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper databaseHelper;//数据库帮助对象

    //初始化数据库帮助对象
    public static void initDatabaseHelper() {
        databaseHelper = new DatabaseHelper(Constants.LOCAL_DATABASE, null, 1);
    }

    //获取数据库帮助对象
    public synchronized static DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    private static final String CREATE_SEARCH = "create table SearchData ("
            + "uid text primary key, "//uid
            + "latitude double, "//纬度
            + "longitude double, "//经度
            + "target_name text, "//目标名
            + "address text, "//目标地址
            + "time long)";//记录时间

    private DatabaseHelper(String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(getApplication(), name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SEARCH);//建搜索记录表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {//升级数据库
        db.execSQL("drop table if exists SearchData");
        db.execSQL("drop table if exists UserData");
        onCreate(db);
    }

}
