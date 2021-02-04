package com.navigation.foxizz.activity.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.tts.client.SpeechSynthesizer;
import com.navigation.foxizz.R;
import com.navigation.foxizz.activity.SettingsActivity;
import com.navigation.foxizz.activity.adapter.SchemeAdapter;
import com.navigation.foxizz.activity.adapter.SearchAdapter;
import com.navigation.foxizz.data.Constants;
import com.navigation.foxizz.data.SPHelper;
import com.navigation.foxizz.data.SchemeItem;
import com.navigation.foxizz.data.SearchDataHelper;
import com.navigation.foxizz.data.SearchItem;
import com.navigation.foxizz.mybaidumap.MyLocation;
import com.navigation.foxizz.mybaidumap.MyNavigateHelper;
import com.navigation.foxizz.mybaidumap.MyOrientationListener;
import com.navigation.foxizz.mybaidumap.MyRoutePlanSearch;
import com.navigation.foxizz.mybaidumap.MySearch;
import com.navigation.foxizz.receiver.LocalReceiver;
import com.navigation.foxizz.util.CityUtil;
import com.navigation.foxizz.util.LayoutUtil;
import com.navigation.foxizz.util.NetworkUtil;
import com.navigation.foxizz.util.SettingUtil;
import com.navigation.foxizz.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import static com.navigation.foxizz.BaseApplication.getApplication;

/**
 * 地图页
 */
public class MainFragment extends Fragment {

    /*
     * 地图控件
     */
    public MapView mMapView;
    public BaiduMap mBaiduMap;
    public UiSettings mUiSettings;

    /*
     * 方向传感器
     */
    public MyOrientationListener myOrientationListener;
    public float mLastX;//方向角度

    /*
     * 定位相关
     */
    public MyLocation myLocation;
    public LocationClient mLocationClient;
    public MyLocationData locData;//地址信息
    public LatLng latLng;//坐标
    public int mLocType;//定位结果
    public float mRadius;//精度半径
    public double mLatitude;//纬度
    public double mLongitude;//经度
    public String mCity;//所在城市

    /*
     * 搜索相关
     */
    public MySearch mySearch;
    public SuggestionSearch mSuggestionSearch;//Sug搜索
    public PoiSearch mPoiSearch;//POI搜索
    public final List<String> searchCityList = new ArrayList<>();//要进行POI搜索的城市列表
    public String searchContent = "";//搜索内容
    public LinearLayout llSearchLayout;//搜索布局
    public EditText etSearch;//搜索输入框
    public ImageButton ibEmpty;//清空按钮
    public Button btSearch;//搜索按钮
    public ImageButton ibSearchExpand;//搜索结果伸缩按钮
    public boolean searchExpandFlag = false;//搜索伸缩状态
    public LinearLayout llSearchDrawer;//搜索抽屉
    public LinearLayout llSearchLoading;//搜索加载
    public RecyclerView recyclerSearchResult;//搜索结果
    public final List<SearchItem> searchList = new ArrayList<>();//搜索列表
    public StaggeredGridLayoutManager searchLayoutManager;//搜索布局管理器
    public SearchAdapter searchAdapter;//搜索适配器
    public LinearLayout llSearchInfoLayout;//详细信息布局
    public LinearLayout llSearchInfoLoading;//信息加载
    public ScrollView svSearchInfo;//详细信息布局的拖动条
    public TextView tvSearchTargetName;//目标名
    public TextView tvSearchAddress;//目标地址
    public TextView tvSearchDistance;//与目标的距离
    public TextView tvSearchOthers;//目标的其它信息（联系方式，营业时间等）
    public boolean isHistorySearchResult = true;//是否是搜索历史记录
    public int currentPage;//当前页
    public int totalPage;//总页数

    /*
     * 路线规划相关
     */
    public MyRoutePlanSearch myRoutePlanSearch;
    public RoutePlanSearch mSearch;
    public final List<LatLng> busStationLocations = new ArrayList<>();//公交导航所有站点的坐标
    public LatLng endLocation;//终点
    //交通选择
    public LinearLayout llSelectLayout;//选择布局
    public final static int DRIVING = 0;//驾车
    public final static int WALKING = 1;//步行
    public final static int BIKING = 2;//骑行
    public final static int TRANSIT = 3;//公交
    public int routePlanSelect = WALKING;//默认为步行
    public Button btSelect1;//选择驾车
    public Button btSelect2;//选择步行
    public Button btSelect3;//选择骑行
    public Button btSelect4;//选择公交
    //方案布局
    public final static int SCHEME_NOT_ALREADY = 0;//未展开
    public final static int SCHEME_LIST = 1;//方案列表
    public final static int SCHEME_INFO = 2;//方案信息
    public int schemeFlag = SCHEME_NOT_ALREADY;//初始为未展开
    public LinearLayout llSchemeDrawer;//方案抽屉
    public LinearLayout llSchemeLoading;//方案加载
    public RecyclerView recyclerSchemeResult;//方案结果
    public LinearLayout llSchemeInfoLayout;//方案信息抽屉
    public ScrollView svSchemeInfo;//方案信息的拖动条
    public TextView tvSchemeInfo;//方案信息
    public List<SchemeItem> schemeList = new ArrayList<>();//方案列表
    public StaggeredGridLayoutManager schemeLayoutManager;//方案布局管理器
    public SchemeAdapter schemeAdapter;//方案适配器

    /*
     * 导航相关
     */
    public MyNavigateHelper myNavigateHelper;
    public LinearLayout llStartLayout;//开始导航布局
    public Button btBack;//返回按钮
    public Button btMiddle;//路线规划、详细信息切换按钮
    public Button btStart;//开始导航按钮
    public boolean infoFlag;//信息显示状态

    /*
     * 控制相关
     */
    public int bodyLength;//屏幕的长
    public int bodyShort;//屏幕的宽
    public InputMethodManager imm;//键盘
    private ImageButton ibSettings;//设置
    private ImageButton ibRefresh;//刷新
    private ImageButton ibLocation;//定位

    /*
     * 设置相关
     */
    private LocalReceiver localReceiver;//设置接收器
    private LocalBroadcastManager localBroadcastManager;//本地广播管理器

    /*
     * 数据相关
     */
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        //获取默认设置
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        initMap(view);//初始化地图控件

        initSettings();//初始化偏好设置

        initLocalBroadcast();//初始化本地广播接收器

        initView(view);//初始化控件

        initMyDirectionSensor();//初始化方向传感器

        myLocation = new MyLocation(this);//初始化定位模块

        mySearch = new MySearch(this);//初始化搜索模块
        mySearch.initSearch();//初始化搜索目标信息

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

        localBroadcastManager.unregisterReceiver(localReceiver);//释放设置接收器实例

        mBaiduMap.setMyLocationEnabled(false);//开启定位的允许

        myOrientationListener.stop();//停止方向传感

        //停止定位服务
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
        }

        //释放地图、Sug搜索、POI搜索、路线规划实例
        mMapView.onDestroy();
        mSuggestionSearch.destroy();
        mPoiSearch.destroy();
        mSearch.destroy();

        SpeechSynthesizer.getInstance().release();//释放语音合成实例
    }

    //初始化地图控件
    private void initMap(View view) {
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
        SearchDataHelper.moveToLastSearchRecordLocation(this);

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
                    ToastUtil.showToast("正在下载离线地图");
                }
            }
        });
        */
    }

    /**
     * 设置地图类型
     */
    public void setMapType() {
        switch (SPHelper.getString(Constants.MAP_TYPE, Constants.STANDARD_MAP)) {
            case Constants.STANDARD_MAP://标准地图
                if (mBaiduMap.getMapType() != BaiduMap.MAP_TYPE_NORMAL)
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                mBaiduMap.setTrafficEnabled(false);
                break;
            case Constants.SATELLITE_MAP://卫星地图
                if (mBaiduMap.getMapType() != BaiduMap.MAP_TYPE_SATELLITE)
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                mBaiduMap.setTrafficEnabled(false);
                break;
            case Constants.TRAFFIC_MAP://交通地图
                if (mBaiduMap.getMapType() != BaiduMap.MAP_TYPE_NORMAL)
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                mBaiduMap.setTrafficEnabled(true);
                break;
            default:
                break;
        }
    }

    /**
     * 设置是否启用3D视角
     */
    public void setAngle3D() {
        mUiSettings.setOverlookingGesturesEnabled(sharedPreferences.getBoolean(Constants.KEY_ANGLE_3D, false));
    }

    /**
     * 设置是否允许地图旋转
     */
    public void setMapRotation() {
        mUiSettings.setRotateGesturesEnabled(sharedPreferences.getBoolean(Constants.KEY_MAP_ROTATION, false));
    }

    /**
     * 设置是否显示比例尺
     */
    public void setScaleControl() {
        mMapView.showScaleControl(sharedPreferences.getBoolean(Constants.KEY_SCALE_CONTROL, true));
    }

    /**
     * 设置是否显示缩放按钮
     */
    public void setZoomControls() {
        mMapView.showZoomControls(sharedPreferences.getBoolean(Constants.KEY_ZOOM_CONTROLS, false));
    }

    /**
     * 设置是否显示指南针
     */
    public void setCompass() {
        mUiSettings.setCompassEnabled(sharedPreferences.getBoolean(Constants.KEY_COMPASS, true));
    }

    //初始化偏好设置
    private void initSettings() {
        //获取键盘对象
        imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        setMapType();
        SettingUtil.initSettings(requireActivity());
        setAngle3D();
        setMapRotation();
        setScaleControl();
        setZoomControls();
        setCompass();
    }

    //初始化本地广播接收器
    private void initLocalBroadcast() {
        localReceiver = new LocalReceiver(requireActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.SETTINGS_BROADCAST);
        localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    //初始化自定义控件
    private void initView(View view) {
        ibSettings = view.findViewById(R.id.ib_settings);
        ibRefresh = view.findViewById(R.id.ib_refresh);
        ibLocation = view.findViewById(R.id.ib_location);

        llSelectLayout = view.findViewById(R.id.ll_select_layout);
        btSelect1 = view.findViewById(R.id.bt_select_1);
        btSelect2 = view.findViewById(R.id.bt_select_2);
        btSelect3 = view.findViewById(R.id.bt_select_3);
        btSelect4 = view.findViewById(R.id.bt_select_4);

        llSearchLayout = view.findViewById(R.id.ll_search_layout);
        etSearch = view.findViewById(R.id.et_search);
        ibEmpty = view.findViewById(R.id.ib_empty);
        btSearch = view.findViewById(R.id.bt_search);
        ibSearchExpand = view.findViewById(R.id.ib_search_expand);
        llSearchDrawer = view.findViewById(R.id.ll_search_drawer);
        llSearchLoading = view.findViewById(R.id.include_search_loading);
        recyclerSearchResult = view.findViewById(R.id.recycler_search_result);

        llSearchInfoLayout = view.findViewById(R.id.ll_search_info_layout);
        llSearchInfoLoading = view.findViewById(R.id.include_search_info_loading);
        svSearchInfo = view.findViewById(R.id.sv_search_info);
        tvSearchTargetName = view.findViewById(R.id.tv_search_target_name);
        tvSearchAddress = view.findViewById(R.id.tv_search_address);
        tvSearchDistance = view.findViewById(R.id.tv_search_distance);
        tvSearchOthers = view.findViewById(R.id.tv_search_others);

        llSchemeDrawer = view.findViewById(R.id.ll_scheme_drawer);
        llSchemeLoading = view.findViewById(R.id.include_scheme_loading);
        recyclerSchemeResult = view.findViewById(R.id.recycler_scheme_result);
        llSchemeInfoLayout = view.findViewById(R.id.ll_scheme_info_layout);
        svSchemeInfo = view.findViewById(R.id.sv_scheme_info);
        tvSchemeInfo = view.findViewById(R.id.tv_scheme_info);

        llStartLayout = view.findViewById(R.id.ll_start_layout);
        btBack = view.findViewById(R.id.bt_return);
        btMiddle = view.findViewById(R.id.bt_middle);
        btStart = view.findViewById(R.id.bt_start);

        //设置选项布局、搜索结果抽屉、详细信息、方案抽屉、方案信息抽屉、开始导航布局初始高度为0
        LayoutUtil.setViewHeight(llSelectLayout, 0);
        LayoutUtil.setViewHeight(llSearchDrawer, 0);
        LayoutUtil.setViewHeight(llSearchInfoLayout, 0);
        LayoutUtil.setViewHeight(llSchemeDrawer, 0);
        LayoutUtil.setViewHeight(llSchemeInfoLayout, 0);
        LayoutUtil.setViewHeight(llStartLayout, 0);

        //设置搜索、搜索信息、方案加载不可见
        llSearchLoading.setVisibility(View.GONE);
        llSearchInfoLoading.setVisibility(View.GONE);
        llSchemeLoading.setVisibility(View.GONE);

        searchAdapter = new SearchAdapter(this);//初始化搜索适配器
        searchLayoutManager = new StaggeredGridLayoutManager(
                1, StaggeredGridLayoutManager.VERTICAL
        );//搜索布局行数为1
        recyclerSearchResult.setAdapter(searchAdapter);//设置搜索适配器
        recyclerSearchResult.setLayoutManager(searchLayoutManager);//设置搜索布局

        schemeAdapter = new SchemeAdapter(this);//初始化方案适配器
        schemeLayoutManager = new StaggeredGridLayoutManager(
                1, StaggeredGridLayoutManager.VERTICAL
        );//方案布局行数为1
        recyclerSchemeResult.setAdapter(schemeAdapter);//设置方案适配器
        recyclerSchemeResult.setLayoutManager(schemeLayoutManager);//设置方案布局

        //设置按钮的点击事件
        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), SettingsActivity.class);
                if (mCity != null) intent.putExtra(Constants.MY_CITY, mCity);
                startActivity(intent);
            }
        });

        //刷新按钮的点击事件
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();//关闭当前活动
                startActivity(requireActivity().getIntent());//重启当前活动
            }
        });

        //定位按钮的点击事件
        ibLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();//申请权限，获得权限后定位
            }
        });

        //默认为步行
        btSelect2.setBackgroundResource(R.drawable.bt_bg_black);

        //驾车按钮的点击事件
        btSelect1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSelect1.setBackgroundResource(R.drawable.bt_bg_black);
                btSelect2.setBackgroundResource(R.drawable.bt_bg_gray);
                btSelect3.setBackgroundResource(R.drawable.bt_bg_gray);
                btSelect4.setBackgroundResource(R.drawable.bt_bg_gray);
                routePlanSelect = DRIVING;

                myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
            }
        });

        //步行按钮的点击事件
        btSelect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSelect1.setBackgroundResource(R.drawable.bt_bg_gray);
                btSelect2.setBackgroundResource(R.drawable.bt_bg_black);
                btSelect3.setBackgroundResource(R.drawable.bt_bg_gray);
                btSelect4.setBackgroundResource(R.drawable.bt_bg_gray);
                routePlanSelect = WALKING;

                myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
            }
        });

        //骑行按钮的点击事件
        btSelect3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSelect1.setBackgroundResource(R.drawable.bt_bg_gray);
                btSelect2.setBackgroundResource(R.drawable.bt_bg_gray);
                btSelect3.setBackgroundResource(R.drawable.bt_bg_black);
                btSelect4.setBackgroundResource(R.drawable.bt_bg_gray);
                routePlanSelect = BIKING;

                myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
            }
        });

        //公交按钮的点击事件
        btSelect4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSelect1.setBackgroundResource(R.drawable.bt_bg_gray);
                btSelect2.setBackgroundResource(R.drawable.bt_bg_gray);
                btSelect3.setBackgroundResource(R.drawable.bt_bg_gray);
                btSelect4.setBackgroundResource(R.drawable.bt_bg_black);
                routePlanSelect = TRANSIT;

                myRoutePlanSearch.startRoutePlanSearch();//开始路线规划

                btMiddle.setText(R.string.middle_button3);
            }
        });

        //输入框获取焦点时
        etSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //如果状态为收起且有搜索数据
                    if (!searchExpandFlag && SearchDataHelper.isHasSearchData()) {
                        expandSearchDrawer(true);//展开搜索抽屉
                        searchExpandFlag = true;//设置状态为展开
                    }
                }
            }
        });

        //监听输入框内容改变
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //根据是否有内容判断显示和隐藏清空按钮
                if (!etSearch.getText().toString().isEmpty()) {
                    ibEmpty.setVisibility(View.VISIBLE);
                } else {
                    ibEmpty.setVisibility(View.INVISIBLE);
                }
            }
        });

        //清空按钮的点击事件
        ibEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ibEmpty.setVisibility(View.INVISIBLE);//隐藏清空按钮
                etSearch.setText("");//清空搜索内容

                if (!isHistorySearchResult) {//如果不是搜索历史记录
                    recyclerSearchResult.stopScroll();//停止信息列表滑动
                    SearchDataHelper.initSearchData(MainFragment.this);//初始化搜索记录
                    isHistorySearchResult = true;//现在是搜索历史记录了
                }
            }
        });

        //搜索按钮的点击事件
        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch();//开始搜索
            }
        });

        //伸缩按钮的点击事件
        ibSearchExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchExpandFlag) {//如果状态为展开
                    expandSearchDrawer(false);//收起搜索抽屉
                    searchExpandFlag = false;//设置状态为收起
                } else {//如果是收起状态
                    expandSearchDrawer(true);//展开搜索抽屉
                    searchExpandFlag = true;//设置状态为展开
                }
            }
        });

        //搜索列表的滑动监听
        recyclerSearchResult.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        //返回按钮的点击事件
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToUpperStory();//返回上一层
            }
        });

        //路线规划、详细信息、交通选择切换按钮的点击事件
        btMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (schemeFlag != SCHEME_NOT_ALREADY) {//如果方案布局已经展开
                    LayoutUtil.expandLayout(llSelectLayout, true);//展开选择布局
                    if (schemeFlag == SCHEME_LIST)//如果方案布局为方案列表
                        LayoutUtil.expandLayout(llSchemeDrawer, false);//收起方案抽屉
                    if (schemeFlag == SCHEME_INFO)//如果方案布局为单个方案
                        LayoutUtil.expandLayout(llSchemeInfoLayout, false);//收起方案信息布局

                    btMiddle.setText(R.string.middle_button2);//设置按钮为详细信息
                    infoFlag = false;//设置信息状态为交通选择
                    schemeFlag = SCHEME_NOT_ALREADY;//设置状态为没有展开
                    return;
                }

                if (infoFlag) {//如果显示为详细信息
                    LayoutUtil.expandLayout(llSelectLayout, true);//展开选择布局
                    LayoutUtil.expandLayout(llSearchInfoLayout, false);//收起详细信息布局

                    btMiddle.setText(R.string.middle_button2);//设置按钮为详细信息
                    infoFlag = false;//设置信息状态交通选择

                    //重置交通类型为步行
                    routePlanSelect = WALKING;
                    btSelect1.setBackgroundResource(R.drawable.bt_bg_gray);
                    btSelect2.setBackgroundResource(R.drawable.bt_bg_black);
                    btSelect3.setBackgroundResource(R.drawable.bt_bg_gray);
                    btSelect4.setBackgroundResource(R.drawable.bt_bg_gray);

                    myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
                } else {//如果显示为交通选择
                    btMiddle.setText(R.string.middle_button1);//设置按钮为路线
                    LayoutUtil.expandLayout(llSelectLayout, false);//收起选择布局
                    LayoutUtil.expandLayout(llSearchInfoLayout, true);//展开详细信息布局
                    infoFlag = true;//设置信息状态为详细信息
                }
            }
        });

        //开始导航按钮的点击事件
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myNavigateHelper.startNavigate();//开始导航
            }
        });
    }

    //初始化方向传感器
    private void initMyDirectionSensor() {
        //方向传感器
        myOrientationListener = new MyOrientationListener();
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
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

    /**
     * 申请权限，获得权限后定位
     */
    public void requestPermission() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };

        List<String> permissionList = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED)
                permissionList.add(permission);
        }

        String[] tmpList = new String[permissionList.size()];

        //如果列表为空，则获取了全部权限不用再获取，否则要获取
        if (permissionList.isEmpty()) {
            myLocation.refreshSearchList = true;//刷新搜索列表
            myLocation.initLocationOption();//初始化定位
        } else {
            //申请权限
            ActivityCompat.requestPermissions(requireActivity(), permissionList.toArray(tmpList), 0);
        }
    }

    /**
     * 计算屏幕的宽高，并据此设置控件的高度
     */
    public void calculateWidthAndHeightOfScreen() {
        //计算屏幕宽高，用于设置控件的高度
        Display defaultDisplay = requireActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);

        Configuration configuration = this.getResources().getConfiguration();//获取设置的配置信息
        int ori = configuration.orientation;//获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {//横屏时
            bodyLength = point.x;
            bodyShort = point.y;
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {//竖屏时
            bodyLength = point.y;
            bodyShort = point.x;
        }

        //设置搜索抽屉的结果列表、详细信息布局的拖动布局、路线方案抽屉的结果列表、路线方案信息的拖动布局的高度
        LayoutUtil.setViewHeight(recyclerSearchResult, bodyLength / 2);
        LayoutUtil.setViewHeight(svSearchInfo, bodyLength / 4);
        LayoutUtil.setViewHeight(recyclerSchemeResult, 2 * bodyLength / 5);
        LayoutUtil.setViewHeight(svSchemeInfo, bodyLength / 4);
    }

    /**
     * 伸缩搜索抽屉
     *
     * @param flag 伸或缩
     */
    public void expandSearchDrawer(boolean flag) {
        LayoutUtil.expandLayout(llSearchDrawer, flag);
        if (flag) LayoutUtil.rotateExpandIcon(ibSearchExpand, 0, 180);//旋转伸展按钮
        else LayoutUtil.rotateExpandIcon(ibSearchExpand, 180, 0);//旋转伸展按钮
    }

    /*
     * 收回键盘
     */
    public void takeBackKeyboard() {
        if (imm != null) imm.hideSoftInputFromWindow(
                requireActivity().getWindow().getDecorView().getWindowToken(), 0
        );
    }

    /**
     * 返回上一层
     */
    public void backToUpperStory() {
        if (schemeFlag == SCHEME_INFO) {//如果方案布局为单个方案
            LayoutUtil.expandLayout(llSchemeDrawer, true);//展开方案抽屉
            LayoutUtil.expandLayout(llSchemeInfoLayout, false);//收起方案信息抽屉
            schemeFlag = SCHEME_LIST;//设置状态为方案列表
        } else {
            LayoutUtil.expandLayout(llSelectLayout, false);//收起选择布局
            LayoutUtil.expandLayout(llSearchLayout, true);//展开搜索布局
            if (!searchExpandFlag) {//如果状态为收起
                expandSearchDrawer(true);//展开搜索抽屉
                searchExpandFlag = true;//设置状态为展开
            }
            LayoutUtil.expandLayout(llSearchInfoLayout, false);//收起详细信息布局
            LayoutUtil.expandLayout(llStartLayout, false);//收起开始导航布局

            if (schemeFlag == SCHEME_LIST) {//如果方案布局为方案列表
                LayoutUtil.expandLayout(llSchemeDrawer, false);//收起方案抽屉
                schemeFlag = SCHEME_NOT_ALREADY;//设置状态为没有展开
            }
        }
    }

    /**
     * 判断是否可以返回上一层
     */
    public boolean canBack() {
        return !(llSelectLayout.getHeight() == 0 &&
                llSearchLayout.getHeight() != 0 &&
                llSearchInfoLayout.getHeight() == 0 &&
                llStartLayout.getHeight() == 0);
    }

    /**
     * 开始搜索
     */
    public void startSearch() {
        if (mySearch.isSearching) {
            ToastUtil.showToast(R.string.wait_for_search_result);
            return;
        }

        if (!NetworkUtil.isNetworkConnected()) {//没有网络连接
            ToastUtil.showToast(R.string.network_error);
            return;
        }

        if (NetworkUtil.isAirplaneModeOn()) {//没有关飞行模式
            ToastUtil.showToast(R.string.close_airplane_mode);
            return;
        }

        searchContent = etSearch.getText().toString();
        if (TextUtils.isEmpty(searchContent)) {//如果搜索内容为空
            if (!isHistorySearchResult) {//如果不是搜索历史记录
                SearchDataHelper.initSearchData(MainFragment.this);//初始化搜索记录
                isHistorySearchResult = true;//现在是搜索历史记录了
            }
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String searchCity = mCity;
                //如果存储的城市不为空，则换用存储的城市
                String saveCity = SPHelper.getString(Constants.DESTINATION_CITY, null);
                if (!TextUtils.isEmpty(saveCity)) searchCity = saveCity;
                if (TextUtils.isEmpty(searchCity)) {
                    requestPermission();//申请权限，获得权限后定位
                    return;
                }

                //如果是省份，则搜索城市列表设置为省份内所有的城市，否则设置为单个城市
                searchCityList.clear();
                if (searchCity != null) {
                    if (CityUtil.checkoutProvinceName(searchCity)) {
                        searchCityList.addAll(CityUtil.getCityList(searchCity));
                    } else {
                        searchCityList.add(searchCity);
                    }
                }

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!searchExpandFlag) {//展开搜索抽屉
                            expandSearchDrawer(true);//展开搜索抽屉
                            searchExpandFlag = true;//设置状态为展开
                        }
                        takeBackKeyboard();//收回键盘

                        mBaiduMap.clear();//清空地图上的所有标记点和绘制的路线

                        recyclerSearchResult.stopScroll();//停止信息列表滑动
                        searchList.clear();//清空searchList
                        searchAdapter.updateList();//通知adapter更新

                        //滚动到顶部
                        recyclerSearchResult.stopScroll();
                        recyclerSearchResult.scrollToPosition(0);
                        //加载搜索信息
                        llSearchLoading.setVisibility(View.VISIBLE);
                        recyclerSearchResult.setVisibility(View.GONE);
                    }
                });

                isHistorySearchResult = false;//已经不是搜索历史记录了
                currentPage = 0;//页数归零

                if (sharedPreferences.getBoolean(Constants.KEY_SEARCH_AROUND, false))
                    mySearch.searchType = MySearch.CONSTRAINT_CITY_SEARCH;//设置搜索类型为强制城市内搜索
                else mySearch.searchType = MySearch.CITY_SEARCH;//设置搜索类型为城市内搜索

                mySearch.startSearch(currentPage);//开始搜索
            }
        }).start();
    }

}
