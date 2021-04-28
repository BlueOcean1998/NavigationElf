package cn.zerokirby.api.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import base.foxizz.BaseApplication.Companion.baseApplication
import cn.zerokirby.api.Constants

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

        private const val CREATE_USER = ("create table User ("
                + "user_id text, " //用户id
                + "username text, " //用户名
                + "password text, " //密码
                + "language text, " //语言
                + "version text, " //版本
                + "display text, " //显示信息
                + "model text, " //型号
                + "brand text, " //品牌
                + "register_time long, " //注册时间
                + "last_use long, " //上次使用时间
                + "last_sync long, " //次同步时间
                + "avatar blob)") //头像

        private const val INIT_USER = "insert into User " +
                "(user_id, username, password, register_time, last_use, last_sync) " +
                "values('0', '', '', '0', '0', '0')"
    }

    override fun onCreate(db: SQLiteDatabase) { //建用户表并初始化
        db.execSQL(CREATE_USER)
        db.execSQL(INIT_USER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { //升级数据库
        db.execSQL("drop table if exists User")
        onCreate(db)
    }
}