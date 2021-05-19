package com.navigation.foxizz.activity

import Constants
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import base.foxizz.BaseActivity
import base.foxizz.imm
import base.foxizz.lbm
import base.foxizz.util.SPUtil
import base.foxizz.util.showToast
import com.navigation.foxizz.R
import com.navigation.foxizz.data.SearchDataHelper
import com.navigation.foxizz.util.CityUtil
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * 设置页
 */
class SettingsActivity : BaseActivity(R.layout.activity_settings) {
    companion object {
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
    }

    //设置目的地所在城市
    private var mCity = "" //所在城市
    private var saveCity = "" //存储的城市
    private var textCity = "" //输入框内输入的城市

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView() //初始化控件

        //初始化PreferenceScreen
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_settings_preferences, PreferenceScreen())
            .commit()
    }

    //初始化控件
    private fun initView() {
        Constants.run {
            when (SPUtil.getString("map_type", STANDARD_MAP)) {
                STANDARD_MAP -> {
                    iv_map_standard.setImageResource(R.drawable.map_standard_on)
                    adaptive_tv_map_standard.setTextColor(getColor(R.color.deepblue))
                }
                SATELLITE_MAP -> {
                    iv_map_satellite.setImageResource(R.drawable.map_satellite_on)
                    adaptive_tv_map_satellite.setTextColor(getColor(R.color.deepblue))
                }
                TRAFFIC_MAP -> {
                    iv_map_traffic.setImageResource(R.drawable.map_traffic_on)
                    adaptive_tv_map_traffic.setTextColor(getColor(R.color.deepblue))
                }
            }
        }

        //标准地图的点击事件
        iv_map_standard.setOnClickListener { //修改图标和文字颜色
            iv_map_standard.setImageResource(R.drawable.map_standard_on)
            adaptive_tv_map_standard.setTextColor(getColor(R.color.deepblue))
            iv_map_satellite.setImageResource(R.drawable.map_satellite_off)
            adaptive_tv_map_satellite.setTextColor(getColor(R.color.black))
            iv_map_traffic.setImageResource(R.drawable.map_traffic_off)
            adaptive_tv_map_traffic.setTextColor(getColor(R.color.black))

            Constants.run {
                //保存地图类型到sharedPreferences
                SPUtil.put(MAP_TYPE, STANDARD_MAP)

                //发送本地广播通知更新地图类型
                lbm.sendBroadcast(
                    Intent(SETTINGS_BROADCAST).putExtra(SETTINGS_TYPE, SET_MAP_TYPE)
                )
            }
        }

        //卫星地图的点击事件
        iv_map_satellite.setOnClickListener { //修改图标和文字颜色
            iv_map_standard.setImageResource(R.drawable.map_standard_off)
            adaptive_tv_map_standard.setTextColor(getColor(R.color.black))
            iv_map_satellite.setImageResource(R.drawable.map_satellite_on)
            adaptive_tv_map_satellite.setTextColor(getColor(R.color.deepblue))
            iv_map_traffic.setImageResource(R.drawable.map_traffic_off)
            adaptive_tv_map_traffic.setTextColor(getColor(R.color.black))

            Constants.run {
                //保存地图类型到sharedPreferences
                SPUtil.put(MAP_TYPE, SATELLITE_MAP)

                //发送本地广播通知更新地图类型
                lbm.sendBroadcast(
                    Intent(SETTINGS_BROADCAST).putExtra(SETTINGS_TYPE, SET_MAP_TYPE)
                )
            }
        }

        //交通地图的点击事件
        iv_map_traffic.setOnClickListener { //修改图标和文字颜色
            iv_map_standard.setImageResource(R.drawable.map_standard_off)
            adaptive_tv_map_standard.setTextColor(getColor(R.color.black))
            iv_map_satellite.setImageResource(R.drawable.map_satellite_off)
            adaptive_tv_map_satellite.setTextColor(getColor(R.color.black))
            iv_map_traffic.setImageResource(R.drawable.map_traffic_on)
            adaptive_tv_map_traffic.setTextColor(getColor(R.color.deepblue))

            Constants.run {
                //保存地图类型到sharedPreferences
                SPUtil.put(MAP_TYPE, TRAFFIC_MAP)

                //发送本地广播通知更新地图类型
                lbm.sendBroadcast(
                    Intent(SETTINGS_BROADCAST).putExtra(SETTINGS_TYPE, SET_MAP_TYPE)
                )
            }
        }
        ib_destination_city_confirm.visibility = View.GONE //隐藏确定按钮
        ib_destination_city_cancel.visibility = View.GONE //隐藏取消按钮

        Constants.run {
            //获取从MainActivity中传来的所在城市名
            mCity = intent.getStringExtra(MY_CITY)
            if (mCity.isEmpty()) mCity = SPUtil.getString(MY_CITY, "")

            //设置城市信息
            saveCity = SPUtil.getString(DESTINATION_CITY, "")
            if (saveCity.isNotEmpty()) et_destination_city.setText(saveCity)
        }

        //设置提示信息为所在城市
        et_destination_city.hint = mCity

        //所在城市按钮的点击事件
        bt_destination_city.setOnClickListener {
            saveCity = ""
            et_destination_city.setText("") //清空输入框
            commitCity() //提交输入的城市
        }

        //监听输入框的内容变化
        et_destination_city.doAfterTextChanged {
            //获取输入框内的城市信息
            textCity = et_destination_city.text.toString()
            ib_destination_city_cancel.visibility = if (textCity != saveCity) { //不等于存储的城市名
                //城市名为空或校验通过
                ib_destination_city_confirm.visibility = if (textCity.isEmpty()
                    || CityUtil.isCityName(textCity)
                    || CityUtil.isProvinceName(textCity)
                ) View.VISIBLE else View.GONE
                View.VISIBLE
            } else {
                ib_destination_city_confirm.visibility = View.GONE
                View.GONE
            }
        }

        //确定按钮的点击事件
        ib_destination_city_confirm.setOnClickListener {
            commitCity() //提交输入的城市
        }

        //取消按钮的点击事件
        ib_destination_city_cancel.setOnClickListener {
            ib_destination_city_confirm.visibility = View.GONE //隐藏确定按钮
            ib_destination_city_cancel.visibility = View.GONE //隐藏取消按钮
            et_destination_city.run {
                if (saveCity.isEmpty()) //如果存储的城市信息为空
                    setText("") //清空输入框
                else {
                    setText(saveCity) //恢复城市数据
                    setSelection(length()) //移动焦点到末尾
                }
            }
        }
    }

    //提交输入的城市
    private fun commitCity() {
        ib_destination_city_confirm.visibility = View.GONE //隐藏确定按钮
        ib_destination_city_cancel.visibility = View.GONE //隐藏取消按钮

        //将城市信息录入sharedPreferences
        SPUtil.put(Constants.DESTINATION_CITY, textCity)
        saveCity = textCity
    }

    //监听按键抬起事件
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        //如果是Enter键
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            commitCity() //提交输入的城市
            imm.hideSoftInputFromWindow(window.decorView.windowToken, 0) //收回键盘
            return false
        }
        return super.onKeyUp(keyCode, event)
    }

    //PreferenceScreen
    class PreferenceScreen : PreferenceFragmentCompat() {
        //创建PreferenceScreen
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_settings, rootKey)
        }

        //设置PreferenceScreen的点击事件
        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            Constants.run {
                when (preference.key) {
                    //发送本地广播通知更新是否允许横屏
                    KEY_LANDSCAPE -> lbm.sendBroadcast(
                        Intent(SETTINGS_BROADCAST).putExtra(SETTINGS_TYPE, SET_LANDSCAPE)
                    )
                    //发送本地广播通知更新否启用3D视角
                    KEY_ANGLE_3D -> lbm.sendBroadcast(
                        Intent(SETTINGS_BROADCAST).putExtra(SETTINGS_TYPE, SET_ANGLE_3D)
                    )
                    //发送本地广播通知更新是否允许地图旋转
                    KEY_MAP_ROTATION -> lbm.sendBroadcast(
                        Intent(SETTINGS_BROADCAST).putExtra(SETTINGS_TYPE, SET_MAP_ROTATION)
                    )
                    //发送本地广播通知更新是否显示比例尺
                    KEY_SCALE_CONTROL -> lbm.sendBroadcast(
                        Intent(SETTINGS_BROADCAST).putExtra(SETTINGS_TYPE, SET_SCALE_CONTROL)
                    )
                    //发送本地广播通知更新是否显示缩放按钮
                    KEY_ZOOM_CONTROLS -> lbm.sendBroadcast(
                        Intent(SETTINGS_BROADCAST).putExtra(SETTINGS_TYPE, SET_ZOOM_CONTROLS)
                    )
                    //发送本地广播通知更新是否显示指南针
                    KEY_COMPASS -> lbm.sendBroadcast(
                        Intent(SETTINGS_BROADCAST)
                            .putExtra(SETTINGS_TYPE, SET_COMPASS)
                    )
                    KEY_INTELLIGENT_SEARCH -> {
                    }
                    //显示删除所有搜索记录对话框
                    KEY_SEARCH_RECORD -> showDeleteAllSearchDataDialog(requireActivity())
                    else -> {
                    }
                }
            }
            return super.onPreferenceTreeClick(preference)
        }

        //显示删除所有搜索记录对话框
        private fun showDeleteAllSearchDataDialog(context: Context) {
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.to_clear))
                .setPositiveButton(R.string.clear) { _, _ -> //发送本地广播通知清空搜索记录
                    Constants.run {
                        lbm.sendBroadcast(
                            Intent(SETTINGS_BROADCAST).putExtra(SETTINGS_TYPE, CLEAN_RECORD)
                        )
                        SearchDataHelper.deleteSearchData() //清空数据库中的搜索记录
                        showToast(R.string.has_cleared)
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    //do nothing
                }
                .show()
        }
    }
}