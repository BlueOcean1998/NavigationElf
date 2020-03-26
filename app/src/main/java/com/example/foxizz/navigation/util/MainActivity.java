package com.example.foxizz.navigation.util;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.foxizz.navigation.demo.MyOrientationListener;
import com.example.foxizz.navigation.demo.MyPoiOverlay;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.demo.Tools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private UiSettings mUiSettings;

    private MyOrientationListener myOrientationListener;//方向传感器
    private float mLastX;//方向角度

    //定位相关
    private LocationClient mLocationClient;
    private boolean isFirstLoc = true;//是否是首次定位
    private MyLocationData locData;//地址信息
    private LatLng latLng;//坐标
    private int mLocType;//定位结果
    private float mRadius = 10;//精度半径
    private double mLatitude;//纬度
    private double mLongitude;//经度
    private String mCity;//所在城市

    //搜索相关
    private PoiSearch mPoiSearch;

    private LinearLayout searchLayout;//搜索布局
    private EditText searchEdit;//搜索输入框
    private Button searchButton1;//清除按钮
    private Button searchButton2;//搜索按钮
    private ImageButton searchExpand;//搜索结果伸缩按钮
    private LinearLayout searchDrawer;//搜索结果抽屉

    private static boolean expandFlag = false;//伸缩状态
    private int bodyHeight;//半个屏幕高度

    private RecyclerView searchResult;
    private List<SearchItem> searchList = new ArrayList<>();
    private StaggeredGridLayoutManager layoutManager;
    private SearchAdapter searchAdapter;

    //路线规划相关
    private RoutePlanSearch mSearch;

    //获取改变控件尺寸动画
    //参数：需要改变高度的layoutDrawer（当然也可以是其它view），动画前的高度，动画后的高度
    private ValueAnimator getValueAnimator(final View view, int startHeight, int endHeight) {
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(startHeight, endHeight);
        //valueAnimator.setDuration(300);//动画时间（默认就是300）
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //逐渐改变view的高度
                view.getLayoutParams().height = (int) animation.getAnimatedValue();
                view.requestLayout();
            }
        });
        return valueAnimator;
    }

    //伸展按钮的旋转动画
    //参数：需要旋转的spreadButton（当然也可以是其它view），动画前的旋转角度，动画后的旋转角度
    private void rotateExpandIcon(final View view, float from, float to) {
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);
        valueAnimator.setInterpolator(new DecelerateInterpolator());//先加速后减速的动画
        //valueAnimator.setDuration(300);//动画时间（默认就是300）
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //逐渐改变view的旋转角度
                view.setRotation((float) valueAnimator.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化地图控件
        setInit();
        //初始化自定义控件
        initMyView();
        //初始化方向传感器
        initMyOrien();
        //初始化定位
        initLocationOption();
        //初始化搜索工具
        initSearch();
        //初始化路线规划
        initRoutePlanSearch();
    }

    //初始化地图控件
    public void setInit() {
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
        mMapView.showZoomControls(false);//去除缩放按钮
        mUiSettings.setCompassEnabled(false);//去除指南针
    }

    //初始化自定义控件
    private void initMyView() {
        searchLayout = findViewById(R.id.search_layout);
        searchEdit = findViewById(R.id.search_edit);
        searchButton1 = findViewById(R.id.search_button1);
        searchButton2 = findViewById(R.id.search_button2);
        searchExpand = findViewById(R.id.search_expand);
        searchDrawer = findViewById(R.id.search_drawer);
        searchResult = findViewById(R.id.search_result);

        searchButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdit.setText("");
            }
        });

        searchButton2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                if(Tools.isNetworkConnected(MainActivity.this)) {
                    if(Tools.isAirplaneModeOn(MainActivity.this)){
                        Toast.makeText(MainActivity.this,
                                "请检查是否有关闭飞行模式", Toast.LENGTH_LONG).show();
                    } else {
                        String searchContent = searchEdit.getText().toString();
                        if(!searchContent.equals("")){
                            if(!expandFlag) {
                                final ValueAnimator valueAnimator;//伸展动画
                                searchResult.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.adapter_alpha2));//动画2，出现;
                                valueAnimator = getValueAnimator(searchDrawer, 0, bodyHeight);//设置抽屉动画为展开
                                rotateExpandIcon(searchExpand, 0, 180);//伸展按钮的旋转动画
                                expandFlag = true;//设置状态为展开
                                valueAnimator.start();//开始抽屉的伸缩动画
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
                } else {
                    Toast.makeText(MainActivity.this,
                            "网络错误，请检查网络连接是否正常", Toast.LENGTH_LONG).show();
                }
            }
        });

        //计算半个屏幕高度，用于下面的伸缩动画
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        bodyHeight = point.y / 2;

        searchExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ValueAnimator valueAnimator;//伸展动画

                if (expandFlag) {//如果状态为展开
                    searchResult.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.adapter_alpha1));//动画1，消失;
                    valueAnimator = getValueAnimator(searchDrawer, bodyHeight, 0);//设置抽屉动画为收起
                    rotateExpandIcon(searchExpand, 180, 0);//伸展按钮的旋转动画
                    expandFlag = false;//设置状态为收起

                } else {//如果状态为收起
                    searchResult.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.adapter_alpha2));//动画2，出现;
                    valueAnimator = getValueAnimator(searchDrawer, 0, bodyHeight);//设置抽屉动画为展开
                    rotateExpandIcon(searchExpand, 0, 180);//伸展按钮的旋转动画
                    expandFlag = true;//设置状态为展开
                }

                valueAnimator.start();//开始抽屉的伸缩动画
            }
        });

        searchAdapter = new SearchAdapter(MainActivity.this, searchList);//初始化适配器
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);//布局行数为1
        searchResult.setAdapter(searchAdapter);//设置适配器
        searchResult.setLayoutManager(layoutManager);//设置布局
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

    /**
     * 初始化定位参数配置
     */
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
                //mRadius = location.getRadius();
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
                    builder.target(latLng).zoom(18.0f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                    if(mLocType == BDLocation.TypeGpsLocation) {//GPS定位结果
                        Toast.makeText(MainActivity.this,
                                location.getAddrStr(), Toast.LENGTH_SHORT).show();
                    } else if(mLocType == BDLocation.TypeNetWorkLocation) {//网络定位结果
                        Toast.makeText(MainActivity.this,
                                location.getAddrStr(), Toast.LENGTH_SHORT).show();
                    } else if(mLocType == BDLocation.TypeOffLineLocation) {//离线定位结果
                        Toast.makeText(MainActivity.this,
                                location.getAddrStr(), Toast.LENGTH_SHORT).show();
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
                    MyPoiOverlay poiOverlay = new MyPoiOverlay(mBaiduMap, mPoiSearch);
                    poiOverlay.setData(poiResult);//设置POI数据
                    mBaiduMap.clear();//清空地图上的标记点
                    mBaiduMap.setOnMarkerClickListener(poiOverlay);
                    poiOverlay.addToMap();//将所有的overlay添加到地图上
                    poiOverlay.zoomToSpan();

                    /*
                    Toast.makeText(MainActivity.this,
                            "总共查到" + poiResult.getTotalPoiNum() + "个兴趣点, 分为"
                                    + poiResult.getTotalPageNum() + "页", Toast.LENGTH_SHORT).show();
                    */

                    searchList.clear();//清空searchList
                    for(PoiInfo info: poiResult.getAllPoi()) {
                        //uid的集合，最多可以传入10个uid，多个uid之间用英文逗号分隔。
                        mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUids(info.uid));
                    }
                }
            }

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

                        //获取定位点到目标点的距离（单位：m，结果除以1000转化为km）
                        double distance = (DistanceUtil.getDistance(latLng, info.getLocation()) / 1000);
                        //保留两位小数
                        BigDecimal bd = new BigDecimal(distance);
                        distance = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        searchItem.setDistance(distance);

                        searchList.add(searchItem);//添加搜到的内容到searchList
                    }
                    searchAdapter.notifyDataSetChanged();//通知searchAdapter更新
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

    private void initRoutePlanSearch() {
        //创建路线规划检索实例
        mSearch = RoutePlanSearch.newInstance();

        //创建路线规划检索结果监听器
        OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        };

        //设置路线规划检索监听器
        mSearch.setOnGetRoutePlanResultListener(listener);
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
        mMapView.onDestroy();
        mPoiSearch.destroy();
    }

}
