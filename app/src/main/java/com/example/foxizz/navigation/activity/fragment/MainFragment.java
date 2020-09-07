package com.example.foxizz.navigation.activity.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.tts.client.SpeechSynthesizer;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.activity.SettingsActivity;
import com.example.foxizz.navigation.broadcastreceiver.SettingsReceiver;
import com.example.foxizz.navigation.database.DatabaseHelper;
import com.example.foxizz.navigation.demo.Tools;
import com.example.foxizz.navigation.schemedata.SchemeAdapter;
import com.example.foxizz.navigation.schemedata.SchemeItem;
import com.example.foxizz.navigation.searchdata.SearchAdapter;
import com.example.foxizz.navigation.searchdata.SearchItem;
import com.example.foxizz.navigation.util.MyLocation;
import com.example.foxizz.navigation.util.MyNavigateHelper;
import com.example.foxizz.navigation.util.MyOrientationListener;
import com.example.foxizz.navigation.util.MyPoiSearch;
import com.example.foxizz.navigation.util.MyRoutePlanSearch;

import java.util.ArrayList;
import java.util.List;

import static com.example.foxizz.navigation.demo.Tools.expandLayout;
import static com.example.foxizz.navigation.demo.Tools.getValueAnimator;
import static com.example.foxizz.navigation.demo.Tools.isAirplaneModeOn;
import static com.example.foxizz.navigation.demo.Tools.isNetworkConnected;
import static com.example.foxizz.navigation.demo.Tools.rotateExpandIcon;

public class MainFragment extends Fragment {

    //地图控件
    public MapView mMapView;
    public BaiduMap mBaiduMap;
    public UiSettings mUiSettings;


    //设置相关
    private SharedPreferences sharedPreferences;
    private SettingsReceiver settingsReceiver;//设置接收器
    private LocalBroadcastManager localBroadcastManager;//本地广播管理器


    //方向传感器
    public MyOrientationListener myOrientationListener;
    public float mLastX;//方向角度


    //定位相关
    public MyLocation myLocation;

    public LocationClient mLocationClient;
    public MyLocationData locData;//地址信息
    public LatLng latLng;//坐标
    public int mLocType;//定位结果
    public float mRadius;//精度半径
    public double mLatitude;//纬度
    public double mLongitude;//经度
    public String mCity;//所在城市


    //搜索相关
    public MyPoiSearch myPoiSearch;

    public PoiSearch mPoiSearch;

    public LinearLayout searchLayout;//搜索布局
    public EditText searchEdit;//搜索输入框
    public ImageButton emptyButton;//清空按钮
    public Button searchButton;//搜索按钮
    public ImageButton searchExpand;//搜索结果伸缩按钮

    public String searchContent = "";//搜索内容
    public boolean searchExpandFlag = false;//搜索伸缩状态
    public int bodyLength;//屏幕的长
    public int bodyShort;//屏幕的宽

    public LinearLayout searchDrawer;//搜索抽屉
    public RecyclerView searchResult;//搜索结果
    public List<SearchItem> searchList = new ArrayList<>();//搜索列表
    public StaggeredGridLayoutManager searchLayoutManager;//搜索布局管理器
    public SearchAdapter searchAdapter;//搜索适配器

    public LinearLayout infoLayout;//详细信息布局
    public ScrollView searchInfoScroll;//详细信息布局的拖动条
    public TextView infoTargetName;//目标名
    public TextView infoAddress;//目标地址
    public TextView infoDistance;//与目标的距离
    public TextView infoOthers;//目标的其它信息（联系方式，营业时间等）

    public boolean isHistorySearchResult = true;//是否是搜索历史记录
    public DatabaseHelper dbHelper;//搜索记录数据库


    //路线规划相关
    public MyRoutePlanSearch myRoutePlanSearch;

    public RoutePlanSearch mSearch;

    public LinearLayout selectLayout;//选择布局
    public Button selectButton1;//选择驾车
    public Button selectButton2;//选择步行
    public Button selectButton3;//选择骑行
    public Button selectButton4;//选择公交

    public final static int DRIVING = 0;//驾车
    public final static int WALKING = 1;//步行
    public final static int BIKING = 2;//骑行
    public final static int TRANSIT = 3;//公交
    public int routePlanSelect = WALKING;//默认为步行

    public List<LatLng> busStationLocations = new ArrayList<>();//公交导航所有站点的坐标
    public LatLng endLocation;//终点

    public LinearLayout schemeLayout;//方案布局
    public ImageButton schemeReturnButton;//返回按钮
    public LinearLayout schemeDrawer;//方案抽屉
    public RecyclerView schemeResult;//方案结果
    public LinearLayout schemeInfoDrawer;//方案信息抽屉
    public ScrollView schemeInfoScroll;//方案信息的拖动条
    public TextView schemeInfo;//方案信息

    //方案信息的展开状态，0：未展开，1：只有列表展开，2：只有单个信息展开
    public int schemeInfoFlag = 0;

    public List<SchemeItem> schemeList = new ArrayList<>();//方案列表
    public StaggeredGridLayoutManager schemeLayoutManager;//方案布局管理器
    public SchemeAdapter schemeAdapter;//方案适配器


    //导航相关
    public MyNavigateHelper myNavigateHelper;

    public LinearLayout startLayout;//开始导航布局
    public Button backButton;//返回按钮
    public Button infoButton;//路线规划、详细信息切换按钮
    public Button startButton;//开始导航按钮

    public boolean infoFlag;//信息显示状态


    //控制相关
    public InputMethodManager imm;//键盘

    private ImageButton settings;//设置
    private ImageButton refresh;//刷新
    private ImageButton location;//定位

    private long clickTime = 0;//防止连续点击按钮

    public void expandSearchDrawer(boolean flag) {//伸缩搜索抽屉
        expandLayout(requireActivity(), searchDrawer, flag);
        if(flag) rotateExpandIcon(searchExpand, 0, 180);//旋转伸展按钮
        else rotateExpandIcon(searchExpand, 180, 0);//旋转伸展按钮
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        //获取偏好设置
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        //新建数据库，已存在则连接数据库
        dbHelper = new DatabaseHelper(requireActivity(), "Navigate.db", null, 1);

        InitMap(view);//初始化地图控件

        initSettings();//初始化偏好设置

        initMyView(view);//初始化自定义控件

        initMyDirectionSensor();//初始化方向传感器

        myLocation = new MyLocation(this);//初始化定位模块

        myPoiSearch = new MyPoiSearch(this);//初始化搜索模块
        myPoiSearch.initSearch();//初始化搜索目标信息

        myRoutePlanSearch = new MyRoutePlanSearch(this);//初始化路线规划模块
        myRoutePlanSearch.initRoutePlanSearch();//初始化路线规划

        myNavigateHelper = new MyNavigateHelper(this);//初始化导航模块

        requestPermission();//申请权限，获得权限后定位

        return view;
    }

    //管理地图的生命周期
    @Override
    public void onStart() {
        super.onStart();
        //开启定位的允许
        mBaiduMap.setMyLocationEnabled(true);
        //开启方向传感
        myOrientationListener.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        //计算屏幕宽高，用于设置控件的高度
        calculateWidthAndHeightOfScreen();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //释放设置接收器实例
        localBroadcastManager.unregisterReceiver(settingsReceiver);

        //开启定位的允许
        mBaiduMap.setMyLocationEnabled(false);
        //停止方向传感
        myOrientationListener.stop();
        //停止定位服务
        if(mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
        //释放地图、POI检索、路线规划实例
        mMapView.onDestroy();
        mPoiSearch.destroy();
        mSearch.destroy();
        //释放语音合成实例
        SpeechSynthesizer.getInstance().release();
    }

    //初始化地图控件
    private void InitMap(View view) {
        //获取地图控件引用
        mMapView = view.findViewById(R.id.map_view);
        mBaiduMap = mMapView.getMap();
        mUiSettings = mBaiduMap.getUiSettings();

        //配置定位图层显示方式，使用默认的定位图标，设置精确度圆的填充色和边框色
        //LocationMode定位模式有三种：普通模式，跟随模式，罗盘模式，在这使用普通模式
        MyLocationConfiguration myLocationConfiguration =
                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,
                        true, null, 0xAABFEFFF, 0xAA9FCFFF);
        mBaiduMap.setMyLocationConfiguration(myLocationConfiguration);

        //mMapView.removeViewAt(1);//去除百度水印
        mMapView.setScaleControlPosition(new Point());//改变比例尺位置
        mMapView.setZoomControlsPosition(new Point());//改变缩放按钮位置

        //设置缩放等级
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(18.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        //移动视角到最近的一条搜索记录
        dbHelper.moveToLastSearchRecordLocation();

        /*离线地图要下载离线包，现在暂时不用
        //下载离线地图
        final MKOfflineMap mOffline = new MKOfflineMap();
        mOffline.init(new MKOfflineMapListener() {
            @Override
            public void onGetOfflineMapState(int i, int i1) {
                //根据城市名获取城市id
                ArrayList<MKOLSearchRecord> records = mOffline.searchCity(mCity);
                if(records != null && records.size() == 1) {
                    mOffline.start(records.get(0).cityID);
                    mOffline.update(records.get(0).cityID);
                    Toast.makeText(MainFragment.this, "正在下载离线地图", Toast.LENGTH_SHORT).show();
                }
            }
        });
        */
    }

    //设置地图类型
    public void setMapType() {
        //切换地图类型
        switch(dbHelper.getSettings("map_type")) {
            case "0"://标准地图
                if(mBaiduMap.getMapType() != BaiduMap.MAP_TYPE_NORMAL)
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                mBaiduMap.setTrafficEnabled(false);
                break;

            case "1"://卫星地图
                if(mBaiduMap.getMapType() != BaiduMap.MAP_TYPE_SATELLITE)
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                mBaiduMap.setTrafficEnabled(false);
                break;

            case "2"://交通地图
                if(mBaiduMap.getMapType() != BaiduMap.MAP_TYPE_NORMAL)
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                mBaiduMap.setTrafficEnabled(true);
                break;
        }
    }

    //设置是否启用3D视角
    public void setAngle3D() {
        if(sharedPreferences.getBoolean("angle_3d", false))
            mUiSettings.setOverlookingGesturesEnabled(true);//启用3D视角
        else mUiSettings.setOverlookingGesturesEnabled(false);//禁用3D视角
    }

    //设置是否允许地图旋转
    public void setMapRotation() {
        if(sharedPreferences.getBoolean("map_rotation", false))
            mUiSettings.setRotateGesturesEnabled(true);//启用地图旋转
        else mUiSettings.setRotateGesturesEnabled(false);//禁用地图旋转
    }

    //设置是否显示比例尺
    public void setScaleControl() {
        if(sharedPreferences.getBoolean("scale_control", false))
            mMapView.showScaleControl(true);//显示比例尺
        else mMapView.showScaleControl(false);//不显示比例尺
    }

    //设置是否显示缩放按钮
    public void setZoomControls() {
        if(sharedPreferences.getBoolean("zoom_controls", false))
            mMapView.showZoomControls(true);//显示缩放按钮
        else mMapView.showZoomControls(false);//不显示缩放按钮
    }

    //设置是否显示指南针
    public void setCompass() {
        if(sharedPreferences.getBoolean("compass", true))
            mUiSettings.setCompassEnabled(true);//显示指南针
        else mUiSettings.setCompassEnabled(false);//不显示指南针
    }

    //初始化偏好设置
    private void initSettings() {
        //获取键盘对象
        imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        setMapType();
        Tools.initSettings(requireActivity());
        setAngle3D();
        setMapRotation();
        setScaleControl();
        setZoomControls();
        setCompass();

        //注册本地广播接收器
        settingsReceiver = new SettingsReceiver(requireActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.foxizz.navigation.broadcast.SETTINGS_BROADCAST");
        localBroadcastManager = LocalBroadcastManager.getInstance(requireActivity());
        localBroadcastManager.registerReceiver(settingsReceiver, intentFilter);
    }

    //初始化自定义控件
    private void initMyView(View view) {
        settings = view.findViewById(R.id.settings_button);
        refresh = view.findViewById(R.id.refresh_button);
        location = view.findViewById(R.id.location_button);

        selectLayout = view.findViewById(R.id.select_layout);
        selectButton1 = view.findViewById(R.id.select_button1);
        selectButton2 = view.findViewById(R.id.select_button2);
        selectButton3 = view.findViewById(R.id.select_button3);
        selectButton4 = view.findViewById(R.id.select_button4);

        searchLayout = view.findViewById(R.id.search_layout);
        searchEdit = view.findViewById(R.id.search_edit);
        emptyButton = view.findViewById(R.id.empty_bottom);
        searchButton = view.findViewById(R.id.search_button);
        searchExpand = view.findViewById(R.id.search_expand);
        searchDrawer = view.findViewById(R.id.search_drawer);
        searchResult = view.findViewById(R.id.search_result);

        infoLayout = view.findViewById(R.id.info_layout);
        searchInfoScroll = view.findViewById(R.id.info_scroll);
        infoTargetName = view.findViewById(R.id.info_target_name);
        infoAddress = view.findViewById(R.id.info_address);
        infoDistance = view.findViewById(R.id.info_distance);
        infoOthers = view.findViewById(R.id.info_others);

        schemeLayout = view.findViewById(R.id.scheme_layout);
        schemeReturnButton = view.findViewById(R.id.scheme_return_button);
        schemeDrawer = view.findViewById(R.id.scheme_drawer);
        schemeResult = view.findViewById(R.id.scheme_result);
        schemeInfoDrawer = view.findViewById(R.id.scheme_info_drawer);
        schemeInfoScroll = view.findViewById(R.id.scheme_info_scroll);
        schemeInfo = view.findViewById(R.id.scheme_info);

        startLayout = view.findViewById(R.id.start_layout);
        backButton = view.findViewById(R.id.return_button);
        infoButton = view.findViewById(R.id.info_button);
        startButton = view.findViewById(R.id.start_button);

        //设置选项布局、搜索结果抽屉、详细信息、路线方案布局、方案抽屉、方案信息抽屉、开始导航布局初始高度为0
        selectLayout.getLayoutParams().height = 0;
        searchDrawer.getLayoutParams().height = 0;
        infoLayout.getLayoutParams().height = 0;
        schemeLayout.getLayoutParams().height = 0;
        schemeDrawer.getLayoutParams().height = 0;
        schemeInfoDrawer.getLayoutParams().height = 0;
        startLayout.getLayoutParams().height = 0;

        searchAdapter = new SearchAdapter(this);//初始化搜索适配器
        searchLayoutManager = new StaggeredGridLayoutManager(
                1, StaggeredGridLayoutManager.VERTICAL
        );//搜索布局行数为1
        searchResult.setAdapter(searchAdapter);//设置搜索适配器
        searchResult.setLayoutManager(searchLayoutManager);//设置搜索布局

        schemeAdapter = new SchemeAdapter(this);//初始化方案适配器
        schemeLayoutManager = new StaggeredGridLayoutManager(
                1, StaggeredGridLayoutManager.VERTICAL
        );//方案布局行数为1
        schemeResult.setAdapter(schemeAdapter);//设置方案适配器
        schemeResult.setLayoutManager(schemeLayoutManager);//设置方案布局

        //设置按钮的点击事件
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        MainFragment.this.requireActivity(), SettingsActivity.class
                );
                if(mCity != null) intent.putExtra("mCity", mCity);
                startActivity(intent);
            }
        });

        //刷新按钮的点击事件
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();//关闭当前活动
                startActivity(requireActivity().getIntent());//重启当前活动
            }
        });

        //定位按钮的点击事件
        location.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                requestPermission();//申请权限，获得权限后定位
            }
        });

        //默认为步行
        selectButton2.setBackgroundResource(R.drawable.button_background_black);

        //驾车按钮的点击事件
        selectButton1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                selectButton1.setBackgroundResource(R.drawable.button_background_black);
                selectButton2.setBackgroundResource(R.drawable.button_background_gray);
                selectButton3.setBackgroundResource(R.drawable.button_background_gray);
                selectButton4.setBackgroundResource(R.drawable.button_background_gray);
                routePlanSelect = DRIVING;

                myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
            }
        });

        //步行按钮的点击事件
        selectButton2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                selectButton1.setBackgroundResource(R.drawable.button_background_gray);
                selectButton2.setBackgroundResource(R.drawable.button_background_black);
                selectButton3.setBackgroundResource(R.drawable.button_background_gray);
                selectButton4.setBackgroundResource(R.drawable.button_background_gray);
                routePlanSelect = WALKING;

                myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
            }
        });

        //骑行按钮的点击事件
        selectButton3.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                selectButton1.setBackgroundResource(R.drawable.button_background_gray);
                selectButton2.setBackgroundResource(R.drawable.button_background_gray);
                selectButton3.setBackgroundResource(R.drawable.button_background_black);
                selectButton4.setBackgroundResource(R.drawable.button_background_gray);
                routePlanSelect = BIKING;

                myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
            }
        });

        //公交按钮的点击事件
        selectButton4.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                selectButton1.setBackgroundResource(R.drawable.button_background_gray);
                selectButton2.setBackgroundResource(R.drawable.button_background_gray);
                selectButton3.setBackgroundResource(R.drawable.button_background_gray);
                selectButton4.setBackgroundResource(R.drawable.button_background_black);
                routePlanSelect = TRANSIT;

                myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
            }
        });

        //从方案列表返回按钮的点击事件
        schemeReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.expandLayout(requireActivity(), selectLayout, true);//展开选择布局
                Tools.expandLayout(requireActivity(), schemeLayout, false);//收起方案抽屉
                Tools.expandLayout(requireActivity(), startLayout, true);//展开开始导航布局
                infoFlag = false;//设置信息状态为交通选择
                infoButton.setText(R.string.info_button2);//设置按钮为详细信息
                schemeInfoFlag = 0;//设置状态为不显示
            }
        });

        //监听输入框内容改变
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //根据是否有内容判断显示和隐藏清空按钮
                if(!searchEdit.getText().toString().isEmpty()) {
                    emptyButton.setVisibility(View.VISIBLE);
                } else {
                    emptyButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        //清空按钮的点击事件
        emptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyButton.setVisibility(View.INVISIBLE);//隐藏清空按钮
                searchEdit.setText("");//清空搜索内容
            }
        });

        //搜索按钮的点击事件
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPoiSearch();//开始POI搜索
            }
        });

        //伸缩按钮的点击事件
        searchExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchExpandFlag) {//如果是展开状态
                    expandSearchDrawer(false);//收起搜索抽屉
                    searchExpandFlag = false;//设置状态为收起
                } else {//如果是收起状态
                    expandSearchDrawer(true);//展开搜索抽屉
                    searchExpandFlag = true;//设置状态为展开
                }
            }
        });

        //返回按钮的点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToUpperStory();//返回上一层
            }
        });

        //路线规划、详细信息、交通选择切换按钮的点击事件
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(schemeInfoFlag != 0) {//如果方案布局已经展开
                    expandLayout(requireActivity(), selectLayout, true);//展开选择布局
                    expandLayout(requireActivity(),schemeLayout, false);//收起方案布局

                    infoButton.setText(R.string.info_button2);//设置按钮为详细信息
                    infoFlag = false;//设置信息状态为交通选择
                    schemeInfoFlag = 0;//设置状态为不显示
                    return;
                }

                if(infoFlag) {//如果显示为详细信息
                    expandLayout(requireActivity(), selectLayout, true);//展开选择布局
                    expandLayout(requireActivity(), infoLayout, false);//收起详细信息布局

                    infoButton.setText(R.string.info_button2);//设置按钮为详细信息
                    infoFlag = false;//设置信息状态交通选择

                    //重置交通类型为步行
                    routePlanSelect = WALKING;
                    selectButton1.setBackgroundResource(R.drawable.button_background_gray);
                    selectButton2.setBackgroundResource(R.drawable.button_background_black);
                    selectButton3.setBackgroundResource(R.drawable.button_background_gray);
                    selectButton4.setBackgroundResource(R.drawable.button_background_gray);

                    myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
                } else {//如果显示为交通选择
                    infoButton.setText(R.string.info_button1);//设置按钮为路线
                    expandLayout(requireActivity(), selectLayout, false);//收起选择布局
                    expandLayout(requireActivity(), infoLayout, true);//展开详细信息布局
                    infoFlag = true;//设置信息状态为详细信息
                }
            }
        });

        //开始导航按钮的点击事件
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myNavigateHelper.startNavigate();//开始导航
            }
        });

    }

    //初始化方向传感器
    private void initMyDirectionSensor() {
        //方向传感器
        myOrientationListener = new MyOrientationListener(requireActivity());
        myOrientationListener.setmOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mLastX = x;

                //更新方向
                locData = new MyLocationData.Builder()
                        .accuracy(mRadius)
                        .direction(mLastX)//此处设置开发者获取到的方向信息，顺时针0-360
                        .latitude(mLatitude)
                        .longitude(mLongitude).build();
                mBaiduMap.setMyLocationData(locData);//设置定位数据
            }
        });
    }

    //申请权限，获得权限后定位
    public void requestPermission() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };

        List<String> permissionList = new ArrayList<>();

        for(String permission: permissions) {
            if(ContextCompat.checkSelfPermission(requireActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED)
                permissionList.add(permission);
        }

        String[] tmpList = new String[permissionList.size()];

        //如果列表为空，则获取了全部权限不用再获取，否则要获取
        if(permissionList.isEmpty()) {
            myLocation.refreshSearchList = true;//刷新搜索列表
            myLocation.initLocationOption();//初始化定位
        } else {
            dbHelper.initSearchData();//初始化搜索记录
            //申请权限
            ActivityCompat.requestPermissions(requireActivity(), permissionList.toArray(tmpList), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                myLocation.refreshSearchList = true;//刷新搜索列表
                myLocation.initLocationOption();//初始化定位
            }
            else
                Toast.makeText(requireActivity(), R.string.get_permission_fail, Toast.LENGTH_SHORT).show();
        }
    }

    //计算屏幕的宽高，并据此设置控件的高度
    public void calculateWidthAndHeightOfScreen() {
        //计算屏幕宽高，用于设置控件的高度
        Display defaultDisplay = requireActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);

        Configuration configuration = this.getResources().getConfiguration();//获取设置的配置信息
        int ori = configuration.orientation;//获取屏幕方向
        if(ori == Configuration.ORIENTATION_LANDSCAPE) {//横屏时
            bodyLength = point.x;
            bodyShort = point.y;
        } else if(ori == Configuration.ORIENTATION_PORTRAIT) {//竖屏时
            bodyLength = point.y;
            bodyShort = point.x;
        }

        //设置搜索抽屉的结果列表、详细信息布局的拖动布局、路线方案抽屉的结果列表、路线方案信息的拖动布局的高度
        searchResult.getLayoutParams().height = bodyLength / 2;
        searchInfoScroll.getLayoutParams().height = bodyLength / 3;
        schemeResult.getLayoutParams().height = bodyLength / 2;
        schemeInfoScroll.getLayoutParams().height = bodyLength / 4;
    }

    //返回上一层
    public void backToUpperStory() {
        if(schemeInfoFlag == 2) {//如果方案布局为单个方案
            expandLayout(requireActivity(), schemeDrawer, true);//展开方案抽屉
            expandLayout(requireActivity(), schemeInfoDrawer, false);//收起方案信息抽屉
            expandLayout(requireActivity(), startLayout, false);//收起开始导航布局

            //调整方案布局的高度
            getValueAnimator(schemeLayout,
                    bodyLength / 4, bodyLength / 2).start();

            schemeInfoFlag = 1;//设置状态为方案列表
        } else {
            expandLayout(requireActivity(), selectLayout, false);//收起选择布局
            expandLayout(requireActivity(), searchLayout, true);//展开搜索布局
            if(!searchExpandFlag) {
                expandSearchDrawer(true);//展开被收起的搜索抽屉
                searchExpandFlag = true;//设置状态为展开
            }
            expandLayout(requireActivity(), infoLayout, false);//收起详细信息布局
            expandLayout(requireActivity(), startLayout, false);//收起开始导航布局

            if(schemeInfoFlag == 1) {//如果方案布局为方案列表
                expandLayout(requireActivity(), schemeLayout, false);//收起方案布局
                schemeInfoFlag = 0;//设置状态为不显示
            }
        }
    }

    //判断是否可以返回上一层
    public boolean canBack() {
        return !(selectLayout.getHeight() == 0 &&
                searchLayout.getHeight() != 0 &&
                infoLayout.getHeight() == 0 &&
                startLayout.getHeight() == 0);
    }

    //开始POI搜索
    public void startPoiSearch() {
        if((System.currentTimeMillis() - clickTime) > 1000) //连续点击间隔时间不能小于1秒
            clickTime = System.currentTimeMillis();
        else return;

        if(!isNetworkConnected(requireActivity())) {
            Toast.makeText(requireActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if(isAirplaneModeOn(requireActivity())) {
            Toast.makeText(requireActivity(), R.string.close_airplane_mode, Toast.LENGTH_SHORT).show();
            return;
        }

        searchContent = searchEdit.getText().toString();

        if(searchContent.isEmpty()) {//如果搜索内容为空
            if(!isHistorySearchResult) {//如果不是搜索历史记录
                searchResult.stopScroll();//停止信息列表滑动
                dbHelper.initSearchData();//初始化搜索记录
                isHistorySearchResult = true;//现在是搜索历史记录了
            }
            return;
        }

        if(!searchExpandFlag) {//展开搜索抽屉
            searchResult.startAnimation(AnimationUtils.loadAnimation(
                    requireActivity(), R.anim.adapter_alpha2)
            );//动画2，出现;
            getValueAnimator(searchDrawer, 0, bodyLength / 2)
                    .start();//展开搜索抽屉
            rotateExpandIcon(searchExpand, 0, 180);//伸展按钮的旋转动画
            searchExpandFlag = true;//设置状态为展开
        }

        if(imm != null) imm.hideSoftInputFromWindow(
                requireActivity().getWindow().getDecorView().getWindowToken(), 0
        );//收回键盘

        String searchCity = null;//进行搜索的城市

        // 定位成功后才可以进行搜索
        if(mCity != null) searchCity = mCity;

        //如果数据库中的城市不为空，则换用数据库中的城市
        String databaseCity = dbHelper.getSettings("destination_city");
        if(!TextUtils.isEmpty(databaseCity)) searchCity = databaseCity;

        if(searchCity == null) {
            requestPermission();//申请权限，获得权限后定位
            return;
        }

        searchResult.stopScroll();//停止信息列表滑动
        mBaiduMap.clear();//清空地图上的所有标记点和绘制的路线
        searchList.clear();//清空searchList
        searchAdapter.notifyDataSetChanged();//通知adapter更新
        isHistorySearchResult = false;//已经不是搜索历史记录了

        if(sharedPreferences.getBoolean("search_around", false))
            myPoiSearch.poiSearchType = MyPoiSearch.CONSTRAINT_CITY_SEARCH;//设置搜索类型为强制城市内搜索
        else myPoiSearch.poiSearchType = MyPoiSearch.CITY_SEARCH;//设置搜索类型为城市内搜索

        //开始城市内搜索
        mPoiSearch.searchInCity(new PoiCitySearchOption()
                .city(searchCity)
                .keyword(searchContent)
                .cityLimit(false));//不限制搜索范围在城市内
    }

}
