package com.navigation.foxizz.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.navigation.foxizz.R;
import com.navigation.foxizz.data.Constants;
import com.navigation.foxizz.data.SPHelper;
import com.navigation.foxizz.data.SearchDataHelper;
import com.navigation.foxizz.util.CityUtil;
import com.navigation.foxizz.util.ToastUtil;
import com.navigation.foxizz.view.AdaptiveTextView;

import java.util.Objects;

/**
 * 设置页
 */
public class SettingsActivity extends BaseActivity {

    //设置地图类型
    private ImageView tvMapStandard;//标准地图
    private AdaptiveTextView adaptiveTvMapStandard;
    private ImageView ivMapSatellite;//卫星地图
    private AdaptiveTextView adaptiveMapSatellite;
    private ImageView ivMapTraffic;//交通地图
    private AdaptiveTextView adaptiveMapTraffic;

    //设置目的地所在城市
    private Button btDestinationCity;//回到所在城市
    private EditText etDestinationCity;//目标城市
    private ImageButton ibDestinationCityConfirm;//确定
    private ImageButton ibDestinationCityCancel;//取消

    private String mCity;//所在城市
    private String saveCity;//存储的城市
    private String textCity;//输入框内输入的城市

    private InputMethodManager imm;//键盘

    private static LocalBroadcastManager mLocalBroadcastManager;//本地广播管理器

    /**
     * 启动设置页
     *
     * @param context 上下文
     * @param mCity   所在城市
     */
    public static void startActivity(Context context, String mCity) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(Constants.MY_CITY, mCity);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        initView();//初始化控件

        //初始化PreferenceScreen
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_settings_preferences, new PreferenceScreen())
                .commit();

        //获取键盘对象
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    //提交输入的城市
    private void commitCity() {
        ibDestinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
        ibDestinationCityCancel.setVisibility(View.GONE);//隐藏取消按钮

        //将城市信息录入sharedPreferences
        SPHelper.putString(Constants.DESTINATION_CITY, textCity);

        saveCity = textCity;
    }

    //监听按键抬起事件
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //如果是Enter键
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            commitCity();//提交输入的城市
            if (imm != null) imm.hideSoftInputFromWindow(
                    getWindow().getDecorView().getWindowToken(), 0
            );//收回键盘
            return false;
        }
        return super.onKeyUp(keyCode, event);
    }

    //初始化控件
    private void initView() {
        tvMapStandard = findViewById(R.id.iv_map_standard);
        adaptiveTvMapStandard = findViewById(R.id.adaptive_tv_map_standard);
        ivMapSatellite = findViewById(R.id.iv_map_satellite);
        adaptiveMapSatellite = findViewById(R.id.adaptive_tv_map_satellite);
        ivMapTraffic = findViewById(R.id.iv_map_traffic);
        adaptiveMapTraffic = findViewById(R.id.adaptive_tv_map_traffic);

        btDestinationCity = findViewById(R.id.bt_destination_city);
        etDestinationCity = findViewById(R.id.et_destination_city);
        ibDestinationCityConfirm = findViewById(R.id.ib_destination_city_confirm);
        ibDestinationCityCancel = findViewById(R.id.ib_destination_city_cancel);

        switch (Objects.requireNonNull(SPHelper.getString("map_type",
                Constants.STANDARD_MAP))) {
            case Constants.STANDARD_MAP:
                tvMapStandard.setImageResource(R.drawable.map_standard_on);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    adaptiveTvMapStandard.setTextColor(getColor(R.color.deepblue));
                break;
            case Constants.SATELLITE_MAP:
                ivMapSatellite.setImageResource(R.drawable.map_satellite_on);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    adaptiveMapSatellite.setTextColor(getColor(R.color.deepblue));
                break;
            case Constants.TRAFFIC_MAP:
                ivMapTraffic.setImageResource(R.drawable.map_traffic_on);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    adaptiveMapTraffic.setTextColor(getColor(R.color.deepblue));
                break;
            default:
                break;
        }

        //标准地图的点击事件
        tvMapStandard.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //修改图标和文字颜色
                tvMapStandard.setImageResource(R.drawable.map_standard_on);
                adaptiveTvMapStandard.setTextColor(getColor(R.color.deepblue));
                ivMapSatellite.setImageResource(R.drawable.map_satellite_off);
                adaptiveMapSatellite.setTextColor(getColor(R.color.black));
                ivMapTraffic.setImageResource(R.drawable.map_traffic_off);
                adaptiveMapTraffic.setTextColor(getColor(R.color.black));

                //保存地图类型到sharedPreferences
                SPHelper.putString(Constants.MAP_TYPE, Constants.STANDARD_MAP);

                //发送本地广播通知更新地图类型
                mLocalBroadcastManager.sendBroadcast(new Intent(Constants.SETTINGS_BROADCAST)
                        .putExtra(Constants.SETTINGS_TYPE, Constants.SET_MAP_TYPE));
            }
        });

        //卫星地图的点击事件
        ivMapSatellite.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //修改图标和文字颜色
                tvMapStandard.setImageResource(R.drawable.map_standard_off);
                adaptiveTvMapStandard.setTextColor(getColor(R.color.black));
                ivMapSatellite.setImageResource(R.drawable.map_satellite_on);
                adaptiveMapSatellite.setTextColor(getColor(R.color.deepblue));
                ivMapTraffic.setImageResource(R.drawable.map_traffic_off);
                adaptiveMapTraffic.setTextColor(getColor(R.color.black));

                //保存地图类型到sharedPreferences
                SPHelper.putString(Constants.MAP_TYPE, Constants.SATELLITE_MAP);

                //发送本地广播通知更新地图类型
                mLocalBroadcastManager.sendBroadcast(new Intent(Constants.SETTINGS_BROADCAST)
                        .putExtra(Constants.SETTINGS_TYPE, Constants.SET_MAP_TYPE));
            }
        });

        //交通地图的点击事件
        ivMapTraffic.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //修改图标和文字颜色
                tvMapStandard.setImageResource(R.drawable.map_standard_off);
                adaptiveTvMapStandard.setTextColor(getColor(R.color.black));
                ivMapSatellite.setImageResource(R.drawable.map_satellite_off);
                adaptiveMapSatellite.setTextColor(getColor(R.color.black));
                ivMapTraffic.setImageResource(R.drawable.map_traffic_on);
                adaptiveMapTraffic.setTextColor(getColor(R.color.deepblue));

                //保存地图类型到sharedPreferences
                SPHelper.putString(Constants.MAP_TYPE, Constants.TRAFFIC_MAP);

                //发送本地广播通知更新地图类型
                mLocalBroadcastManager.sendBroadcast(new Intent(Constants.SETTINGS_BROADCAST)
                        .putExtra(Constants.SETTINGS_TYPE, Constants.SET_MAP_TYPE));
            }
        });

        ibDestinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
        ibDestinationCityCancel.setVisibility(View.GONE);//隐藏取消按钮

        //获取从MainActivity中传来的所在城市名
        Intent intent = getIntent();
        mCity = intent.getStringExtra(Constants.MY_CITY);
        if (TextUtils.isEmpty(mCity)) {
            mCity = SPHelper.getString(Constants.MY_CITY, "");
        }

        //设置城市信息
        saveCity = SPHelper.getString(Constants.DESTINATION_CITY, "");
        if (!TextUtils.isEmpty(saveCity))//如果存储的城市信息不为空
            etDestinationCity.setText(saveCity);//设置城市信息

        //设置提示信息为所在城市
        etDestinationCity.setHint(mCity);

        //所在城市按钮的点击事件
        btDestinationCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCity = null;
                etDestinationCity.setText("");//清空输入框
                commitCity();//提交输入的城市
            }
        });

        //监听输入框的内容变化
        etDestinationCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //获取输入框内的城市信息
                textCity = etDestinationCity.getText().toString();

                if (!textCity.equals(saveCity)) {//不等于存储的城市名
                    ibDestinationCityCancel.setVisibility(View.VISIBLE);//显示取消按钮
                    //城市名为空或校验通过
                    if (TextUtils.isEmpty(textCity)
                            || (CityUtil.checkCityName(textCity)
                            && CityUtil.checkProvinceName(textCity))) {
                        ibDestinationCityConfirm.setVisibility(View.VISIBLE);//显示确定按钮
                    } else {
                        ibDestinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
                    }
                } else {
                    ibDestinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
                    ibDestinationCityCancel.setVisibility(View.GONE);//隐藏取消按钮
                }
            }
        });

        //确定按钮的点击事件
        ibDestinationCityConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commitCity();//提交输入的城市
            }
        });

        //取消按钮的点击事件
        ibDestinationCityCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ibDestinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
                ibDestinationCityCancel.setVisibility(View.GONE);//隐藏取消按钮

                if (TextUtils.isEmpty(saveCity))//如果存储的城市信息为空
                    etDestinationCity.setText("");//清空输入框
                else {
                    etDestinationCity.setText(saveCity);//恢复城市数据
                    etDestinationCity.setSelection(saveCity.length());//移动焦点到末尾
                }
            }
        });

    }

    //PreferenceScreen
    public static class PreferenceScreen extends PreferenceFragmentCompat {
        //创建PreferenceScreen
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_settings, rootKey);
        }

        //设置PreferenceScreen的点击事件
        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            switch (preference.getKey()) {
                case Constants.KEY_LANDSCAPE:
                    //发送本地广播通知更新是否允许横屏
                    mLocalBroadcastManager.sendBroadcast(new Intent(Constants.SETTINGS_BROADCAST)
                            .putExtra(Constants.SETTINGS_TYPE, Constants.SET_LANDSCAPE));
                    break;
                case Constants.KEY_ANGLE_3D:
                    //发送本地广播通知更新否启用3D视角
                    mLocalBroadcastManager.sendBroadcast(new Intent(Constants.SETTINGS_BROADCAST)
                            .putExtra(Constants.SETTINGS_TYPE, Constants.SET_ANGLE_3D));
                    break;
                case Constants.KEY_MAP_ROTATION:
                    //发送本地广播通知更新是否允许地图旋转
                    mLocalBroadcastManager.sendBroadcast(new Intent(Constants.SETTINGS_BROADCAST)
                            .putExtra(Constants.SETTINGS_TYPE, Constants.SET_MAP_ROTATION));
                    break;
                case Constants.KEY_SCALE_CONTROL:
                    //发送本地广播通知更新是否显示比例尺
                    mLocalBroadcastManager.sendBroadcast(new Intent(Constants.SETTINGS_BROADCAST)
                            .putExtra(Constants.SETTINGS_TYPE, Constants.SET_SCALE_CONTROL));
                    break;
                case Constants.KEY_ZOOM_CONTROLS:
                    //发送本地广播通知更新是否显示缩放按钮
                    mLocalBroadcastManager.sendBroadcast(new Intent(Constants.SETTINGS_BROADCAST)
                            .putExtra(Constants.SETTINGS_TYPE, Constants.SET_ZOOM_CONTROLS));
                    break;
                case Constants.KEY_COMPASS:
                    //发送本地广播通知更新是否显示指南针
                    mLocalBroadcastManager.sendBroadcast(new Intent(Constants.SETTINGS_BROADCAST)
                            .putExtra(Constants.SETTINGS_TYPE, Constants.SET_COMPASS));
                    break;
                case Constants.KEY_SEARCH_AROUND:
                    break;
                case Constants.KEY_SEARCH_RECORD:
                    showDeleteAllSearchDataDialog(requireActivity());//显示删除所有搜索记录对话框
                default:
                    break;
            }
            return super.onPreferenceTreeClick(preference);
        }
    }

    //显示删除所有搜索记录对话框
    private static void showDeleteAllSearchDataDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.warning));
        builder.setMessage(context.getString(R.string.to_clear));

        builder.setPositiveButton(R.string.clear, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //发送本地广播通知清空搜索记录
                mLocalBroadcastManager.sendBroadcast(new Intent(Constants.SETTINGS_BROADCAST)
                        .putExtra(Constants.SETTINGS_TYPE, Constants.CLEAN_RECORD));

                SearchDataHelper.deleteSearchData();//清空数据库中的搜索记录

                ToastUtil.showToast(R.string.has_cleared);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //do nothing
            }
        });

        builder.show();
    }

}
