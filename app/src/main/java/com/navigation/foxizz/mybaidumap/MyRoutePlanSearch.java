package com.navigation.foxizz.mybaidumap;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
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
import com.navigation.foxizz.R;
import com.navigation.foxizz.activity.fragment.MainFragment;
import com.navigation.foxizz.data.SchemeItem;
import com.navigation.foxizz.mybaidumap.overlayutil.BikingRouteOverlay;
import com.navigation.foxizz.mybaidumap.overlayutil.DrivingRouteOverlay;
import com.navigation.foxizz.mybaidumap.overlayutil.IndoorRouteOverlay;
import com.navigation.foxizz.mybaidumap.overlayutil.MassTransitRouteOverlay;
import com.navigation.foxizz.mybaidumap.overlayutil.TransitRouteOverlay;
import com.navigation.foxizz.mybaidumap.overlayutil.WalkingRouteOverlay;
import com.navigation.foxizz.util.LayoutUtil;
import com.navigation.foxizz.util.NetworkUtil;
import com.navigation.foxizz.util.SettingUtil;
import com.navigation.foxizz.util.TimeUtil;
import com.navigation.foxizz.util.ToastUtil;

import java.util.List;

/**
 * 路线规划模块
 */
public class MyRoutePlanSearch {

    private final MainFragment mainFragment;
    public MyRoutePlanSearch(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    /**
     * 开始路线规划
     */
    public void startRoutePlanSearch() {
        if (!NetworkUtil.isNetworkConnected()) {//没有网络连接
            ToastUtil.showToast(R.string.network_error);
            return;
        }

        if (NetworkUtil.isAirplaneModeOn()) {//没有关飞行模式
            ToastUtil.showToast(R.string.close_airplane_mode);
            return;
        }

        if (SettingUtil.haveReadWriteAndLocationPermissions()) {//权限不足
            mainFragment.requestPermission();//申请权限，获得权限后定位
            return;
        }

        if (mainFragment.latLng == null) {//还没有得到定位
            ToastUtil.showToast(R.string.wait_for_location_result);
            return;
        }

        if (mainFragment.endLocation == null) {
            ToastUtil.showToast(R.string.end_location_is_null);
            return;
        }

        //获取定位点和目标点
        PlanNode startNode = PlanNode.withLocation(mainFragment.latLng);
        PlanNode endNode = PlanNode.withLocation(mainFragment.endLocation);

        switch (mainFragment.routePlanSelect) {
            //驾车路线规划
            case 0:
                mainFragment.mSearch.drivingSearch((new DrivingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
                break;

            //步行路线规划
            case 1:
                mainFragment.mSearch.walkingSearch((new WalkingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
                break;

            //骑行路线规划
            case 2:
                mainFragment.mSearch.bikingSearch((new BikingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
                break;

            //公交路线规划
            case 3:
                //加载路线方案
                mainFragment.schemeLoading.setVisibility(View.VISIBLE);
                mainFragment.schemeResult.setVisibility(View.GONE);

                //收回所有展开的方案
                for (int i = 0; i < mainFragment.schemeList.size(); i++) {//遍历所有item
                    if (mainFragment.schemeList.get(i).getExpandFlag()) {//如果是展开状态
                        //用layoutManager找到相应的item
                        View view = mainFragment.schemeLayoutManager.findViewByPosition(i);
                        if (view != null) {
                            LinearLayout infoDrawer = view.findViewById(R.id.info_drawer);
                            ImageButton schemeExpand = view.findViewById(R.id.scheme_expand);
                            LayoutUtil.expandLayout(infoDrawer, false);
                            LayoutUtil.rotateExpandIcon(schemeExpand, 180, 0);//旋转伸展按钮
                            mainFragment.schemeList.get(i).setExpandFlag(false);
                            mainFragment.schemeAdapter.updateList();//通知adapter更新
                        }
                    }
                }

                mainFragment.schemeList.clear();//清空方案列表
                mainFragment.schemeAdapter.updateList();//通知adapter更新

                mainFragment.mSearch.masstransitSearch((new MassTransitRoutePlanOption())
                        .from(startNode)
                        .to(endNode));

                //设置方案信息抽屉的高度为0
                LayoutUtil.setViewHeight(mainFragment.schemeInfoLayout, 0);

                LayoutUtil.expandLayout(mainFragment.selectLayout, false);//收起选择布局
                LayoutUtil.expandLayout(mainFragment.schemeDrawer, true);//展开方案抽屉

                mainFragment.schemeFlag = MainFragment.SCHEME_LIST;//设置状态为方案列表
                break;
        }
    }

    /**
     * 初始化路线规划
     */
    public void initRoutePlanSearch() {
        //创建路线规划检索实例
        mainFragment.mSearch = RoutePlanSearch.newInstance();

        //创建路线规划检索结果监听器
        OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                if (walkingRouteResult.getRouteLines() == null
                        || walkingRouteResult.getRouteLines().size() == 0) {
                    ToastUtil.showToast(R.string.suggest_not_to_walk);
                    return;
                }

                //创建WalkingRouteOverlay实例
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mainFragment.mBaiduMap);
                //清空地图上的标记
                mainFragment.mBaiduMap.clear();
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
                if (transitRouteResult.getRouteLines() == null
                        || transitRouteResult.getRouteLines().size() == 0) {
                    ToastUtil.showToast(R.string.suggest_to_walk);
                    return;
                }

                //创建TransitRouteOverlay实例
                TransitRouteOverlay overlay = new TransitRouteOverlay(mainFragment.mBaiduMap);
                //清空地图上的标记
                mainFragment.mBaiduMap.clear();
                //获取路径规划数据,(以返回的第一条数据为例)
                //为TransitRouteOverlay实例设置路径数据
                overlay.setData(transitRouteResult.getRouteLines().get(0));
                //在地图上绘制TransitRouteOverlay
                overlay.addToMap();
                //将路线放在最佳视野位置
                overlay.zoomToSpan();
            }

            @Override
            public void onGetMassTransitRouteResult(final MassTransitRouteResult massTransitRouteResult) {
                //路线方案加载完成
                mainFragment.schemeLoading.setVisibility(View.GONE);
                mainFragment.schemeResult.setVisibility(View.VISIBLE);

                if (massTransitRouteResult.getRouteLines() == null
                        || massTransitRouteResult.getRouteLines().size() == 0) {
                    ToastUtil.showToast(R.string.suggest_to_walk);
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //所有的路线
                        for (MassTransitRouteLine massTransitRouteLine : massTransitRouteResult.getRouteLines()) {
                            SchemeItem schemeItem = new SchemeItem();
                            schemeItem.setRouteLine(massTransitRouteLine);

                            //获取公交路线信息
                            StringBuilder allStationInfo = new StringBuilder();
                            StringBuilder simpleInfo = new StringBuilder();
                            //每条路线的所有段
                            for (List<MassTransitRouteLine.TransitStep> transitSteps :
                                    massTransitRouteLine.getNewSteps()) {
                                //每一段的所有信息
                                for (MassTransitRouteLine.TransitStep transitStep : transitSteps) {
                                    //只收集巴士和长途巴士的信息
                                    if (transitStep.getVehileType() == //巴士
                                            MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_BUS
                                            || transitStep.getVehileType() == //长途巴士
                                            MassTransitRouteLine.TransitStep.StepVehicleInfoType.ESTEP_COACH) {
                                        if (transitStep.getBusInfo() != null) {//巴士
                                            simpleInfo.append("—").append(transitStep.getBusInfo().getName());
                                            allStationInfo.append(transitStep.getBusInfo().getName());
                                        }
                                        if (transitStep.getCoachInfo() != null) {//长途巴士
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
                            StringBuilder detailInfo = new StringBuilder();

                            long spendTime;
                            long nowTime = System.currentTimeMillis();
                            long arriveTime = TimeUtil.parse(massTransitRouteLine.getArriveTime(),
                                    TimeUtil.FORMATION_yMdHms).getTime();
                            spendTime = arriveTime - nowTime;
                            if (spendTime < 3 * 60 * 60 * 1000) {//小于3小时
                                detailInfo.append(mainFragment.getString(R.string.spend_time))
                                        .append(spendTime / 1000 / 60).append(mainFragment.getString(R.string.minute));
                            } else {
                                detailInfo.append(mainFragment.getString(R.string.spend_time))
                                        .append(spendTime / 1000 / 60 / 60).append(mainFragment.getString(R.string.hour))
                                        .append(spendTime / 1000 / 60 % 60).append(mainFragment.getString(R.string.minute));
                            }

                            if (massTransitRouteLine.getPrice() > 10) {
                                detailInfo.append("\n")
                                        .append(mainFragment.getString(R.string.budget))
                                        .append((int) massTransitRouteLine.getPrice()).append(mainFragment.getString(R.string.yuan));
                            }

                            schemeItem.setDetailInfo(detailInfo.toString());

                            mainFragment.schemeList.add(schemeItem);//添加到列表中
                            mainFragment.schemeAdapter.updateList();//通知adapter更新
                        }

                        startMassTransitRoutePlan(0);//默认选择第一个方案
                    }
                }).start();
            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                if (drivingRouteResult.getRouteLines() == null
                        || drivingRouteResult.getRouteLines().size() == 0)
                    return;

                //创建DrivingRouteOverlay实例
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(mainFragment.mBaiduMap);
                //清空地图上的标记
                mainFragment.mBaiduMap.clear();
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
                if (indoorRouteResult.getRouteLines() == null
                        || indoorRouteResult.getRouteLines().size() == 0)
                    return;

                //创建IndoorRouteOverlay实例
                IndoorRouteOverlay overlay = new IndoorRouteOverlay(mainFragment.mBaiduMap);
                //清空地图上的标记
                mainFragment.mBaiduMap.clear();
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
                if (bikingRouteResult.getRouteLines() == null
                        || bikingRouteResult.getRouteLines().size() == 0) {
                    ToastUtil.showToast(R.string.suggest_not_to_bike);
                    return;
                }

                //创建BikingRouteOverlay实例
                BikingRouteOverlay overlay = new BikingRouteOverlay(mainFragment.mBaiduMap);
                //清空地图上的标记
                mainFragment.mBaiduMap.clear();
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
        mainFragment.mSearch.setOnGetRoutePlanResultListener(listener);
    }

    /**
     * 开始跨城公交路线规划
     */
    @SuppressLint("SetTextI18n")
    public void startMassTransitRoutePlan(int index) {
        SchemeItem schemeItem = mainFragment.schemeList.get(index);

        //设置方案信息
        mainFragment.schemeInfo.setText(schemeItem.getAllStationInfo()
                + "\n" + schemeItem.getDetailInfo() + "\n");

        //创建MassTransitRouteOverlay实例
        MassTransitRouteOverlay overlay = new MassTransitRouteOverlay(mainFragment.mBaiduMap);

        //清空地图上的所有标记点和绘制的路线
        mainFragment.mBaiduMap.clear();
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_to_location);

        //清空临时保存的公交站点信息
        mainFragment.busStationLocations.clear();

        /*
         * getRouteLines(): 所有规划好的路线
         * get(0): 第1条规划好的路线
         *
         * getNewSteps():
         * 起终点为同城时，该list表示一个step中的多个方案scheme（方案1、方案2、方案3...）
         * 起终点为跨城时，该list表示一个step中多个子步骤sub_step（如：步行->公交->火车->步行）
         *
         * get(0): 方案1或第1步
         * get(0): 步行到第1站点
         * getEndLocation(): 终点站，即步行导航的终点站
         *
         */
        //获取所有站点信息
        for (List<MassTransitRouteLine.TransitStep> transitSteps :
                schemeItem.getRouteLine().getNewSteps()) {
            for (MassTransitRouteLine.TransitStep transitStep : transitSteps) {
                //将获取到的站点信息临时保存
                mainFragment.busStationLocations.add(transitStep.getEndLocation());

                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(transitStep.getEndLocation())
                        .icon(bitmap);

                //在地图上添加Marker，并显示
                mainFragment.mBaiduMap.addOverlay(option);
            }
        }

        try {
            //获取路线规划数据
            //为MassTransitRouteOverlay设置数据
            overlay.setData(schemeItem.getRouteLine());
            //在地图上绘制Overlay
            overlay.addToMap();
            //将路线放在最佳视野位置
            overlay.zoomToSpan();
        } catch (Exception ignored) {
            mainFragment.requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast(R.string.draw_route_fail);
                }
            });
        }
    }

}
