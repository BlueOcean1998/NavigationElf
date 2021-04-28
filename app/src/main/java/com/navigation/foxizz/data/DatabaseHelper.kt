package com.navigation.foxizz.data

import Constants
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import base.foxizz.BaseApplication.Companion.baseApplication

/**
 * 数据库帮助类
 */
class DatabaseHelper private constructor(
        context: Context, name: String, factory: CursorFactory?, version: Int) :
        SQLiteOpenHelper(context, name, factory, version) {
    companion object {
        /**
         * 获取数据库帮助对象
         */
        @get:Synchronized
        val databaseHelper = DatabaseHelper(baseApplication, Constants.LOCAL_DATABASE, null, 1)

        private const val CREATE_SEARCH = ("create table SearchData ("
                + "uid text primary key, " //uid
                + "latitude double, " //纬度
                + "longitude double, " //经度
                + "target_name text, " //目标名
                + "address text, " //目标地址
                + "time long)") //记录时间
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_SEARCH) //建搜索记录表
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { //升级数据库
        db.execSQL("drop table if exists SearchData")
        db.execSQL("drop table if exists UserData")
        onCreate(db)
    }
}