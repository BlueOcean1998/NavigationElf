package com.example.foxizz.navigation.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.searchdata.SearchAdapter;
import com.example.foxizz.navigation.searchdata.SearchDatabase;
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
 * time: 2020-04-03
 */
public class MainActivity extends AppCompatActivity {

    //地图控件
    public MapView mMapView;
    public BaiduMap mBaiduMap;
    public UiSettings mUiSettings;


    //方向传感器
    public MyOrientationListener myOrientationListener;
    public float mLastX;//方向角度


    //动态申请权限相关
    public int permissionFlag;//权限状态
    public static final int READY_TO_LOCATION = 0;//准备定位
    public static final int REQUEST_FAILED = 1;//申请失败


    //定位相关
    public MyLocation myLocation;

    public LocationClient mLocationClient;
    public boolean isFirstLoc = true;//是否是首次定位
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
    public int bodyHeight;//屏幕高度

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

    public boolean isHistorySearchResult = true;//是否是搜索历史记录
    public SearchDatabase dbHelper;//搜索记录数据库


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

    public LinearLayout startLayout;//开始导航布局
    public Button returnButton;//返回按钮
    public Button infoButton;//路线规划、详细信息切换按钮
    public Button startButton;//开始导航按钮

    public boolean infoFlag;//信息显示状态


    //导航相关
    public MyNavigateHelper myNavigateHelper;


    //控制布局相关
    private ImageButton setting;//设置
    private ImageButton refresh;//刷新

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

    public void expandStartLayout(boolean flag) {//伸缩开始导航布局
        expandLayout(this, startLayout, flag);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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

        myNavigateHelper = new MyNavigateHelper(this);//初始化导航模块

        requestPermission();//申请权限

        //新建搜索记录数据库，已存在则连接数据库
        dbHelper = new SearchDatabase(MainActivity.this, "Navigate.db", null, 1);
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

        /*离线地图要下载离线包，现在暂时不用
        //下载离线地图
        final MKOfflineMap mOffline = new MKOfflineMap();
        mOffline.init(new MKOfflineMapListener() {
            @Override
            public void onGetOfflineMapState(int i, int i1) {
                //根据城市名获取城市id
                ArrayList<MKOLSearchRecord> records = mOffline.searchCity(mCity);
                if (records != null && records.size() == 1) {
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
        setting = findViewById(R.id.setting);
        refresh = findViewById(R.id.refresh);

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

        startLayout = findViewById(R.id.start_layout);
        returnButton = findViewById(R.id.return_button);
        infoButton = findViewById(R.id.info_button);
        startButton = findViewById(R.id.start_button);

        //计算半个屏幕高度，用于下面的伸缩动画
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        bodyHeight = point.y;

        //设置搜索抽屉的结果列表、详细信息布局的拖动布局的高度
        searchResult.getLayoutParams().height = bodyHeight / 2;
        infoScroll.getLayoutParams().height = bodyHeight / 3;

        //设置选项布局、搜索结果抽屉、详细信息、开始导航初始高度为0
        selectLayout.getLayoutParams().height = 0;
        searchDrawer.getLayoutParams().height = 0;
        infoLayout.getLayoutParams().height = 0;
        startLayout.getLayoutParams().height = 0;

        searchAdapter = new SearchAdapter(this, searchList);//初始化适配器
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);//布局行数为1
        searchResult.setAdapter(searchAdapter);//设置适配器
        searchResult.setLayoutManager(layoutManager);//设置布局

        //设置按钮的点击事件
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
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

        //清空按钮的点击事件
        emptyButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                if(isHistorySearchResult) {//如果是搜索历史记录
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
                if((System.currentTimeMillis() - clickTime) > 1000) {//连续点击间隔时间不能小于1秒
                    clickTime = System.currentTimeMillis();
                    if(isNetworkConnected(MainActivity.this)) {
                        if(!isAirplaneModeOn(MainActivity.this)){
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
                                    if(imm != null) imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                                    if(mCity != null) {//定位成功后才可以进行搜索
                                        searchResult.stopScroll();//停止信息列表滑动

                                        myPoiSearch.poiSearchType = MyPoiSearch.CITY_SEARCH;//设置搜索类型为城市内搜索
                                        //开始城市内搜索
                                        mPoiSearch.searchInCity(new PoiCitySearchOption()
                                                .city(mCity)
                                                .keyword(searchContent));
                                    } else {
                                        Toast.makeText(MainActivity.this, getString(R.string.wait_for_location_result), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                requestPermission();//申请权限
                            }
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.close_airplane_mode), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                    }
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
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                if(infoFlag) {//如果状态为展开
                    infoButton.setText(R.string.info_button2);//设置按钮为详细信息
                    expandSelectLayout(true);//展开选择布局
                    expandInfoLayout(false);//收起详细信息布局
                    infoFlag = false;//设置信息状态为收起

                    myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
                } else {//如果状态为收起
                    infoButton.setText(R.string.info_button1);//设置按钮为路线
                    expandSelectLayout(false);//收起选择布局
                    expandInfoLayout(true);//展开详细信息布局
                    infoFlag = true;//设置信息状态为展开
                }
            }
        });

        //开始导航按钮的点击事件
        startButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                if(permissionFlag == READY_TO_LOCATION) {
                    myNavigateHelper.startNavigate();//开始导航
                }
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
            myPoiSearch.initSearch();//初始化搜索目标信息
            myRoutePlanSearch.initRoutePlanSearch();//初始化路线规划

            permissionFlag = READY_TO_LOCATION;
        } else {
            ActivityCompat.requestPermissions(this, permissionList.toArray(tmpList), 0);

            permissionFlag = REQUEST_FAILED;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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
                Toast.makeText(this, getString(R.string.get_permission_fail), Toast.LENGTH_SHORT).show();
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
