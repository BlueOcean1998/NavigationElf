package cn.zerokirby.api.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static cn.zerokirby.api.ZerokirbyApi.getApplication;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String CREATE_USER = "create table User ("
            + "user_id text, "
            + "username text, "
            + "password text, "
            + "language text, "
            + "version text, "
            + "display text, "
            + "model text, "
            + "brand text, "
            + "register_time long, "
            + "last_use long, "
            + "last_sync long, "
            + "avatar blob)";

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
