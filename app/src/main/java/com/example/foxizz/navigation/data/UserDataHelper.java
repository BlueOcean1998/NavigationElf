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

    public UserDataHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

}
