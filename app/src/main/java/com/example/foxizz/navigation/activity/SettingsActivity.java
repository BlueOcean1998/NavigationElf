package com.example.foxizz.navigation.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.database.DatabaseHelper;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    //设置地图类型
    private ImageView mapStandardImage;//标准地图
    private TextView mapStandardText;
    private ImageView mapSatelliteImage;//卫星地图
    private TextView mapSatelliteText;
    private ImageView mapTrafficImage;//交通地图
    private TextView mapTrafficText;

    //设置目的地所在城市
    private String mCity;//所在城市
    private String databaseCity;//数据库中的城市
    private String textCity;//输入框内输入的城市
    private Button destinationCityButton;//回到所在城市
    private EditText destinationCityEditText;//目标城市
    private ImageButton destinationCityConfirm;//确定
    private ImageButton destinationCityCancel;//取消

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //新建数据库，已存在则连接数据库
        dbHelper = new DatabaseHelper(SettingsActivity.this, "Navigate.db", null, 1);

        //标题栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //自定义设置
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initMySettings();
        }

        //PreferenceScreen提供的设置
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
    }

    //自定义设置
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initMySettings() {
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

        switch(dbHelper.getSettings("map_type")) {
            case "0":
                mapStandardImage.setImageResource(R.drawable.map_standard_on);
                mapStandardText.setTextColor(getColor(R.color.deepblue));
                break;

            case "1":
                mapSatelliteImage.setImageResource(R.drawable.map_satellite_on);
                mapSatelliteText.setTextColor(getColor(R.color.deepblue));
                break;

            case "2":
                mapTrafficImage.setImageResource(R.drawable.map_traffic_on);
                mapTrafficText.setTextColor(getColor(R.color.deepblue));
                break;
        }

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

                //保存设置到数据库
                dbHelper.modifySettings("map_type", "0");
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

                //保存设置到数据库
                dbHelper.modifySettings("map_type", "1");
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

                //保存设置到数据库
                dbHelper.modifySettings("map_type", "2");
            }
        });

        destinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
        destinationCityCancel.setVisibility(View.GONE);//隐藏取消按钮

        //获取从MainActivity中传来的所在城市名
        Intent intent = getIntent();
        mCity = intent.getStringExtra("mCity");

        //设置城市信息
        databaseCity = dbHelper.getSettings("destination_city");
        if(!TextUtils.isEmpty(databaseCity))//如果数据库中的城市信息不为空
            destinationCityEditText.setText(databaseCity);//设置城市信息

        //设置提示信息为所在城市
        destinationCityEditText.setHint(mCity);

        //所在城市按钮的点击事件
        destinationCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //置空数据库中的城市信息
                dbHelper.modifySettings("destination_city", "");
                databaseCity = "";

                destinationCityEditText.setText("");//清空输入框
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

                if(!textCity.equals(databaseCity)) {//不等于数据库中的城市名
                    destinationCityConfirm.setVisibility(View.VISIBLE);//显示确定按钮
                    destinationCityCancel.setVisibility(View.VISIBLE);//显示取消按钮
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
                destinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
                destinationCityCancel.setVisibility(View.GONE);//隐藏取消按钮

                if(textCity.isEmpty())//若输入的城市信息为空
                    //置空数据库中的城市信息
                    dbHelper.modifySettings("destination_city", null);
                //将城市信息录入数据库
                else dbHelper.modifySettings("destination_city", textCity);
            }
        });

        //取消按钮的点击事件
        destinationCityCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destinationCityConfirm.setVisibility(View.GONE);//隐藏确定按钮
                destinationCityCancel.setVisibility(View.GONE);//隐藏取消按钮

                if(TextUtils.isEmpty(databaseCity))//如果数据库中的城市信息为空
                    destinationCityEditText.setText("");//清空输入框
                else {
                    destinationCityEditText.setText(databaseCity);//恢复城市数据
                    destinationCityEditText.setSelection(databaseCity.length());//移动焦点到末尾
                }
            }
        });

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        //创建PreferenceScreen
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

    }

}
