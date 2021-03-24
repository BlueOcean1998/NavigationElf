package com.navigation.foxizz.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.navigation.foxizz.R
import com.navigation.foxizz.data.Constants
import com.navigation.foxizz.data.SPHelper
import com.navigation.foxizz.data.SearchDataHelper
import com.navigation.foxizz.util.CityUtil
import com.navigation.foxizz.util.showToast
import com.navigation.foxizz.view.AdaptiveTextView
import java.util.*

/**
 * 设置页
 */
class SettingsActivity : BaseActivity() {
    companion object {
        private lateinit var mLocalBroadcastManager: LocalBroadcastManager//本地广播管理器

        /**
         * 启动设置页
         *
         * @param context 上下文
         * @param mCity   所在城市
         */
        fun startActivity(context: Context, mCity: String?) {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.putExtra(Constants.MY_CITY, mCity)
            context.startActivity(intent)
        }

        //显示删除所有搜索记录对话框
        private fun showDeleteAllSearchDataDialog(context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.warning))
            builder.setMessage(context.getString(R.string.to_clear))
            builder.setPositiveButton(R.string.clear) { _, _ -> //发送本地广播通知清空搜索记录
                mLocalBroadcastManager.sendBroadcast(Intent(Constants.SETTINGS_BROADCAST)
                        .putExtra(Constants.SETTINGS_TYPE, Constants.CLEAN_RECORD))
                SearchDataHelper.deleteSearchData() //清空数据库中的搜索记录
                R.string.has_cleared.showToast()
            }
            builder.setNegativeButton(R.string.cancel) { _, _ ->
                //do nothing
            }
            builder.show()
        }
    }

    //设置地图类型
    private lateinit var tvMapStandard: ImageView //标准地图
    private lateinit var adaptiveTvMapStandard: AdaptiveTextView
    private lateinit var ivMapSatellite: ImageView //卫星地图
    private lateinit var adaptiveMapSatellite: AdaptiveTextView
    private lateinit var ivMapTraffic: ImageView //交通地图
    private lateinit var adaptiveMapTraffic: AdaptiveTextView

    //设置目的地所在城市
    private lateinit var btDestinationCity: Button //回到所在城市
    private lateinit var etDestinationCity: EditText //目标城市
    private lateinit var ibDestinationCityConfirm: ImageButton //确定
    private lateinit var ibDestinationCityCancel: ImageButton //取消
    private lateinit var mCity: String //所在城市
    private lateinit var saveCity: String //存储的城市
    private lateinit var textCity: String //输入框内输入的城市
    private lateinit var imm: InputMethodManager //键盘

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
        initView() //初始化控件

        //初始化PreferenceScreen
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_settings_preferences, PreferenceScreen())
                .commit()

        //获取键盘对象
        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    }

    //提交输入的城市
    private fun commitCity() {
        ibDestinationCityConfirm.visibility = View.GONE //隐藏确定按钮
        ibDestinationCityCancel.visibility = View.GONE //隐藏取消按钮

        //将城市信息录入sharedPreferences
        SPHelper.putString(Constants.DESTINATION_CITY, textCity)
        saveCity = textCity
    }

    //监听按键抬起事件
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        //如果是Enter键
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            commitCity() //提交输入的城市
            imm.hideSoftInputFromWindow(window.decorView.windowToken, 0) //收回键盘
            return false
        }
        return super.onKeyUp(keyCode, event)
    }

    //初始化控件
    private fun initView() {
        tvMapStandard = findViewById(R.id.iv_map_standard)
        adaptiveTvMapStandard = findViewById(R.id.adaptive_tv_map_standard)
        ivMapSatellite = findViewById(R.id.iv_map_satellite)
        adaptiveMapSatellite = findViewById(R.id.adaptive_tv_map_satellite)
        ivMapTraffic = findViewById(R.id.iv_map_traffic)
        adaptiveMapTraffic = findViewById(R.id.adaptive_tv_map_traffic)
        btDestinationCity = findViewById(R.id.bt_destination_city)
        etDestinationCity = findViewById(R.id.et_destination_city)
        ibDestinationCityConfirm = findViewById(R.id.ib_destination_city_confirm)
        ibDestinationCityCancel = findViewById(R.id.ib_destination_city_cancel)
        when (Objects.requireNonNull(SPHelper.getString("map_type", Constants.STANDARD_MAP))) {
            Constants.STANDARD_MAP -> {
                tvMapStandard.setImageResource(R.drawable.map_standard_on)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    adaptiveTvMapStandard.setTextColor(getColor(R.color.deepblue))
            }
            Constants.SATELLITE_MAP -> {
                ivMapSatellite.setImageResource(R.drawable.map_satellite_on)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    adaptiveMapSatellite.setTextColor(getColor(R.color.deepblue))
            }
            Constants.TRAFFIC_MAP -> {
                ivMapTraffic.setImageResource(R.drawable.map_traffic_on)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    adaptiveMapTraffic.setTextColor(getColor(R.color.deepblue))
            }
        }

        //标准地图的点击事件
        tvMapStandard.setOnClickListener { //修改图标和文字颜色
            tvMapStandard.setImageResource(R.drawable.map_standard_on)
            adaptiveTvMapStandard.setTextColor(getColor(R.color.deepblue))
            ivMapSatellite.setImageResource(R.drawable.map_satellite_off)
            adaptiveMapSatellite.setTextColor(getColor(R.color.black))
            ivMapTraffic.setImageResource(R.drawable.map_traffic_off)
            adaptiveMapTraffic.setTextColor(getColor(R.color.black))

            //保存地图类型到sharedPreferences
            SPHelper.putString(Constants.MAP_TYPE, Constants.STANDARD_MAP)

            //发送本地广播通知更新地图类型
            mLocalBroadcastManager.sendBroadcast(Intent(Constants.SETTINGS_BROADCAST)
                    .putExtra(Constants.SETTINGS_TYPE, Constants.SET_MAP_TYPE))
        }

        //卫星地图的点击事件
        ivMapSatellite.setOnClickListener { //修改图标和文字颜色
            tvMapStandard.setImageResource(R.drawable.map_standard_off)
            adaptiveTvMapStandard.setTextColor(getColor(R.color.black))
            ivMapSatellite.setImageResource(R.drawable.map_satellite_on)
            adaptiveMapSatellite.setTextColor(getColor(R.color.deepblue))
            ivMapTraffic.setImageResource(R.drawable.map_traffic_off)
            adaptiveMapTraffic.setTextColor(getColor(R.color.black))

            //保存地图类型到sharedPreferences
            SPHelper.putString(Constants.MAP_TYPE, Constants.SATELLITE_MAP)

            //发送本地广播通知更新地图类型
            mLocalBroadcastManager.sendBroadcast(Intent(Constants.SETTINGS_BROADCAST)
                    .putExtra(Constants.SETTINGS_TYPE, Constants.SET_MAP_TYPE))
        }

        //交通地图的点击事件
        ivMapTraffic.setOnClickListener { //修改图标和文字颜色
            tvMapStandard.setImageResource(R.drawable.map_standard_off)
            adaptiveTvMapStandard.setTextColor(getColor(R.color.black))
            ivMapSatellite.setImageResource(R.drawable.map_satellite_off)
            adaptiveMapSatellite.setTextColor(getColor(R.color.black))
            ivMapTraffic.setImageResource(R.drawable.map_traffic_on)
            adaptiveMapTraffic.setTextColor(getColor(R.color.deepblue))

            //保存地图类型到sharedPreferences
            SPHelper.putString(Constants.MAP_TYPE, Constants.TRAFFIC_MAP)

            //发送本地广播通知更新地图类型
            mLocalBroadcastManager.sendBroadcast(Intent(Constants.SETTINGS_BROADCAST)
                    .putExtra(Constants.SETTINGS_TYPE, Constants.SET_MAP_TYPE))
        }
        ibDestinationCityConfirm.visibility = View.GONE //隐藏确定按钮
        ibDestinationCityCancel.visibility = View.GONE //隐藏取消按钮

        //获取从MainActivity中传来的所在城市名
        val intent = intent
        mCity = intent.getStringExtra(Constants.MY_CITY)
        if (mCity.isEmpty()) {
            mCity = SPHelper.getString(Constants.MY_CITY, "")
        }

        //设置城市信息
        saveCity = SPHelper.getString(Constants.DESTINATION_CITY, "")
        if (saveCity.isNotEmpty()) //如果存储的城市信息不为空
            etDestinationCity.setText(saveCity) //设置城市信息

        //设置提示信息为所在城市
        etDestinationCity.hint = mCity

        //所在城市按钮的点击事件
        btDestinationCity.setOnClickListener {
            saveCity = ""
            etDestinationCity.setText("") //清空输入框
            commitCity() //提交输入的城市
        }

        //监听输入框的内容变化
        etDestinationCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                //获取输入框内的城市信息
                textCity = etDestinationCity.text.toString()
                if (textCity != saveCity) { //不等于存储的城市名
                    ibDestinationCityCancel.visibility = View.VISIBLE //显示取消按钮
                    //城市名为空或校验通过
                    if (textCity.isEmpty()
                            || CityUtil.checkCityName(textCity)
                            || CityUtil.checkProvinceName(textCity)) {
                        ibDestinationCityConfirm.visibility = View.VISIBLE //显示确定按钮
                    } else {
                        ibDestinationCityConfirm.visibility = View.GONE //隐藏确定按钮
                    }
                } else {
                    ibDestinationCityConfirm.visibility = View.GONE //隐藏确定按钮
                    ibDestinationCityCancel.visibility = View.GONE //隐藏取消按钮
                }
            }
        })

        //确定按钮的点击事件
        ibDestinationCityConfirm.setOnClickListener {
            commitCity() //提交输入的城市
        }

        //取消按钮的点击事件
        ibDestinationCityCancel.setOnClickListener {
            ibDestinationCityConfirm.visibility = View.GONE //隐藏确定按钮
            ibDestinationCityCancel.visibility = View.GONE //隐藏取消按钮
            if (saveCity.isEmpty()) //如果存储的城市信息为空
                etDestinationCity.setText("") //清空输入框
            else {
                etDestinationCity.setText(saveCity) //恢复城市数据
                etDestinationCity.setSelection(saveCity.length) //移动焦点到末尾
            }
        }
    }

    //PreferenceScreen
    class PreferenceScreen : PreferenceFragmentCompat() {
        //创建PreferenceScreen
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_settings, rootKey)
        }

        //设置PreferenceScreen的点击事件
        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            when (preference.key) {
                //发送本地广播通知更新是否允许横屏
                Constants.KEY_LANDSCAPE ->
                    mLocalBroadcastManager.sendBroadcast(Intent(Constants.SETTINGS_BROADCAST)
                            .putExtra(Constants.SETTINGS_TYPE, Constants.SET_LANDSCAPE))
                //发送本地广播通知更新否启用3D视角
                Constants.KEY_ANGLE_3D ->
                    mLocalBroadcastManager.sendBroadcast(Intent(Constants.SETTINGS_BROADCAST)
                            .putExtra(Constants.SETTINGS_TYPE, Constants.SET_ANGLE_3D))
                //发送本地广播通知更新是否允许地图旋转
                Constants.KEY_MAP_ROTATION ->
                    mLocalBroadcastManager.sendBroadcast(Intent(Constants.SETTINGS_BROADCAST)
                            .putExtra(Constants.SETTINGS_TYPE, Constants.SET_MAP_ROTATION))
                //发送本地广播通知更新是否显示比例尺
                Constants.KEY_SCALE_CONTROL ->
                    mLocalBroadcastManager.sendBroadcast(Intent(Constants.SETTINGS_BROADCAST)
                            .putExtra(Constants.SETTINGS_TYPE, Constants.SET_SCALE_CONTROL))
                //发送本地广播通知更新是否显示缩放按钮
                Constants.KEY_ZOOM_CONTROLS ->
                    mLocalBroadcastManager.sendBroadcast(Intent(Constants.SETTINGS_BROADCAST)
                            .putExtra(Constants.SETTINGS_TYPE, Constants.SET_ZOOM_CONTROLS))
                //发送本地广播通知更新是否显示指南针
                Constants.KEY_COMPASS ->
                    mLocalBroadcastManager.sendBroadcast(Intent(Constants.SETTINGS_BROADCAST)
                            .putExtra(Constants.SETTINGS_TYPE, Constants.SET_COMPASS))
                Constants.KEY_INTELLIGENT_SEARCH -> {
                }
                //显示删除所有搜索记录对话框
                Constants.KEY_SEARCH_RECORD -> showDeleteAllSearchDataDialog(requireActivity())
            }
            return super.onPreferenceTreeClick(preference)
        }
    }
}