package com.navigation.foxizz.data;

/**
 * 常量
 */
public class Constants {

    public final static String MY_CITY = "my_city";//所在城市

    //SettingsSharedPreferences的设置相关键
    public final static String SETTINGS_SHARED_PREFERENCES = "settings";//设置SharedPreferences
    public final static String MAP_TYPE = "map_type";//地图类型
    public final static String STANDARD_MAP = "standard_map";//标准地图
    public final static String SATELLITE_MAP = "satellite_map";//卫星地图
    public final static String TRAFFIC_MAP = "traffic_map";//交通地图
    public final static String DESTINATION_CITY = "destination_city";//目的城市
    public final static String REMEMBER_USERNAME = "remember_username";//记住用户名
    public final static String REMEMBER_PASSWORD = "remember_password";//记住密码

    //DefaultSharedPreferences的设置相关键
    public final static String KEY_LANDSCAPE = "landscape";
    public final static String KEY_ANGLE_3D = "angle_3d";
    public final static String KEY_MAP_ROTATION = "map_rotation";
    public final static String KEY_SCALE_CONTROL = "scale_control";
    public final static String KEY_ZOOM_CONTROLS = "zoom_controls";
    public final static String KEY_COMPASS = "compass";
    public final static String KEY_SEARCH_AROUND = "search_around";
    public final static String KEY_SEARCH_RECORD = "search_record";

    //SettingsBroadcast的广播类型
    public final static String SETTINGS_BROADCAST = "com.navigation.foxizz.navigation.broadcast.SETTINGS_BROADCAST";//设置变化广播
    public final static String SETTINGS_TYPE = "settings_type";//设置广播类型
    public final static int SET_MAP_TYPE = 1;//设置地图类型
    public final static int SET_LANDSCAPE = 2;//设置是否允许横屏
    public final static int SET_ANGLE_3D = 3;//设置是否启用3D视角
    public final static int SET_MAP_ROTATION = 4;//设置是否允许地图旋转
    public final static int SET_SCALE_CONTROL = 5;//设置是否显示比例尺
    public final static int SET_ZOOM_CONTROLS = 6;//设置是否显示缩放按钮
    public final static int SET_COMPASS = 7;//设置是否显示指南针
    public final static int CLEAN_RECORD = 8;//清空搜索记录

    //DefaultSharedPreferences的用户相关键
    public final static String KEY_TO_SETTINGS = "to_settings";
    public final static String KEY_CHECK_UPDATE = "check_update";
    public final static String KEY_SOUND_CODE = "sound_code";
    public final static String KEY_CONTACT_ME = "contact_me";
    public final static String KEY_LOGOUT = "logout";

    //LoginBroadcast的广播类型
    public final static String LOGIN_BROADCAST = "com.navigation.foxizz.navigation.broadcast.LOGIN_BROADCAST";//登录变化广播
    public final static String LOGIN_TYPE = "login_type";//设置广播类型
    public final static int SET_AVATAR = 1;//设置头像

}
