package com.example.foxizz.navigation.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
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
import com.example.foxizz.navigation.overlayutil.TransitRouteOverlay;
import com.example.foxizz.navigation.overlayutil.WalkingRouteOverlay;
import com.example.foxizz.navigation.schemedata.SchemeItem;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.example.foxizz.navigation.demo.Tools.expandLayout;
import static com.example.foxizz.navigation.demo.Tools.haveReadWriteAndLocationPermissions;
import static com.example.foxizz.navigation.demo.Tools.isAirplaneModeOn;
import static com.example.foxizz.navigation.demo.Tools.isNetworkConnected;
import static com.example.foxizz.navigation.demo.Tools.rotateExpandIcon;

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

        if(!haveReadWriteAndLocationPermissions(mainActivity)) {//权限不足
            mainActivity.requestPermission();//申请权限，获得权限后定位
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
                //收回所有展开的方案
                for(int i = 0; i < mainActivity.schemeList.size(); i++) {//遍历所有item
                    if(mainActivity.schemeList.get(i).getExpandFlag()) {//如果是展开状态
                        //用layoutManager找到相应的item
                        View view = mainActivity.schemeLayoutManager.findViewByPosition(i);
                        if(view != null) {
                            LinearLayout infoDrawer = view.findViewById(R.id.info_drawer);
                            ImageButton schemeExpand = view.findViewById(R.id.scheme_expand);
                            expandLayout(mainActivity, infoDrawer, false);
                            rotateExpandIcon(schemeExpand, 180, 0);//旋转伸展按钮
                            mainActivity.schemeList.get(i).setExpandFlag(false);
                            mainActivity.schemeAdapter.notifyDataSetChanged();//通知adapter更新
                        }
                    }
                }

                mainActivity.schemeList.clear();//清空方案列表
                mainActivity.schemeAdapter.notifyDataSetChanged();//通知adapter更新

                mainActivity.mSearch.masstransitSearch((new MassTransitRoutePlanOption())
                        .from(startNode)
                        .to(endNode));

                mainActivity.expandSelectLayout(false);//收起选择布局

                mainActivity.schemeInfoDrawer.getLayoutParams().height = 0;//设置方案信息抽屉的高度为0
                mainActivity.expandSchemeLayout(true);//展开方案布局
                mainActivity.expandSchemeDrawer(true);//展开方案抽屉
                mainActivity.schemeInfoFlag = 1;//设置状态为方案列表
                mainActivity.expandStartLayout(false);//收起开始导航布局
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
                        || transitRouteResult.getRouteLines().size() == 0) {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.suggest_to_walk), Toast.LENGTH_SHORT).show();
                    return;
                }

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
            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
                if(massTransitRouteResult.getRouteLines() == null
                        || massTransitRouteResult.getRouteLines().size() == 0) {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.suggest_to_walk), Toast.LENGTH_SHORT).show();
                    return;
                }

                //所有的路线
                for(MassTransitRouteLine massTransitRouteLine: massTransitRouteResult.getRouteLines()) {
                    SchemeItem schemeItem = new SchemeItem();
                    schemeItem.setRouteLine(massTransitRouteLine);

                    //获取公交路线信息
                    StringBuilder allStationInfo = new StringBuilder();
                    StringBuilder simpleInfo = new StringBuilder();
                    //每条路线的所有段
                    for(List<MassTransitRouteLine.TransitStep> transitSteps:
                            massTransitRouteLine.getNewSteps()) {
                        //每一段的所有信息
                        for(MassTransitRouteLine.TransitStep transitStep: transitSteps) {
                            //只收集巴士和长途巴士的信息
                            if(transitStep.getVehileType() == //巴士
                                    MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_BUS
                            || transitStep.getVehileType() == //长途巴士
                                    MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_COACH ) {
                                if(transitStep.getBusInfo() != null) {//巴士
                                    simpleInfo.append("—").append(transitStep.getBusInfo().getName());
                                    allStationInfo.append(transitStep.getBusInfo().getName());
                                }
                                if(transitStep.getCoachInfo() != null) {//长途巴士
                                    simpleInfo.append("—").append(transitStep.getCoachInfo().getName());
                                    allStationInfo.append(transitStep.getCoachInfo().getName());
                                }
                                //终点站
                                allStationInfo.append("—终点站：").append(
                                        transitStep.getBusInfo().getArriveStation()).append("\n");
                            }
                        }
                    }
                    simpleInfo = new StringBuilder(simpleInfo.substring(1));
                    schemeItem.setSimpleInfo(simpleInfo.toString());
                    schemeItem.setAllStationInfo(allStationInfo.toString());

                    //获取详细信息
                    String detailInfo = "";

                    try {
                        long spendTime;
                        @SuppressLint("SimpleDateFormat")
                        DateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                        long nowTime = sdf.parse(sdf.format(new Date())).getTime();
                        long arriveTime = sdf.parse(massTransitRouteLine.getArriveTime()).getTime();
                        spendTime = arriveTime - nowTime;
                        if(spendTime < 3 * 60 * 60 * 1000) {//小于3小时
                            detailInfo += mainActivity.getString(R.string.spend_time)
                                    + spendTime / 1000 / 60 + mainActivity.getString(R.string.minute);
                        } else {
                            detailInfo += mainActivity.getString(R.string.spend_time)
                                    + spendTime / 1000 / 60 / 60 + mainActivity.getString(R.string.hour)
                                    + spendTime / 1000 / 60 % 60 + mainActivity.getString(R.string.minute);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if(massTransitRouteLine.getPrice() > 10) {
                        detailInfo += "\n" + mainActivity.getString(R.string.budget) + (int) massTransitRouteLine.getPrice() + mainActivity.getString(R.string.yuan);
                    }

                    schemeItem.setDetailInfo(detailInfo);

                    mainActivity.schemeList.add(schemeItem);//添加到列表中
                    mainActivity.schemeAdapter.notifyDataSetChanged();//通知adapter更新
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
