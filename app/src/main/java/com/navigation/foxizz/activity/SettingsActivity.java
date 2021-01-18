package com.navigation.foxizz.activity;

import android.annotation.SuppressLint;
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
import com.navigation.foxizz.activity.fragment.MainFragment;
import com.navigation.foxizz.data.Constants;
import com.navigation.foxizz.data.SearchDataHelper;
import com.navigation.foxizz.util.CityUtil;
import com.navigation.foxizz.data.SPHelper;
import com.navigation.foxizz.view.AdaptationTextView;

import java.util.Objects;

/**
 * 设置页
 */
public class SettingsActivity extends BaseActivity {

    //SettingsActivity实例
    @SuppressLint("StaticFieldLeak")
    private static SettingsActivity instance;
    public static SettingsActivity getInstance() {
        return instance;
    }

    private static LocalBroadcastManager localBroadcastManager;//本地广播管理器
    private static Intent resettingIntent;//用于发送设置广播

    //设置地图类型
    private ImageView mapStandardImage;//标准地图
    private AdaptationTextView mapStandardText;
    private ImageView mapSatelliteImage;//卫星地图
    private AdaptationTextView mapSatelliteText;
    private ImageView mapTrafficImage;//交通地图
    private AdaptationTextView mapTrafficText;

    //设置目的地所在城市
    private String mCity;//所在城市
    private String saveCity;//存储的城市
    private String textCity;//输入框内输入的城市
    private Button destinationCityButton;//回到所在城市
    private EditText destinationCityEditText;//目标城市
    private ImageButton destinationCityConfirm;//确定
    private ImageButton destinationCityCancel;//取消
    private InputMethodManager imm;//键盘

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        instance = this;//获取SettingsActivity实例

        initView();//初始化控件

        //初始化PreferenceScreen
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_preferences, new PreferenceScreen())
                .commit();

        //获取键盘对象
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;//释放SettingsActivity实例
    }

    //提交输入的城市
    private void commitCity() {
        destinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
        destinationCityCancel.setVisibility(View.GONE);//隐藏取消按钮

        //将城市信息录入sharedPreferences
        SPHelper.putString("destination_city", textCity);

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
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    //初始化控件
    private void initView() {
        mapStandardImage = findViewById(R.id.map_standard_image);
        mapStandardText = findViewById(R.id.map_standard_text);
        mapSatelliteImage = findViewById(R.id.map_satellite_image);
        mapSatelliteText = findViewById(R.id.map_satellite_text);
        mapTrafficImage = findViewById(R.id.map_traffic_image);
        mapTrafficText = findViewById(R.id.map_traffic_text);

        destinationCityButton = findViewById(R.id.destination_city_button);
        destinationCityEditText = findViewById(R.id.destination_city_edit_text);
        destinationCityConfirm = findViewById(R.id.destination_city_confirm);
        destinationCityCancel = findViewById(R.id.destination_city_cancel);

        switch (Objects.requireNonNull(SPHelper.getString("map_type",
                Constants.STANDARD_MAP))) {
            case Constants.STANDARD_MAP:
                mapStandardImage.setImageResource(R.drawable.map_standard_on);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    mapStandardText.setTextColor(getColor(R.color.deepblue));
                break;
            case Constants.SATELLITE_MAP:
                mapSatelliteImage.setImageResource(R.drawable.map_satellite_on);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    mapSatelliteText.setTextColor(getColor(R.color.deepblue));
                break;
            case Constants.TRAFFIC_MAP:
                mapTrafficImage.setImageResource(R.drawable.map_traffic_on);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    mapTrafficText.setTextColor(getColor(R.color.deepblue));
                break;
            default:
                break;
        }

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        resettingIntent = new Intent("com.navigation.foxizz.navigation.broadcast.SETTINGS_BROADCAST");

        //标准地图的点击事件
        mapStandardImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //修改图标和文字颜色
                mapStandardImage.setImageResource(R.drawable.map_standard_on);
                mapStandardText.setTextColor(getColor(R.color.deepblue));
                mapSatelliteImage.setImageResource(R.drawable.map_satellite_off);
                mapSatelliteText.setTextColor(getColor(R.color.black));
                mapTrafficImage.setImageResource(R.drawable.map_traffic_off);
                mapTrafficText.setTextColor(getColor(R.color.black));

                //保存地图类型到sharedPreferences
                SPHelper.putString("map_type", Constants.STANDARD_MAP);

                //发送本地广播通知更新地图类型
                localBroadcastManager.sendBroadcast(resettingIntent
                        .putExtra("settings_type", Constants.SET_MAP_TYPE));
            }
        });

        //卫星地图的点击事件
        mapSatelliteImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //修改图标和文字颜色
                mapStandardImage.setImageResource(R.drawable.map_standard_off);
                mapStandardText.setTextColor(getColor(R.color.black));
                mapSatelliteImage.setImageResource(R.drawable.map_satellite_on);
                mapSatelliteText.setTextColor(getColor(R.color.deepblue));
                mapTrafficImage.setImageResource(R.drawable.map_traffic_off);
                mapTrafficText.setTextColor(getColor(R.color.black));

                //保存地图类型到sharedPreferences
                SPHelper.putString("map_type", Constants.SATELLITE_MAP);

                //发送本地广播通知更新地图类型
                localBroadcastManager.sendBroadcast(resettingIntent
                        .putExtra("settings_type", Constants.SET_MAP_TYPE));
            }
        });

        //交通地图的点击事件
        mapTrafficImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //修改图标和文字颜色
                mapStandardImage.setImageResource(R.drawable.map_standard_off);
                mapStandardText.setTextColor(getColor(R.color.black));
                mapSatelliteImage.setImageResource(R.drawable.map_satellite_off);
                mapSatelliteText.setTextColor(getColor(R.color.black));
                mapTrafficImage.setImageResource(R.drawable.map_traffic_on);
                mapTrafficText.setTextColor(getColor(R.color.deepblue));

                //保存地图类型到sharedPreferences
                SPHelper.putString("map_type", Constants.TRAFFIC_MAP);

                //发送本地广播通知更新地图类型
                localBroadcastManager.sendBroadcast(resettingIntent
                        .putExtra("settings_type", Constants.SET_MAP_TYPE));
            }
        });

        destinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
        destinationCityCancel.setVisibility(View.GONE);//隐藏取消按钮

        //获取从MainActivity中传来的所在城市名
        Intent intent = getIntent();
        mCity = intent.getStringExtra("mCity");

        //设置城市信息
        saveCity = SPHelper.getString("destination_city", "");
        if (!TextUtils.isEmpty(saveCity))//如果存储的城市信息不为空
            destinationCityEditText.setText(saveCity);//设置城市信息

        //设置提示信息为所在城市
        destinationCityEditText.setHint(mCity);

        //所在城市按钮的点击事件
        destinationCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCity = null;
                destinationCityEditText.setText("");//清空输入框
                commitCity();//提交输入的城市
            }
        });

        //监听输入框的内容变化
        destinationCityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //获取输入框内的城市信息
                textCity = destinationCityEditText.getText().toString();

                if (!textCity.equals(saveCity)) {//不等于存储的城市名
                    destinationCityCancel.setVisibility(View.VISIBLE);//显示取消按钮
                    //校验通过或城市名为空
                    if (CityUtil.checkoutCityName(textCity) || TextUtils.isEmpty(textCity)) {
                        destinationCityConfirm.setVisibility(View.VISIBLE);//显示确定按钮
                    } else {
                        destinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
                    }
                } else {
                    destinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
                    destinationCityCancel.setVisibility(View.GONE);//隐藏取消按钮
                }
            }
        });

        //确定按钮的点击事件
        destinationCityConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commitCity();//提交输入的城市
            }
        });

        //取消按钮的点击事件
        destinationCityCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
                destinationCityCancel.setVisibility(View.GONE);//隐藏取消按钮

                if (TextUtils.isEmpty(saveCity))//如果存储的城市信息为空
                    destinationCityEditText.setText("");//清空输入框
                else {
                    destinationCityEditText.setText(saveCity);//恢复城市数据
                    destinationCityEditText.setSelection(saveCity.length());//移动焦点到末尾
                }
            }
        });

    }

    //PreferenceScreen
    public static class PreferenceScreen extends PreferenceFragmentCompat {
        //创建PreferenceScreen
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey);
        }

        //设置PreferenceScreen的点击事件
        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            switch (preference.getKey()) {
                case "landscape":
                    //发送本地广播通知更新是否允许横屏
                    localBroadcastManager.sendBroadcast(resettingIntent
                            .putExtra("settings_type", Constants.SET_LANDSCAPE));
                    break;
                case "angle_3d":
                    //发送本地广播通知更新否启用3D视角
                    localBroadcastManager.sendBroadcast(resettingIntent
                            .putExtra("settings_type", Constants.SET_ANGLE_3D));
                    break;
                case "map_rotation":
                    //发送本地广播通知更新是否允许地图旋转
                    localBroadcastManager.sendBroadcast(resettingIntent
                            .putExtra("settings_type", Constants.SET_MAP_ROTATION));
                    break;
                case "scale_control":
                    //发送本地广播通知更新是否显示比例尺
                    localBroadcastManager.sendBroadcast(resettingIntent
                            .putExtra("settings_type", Constants.SET_SCALE_CONTROL));
                    break;
                case "zoom_controls":
                    //发送本地广播通知更新是否显示缩放按钮
                    localBroadcastManager.sendBroadcast(resettingIntent
                            .putExtra("settings_type", Constants.SET_ZOOM_CONTROLS));
                    break;
                case "compass":
                    //发送本地广播通知更新是否显示指南针
                    localBroadcastManager.sendBroadcast(resettingIntent
                            .putExtra("settings_type", Constants.SET_COMPASS));
                    break;
                case "search_around":
                    break;
                case "clean_record":
                    showDeleteAllSearchDataDialog();//显示删除所有搜索记录对话框
                default:
                    break;
            }
            return super.onPreferenceTreeClick(preference);
        }
    }

    //显示删除所有搜索记录对话框
    private static void showDeleteAllSearchDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(instance);
        builder.setTitle(instance.getString(R.string.warning));
        builder.setMessage(instance.getString(R.string.to_clear));

        builder.setPositiveButton(R.string.clear, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //找到mainFragment
                MainFragment mainFragment = MainActivity.getInstance().getMainFragment();
                mainFragment.searchList.clear();//清空搜索列表
                mainFragment.searchAdapter.notifyDataSetChanged();//通知adapter更新

                SearchDataHelper.deleteSearchData();//清空数据库中的搜索记录
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
