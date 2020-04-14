package com.example.foxizz.navigation.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.overlayutil.BikingRouteOverlay;
import com.example.foxizz.navigation.overlayutil.DrivingRouteOverlay;
import com.example.foxizz.navigation.overlayutil.IndoorRouteOverlay;
import com.example.foxizz.navigation.overlayutil.MassTransitRouteOverlay;
import com.example.foxizz.navigation.overlayutil.TransitRouteOverlay;
import com.example.foxizz.navigation.overlayutil.WalkingRouteOverlay;

import static com.example.foxizz.navigation.demo.Tools.ifHaveReadWriteAndLocationPermissions;
import static com.example.foxizz.navigation.demo.Tools.isAirplaneModeOn;
import static com.example.foxizz.navigation.demo.Tools.isNetworkConnected;

/**
 * 路线规划模块
 */
@SuppressLint("Registered")
public class MyRoutePlanSearch {

    private MainActivity mainActivity;
    public MyRoutePlanSearch(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    //开始路线规划
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void startRoutePlanSearch() {
        if(!isNetworkConnected(mainActivity)) {//没有开网络
            Toast.makeText(mainActivity, mainActivity.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if(isAirplaneModeOn(mainActivity)) {//开启了飞行模式
            Toast.makeText(mainActivity, mainActivity.getString(R.string.close_airplane_mode), Toast.LENGTH_SHORT).show();
            return;
        }

        if(!ifHaveReadWriteAndLocationPermissions(mainActivity)) {//权限不足
            mainActivity.requestPermission();
            return;
        }

        if(mainActivity.latLng == null) {//还没有得到定位
            Toast.makeText(mainActivity, mainActivity.getString(R.string.wait_for_location_result), Toast.LENGTH_SHORT).show();
            return;
        }

        if(mainActivity.endLocation == null) {
            Toast.makeText(mainActivity, mainActivity.getString(R.string.end_location_is_null), Toast.LENGTH_SHORT).show();
            return;
        }

        //获取定位点和目标点
        PlanNode startNode = PlanNode.withLocation(mainActivity.latLng);
        PlanNode endNode = PlanNode.withLocation(mainActivity.endLocation);

        switch(mainActivity.routePlanSelect) {
            //驾车路线规划
            case 0:
                mainActivity.mSearch.drivingSearch((new DrivingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
                break;

            //步行路线规划
            case 1:
                mainActivity.mSearch.walkingSearch((new WalkingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
                break;

            //骑行路线规划
            case 2:
                mainActivity.mSearch.bikingSearch((new BikingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
                break;

            //公交路线规划
            case 3:
                mainActivity.startBusStationLocation = null;//第一站置空
                mainActivity.mSearch.masstransitSearch((new MassTransitRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
                break;
        }
    }

    //初始化路线规划
    public void initRoutePlanSearch() {
        //创建路线规划检索实例
        mainActivity.mSearch = RoutePlanSearch.newInstance();

        //创建路线规划检索结果监听器
        OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                if(walkingRouteResult.getRouteLines() == null
                        || walkingRouteResult.getRouteLines().size() == 0) {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.suggest_not_to_walk), Toast.LENGTH_SHORT).show();
                    return;
                }

                //创建WalkingRouteOverlay实例
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mainActivity.mBaiduMap);
                //清空地图上的标记
                mainActivity.mBaiduMap.clear();
                //获取路径规划数据,(以返回的第一条数据为例)
                //为WalkingRouteOverlay实例设置路径数据
                overlay.setData(walkingRouteResult.getRouteLines().get(0));
                //在地图上绘制WalkingRouteOverlay
                overlay.addToMap();
                //将路线放在最佳视野位置
                overlay.zoomToSpan();
            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
                if(transitRouteResult.getRouteLines() == null
                        || transitRouteResult.getRouteLines().size() == 0)
                    return;

                try {
                    //创建TransitRouteOverlay实例
                    TransitRouteOverlay overlay = new TransitRouteOverlay(mainActivity.mBaiduMap);
                    //清空地图上的标记
                    mainActivity.mBaiduMap.clear();
                    //获取路径规划数据,(以返回的第一条数据为例)
                    //为TransitRouteOverlay实例设置路径数据
                    overlay.setData(transitRouteResult.getRouteLines().get(0));
                    //在地图上绘制TransitRouteOverlay
                    overlay.addToMap();
                    //将路线放在最佳视野位置
                    overlay.zoomToSpan();
                } catch(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.suggest_to_walk), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
                if(massTransitRouteResult.getRouteLines() == null) return;

                if(massTransitRouteResult.getRouteLines().size() > 0){
                    try {
                        //获取第一站的坐标，用于步行导航
                        if(massTransitRouteResult.getRouteLines().get(0).getNewSteps().get(0).size() == 1) {
                            mainActivity.startBusStationLocation = massTransitRouteResult
                                    .getRouteLines()
                                    .get(0)
                                    .getNewSteps()
                                    .get(1)
                                    .get(0)
                                    .getStartLocation();
                        } else {
                            mainActivity.startBusStationLocation = massTransitRouteResult
                                    .getRouteLines()
                                    .get(0)
                                    .getNewSteps()
                                    .get(0)
                                    .get(1)
                                    .getStartLocation();
                        }
                    } catch (Exception e) {
                        Toast.makeText(mainActivity, mainActivity.getString(R.string.can_not_get_start_station_info), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return;
                    }

                    //创建MassTransitRouteOverlay实例
                    MassTransitRouteOverlay overlay = new MassTransitRouteOverlay(mainActivity.mBaiduMap);
                    //清空地图上的所有标记点和绘制的路线
                    mainActivity.mBaiduMap.clear();
                    //构建Marker图标
                    BitmapDescriptor bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_to_location);
                    //构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions()
                            .position(mainActivity.startBusStationLocation)
                            .icon(bitmap);
                    //在地图上添加Marker，并显示
                    mainActivity.mBaiduMap.addOverlay(option);

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
                if(drivingRouteResult.getRouteLines() == null
                        || drivingRouteResult.getRouteLines().size() == 0)
                    return;

                //创建DrivingRouteOverlay实例
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(mainActivity.mBaiduMap);
                //清空地图上的标记
                mainActivity.mBaiduMap.clear();
                //获取路径规划数据,(以返回的第一条路线为例）
                //为DrivingRouteOverlay实例设置数据
                overlay.setData(drivingRouteResult.getRouteLines().get(0));
                //在地图上绘制DrivingRouteOverlay
                overlay.addToMap();
                //将路线放在最佳视野位置
                overlay.zoomToSpan();

            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
                if(indoorRouteResult.getRouteLines() == null
                    || indoorRouteResult.getRouteLines().size() == 0)
                    return;

                //创建IndoorRouteOverlay实例
                IndoorRouteOverlay overlay = new IndoorRouteOverlay(mainActivity.mBaiduMap);
                //清空地图上的标记
                mainActivity.mBaiduMap.clear();
                //获取室内路径规划数据（以返回的第一条路线为例）
                //为IndoorRouteOverlay实例设置数据
                overlay.setData(indoorRouteResult.getRouteLines().get(0));
                //在地图上绘制IndoorRouteOverlay
                overlay.addToMap();
                //将路线放在最佳视野位置
                overlay.zoomToSpan();
            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
                if(bikingRouteResult.getRouteLines() == null
                        || bikingRouteResult.getRouteLines().size() == 0) {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.suggest_not_to_bike), Toast.LENGTH_SHORT).show();
                    return;
                }

                //创建BikingRouteOverlay实例
                BikingRouteOverlay overlay = new BikingRouteOverlay(mainActivity.mBaiduMap);
                //清空地图上的标记
                mainActivity.mBaiduMap.clear();
                //获取路径规划数据,(以返回的第一条路线为例）
                //为BikingRouteOverlay实例设置数据
                overlay.setData(bikingRouteResult.getRouteLines().get(0));
                //在地图上绘制BikingRouteOverlay
                overlay.addToMap();
                //将路线放在最佳视野位置
                overlay.zoomToSpan();
            }
        };

        //设置路线规划检索监听器
        mainActivity.mSearch.setOnGetRoutePlanResultListener(listener);
    }

}
