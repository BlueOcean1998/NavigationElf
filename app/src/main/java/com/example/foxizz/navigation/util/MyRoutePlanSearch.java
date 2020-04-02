package com.example.foxizz.navigation.util;

import android.annotation.SuppressLint;
import android.widget.Toast;

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
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.overlayutil.BikingRouteOverlay;
import com.example.foxizz.navigation.overlayutil.DrivingRouteOverlay;
import com.example.foxizz.navigation.overlayutil.IndoorRouteOverlay;
import com.example.foxizz.navigation.overlayutil.MassTransitRouteOverlay;
import com.example.foxizz.navigation.overlayutil.TransitRouteOverlay;
import com.example.foxizz.navigation.overlayutil.WalkingRouteOverlay;

/**
 * 路线规划模块
 */
@SuppressLint("Registered")
public class MyRoutePlanSearch {

    private MainActivity mainActivity;
    public MyRoutePlanSearch(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    //初始化路线规划
    public void initRoutePlanSearch() {
        //创建路线规划检索实例
        mainActivity.mSearch = RoutePlanSearch.newInstance();

        //创建路线规划检索结果监听器
        OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                //创建WalkingRouteOverlay实例
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mainActivity.mBaiduMap);
                if(walkingRouteResult.getRouteLines().size() > 0) {
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
            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
                try {
                    //创建TransitRouteOverlay实例
                    TransitRouteOverlay overlay = new TransitRouteOverlay(mainActivity.mBaiduMap);
                    if(transitRouteResult.getRouteLines().size() > 0) {
                        //清空地图上的标记
                        mainActivity.mBaiduMap.clear();
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
                    Toast.makeText(mainActivity,
                            "路程太短，建议步行", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
                //创建MassTransitRouteOverlay实例
                MassTransitRouteOverlay overlay = new MassTransitRouteOverlay(mainActivity.mBaiduMap);
                if(massTransitRouteResult.getRouteLines() != null && massTransitRouteResult.getRouteLines().size() > 0){
                    //清空地图上的标记
                    mainActivity.mBaiduMap.clear();
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
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(mainActivity.mBaiduMap);
                if(drivingRouteResult.getRouteLines().size() > 0) {
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
            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
                //创建IndoorRouteOverlay实例
                IndoorRouteOverlay overlay = new IndoorRouteOverlay(mainActivity.mBaiduMap);
                if(indoorRouteResult.getRouteLines() != null && indoorRouteResult.getRouteLines().size() > 0) {
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
            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
                //创建BikingRouteOverlay实例
                BikingRouteOverlay overlay = new BikingRouteOverlay(mainActivity.mBaiduMap);
                if(bikingRouteResult.getRouteLines().size() > 0) {
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
            }
        };

        //设置路线规划检索监听器
        mainActivity.mSearch.setOnGetRoutePlanResultListener(listener);
    }

    //开始路线规划
    public void startRoutePlanSearch() {
        //获取定位点和目标点
        PlanNode startNode = PlanNode.withLocation(MainActivity.latLng);
        PlanNode endNode = PlanNode.withLocation(mainActivity.searchList.get(MainActivity.searchItemSelect).getLatLng());

        switch(MainActivity.routePlanSelect) {
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

            //公交路线规划
            case 2:
                TransitRoutePlanOption transitRoutePlanOption = new TransitRoutePlanOption();
                transitRoutePlanOption.city(MainActivity.mCity);
                transitRoutePlanOption.from(startNode);
                transitRoutePlanOption.to(endNode);
                mainActivity.mSearch.transitSearch(transitRoutePlanOption);
                break;
        }
    }

}
