package cn.zerokirby.api.data

import android.text.TextUtils
import base.foxizz.util.SystemUtil
import cn.zerokirby.api.Constants
import cn.zerokirby.api.data.DatabaseHelper.Companion.databaseHelper
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

/**
 * 用户数据帮助类
 */
object UserDataHelper {
    /**
     * 初始化手机信息
     */
    fun initPhoneInfo() {
        try {
            databaseHelper.writableDatabase.use { db ->
                db.execSQL("update User set language = ?, version = ?, " +
                        "display = ?, model = ?, brand = ?", arrayOf(
                        SystemUtil.systemLanguage,
                        SystemUtil.systemVersion,
                        SystemUtil.systemDisplay,
                        SystemUtil.systemModel,
                        SystemUtil.deviceBrand))
            }
        } catch (e: Exception) {
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
    fun loginRegisterSendRequest(
            username: String, password: String, isLogin: Boolean): JSONObject {
        var response: Response? = null
        return try {
            val client = OkHttpClient() //使用OkHttp发送HTTP请求调用服务端登录servlet
            //创建请求返回的数据格式
            val requestBody = FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .add("language", SystemUtil.systemLanguage)
                    .add("version", SystemUtil.systemVersion)
                    .add("display", SystemUtil.systemDisplay)
                    .add("model", SystemUtil.systemModel)
                    .add("brand", SystemUtil.deviceBrand).build()
            //发送登录或注册请求
            val request: Request = if (isLogin) { //登录页
                Request.Builder().url(Constants.LOGIN_URL).post(requestBody).build()
            } else { //注册页
                Request.Builder().url(Constants.REGISTER_URL).post(requestBody).build()
            }
            response = client.newCall(request).execute() //等待接收返回数据
            val responseData = response.body?.string() ?: "" //将得到的数据转为String类型
            JSONObject(responseData)
        } catch (e: Exception) {
            JSONObject()
        } finally {
            response?.close()
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
    fun loginRegisterInitUserInfo(
            jsonObject: JSONObject, username: String, password: String, isLogin: Boolean) {
        try {
            val user = User()
            val userId = jsonObject.getString("Id")
            user.userId = userId
            user.username = username
            user.password = password
            user.lastUse = System.currentTimeMillis()
            if (isLogin) { //登录
                user.registerTime = jsonObject.getLong("RegisterTime")
                user.lastSync = jsonObject.getLong("SyncTime")
            } else { //注册
                user.registerTime = System.currentTimeMillis()
            }
            login(user) //更新用户数据库
        } catch (e: Exception) {
        }
    }

    /**
     * 获取登录用户id
     *
     * @return userId
     */
    val loginUserId: String
        get() {
            try {
                databaseHelper.readableDatabase.use { db ->
                    db.rawQuery("select * from User", null).use { cursor ->
                        if (cursor != null && cursor.moveToFirst()) {
                            cursor.run {
                                do {
                                    if (!TextUtils.isEmpty(
                                                    getString(getColumnIndex("password")))) {
                                        return getString(getColumnIndex("user_id"))
                                    }
                                } while (moveToNext())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
            }
            return "0"
        }


    /**
     * 登录，保存用户数据
     *
     * @param user 用户对象
     */
    private fun login(user: User) {
        try {
            databaseHelper.writableDatabase.use { db ->
                user.run {
                    db.execSQL("update User set user_id = ?, username = ?, password = ?, " +
                            "register_time = ?, last_use = ?, last_sync = ? where user_id = '0'",
                            arrayOf(userId,
                                    username,
                                    password,
                                    registerTime,
                                    System.currentTimeMillis(),
                                    lastSync)
                    )
                }
            }
        } catch (e: Exception) {
        }
    }

    /**
     * 退出登录，清空登录用户除用户名和密码外的信息
     */
    fun logout() {
        updateUser("user_id", "0", loginUserId)
        AvatarDataHelper.saveAvatar("".toByteArray(), "0") //清空数据库中的头像
    }

    /**
     * 获取某一用户信息
     *
     * @param userId 用户id
     * @return User
     */
    fun getUser(userId: String): User {
        val user = User()
        try {
            databaseHelper.readableDatabase.use { db ->
                db.rawQuery("select * from User where user_id = ?", arrayOf(userId)).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        cursor.run {
                            user.run {
                                this.userId = userId //设置用户id
                                username = getString(getColumnIndex("username"))
                                password = getString(getColumnIndex("password"))
                                language = getString(getColumnIndex("language"))
                                version = getString(getColumnIndex("version"))
                                display = getString(getColumnIndex("display"))
                                model = getString(getColumnIndex("model"))
                                brand = getString(getColumnIndex("brand"))
                                registerTime = getLong(getColumnIndex("register_time"))
                                lastUse = getLong(getColumnIndex("last_use"))
                                lastSync = getLong(getColumnIndex("last_sync"))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
        }
        return user
    }

    /**
     * 更新单列用户数据
     *
     * @param column 列名
     * @param value  值
     */
    fun updateUser(column: String, value: String, userId: String) {
        try {
            databaseHelper.writableDatabase.use { db ->
                db.execSQL("update User set $column = ? where user_id = ?",
                        arrayOf(value, userId))
            }
        } catch (e: Exception) {
        }
    }
}