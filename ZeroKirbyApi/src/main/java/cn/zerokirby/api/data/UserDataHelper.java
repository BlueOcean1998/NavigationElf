package cn.zerokirby.api.data;

import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

import java.util.Objects;

import cn.zerokirby.api.util.SystemUtil;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserDataHelper {

    public static void initUserDataHelper() {
        databaseHelper = new DatabaseHelper(Constants.LOCAL_DATABASE, null, 1);
        initPhoneInfo();//初始化手机信息
    }

    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase db;
    private static android.database.Cursor cursor;

    /**
     * 初始化手机信息
     */
    public static void initPhoneInfo() {
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("update User set language = ?, version = ?, " +
                            "display = ?, model = ?, brand = ?",
                    new String[]{
                            SystemUtil.getSystemLanguage(),
                            SystemUtil.getSystemVersion(),
                            SystemUtil.getSystemDisplay(),
                            SystemUtil.getSystemModel(),
                            SystemUtil.getDeviceBrand()});
        } catch (Exception ignored) {

        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * 提交账号信息，获取返回结果
     * 请不要在主线程使用该方法！！！
     *
     * @param username 用户名
     * @param password 密码
     * @param isLogin  是否是登录（而不是注册）
     * @return 从服务器获取到的json字符串
     */
    public static JSONObject sendRequestWithOkHttp(
            final String username, final String password, final boolean isLogin) {
        Response response = null;
        try {
            OkHttpClient client = new OkHttpClient();//使用OkHttp发送HTTP请求调用服务端登录servlet
            //创建请求返回的数据格式
            RequestBody requestBody = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .add("language", SystemUtil.getSystemLanguage())
                    .add("version", SystemUtil.getSystemVersion())
                    .add("display", SystemUtil.getSystemDisplay())
                    .add("model", SystemUtil.getSystemModel())
                    .add("brand", SystemUtil.getDeviceBrand()).build();
            //发送登录或注册请求
            Request request;
            if (isLogin) {//登录页
                request = new Request.Builder().url(Constants.LOGIN_URL).post(requestBody).build();
            } else {//注册页
                request = new Request.Builder().url(Constants.REGISTER_URL).post(requestBody).build();
            }

            response = client.newCall(request).execute();//等待接收返回数据
            String responseData;//将得到的数据转为String类型
            responseData = Objects.requireNonNull(response.body()).string();

            return new JSONObject(responseData);
        } catch (Exception ignored) {
            return null;
        } finally {
            if (response != null) response.close();
        }
    }

    /**
     * 初始化用户信息
     *
     * @param jsonObject 从服务器获取到的json字符串
     * @param username   用户名
     * @param password   密码
     * @param isLogin    是否是登录（而不是注册）
     */
    public static void initUserInfo(
            final JSONObject jsonObject,
            final String username, final String password, final boolean isLogin) {
        try {
            User user = new User();
            String userId = jsonObject.getString("Id");
            user.setUserId(userId);
            user.setUsername(username);
            user.setPassword(password);
            user.setLastUse(System.currentTimeMillis());
            if (isLogin) {//登录
                user.setRegisterTime(jsonObject.getLong("RegisterTime"));
                user.setLastSync(jsonObject.getLong("SyncTime"));
            } else {//注册
                user.setRegisterTime(System.currentTimeMillis());
            }
            login(user);//更新用户数据库
        } catch (Exception ignored) {

        }
    }

    /**
     * 获取用户信息
     *
     * @return User 用户对象
     */
    public static User getUser() {
        User user = new User();
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.rawQuery("select * from User", null);//查询用户数据表

            if (cursor.moveToFirst()) {
                user.setValid(true);//默认为有效用户
                user.setUserId(cursor.getString(cursor.getColumnIndex("user_id")));//读取ID
                user.setUsername(cursor.getString(cursor.getColumnIndex("username")));//读取用户名
                user.setPassword(cursor.getString(cursor.getColumnIndex("password")));//读取密码
                user.setLanguage(cursor.getString(cursor.getColumnIndex("language")));//读取语言
                user.setVersion(cursor.getString(cursor.getColumnIndex("version")));//读取版本
                user.setDisplay(cursor.getString(cursor.getColumnIndex("display")));//读取显示信息
                user.setModel(cursor.getString(cursor.getColumnIndex("model")));//读取型号
                user.setBrand(cursor.getString(cursor.getColumnIndex("brand")));//读取品牌
                user.setRegisterTime(cursor.getLong(cursor.getColumnIndex("register_time")));//读取注册时间
                user.setLastUse(cursor.getLong(cursor.getColumnIndex("last_use")));//读取上次登录时间
                user.setLastSync(cursor.getLong(cursor.getColumnIndex("last_sync")));//读取上次同步时间
            }
        } catch (Exception ignored) {

        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return user;
    }

    /**
     * 登录，保存用户数据
     *
     * @param user 用户类
     */
    public static void login(User user) {
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("update User set user_id = ?, username = ?, password = ?, " +
                            "register_time = ?, last_use = ?, last_sync = ?",
                    new String[]{
                            user.getUserId(),
                            user.getUsername(),
                            user.getPassword(),
                            String.valueOf(user.getRegisterTime()),
                            String.valueOf(System.currentTimeMillis()),
                            String.valueOf(user.getLastSync())});
        } catch (Exception ignored) {

        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * 更新单列用户数据
     *
     * @param column 列名
     * @param value  值
     */
    public static void updateUser(String column, String value) {
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("update User set " + column + " = ?",
                    new String[]{value});
        } catch (Exception ignored) {

        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * 更新同步时间
     */
    public static void updateSyncTime() {
        updateUser("last_sync", String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 退出登录，设置用户ID和上次同步时间为0
     */
    public static void logout() {
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("update User set user_id = ?, last_sync = ?",
                    new String[]{"0", "0"});
        } catch (Exception ignored) {

        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * 关闭数据库，防止内存泄漏
     */
    public static void close() {
        if (databaseHelper != null) databaseHelper.close();
    }

}
