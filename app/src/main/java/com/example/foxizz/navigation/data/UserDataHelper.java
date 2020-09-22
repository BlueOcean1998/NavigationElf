package com.example.foxizz.navigation.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 用户数据帮助类
 * 操作用户数据表
 */
public class UserDataHelper {

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    public UserDataHelper() {
        databaseHelper = new DatabaseHelper("Navigate.db", null, 1);
    }

    /**
     * 关闭数据库，防止内存泄漏
     */
    public void close() {
        if(databaseHelper != null) databaseHelper.close();
    }

}
