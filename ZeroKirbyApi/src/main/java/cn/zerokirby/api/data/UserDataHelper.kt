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
    fun initPhoneInfo() = try {
        databaseHelper.writableDatabase.use { db ->
            SystemUtil.run {
                db.execSQL(
                    "update User set language = ?, version = ?, " +
                            "display = ?, model = ?, brand = ?", arrayOf(
                        systemLanguage,
                        systemVersion,
                        systemDisplay,
                        systemModel,
                        deviceBrand
                    )
                )
            }
        }
    } catch (e: Exception) {
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
        username: String, password: String, isLogin: Boolean
    ): JSONObject {
        var response: Response? = null
        return try {
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
            val request = Request.Builder().url(
                if (isLogin) Constants.LOGIN_URL //登录页
                else Constants.REGISTER_URL //注册页
            ).post(requestBody).build()
            response = OkHttpClient().newCall(request).execute()
            val responseData = response.body?.string() ?: ""
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
        jsonObject: JSONObject, username: String, password: String, isLogin: Boolean
    ) = try {
        val userId = jsonObject.getString("Id")
        User().let {
            it.userId = userId
            it.username = username
            it.password = password
            it.lastUse = System.currentTimeMillis()
            if (isLogin) { //登录
                it.registerTime = jsonObject.getLong("RegisterTime")
                it.lastSync = jsonObject.getLong("SyncTime")
            } else { //注册
                it.registerTime = System.currentTimeMillis()
            }
            login(it) //更新用户数据库
        }
    } catch (e: Exception) {
    }

    /**
     * 获取登录用户id
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
                                            getString(getColumnIndex("password"))
                                        )
                                    ) {
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
    private fun login(user: User) = try {
        databaseHelper.writableDatabase.use { db ->
            user.run {
                db.execSQL(
                    "update User set user_id = ?, username = ?, password = ?, " +
                            "register_time = ?, last_use = ?, last_sync = ? where user_id = '0'",
                    arrayOf(
                        userId,
                        username,
                        password,
                        registerTime,
                        System.currentTimeMillis(),
                        lastSync
                    )
                )
            }
        }
    } catch (e: Exception) {
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
     */
    fun getUser(userId: String) = User().apply {
        try {
            databaseHelper.readableDatabase.use { db ->
                db.rawQuery("select * from User where user_id = ?", arrayOf(userId))
                    .use { cursor ->
                        if (cursor != null && cursor.moveToFirst()) {
                            this.userId = userId //设置用户id
                            cursor.run {
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
        } catch (e: Exception) {
        }
    }

    /**
     * 更新单列用户数据
     *
     * @param column 列名
     * @param value  值
     */
    fun updateUser(column: String, value: String, userId: String) = try {
        databaseHelper.writableDatabase.use { db ->
            db.execSQL(
                "update User set $column = ? where user_id = ?",
                arrayOf(value, userId)
            )
        }
    } catch (e: Exception) {
    }
}