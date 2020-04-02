package com.example.foxizz.navigation.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
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
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.searchdata.SearchAdapter;
import com.example.foxizz.navigation.searchdata.SearchItem;
import com.example.foxizz.navigation.util.MyLocation;
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
 * time: 2020-04-02
 */
public class MainActivity extends AppCompatActivity {

    //地图控件
    public MapView mMapView;
    public BaiduMap mBaiduMap;
    public UiSettings mUiSettings;


    //方向传感器
    public MyOrientationListener myOrientationListener;
    public static float mLastX;//方向角度


    //动态申请权限相关
    public static int READY_TO_LOCATION = 0;//准备定位
    public static int REQUEST_FAILED = 1;//申请失败
    public static int permissionFlag;//权限状态


    //定位相关
    private MyLocation myLocation;

    public LocationClient mLocationClient;
    public static boolean isFirstLoc = true;//是否是首次定位
    public static MyLocationData locData;//地址信息
    public static LatLng latLng;//坐标
    public static int mLocType;//定位结果
    public static float mRadius;//精度半径
    public static double mLatitude;//纬度
    public static double mLongitude;//经度
    public static String mCity;//所在城市


    //搜索相关
    private MyPoiSearch myPoiSearch;

    public PoiSearch mPoiSearch;
    public final static int CITY_SEARCH = 0;//城市内搜索
    public final static int NEARBY_SEARCH = 1;//周边搜索
    public static int poiSearchType = CITY_SEARCH;//使用的搜索类型

    public LinearLayout searchLayout;//搜索布局
    public EditText searchEdit;//搜索输入框
    public Button emptyButton;//清空按钮
    public Button searchButton;//搜索按钮
    public ImageButton searchExpand;//搜索结果伸缩按钮

    public static String searchContent = "";//搜索内容
    public static boolean expandFlag = false;//伸缩状态
    public static int bodyHeight;//屏幕高度

    public LinearLayout searchDrawer;//搜索结果抽屉
    public RecyclerView searchResult;//搜索结果列表
    public List<SearchItem> searchList = new ArrayList<>();
    public StaggeredGridLayoutManager layoutManager;
    public SearchAdapter searchAdapter;//列表适配器

    public LinearLayout infoLayout;//详细信息布局
    public ScrollView infoScroll;//详细信息布局的拖动条
    public TextView infoTargetName;//目标名
    public TextView infoAddress;//目标地址
    public TextView infoDistance;//与目标的距离
    public TextView infoOthers;//目标的其它信息（联系方式，营业时间等）


    //路线规划相关
    private MyRoutePlanSearch myRoutePlanSearch;

    public RoutePlanSearch mSearch;

    public LinearLayout selectLayout;//选择布局
    public Button selectButton1;//选择驾车
    public Button selectButton2;//选择步行
    public Button selectButton3;//选择公交

    public final static int DRIVING = 0;//驾车
    public final static int WALKING = 1;//步行
    public final static int TRANSIT = 2;//公交
    public static int routePlanSelect = DRIVING;//默认为驾车

    public static int searchItemSelect = 0;//选择的是哪个item

    public LinearLayout startLayout;//开始导航布局
    public Button returnButton;//返回按钮
    public Button infoButton;//路线规划、详细信息切换按钮
    public Button startButton;//开始导航按钮

    public static boolean infoFlag;//信息显示状态
    public void setInfoFlag(boolean infoFlag) {
        MainActivity.infoFlag = infoFlag;
    }

    public LinearLayout endLayout;//结束导航布局
    public Button endButton;//结束导航按钮


    public long exitTime = 0;//实现再按一次退出程序时，用于保存系统时间


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

        initMyDirectionSensor();//初始化方向传感器

        myLocation = new MyLocation(this);//初始化定位模块

        myPoiSearch = new MyPoiSearch(this);//初始化搜索模块

        myRoutePlanSearch = new MyRoutePlanSearch(this);//初始化路线规划模块

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

                    myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
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
    private void requestPermission() {
        List<String> permissionList = new ArrayList<>();

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        //如果列表为空，则获取了全部权限不用再获取，否则要获取
        if(permissionList.isEmpty()) {
            myLocation.initLocationOption();//初始化定位
            myPoiSearch.initSearch();//初始化搜索目标信息
            myRoutePlanSearch.initRoutePlanSearch();//初始化路线规划

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
                myLocation.initLocationOption();//初始化定位
                myPoiSearch.initSearch();//初始化搜索目标信息
                myRoutePlanSearch.initRoutePlanSearch();//初始化路线规划
            } else {
                Toast.makeText(MainActivity.this,
                        "获取权限失败，若要定位请手动开启", Toast.LENGTH_SHORT).show();
            }
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
