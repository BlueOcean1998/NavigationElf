/**
 * App name: NavigationElf
 * Author: Foxizz
 * Accomplish date: 2020-04-30
 * Last modify date: 2021-06-24
 */
object Constants {
    const val LOCAL_DATABASE = "Navigate.db" //本地数据库

    //SettingsSharedPreferences的设置相关键
    const val MAP_TYPE = "map_type" //地图类型
    const val STANDARD_MAP = "standard_map" //标准地图
    const val SATELLITE_MAP = "satellite_map" //卫星地图
    const val TRAFFIC_MAP = "traffic_map" //交通地图
    const val MY_CITY = "my_city" //所在城市
    const val DESTINATION_CITY = "destination_city" //目的城市
    const val OFFLINE_CITIES = "offline_cities" //已下载离线地图的城市id
    const val REMEMBER_USERNAME = "remember_username" //记住用户名
    const val REMEMBER_PASSWORD = "remember_password" //记住密码

    //DefaultSharedPreferences的设置相关键
    const val KEY_LANDSCAPE = "landscape"
    const val KEY_ANGLE_3D = "angle_3d"
    const val KEY_MAP_ROTATION = "map_rotation"
    const val KEY_SCALE_CONTROL = "scale_control"
    const val KEY_ZOOM_CONTROLS = "zoom_controls"
    const val KEY_COMPASS = "compass"
    const val KEY_INTELLIGENT_SEARCH = "intelligent_search"
    const val KEY_SEARCH_RECORD = "search_record"

    //DefaultSharedPreferences的用户相关键
    const val KEY_TO_SETTINGS = "to_settings"
    const val KEY_CHECK_UPDATE = "check_update"
    const val KEY_SOUND_CODE = "sound_code"
    const val KEY_CONTACT_ME = "contact_me"
    const val KEY_LOGOUT = "logout"

    //系统广播类型
    const val CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE" //网络连接状态改变

    //SettingsBroadcast的广播类型
    const val SETTINGS_BROADCAST =
        "com.navigation.foxizz.navigation.broadcast.SETTINGS_BROADCAST" //设置变化广播
    const val SETTINGS_TYPE = "settings_type" //设置广播类型
    const val SET_MAP_TYPE = 1 //设置地图类型
    const val SET_LANDSCAPE = 2 //设置是否允许横屏
    const val SET_ANGLE_3D = 3 //设置是否启用3D视角
    const val SET_MAP_ROTATION = 4 //设置是否允许地图旋转
    const val SET_SCALE_CONTROL = 5 //设置是否显示比例尺
    const val SET_ZOOM_CONTROLS = 6 //设置是否显示缩放按钮
    const val SET_COMPASS = 7 //设置是否显示指南针
    const val CLEAN_RECORD = 8 //清空搜索记录

    //LoginBroadcast的广播类型
    const val LOGIN_BROADCAST =
        "com.navigation.foxizz.navigation.broadcast.LOGIN_BROADCAST" //登录变化广播
    const val LOGIN_TYPE = "login_type" //设置广播类型
    const val SET_USERNAME = 1 //设置用户名
    const val SET_AVATAR = 2 //设置头像
}