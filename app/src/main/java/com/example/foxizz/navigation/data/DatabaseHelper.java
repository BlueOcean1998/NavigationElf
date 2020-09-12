package com.example.foxizz.navigation.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 搜索到的信息的数据库
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String CREATE_SEARCH = "create table SearchData ("
            + "uid text primary key, "//uid
            + "latitude double, "//纬度
            + "longitude double, "//经度
            + "target_name text, "//目标名
            + "address text, "//目标地址
            + "time long)";//记录时间

    private static final String CREATE_USER = "create table User ("
            + "user_id integer, "//用户id
            + "user_name text, "//用户名
            + "password text, "//密码
            + "language text, "//语言
            + "version text, "//版本
            + "display text, "//
            + "model text, "//机型
            + "brand text, "//
            + "register_time long, "//注册时间
            + "last_login long, "//最后登录
            + "portrait blob)";//头像目录

    public DatabaseHelper(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SEARCH);//建搜索记录表
        db.execSQL(CREATE_USER);//建用户表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {//升级数据库
        db.execSQL("drop table if exists SearchData");
        db.execSQL("drop table if exists User");
        onCreate(db);
    }

}
