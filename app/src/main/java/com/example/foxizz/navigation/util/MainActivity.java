package com.example.foxizz.navigation.util;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.foxizz.navigation.demo.MyOrientationListener;
import com.example.foxizz.navigation.overlayutil.BikingRouteOverlay;
import com.example.foxizz.navigation.overlayutil.DrivingRouteOverlay;
import com.example.foxizz.navigation.overlayutil.IndoorRouteOverlay;
import com.example.foxizz.navigation.overlayutil.MassTransitRouteOverlay;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.overlayutil.TransitRouteOverlay;
import com.example.foxizz.navigation.overlayutil.WalkingRouteOverlay;
import com.example.foxizz.navigation.searchdata.SearchAdapter;
import com.example.foxizz.navigation.searchdata.SearchItem;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.foxizz.navigation.demo.Tools.expandLayout;
import static com.example.foxizz.navigation.demo.Tools.getValueAnimator;
import static com.example.foxizz.navigation.demo.Tools.isAirplaneModeOn;
import static com.example.foxizz.navigation.demo.Tools.isEffectiveDate;
import static com.example.foxizz.navigation.demo.Tools.isNetworkConnected;
import static com.example.foxizz.navigation.demo.Tools.rotateExpandIcon;

public class MainActivity extends AppCompatActivity {

    //地图控件
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private UiSettings mUiSettings;


    //方向传感器
    private MyOrientationListener myOrientationListener;
    private static float mLastX;//方向角度


    //动态申请权限相关
    private static int READY_TO_LOCATION = 0;//准备定位
    private static int REQUEST_FAILED = 1;//申请失败
    private static int permissionFlag;//权限状态


    //定位相关
    private LocationClient mLocationClient;
    private static boolean isFirstLoc = true;//是否是首次定位
    private static MyLocationData locData;//地址信息
    private static LatLng latLng;//坐标
    private static int mLocType;//定位结果
    private static float mRadius;//精度半径
    private static double mLatitude;//纬度
    private static double mLongitude;//经度
    private static String mCity;//所在城市


    //搜索相关
    private PoiSearch mPoiSearch;
    private final static int CITY_SEARCH = 0;//城市内搜索
    private final static int NEARBY_SEARCH = 1;//周边搜索
    private static int poiSearchType = CITY_SEARCH;//使用的搜索类型

    private LinearLayout searchLayout;//搜索布局
    private EditText searchEdit;//搜索输入框
    private Button emptyButton;//清空按钮
    private Button searchButton;//搜索按钮
    private ImageButton searchExpand;//搜索结果伸缩按钮

    private static String searchContent = "";//搜索内容
    private static boolean expandFlag = false;//伸缩状态
    public static boolean getExpandFlag() {
        return expandFlag;
    }
    public static void setExpandFlag(boolean expandFlag) {
        MainActivity.expandFlag = expandFlag;
    }
    private static int bodyHeight;//屏幕高度

    private LinearLayout searchDrawer;//搜索结果抽屉
    private RecyclerView searchResult;//搜索结果列表
    private List<SearchItem> searchList = new ArrayList<>();
    private StaggeredGridLayoutManager layoutManager;
    private SearchAdapter searchAdapter;//列表适配器

    private LinearLayout infoLayout;//详细信息布局
    private ScrollView infoScroll;//详细信息布局的拖动条
    private TextView infoTargetName;//目标名
    public TextView getInfoTargetName() {
        return infoTargetName;
    }
    private TextView infoAddress;//目标地址
    public TextView getInfoAddress() {
        return infoAddress;
    }
    private TextView infoDistance;//与目标的距离
    public TextView getInfoDistance() {
        return infoDistance;
    }
    private TextView infoOthers;//目标的其它信息（联系方式，营业时间等）
    public TextView getInfoOthers() {
        return infoOthers;
    }


    //路线规划相关
    private RoutePlanSearch mSearch;

    private LinearLayout selectLayout;//选择布局
    private Button selectButton1;//选择驾车
    private Button selectButton2;//选择步行
    private Button selectButton3;//选择公交

    private final static int DRIVING = 0;//驾车
    private final static int WALKING = 1;//步行
    private final static int TRANSIT = 2;//公交
    private static int routePlanSelect = DRIVING;//默认为驾车
    public static int getRoutePlanSelect() {
        return routePlanSelect;
    }

    private static int searchItemSelect = 0;//选择的是哪个item
    public void setSearchItemSelect(int searchItemSelect) {
        MainActivity.searchItemSelect = searchItemSelect;
    }

    private LinearLayout startLayout;//开始导航布局
    private Button returnButton;//返回按钮
    private Button infoButton;//路线规划、详细信息切换按钮
    public Button getInfoButton() {
        return infoButton;
    }
    private Button startButton;//开始导航按钮

    private static boolean infoFlag;//信息显示状态
    public void setInfoFlag(boolean infoFlag) {
        MainActivity.infoFlag = infoFlag;
    }

    private LinearLayout endLayout;//结束导航布局
    private Button endButton;//结束导航按钮


    private long exitTime = 0;//实现再按一次退出程序时，用于保存系统时间


    //控制布局相关
    public void expandSelectLayout(boolean flag) {//伸缩选择布局
        expandLayout(MainActivity.this, selectLayout, flag);
    }

    public void expandSearchLayout(boolean flag) {//伸缩搜索布局
        expandLayout(MainActivity.this, searchLayout, flag);
    }

    public void expandSearchDrawer(boolean flag) {//伸缩搜索抽屉
        expandLayout(MainActivity.this, searchDrawer, flag);
        if(flag) rotateExpandIcon(searchExpand, 0, 180);//旋转伸展按钮
        else rotateExpandIcon(searchExpand, 180, 0);//旋转伸展按钮
    }

    public void expandInfoLayout(boolean flag) {//伸缩详细信息布局
        expandLayout(MainActivity.this, infoLayout, flag);
    }

    //伸缩开始导航布局
    public void expandStartLayout(boolean flag) {
        expandLayout(MainActivity.this, startLayout, flag);
    }

    public void expandEndLayout(boolean flag) {//伸缩结束导航布局
        expandLayout(MainActivity.this, endLayout, flag);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitMap();//初始化地图控件

        initMyView();//初始化自定义控件

        initMyOrien();//初始化方向传感器

        requestPermission();//申请权限
    }

    //初始化地图控件
    private void InitMap() {
        //获取地图控件引用
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mUiSettings = mBaiduMap.getUiSettings();

        //配置定位图层显示方式，使用默认的定位图标，设置精确度圆的填充色和边框色
        //LocationMode定位模式有三种：普通模式，跟随模式，罗盘模式，在这使用普通模式
        MyLocationConfiguration myLocationConfiguration =
                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,
                        true, null, 0xAA9FCFFF, 0xAA5F7FBF);
        mBaiduMap.setMyLocationConfiguration(myLocationConfiguration);

        mMapView.removeViewAt(1);//去除百度水印
        mMapView.showScaleControl(true);//显示比例尺
        mMapView.setScaleControlPosition(new Point(0, bodyHeight));//调整比例尺位置到左上角
        mMapView.showZoomControls(false);//去除缩放按钮
        mUiSettings.setCompassEnabled(false);//去除指南针

        //设置缩放等级
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(18.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    //初始化自定义控件
    private void initMyView() {
        selectLayout = findViewById(R.id.select_layout);
        selectButton1 = findViewById(R.id.select_button1);
        selectButton2 = findViewById(R.id.select_button2);
        selectButton3 = findViewById(R.id.select_button3);

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

        startLayout = findViewById(R.id.start_layout);
        returnButton = findViewById(R.id.return_button);
        infoButton = findViewById(R.id.info_button);
        startButton = findViewById(R.id.start_button);

        endLayout = findViewById(R.id.end_layout);
        endButton = findViewById(R.id.end_button);

        //计算半个屏幕高度，用于下面的伸缩动画
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        bodyHeight = point.y;

        //设置搜索抽屉的结果列表、详细信息布局的拖动布局的高度
        searchResult.getLayoutParams().height = bodyHeight / 2;
        infoScroll.getLayoutParams().height = bodyHeight / 3;

        //设置选项布局、搜索结果抽屉、详细信息、开始导航、结束导航布局初始高度为0
        selectLayout.getLayoutParams().height = 0;
        searchDrawer.getLayoutParams().height = 0;
        infoLayout.getLayoutParams().height = 0;
        startLayout.getLayoutParams().height = 0;
        endLayout.getLayoutParams().height = 0;

        searchAdapter = new SearchAdapter(MainActivity.this, searchList);//初始化适配器
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);//布局行数为1
        searchResult.setAdapter(searchAdapter);//设置适配器
        searchResult.setLayoutManager(layoutManager);//设置布局

        //默认为驾车
        selectButton1.setBackgroundResource(R.drawable.button_background_gray);

        //驾车按钮的点击事件
        selectButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectButton1.setBackgroundResource(R.drawable.button_background_gray);
                selectButton2.setBackgroundResource(R.drawable.button_background_black);
                selectButton3.setBackgroundResource(R.drawable.button_background_black);
                routePlanSelect = DRIVING;

                //获取定位坐标和目标坐标
                PlanNode startNode = PlanNode.withLocation(latLng);
                PlanNode endNode = PlanNode.withLocation(searchList.get(searchItemSelect).getLatLng());

                //开始驾车路线规划
                mSearch.drivingSearch((new DrivingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
            }
        });

        //步行按钮的点击事件
        selectButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectButton1.setBackgroundResource(R.drawable.button_background_black);
                selectButton2.setBackgroundResource(R.drawable.button_background_gray);
                selectButton3.setBackgroundResource(R.drawable.button_background_black);
                routePlanSelect = WALKING;

                //获取定位坐标和目标坐标
                PlanNode startNode = PlanNode.withLocation(latLng);
                PlanNode endNode = PlanNode.withLocation(searchList.get(searchItemSelect).getLatLng());

                //开始步行路线规划
                mSearch.walkingSearch((new WalkingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
            }
        });

        //公交按钮的点击事件
        selectButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectButton1.setBackgroundResource(R.drawable.button_background_black);
                selectButton2.setBackgroundResource(R.drawable.button_background_black);
                selectButton3.setBackgroundResource(R.drawable.button_background_gray);
                routePlanSelect = TRANSIT;

                //获取定位坐标和目标坐标
                PlanNode startNode = PlanNode.withLocation(latLng);
                PlanNode endNode = PlanNode.withLocation(searchList.get(searchItemSelect).getLatLng());

                //开始公交路线规划
                TransitRoutePlanOption transitRoutePlanOption = new TransitRoutePlanOption();
                transitRoutePlanOption.city(mCity);
                transitRoutePlanOption.from(startNode);
                transitRoutePlanOption.to(endNode);
                mSearch.transitSearch(transitRoutePlanOption);
            }
        });

        //清空按钮的点击事件
        emptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdit.setText("");

                if(expandFlag) {
                    expandSearchDrawer(false);//收起展开的搜索抽屉
                    expandFlag = false;//设置状态为收起
                }

                searchList.clear();//清空搜索结果列表
                searchAdapter.notifyDataSetChanged();//通知adapter更新
            }
        });

        //搜索按钮的点击事件
        searchButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                if(isNetworkConnected(MainActivity.this)) {
                    if(isAirplaneModeOn(MainActivity.this)){
                        Toast.makeText(MainActivity.this,
                                "请检查是否有关闭飞行模式", Toast.LENGTH_LONG).show();
                    } else {
                        requestPermission();//申请权限
                        if(permissionFlag == READY_TO_LOCATION) {
                            searchContent = searchEdit.getText().toString();
                            if(!searchContent.equals("")){
                                if(!expandFlag) {//展开搜索抽屉
                                    searchResult.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.adapter_alpha2));//动画2，出现;
                                    getValueAnimator(searchDrawer, 0, bodyHeight / 2).start();//展开搜索抽屉
                                    rotateExpandIcon(searchExpand, 0, 180);//伸展按钮的旋转动画
                                    expandFlag = true;//设置状态为展开
                                }

                                //收回键盘
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                                //开始城市内搜索
                                mPoiSearch.searchInCity(new PoiCitySearchOption()
                                        .city(mCity)
                                        .keyword(searchContent));
                            }
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this,
                            "网络错误，请检查网络连接是否正常", Toast.LENGTH_LONG).show();
                }
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
            }
        });

        //路线规划、详细信息切换按钮的点击事件
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(infoFlag) {//如果状态为展开
                    infoButton.setText(R.string.info_button2);//设置按钮为详细信息
                    expandSelectLayout(true);//展开选择布局
                    expandInfoLayout(false);//收起详细信息布局
                    setInfoFlag(false);//设置信息状态为收起

                    startRoutePlanSearch();//开始路线规划
                } else {//如果状态为收起
                    infoButton.setText(R.string.info_button1);//设置按钮为路线
                    expandSelectLayout(false);//收起选择布局
                    expandInfoLayout(true);//展开详细信息布局
                    setInfoFlag(true);//设置信息状态为展开
                }
            }
        });

        //开始导航按钮的点击事件
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandSelectLayout(false);//收起选择布局
                expandInfoLayout(false);//收起详细信息布局
                expandStartLayout(false);//收起开始导航布局
                expandEndLayout(true);//展开结束导航布局


            }
        });

        //结束导航按钮的点击事件
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandSearchLayout(true);//展开搜索布局
                if(!expandFlag) {
                    expandSearchDrawer(true);//展开搜索抽屉
                    expandFlag = true;
                }
                expandEndLayout(false);//收起结束布局


            }
        });

    }

    //初始化方向传感器
    private void initMyOrien() {
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
    private void requestPermission() {
        List<String> permissionList = new ArrayList<>();

        /*
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        */
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        /*
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        */

        //如果列表为空，则获取了全部权限不用再获取，否则要获取
        if(permissionList.isEmpty()) {
            initLocationOption();//初始化定位
            initSearch();//初始化搜索目标信息
            initRoutePlanSearch();//初始化路线规划

            permissionFlag = READY_TO_LOCATION;
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    permissionList.toArray(new String[0]), 0);

            permissionFlag = REQUEST_FAILED;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocationOption();//初始化定位
                initSearch();//初始化搜索目标信息
                initRoutePlanSearch();//初始化路线规划
            } else {
                Toast.makeText(MainActivity.this,
                        "获取权限失败，若要定位请手动开启", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //初始化定位
    private void initLocationOption() {
        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mLocationClient = new LocationClient(this);

        //定位监听
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                //mapView 销毁后不在处理新接收的位置
                if (location == null || mMapView == null){
                    return;
                }

                //获取定位数据
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mLocType = location.getLocType();
                mRadius = location.getRadius();
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();
                mCity = location.getCity();

                //更新定位
                locData = new MyLocationData.Builder()
                        .accuracy(mRadius)
                        .direction(mLastX)
                        .latitude(mLatitude)
                        .longitude(mLongitude).build();
                mBaiduMap.setMyLocationData(locData);//设置定位数据

                if(isFirstLoc) {
                    isFirstLoc = false;

                    //改变地图状态
                    MapStatusUpdate msu= MapStatusUpdateFactory.newLatLng(latLng);
                    mBaiduMap.setMapStatus(msu);
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(latLng);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                    if(mLocType == BDLocation.TypeGpsLocation //GPS定位结果
                            || mLocType == BDLocation.TypeNetWorkLocation //网络定位结果
                            || mLocType == BDLocation.TypeOffLineLocation) {//离线定位结果
                        //Toast.makeText(MainActivity.this,
                        //location.getAddrStr(), Toast.LENGTH_SHORT).show();
                    } else if(mLocType == BDLocation.TypeServerError) {//服务器错误
                        Toast.makeText(MainActivity.this,
                                "服务器错误", Toast.LENGTH_SHORT).show();
                    } else if(mLocType == BDLocation.TypeNetWorkException) {//网络错误
                        Toast.makeText(MainActivity.this,
                                "网络错误，请检查网络连接是否正常", Toast.LENGTH_SHORT).show();
                    } else if(mLocType == BDLocation.TypeCriteriaException) {//手机模式错误
                        Toast.makeText(MainActivity.this,
                                "请检查是否有关闭飞行模式", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //声明LocationClient类实例并配置定位参数
        LocationClientOption option = new LocationClientOption();
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        option.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(1000);
        //可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址描述
        option.setIsNeedLocationDescribe(true);
        //可选，设置是否需要设备方向结果
        option.setNeedDeviceDirect(true);
        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setLocationNotify(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，
        //结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(true);
        //可选，默认false，设置是否开启Gps定位
        option.setOpenGps(true);
        //可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        option.setIsNeedAltitude(false);
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，
        //该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        option.setOpenAutoNotifyMode();
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        //option.setOpenAutoNotifyMode(3000, 1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        mLocationClient.setLocOption(option);
        //开启定位
        mLocationClient.start();
    }

    //初始化搜索目标信息
    private void initSearch() {
        mPoiSearch = PoiSearch.newInstance();

        OnGetPoiSearchResultListener listener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if(poiResult == null
                        || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {//没有找到检索结果
                    Toast.makeText(MainActivity.this,
                            "未找到结果", Toast.LENGTH_LONG).show();
                    return;
                }

                if(poiResult.error == SearchResult.ERRORNO.NO_ERROR) {//检索结果正常返回
                    //如果搜索到的目标数量小于50或使用的是周边搜索
                    if(poiResult.getTotalPoiNum() < 50 || poiSearchType == NEARBY_SEARCH) {
                        /*
                        Toast.makeText(MainActivity.this,
                                "总共查到" + poiResult.getTotalPoiNum() + "个兴趣点, 分为"
                                        + poiResult.getTotalPageNum() + "页", Toast.LENGTH_SHORT).show();
                        */

                        mBaiduMap.clear();//清空地图上的所有标记点和绘制的路线
                        searchList.clear();//清空searchList

                        /*
                        MyPoiOverlay poiOverlay = new MyPoiOverlay(mBaiduMap, mPoiSearch);
                        mBaiduMap.setOnMarkerClickListener(poiOverlay);
                        poiOverlay.setData(poiResult);//设置POI数据
                        poiOverlay.addToMap();//将所有的overlay添加到地图上
                        poiOverlay.zoomToSpan();
                        */

                        //详细搜索所有页的所有内容，超过5页则只搜索5页内容
                        int searchNum = 5;
                        if(poiResult.getTotalPageNum() < searchNum) {
                            searchNum = poiResult.getTotalPageNum();
                        }
                        for(int i = 0; i < searchNum; i++) {
                            for(PoiInfo info: poiResult.getAllPoi()) {
                                //uid的集合，最多可以传入10个uid，多个uid之间用英文逗号分隔。
                                mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUids(info.getUid()));
                            }

                            poiResult.setCurrentPageNum(i);
                        }

                        poiSearchType = CITY_SEARCH;//还原搜索类型为城市内搜索

                    } else {//如果搜索到的目标数量不小于50则用周边搜索周围5km内的目标
                        mPoiSearch.searchNearby(new PoiNearbySearchOption()
                                .location(latLng)
                                .radius(5000)
                                .keyword(searchContent));

                        poiSearchType = NEARBY_SEARCH;//设置搜索类型为周边搜索
                    }
                }
            }

            @SuppressLint("SimpleDateFormat")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailResult) {
                if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this,
                            "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                } else {//正常返回结果的时候，此处可以获得很多相关信息
                    SearchItem searchItem = new SearchItem();
                    //详细检索结果往往只有一个
                    for(PoiDetailInfo info: poiDetailResult.getPoiDetailInfoList()) {
                        searchItem.setTargetName(info.getName());//获取并设置目标名
                        searchItem.setAddress(info.getAddress());//获取并设置目标地址

                        LatLng tLatLng = info.getLocation();//获取目标坐标
                        searchItem.setLatLng(tLatLng);//设置目标坐标

                        //获取定位点到目标点的距离（单位：m，结果除以1000转化为km）
                        double distance = (DistanceUtil.getDistance(latLng, tLatLng) / 1000);
                        //保留两位小数
                        BigDecimal bd = new BigDecimal(distance);
                        distance = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        searchItem.setDistance(distance);


                        String otherInfo = "";

                        if(!info.getTelephone().equals("")) {
                            otherInfo += "联系方式：" + info.getTelephone() + "\n";
                        }

                        if(!info.getShopHours().equals("")) {
                            otherInfo += "营业时间：" + info.getShopHours();
                            try {
                                boolean flag = false;

                                DateFormat sdf = new SimpleDateFormat("HH:mm");
                                Date nowTime = sdf.parse(sdf.format(new Date()));
                                String[] shopHours = info.getShopHours().split(",");
                                for(String shopHour: shopHours) {
                                    String[] time = shopHour.split("-");
                                    Date startTime = sdf.parse(time[0]);
                                    Date endTime = sdf.parse(time[1]);
                                    if(isEffectiveDate(nowTime, startTime, endTime)) {
                                        flag = true;
                                    }
                                }

                                if(flag) otherInfo += " 营业中";
                                else otherInfo += " 休息中";
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            otherInfo += "\n";
                        }

                        if(info.getPrice() != 0) {
                            otherInfo += "平均消费：" + info.getPrice() + "\n";
                        }

                        searchItem.setOtherInfo(otherInfo);

                        searchList.add(searchItem);//添加搜到的内容到searchList
                    }

                    //这里的poiSearchType因为线程不同步问题始终不会等于NEARBY_SEARCH，待解决，（影响不大）
                    if(poiSearchType == NEARBY_SEARCH) {//如果是使用周边搜索
                        //按距离升序排序
                        searchList.sort(new Comparator<SearchItem>() {
                            @Override
                            public int compare(SearchItem o1, SearchItem o2) {
                                return o1.getDistance().compareTo(o2.getDistance());
                            }
                        });
                    }

                    searchAdapter.notifyDataSetChanged();//通知searchAdapter更新
                    searchResult.scrollToPosition(0);//移动回头部
                }
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }

            //已弃用的方法，但仍需实现
            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }
        };

        mPoiSearch.setOnGetPoiSearchResultListener(listener);
    }

    //初始化路线规划
    private void initRoutePlanSearch() {
        //创建路线规划检索实例
        mSearch = RoutePlanSearch.newInstance();

        //创建路线规划检索结果监听器
        OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                //创建WalkingRouteOverlay实例
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
                if(walkingRouteResult.getRouteLines().size() > 0) {
                    //清空地图上的标记
                    mBaiduMap.clear();
                    //获取路径规划数据,(以返回的第一条数据为例)
                    //为WalkingRouteOverlay实例设置路径数据
                    overlay.setData(walkingRouteResult.getRouteLines().get(0));
                    //在地图上绘制WalkingRouteOverlay
                    overlay.addToMap();
                    //将路线放在最佳视野位置
                    overlay.zoomToSpan();
                }
            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
                try {
                    //创建TransitRouteOverlay实例
                    TransitRouteOverlay overlay = new TransitRouteOverlay(mBaiduMap);
                    if(transitRouteResult.getRouteLines().size() > 0) {
                        //清空地图上的标记
                        mBaiduMap.clear();
                        //获取路径规划数据,(以返回的第一条数据为例)
                        //为TransitRouteOverlay实例设置路径数据
                        overlay.setData(transitRouteResult.getRouteLines().get(0));
                        //在地图上绘制TransitRouteOverlay
                        overlay.addToMap();
                        //将路线放在最佳视野位置
                        overlay.zoomToSpan();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,
                            "路程太短，建议步行", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
                //创建MassTransitRouteOverlay实例
                MassTransitRouteOverlay overlay = new MassTransitRouteOverlay(mBaiduMap);
                if(massTransitRouteResult.getRouteLines() != null && massTransitRouteResult.getRouteLines().size() > 0){
                    //清空地图上的标记
                    mBaiduMap.clear();
                    //获取路线规划数据（以返回的第一条数据为例）
                    //为MassTransitRouteOverlay设置数据
                    overlay.setData(massTransitRouteResult.getRouteLines().get(0));
                    //在地图上绘制Overlay
                    overlay.addToMap();
                    //将路线放在最佳视野位置
                    overlay.zoomToSpan();
                }
            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                //创建DrivingRouteOverlay实例
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
                if(drivingRouteResult.getRouteLines().size() > 0) {
                    //清空地图上的标记
                    mBaiduMap.clear();
                    //获取路径规划数据,(以返回的第一条路线为例）
                    //为DrivingRouteOverlay实例设置数据
                    overlay.setData(drivingRouteResult.getRouteLines().get(0));
                    //在地图上绘制DrivingRouteOverlay
                    overlay.addToMap();
                    //将路线放在最佳视野位置
                    overlay.zoomToSpan();
                }
            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
                //创建IndoorRouteOverlay实例
                IndoorRouteOverlay overlay = new IndoorRouteOverlay(mBaiduMap);
                if(indoorRouteResult.getRouteLines() != null && indoorRouteResult.getRouteLines().size() > 0) {
                    //清空地图上的标记
                    mBaiduMap.clear();
                    //获取室内路径规划数据（以返回的第一条路线为例）
                    //为IndoorRouteOverlay实例设置数据
                    overlay.setData(indoorRouteResult.getRouteLines().get(0));
                    //在地图上绘制IndoorRouteOverlay
                    overlay.addToMap();
                    //将路线放在最佳视野位置
                    overlay.zoomToSpan();
                }
            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
                //创建BikingRouteOverlay实例
                BikingRouteOverlay overlay = new BikingRouteOverlay(mBaiduMap);
                if(bikingRouteResult.getRouteLines().size() > 0) {
                    //清空地图上的标记
                    mBaiduMap.clear();
                    //获取路径规划数据,(以返回的第一条路线为例）
                    //为BikingRouteOverlay实例设置数据
                    overlay.setData(bikingRouteResult.getRouteLines().get(0));
                    //在地图上绘制BikingRouteOverlay
                    overlay.addToMap();
                    //将路线放在最佳视野位置
                    overlay.zoomToSpan();
                }
            }
        };

        //设置路线规划检索监听器
        mSearch.setOnGetRoutePlanResultListener(listener);
    }

    //开始路线规划
    public void startRoutePlanSearch() {
        //获取定位点和目标点
        PlanNode startNode = PlanNode.withLocation(latLng);
        PlanNode endNode = PlanNode.withLocation(searchList.get(searchItemSelect).getLatLng());

        switch(MainActivity.getRoutePlanSelect()) {
            //驾车路线规划
            case 0:
                mSearch.drivingSearch((new DrivingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
                break;

            //步行路线规划
            case 1:
                mSearch.walkingSearch((new WalkingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
                break;

            //公交路线规划
            case 2:
                TransitRoutePlanOption transitRoutePlanOption = new TransitRoutePlanOption();
                transitRoutePlanOption.city(mCity);
                transitRoutePlanOption.from(startNode);
                transitRoutePlanOption.to(endNode);
                mSearch.transitSearch(transitRoutePlanOption);
                break;
        }
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
        if (mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
        //释放地图、POI检索、路线规划实例
        mMapView.onDestroy();
        mPoiSearch.destroy();
        mSearch.destroy();
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
