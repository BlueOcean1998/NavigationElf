package com.example.foxizz.navigation.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.schemedata.SchemeAdapter;
import com.example.foxizz.navigation.schemedata.SchemeItem;
import com.example.foxizz.navigation.searchdata.SearchAdapter;
import com.example.foxizz.navigation.database.DatabaseHelper;
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

/**
 * app_name: Navigation
 * author: Foxizz
 * time: 2020-04-16
 */
public class MainActivity extends AppCompatActivity {

    //设置相关
    private SharedPreferences sharedPreferences;


    //地图控件
    public MapView mMapView;
    public BaiduMap mBaiduMap;
    public UiSettings mUiSettings;


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
    public Button emptyButton;//清空按钮
    public Button searchButton;//搜索按钮
    public ImageButton searchExpand;//搜索结果伸缩按钮

    public String searchContent = "";//搜索内容
    public boolean expandFlag = false;//伸缩状态
    public int bodyLength;//屏幕的长
    public int bodyShort;//屏幕的宽

    public LinearLayout searchDrawer;//搜索抽屉
    public RecyclerView searchResult;//搜索结果
    public List<SearchItem> searchList = new ArrayList<>();//搜索列表
    public StaggeredGridLayoutManager searchLayoutManager;//搜索布局管理器
    public SearchAdapter searchAdapter;//搜索适配器

    public LinearLayout infoLayout;//详细信息布局
    public ScrollView infoScroll;//详细信息布局的拖动条
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

    public LatLng startBusStationLocation;//公交导航第一站的坐标
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
    public Button returnButton;//返回按钮
    public Button infoButton;//路线规划、详细信息切换按钮
    public Button startButton;//开始导航按钮

    public boolean infoFlag;//信息显示状态


    //控制布局相关
    private ImageButton settings;//设置
    private ImageButton refresh;//刷新
    private ImageButton location;//定位

    private long exitTime = 0;//实现再按一次退出程序时，用于保存系统时间
    private long clickTime = 0;//防止连续点击按钮

    public void expandSelectLayout(boolean flag) {//伸缩选择布局
        expandLayout(this, selectLayout, flag);
    }

    public void expandSearchLayout(boolean flag) {//伸缩搜索布局
        expandLayout(this, searchLayout, flag);
    }

    public void expandSearchDrawer(boolean flag) {//伸缩搜索抽屉
        expandLayout(this, searchDrawer, flag);
        if(flag) rotateExpandIcon(searchExpand, 0, 180);//旋转伸展按钮
        else rotateExpandIcon(searchExpand, 180, 0);//旋转伸展按钮
    }

    public void expandInfoLayout(boolean flag) {//伸缩详细信息布局
        expandLayout(this, infoLayout, flag);
    }

    public void expandSchemeLayout(boolean flag) {//伸缩方案布局
        expandLayout(this, schemeLayout, flag);
    }

    public void expandSchemeDrawer(boolean flag) {//伸缩方案抽屉
        expandLayout(this, schemeDrawer, flag);
    }

    public void expandSchemeInfoDrawer(boolean flag) {//伸缩方案信息抽屉
        expandLayout(this, schemeInfoDrawer, flag);
    }

    public void expandStartLayout(boolean flag) {//伸缩开始导航布局
        expandLayout(this, startLayout, flag);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取偏好设置
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //新建数据库，已存在则连接数据库
        dbHelper = new DatabaseHelper(MainActivity.this, "Navigate.db", null, 1);

        InitMap();//初始化地图控件

        initMyView();//初始化自定义控件

        initMyDirectionSensor();//初始化方向传感器

        myLocation = new MyLocation(this);//初始化定位模块

        myPoiSearch = new MyPoiSearch(this);//初始化搜索模块
        myPoiSearch.initSearch();//初始化搜索目标信息

        myRoutePlanSearch = new MyRoutePlanSearch(this);//初始化路线规划模块
        myRoutePlanSearch.initRoutePlanSearch();//初始化路线规划

        myNavigateHelper = new MyNavigateHelper(this);//初始化导航模块

        requestPermission();//申请权限
    }

    //管理地图的生命周期
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();
        //开启定位的允许
        mBaiduMap.setMyLocationEnabled(true);
        //开启方向传感
        myOrientationListener.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();

        reSettings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    }

    //重新设置偏好
    @SuppressLint("SourceLockedOrientationActivity")
    private void reSettings() {
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

        if(sharedPreferences.getBoolean("landscape", false))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//自动旋转
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//只允许竖屏

        if(sharedPreferences.getBoolean("angle_3d", false))
            mUiSettings.setOverlookingGesturesEnabled(true);//启用3D视角
        else mUiSettings.setOverlookingGesturesEnabled(false);//禁用3D视角

        if(sharedPreferences.getBoolean("map_rotation", false))
            mUiSettings.setRotateGesturesEnabled(true);//启用地图旋转
        else mUiSettings.setRotateGesturesEnabled(false);//禁用地图旋转

        if(sharedPreferences.getBoolean("scale_control", false))
            mMapView.showScaleControl(true);//显示比例尺
        else mMapView.showScaleControl(false);//不显示比例尺

        if(sharedPreferences.getBoolean("zoom_controls", false))
            mMapView.showZoomControls(true);//显示缩放按钮
        else mMapView.showZoomControls(false);//不显示缩放按钮

        if(sharedPreferences.getBoolean("compass", true))
            mUiSettings.setCompassEnabled(true);//显示指南针
        else mUiSettings.setCompassEnabled(false);//不显示指南针
    }

    //初始化地图控件
    private void InitMap() {
        //获取地图控件引用
        mMapView = findViewById(R.id.map_view);
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
                    Toast.makeText(MainActivity.this, "正在下载离线地图", Toast.LENGTH_SHORT).show();
                }
            }
        });
        */
    }

    //初始化自定义控件
    private void initMyView() {
        settings = findViewById(R.id.settings_button);
        refresh = findViewById(R.id.refresh_button);
        location = findViewById(R.id.location_button);

        selectLayout = findViewById(R.id.select_layout);
        selectButton1 = findViewById(R.id.select_button1);
        selectButton2 = findViewById(R.id.select_button2);
        selectButton3 = findViewById(R.id.select_button3);
        selectButton4 = findViewById(R.id.select_button4);

        searchLayout = findViewById(R.id.search_layout);
        searchEdit = findViewById(R.id.search_edit);
        emptyButton = findViewById(R.id.empty_bottom);
        searchButton = findViewById(R.id.search_button);
        searchExpand = findViewById(R.id.search_expand);
        searchDrawer = findViewById(R.id.search_drawer);
        searchResult = findViewById(R.id.search_result);

        infoLayout = findViewById(R.id.info_layout);
        infoScroll = findViewById(R.id.info_scroll);
        infoTargetName = findViewById(R.id.info_target_name);
        infoAddress = findViewById(R.id.info_address);
        infoDistance = findViewById(R.id.info_distance);
        infoOthers = findViewById(R.id.info_others);

        schemeLayout = findViewById(R.id.scheme_layout);
        schemeReturnButton = findViewById(R.id.scheme_return_button);
        schemeDrawer = findViewById(R.id.scheme_drawer);
        schemeResult = findViewById(R.id.scheme_result);
        schemeInfoDrawer = findViewById(R.id.scheme_info_drawer);
        schemeInfoScroll = findViewById(R.id.scheme_info_scroll);
        schemeInfo = findViewById(R.id.scheme_info);

        startLayout = findViewById(R.id.start_layout);
        returnButton = findViewById(R.id.return_button);
        infoButton = findViewById(R.id.info_button);
        startButton = findViewById(R.id.start_button);

        //计算屏幕高度，用于下面的伸缩动画
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);

        Configuration configuration = this.getResources().getConfiguration();//获取设置的配置信息
        int ori = configuration.orientation;//获取屏幕方向
        if(ori == Configuration.ORIENTATION_LANDSCAPE) {//横屏时
            bodyLength = point.x;
            bodyShort = point.y;
        }
        else if(ori == Configuration.ORIENTATION_PORTRAIT) {//竖屏时
            bodyLength = point.y;
            bodyShort = point.x;
        }

        //设置搜索抽屉的结果列表、详细信息布局的拖动布局、路线方案抽屉的结果列表、路线方案信息的拖动布局的高度
        searchResult.getLayoutParams().height = bodyLength / 2;
        infoScroll.getLayoutParams().height = bodyLength / 3;
        schemeResult.getLayoutParams().height = bodyLength / 2;
        schemeInfoScroll.getLayoutParams().height = bodyLength / 4;

        //设置选项布局、搜索结果抽屉、详细信息、路线方案布局、方案抽屉、方案信息抽屉、开始导航布局初始高度为0
        selectLayout.getLayoutParams().height = 0;
        searchDrawer.getLayoutParams().height = 0;
        infoLayout.getLayoutParams().height = 0;
        schemeLayout.getLayoutParams().height = 0;
        schemeDrawer.getLayoutParams().height = 0;
        schemeInfoDrawer.getLayoutParams().height = 0;
        startLayout.getLayoutParams().height = 0;

        searchAdapter = new SearchAdapter(this);//初始化搜索适配器
        searchLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);//搜索布局行数为1
        searchResult.setAdapter(searchAdapter);//设置搜索适配器
        searchResult.setLayoutManager(searchLayoutManager);//设置搜索布局

        schemeAdapter = new SchemeAdapter(this);//初始化方案适配器
        schemeLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);//方案布局行数为1
        schemeResult.setAdapter(schemeAdapter);//设置方案适配器
        schemeResult.setLayoutManager(schemeLayoutManager);//设置方案布局

        //设置按钮的点击事件
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                if(mCity != null) intent.putExtra("mCity", mCity);
                startActivity(intent);
            }
        });

        //刷新按钮的点击事件
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//关闭当前活动
                startActivity(getIntent());//重启当前活动
            }
        });

        //定位按钮的点击事件
        location.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                myLocation.initLocationOption();//初始化定位
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
                //收回所有展开的item
                for(int i = 0; i < schemeList.size(); i++) {//遍历所有item
                    if(schemeList.get(i).getExpandFlag()) {//如果是展开状态
                        //用layoutManager找到相应的item
                        View view = schemeLayoutManager.findViewByPosition(i);
                        if(view != null) {
                            LinearLayout infoDrawer = view.findViewById(R.id.info_drawer);
                            ImageButton schemeExpand = view.findViewById(R.id.scheme_expand);
                            expandLayout(MainActivity.this, infoDrawer, false);
                            rotateExpandIcon(schemeExpand, 180, 0);//旋转伸展按钮
                            schemeList.get(i).setExpandFlag(false);
                            schemeAdapter.notifyDataSetChanged();//通知adapter更新
                        }
                    }
                }

                expandSelectLayout(true);//展开选择布局
                expandSchemeLayout(false);//收起方案抽屉
                expandStartLayout(true);//展开开始导航布局
                infoFlag = false;//设置信息状态为交通选择
                schemeInfoFlag = 0;//设置状态为不显示
            }
        });

        //清空按钮的点击事件
        emptyButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                if(isHistorySearchResult) {//如果是搜索历史记录
                    //如果有搜索内容则清空
                    if(!searchEdit.getText().toString().isEmpty()) {
                        searchEdit.setText("");
                        return;
                    }

                    //没有搜索记录
                    if(!dbHelper.ifHasSearchData()) return;

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(getString(R.string.warning));
                    builder.setMessage(getString(R.string.to_clear));

                    builder.setPositiveButton(getString(R.string.clear), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            searchEdit.setText("");//清空搜索输出框

                            if(expandFlag) {
                                expandSearchDrawer(false);//收起展开的搜索抽屉
                                expandFlag = false;//设置状态为收起
                            }

                            searchResult.stopScroll();//停止信息列表滑动

                            dbHelper.deleteAllSearchData();//清空数据库中的搜索记录
                        }
                    });

                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    });

                    builder.show();

                } else {//如果不是
                    isHistorySearchResult = true;//现在是搜索历史记录了
                    searchEdit.setText("");//清空搜索输出框
                    searchResult.stopScroll();//停止信息列表滑动
                    dbHelper.initSearchData();//初始化搜索记录
                }
            }
        });

        //搜索按钮的点击事件
        searchButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                if((System.currentTimeMillis() - clickTime) > 1000) //连续点击间隔时间不能小于1秒
                    clickTime = System.currentTimeMillis();
                else return;

                if(!isNetworkConnected(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isAirplaneModeOn(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, getString(R.string.close_airplane_mode), Toast.LENGTH_SHORT).show();
                    return;
                }

                searchContent = searchEdit.getText().toString();

                if(searchContent.isEmpty()) return;

                if(!expandFlag) {//展开搜索抽屉
                    searchResult.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.adapter_alpha2));//动画2，出现;
                    getValueAnimator(searchDrawer, 0, bodyLength / 2).start();//展开搜索抽屉
                    rotateExpandIcon(searchExpand, 0, 180);//伸展按钮的旋转动画
                    expandFlag = true;//设置状态为展开
                }

                //收回键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null) imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                String searchCity = null;//进行搜索的城市

                // 定位成功后才可以进行搜索
                if(mCity != null) searchCity = mCity;

                //如果数据库中的城市不为空，则换用数据库中的城市
                String databaseCity = dbHelper.getSettings("destination_city");
                if(!TextUtils.isEmpty(databaseCity)) searchCity = databaseCity;

                searchResult.stopScroll();//停止信息列表滑动

                if(sharedPreferences.getBoolean("search_around", false))
                    myPoiSearch.poiSearchType = MyPoiSearch.CONSTRAINT_CITY_SEARCH;//设置搜索类型为强制城市内搜索
                else myPoiSearch.poiSearchType = MyPoiSearch.CITY_SEARCH;//设置搜索类型为城市内搜索

                if(searchCity == null) {
                    requestPermission();//申请权限
                    return;
                }

                //开始城市内搜索
                mPoiSearch.searchInCity(new PoiCitySearchOption()
                        .city(searchCity)
                        .keyword(searchContent)
                        .cityLimit(false));//不限制搜索范围在城市内
            }
        });

        //伸缩按钮的点击事件
        searchExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(expandFlag) {//如果是展开状态
                    expandSearchDrawer(false);//收起搜索抽屉
                    expandFlag = false;//设置状态为收起
                } else {//如果是收起状态
                    expandSearchDrawer(true);//展开搜索抽屉
                    expandFlag = true;//设置状态为展开
                }
            }
        });

        //返回按钮的点击事件
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandSelectLayout(false);//收起选择布局
                expandSearchLayout(true);//展开搜索布局
                if(!expandFlag) {
                    expandSearchDrawer(true);//展开被收起的搜索抽屉
                    expandFlag = true;//设置状态为展开
                }
                expandInfoLayout(false);//收起详细信息布局
                expandStartLayout(false);//收起开始导航布局

                if(schemeInfoFlag != 0) {//如果方案布局已经展开
                    expandSchemeLayout(false);//收起方案布局
                    schemeInfoFlag = 0;//设置状态为不显示
                }
            }
        });

        //路线规划、详细信息切换按钮的点击事件
        infoButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                if(schemeInfoFlag != 0) {//如果方案布局已经展开
                    expandSelectLayout(true);//展开选择布局
                    infoButton.setText(R.string.info_button2);//设置按钮为详细信息
                    infoFlag = false;//设置信息状态为交通选择

                    expandSchemeLayout(false);//收起方案布局
                    schemeInfoFlag = 0;//设置状态为不显示
                    return;
                }

                if(infoFlag) {//如果显示为详细信息
                    infoButton.setText(R.string.info_button2);//设置按钮为详细信息
                    expandSelectLayout(true);//展开选择布局
                    expandInfoLayout(false);//收起详细信息布局
                    infoFlag = false;//设置信息状态交通选择

                    //重置交通类型为步行
                    routePlanSelect = MainActivity.WALKING;
                    selectButton1.setBackgroundResource(R.drawable.button_background_gray);
                    selectButton2.setBackgroundResource(R.drawable.button_background_black);
                    selectButton3.setBackgroundResource(R.drawable.button_background_gray);
                    selectButton4.setBackgroundResource(R.drawable.button_background_gray);

                    myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
                } else {//如果显示为交通选择
                    infoButton.setText(R.string.info_button1);//设置按钮为路线
                    expandSelectLayout(false);//收起选择布局
                    expandInfoLayout(true);//展开详细信息布局
                    infoFlag = true;//设置信息状态为详细信息
                }
            }
        });

        //开始导航按钮的点击事件
        startButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                myNavigateHelper.startNavigate();//开始导航
            }
        });

    }

    //初始化方向传感器
    private void initMyDirectionSensor() {
        //方向传感器
        myOrientationListener = new MyOrientationListener(this);
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

    //申请权限
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void requestPermission() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };

        List<String> permissionList = new ArrayList<>();

        for(String permission: permissions) {
            if(ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED)
                permissionList.add(permission);
        }

        String[] tmpList = new String[permissionList.size()];

        //如果列表为空，则获取了全部权限不用再获取，否则要获取
        if(permissionList.isEmpty()) {
            myLocation.initLocationOption();//初始化定位
        } else {
            //申请权限
            ActivityCompat.requestPermissions(this, permissionList.toArray(tmpList), 0);

            dbHelper.initSearchData();//初始化搜索记录
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                myLocation.initLocationOption();//初始化定位
            else
                Toast.makeText(this, getString(R.string.get_permission_fail), Toast.LENGTH_SHORT).show();
        }
    }

    //重写，实现再按一次退出以及关闭抽屉
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, getString(R.string.exit_app), Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
