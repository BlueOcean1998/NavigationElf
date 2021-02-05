package cn.zerokirby.api.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static cn.zerokirby.api.ZerokirbyApi.getApplication;

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

    private static final String CREATE_USER = "create table User ("
            + "user_id text, "//用户id
            + "username text, "//用户名
            + "password text, "//密码
            + "language text, "//语言
            + "version text, "//版本
            + "display text, "//显示信息
            + "model text, "//型号
            + "brand text, "//品牌
            + "register_time long, "//注册时间
            + "last_use long, "//上次使用时间
            + "last_sync long, "//次同步时间
            + "avatar blob)";//头像

    private static final String INIT_USER = "insert into User " +
            "(user_id, username, password, register_time, last_use, last_sync) " +
            "values('0', '', '', '0', '0', '0')";

    public DatabaseHelper(String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(getApplication(), name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {//建用户表并初始化
        db.execSQL(CREATE_USER);
        db.execSQL(INIT_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {//升级数据库
        db.execSQL("drop table if exists User");
        onCreate(db);
    }

}
