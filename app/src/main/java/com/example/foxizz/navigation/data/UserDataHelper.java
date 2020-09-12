package com.example.foxizz.navigation.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.activity.SettingsActivity;
import com.example.foxizz.navigation.activity.fragment.MainFragment;
import com.example.foxizz.navigation.activity.fragment.UserFragment;

//用户数据帮助类
public class UserDataHelper {

    private MainActivity mainActivity;
    private MainFragment mainFragment;
    private UserFragment userFragment;
    private SettingsActivity settingsActivity;

    public UserDataHelper(Context context, DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        if(context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
            mainFragment = mainActivity.getMainFragment();
            userFragment = mainActivity.getUserFragment();
        }
        if(context instanceof SettingsActivity) settingsActivity = (SettingsActivity) context;
    }

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

}
