package cn.zerokirby.api.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.Objects;

import cn.zerokirby.api.util.SystemUtil;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 用户数据帮助类
 */
public class UserDataHelper {

    /**
     * 初始化手机信息
     */
    public static void initPhoneInfo() {
        try (SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getWritableDatabase()) {
            db.execSQL("update User set language = ?, version = ?, " +
                            "display = ?, model = ?, brand = ?",
                    new String[]{
                            SystemUtil.getSystemLanguage(),
                            SystemUtil.getSystemVersion(),
                            SystemUtil.getSystemDisplay(),
                            SystemUtil.getSystemModel(),
                            SystemUtil.getDeviceBrand()});
        } catch (Exception ignored) {

        }
    }

    /**
     * 登录或注册后向服务器提交账号信息并获取返回结果
     * 请不要在主线程使用该方法！！！
     *
     * @param username 用户名
     * @param password 密码
     * @param isLogin  是否是登录（而不是注册）
     * @return 从服务器获取到的json字符串
     */
    public static JSONObject loginRegisterSendRequest(
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
     * 登录或注册后从服务器读取用户信息并录入本地数据库
     *
     * @param jsonObject 从服务器获取到的json字符串
     * @param username   用户名
     * @param password   密码
     * @param isLogin    是否是登录（而不是注册）
     */
    public static void loginRegisterInitUserInfo(
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
     * 获取登录用户id
     *
     * @return userId
     */
    public static String getLoginUserId() {
        try (SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getReadableDatabase();
             Cursor cursor = db.rawQuery("select * from User", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    if (!TextUtils.isEmpty(
                            cursor.getString(cursor.getColumnIndex("password")))) {
                        return cursor.getString(cursor.getColumnIndex("user_id"));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception ignored) {

        }
        return "0";
    }

    /**
     * 登录，保存用户数据
     *
     * @param user 用户对象
     */
    public static void login(User user) {
        try (SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getWritableDatabase()) {
            db.execSQL("update User set user_id = ?, username = ?, password = ?, " +
                            "register_time = ?, last_use = ?, last_sync = ? where user_id = '0'",
                    new String[]{
                            user.getUserId(),
                            user.getUsername(),
                            user.getPassword(),
                            String.valueOf(user.getRegisterTime()),
                            String.valueOf(System.currentTimeMillis()),
                            String.valueOf(user.getLastSync())});
        } catch (Exception ignored) {

        }
    }

    /**
     * 退出登录，清空登录用户除用户名和密码外的信息
     */
    public static void logout() {
        String userId = getLoginUserId();
        updateUser("user_id", "0", userId);
        AvatarDataHelper.saveAvatar("".getBytes(), "0");//清空数据库中的头像
    }

    /**
     * 获取某一用户信息
     *
     * @param userId 用户id
     * @return User
     */
    public static User getUser(String userId) {
        User user = new User();
        try (SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getReadableDatabase();
             Cursor cursor = db.rawQuery("select * from User where user_id = ?",
                     new String[]{userId})) {
            if (cursor != null && cursor.moveToFirst()) {
                user.setUserId(userId);//设置用户id
                user.setUsername(cursor.getString(cursor.getColumnIndex("username")));
                user.setPassword(cursor.getString(cursor.getColumnIndex("password")));
                user.setLanguage(cursor.getString(cursor.getColumnIndex("language")));
                user.setVersion(cursor.getString(cursor.getColumnIndex("version")));
                user.setDisplay(cursor.getString(cursor.getColumnIndex("display")));
                user.setModel(cursor.getString(cursor.getColumnIndex("model")));
                user.setBrand(cursor.getString(cursor.getColumnIndex("brand")));
                user.setRegisterTime(cursor.getLong(cursor.getColumnIndex("register_time")));
                user.setLastUse(cursor.getLong(cursor.getColumnIndex("last_use")));
                user.setLastSync(cursor.getLong(cursor.getColumnIndex("last_sync")));
            }
        } catch (Exception ignored) {

        }
        return user;
    }

    /**
     * 更新单列用户数据
     *
     * @param column 列名
     * @param value  值
     */
    public static void updateUser(String column, String value, String userId) {
        try (SQLiteDatabase db = DatabaseHelper.getDatabaseHelper().getWritableDatabase()) {
            db.execSQL("update User set " + column + " = ? where user_id = ?",
                    new String[]{value, userId});
        } catch (Exception ignored) {

        }
    }

}
