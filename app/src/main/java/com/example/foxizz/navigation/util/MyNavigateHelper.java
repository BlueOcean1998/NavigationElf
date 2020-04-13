package com.example.foxizz.navigation.util;

import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener;
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener;
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.bikenavi.params.BikeRouteNodeInfo;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.BNaviGuideActivity;
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.activity.WNaviGuideActivity;

import static com.example.foxizz.navigation.demo.Tools.ifHaveReadWriteAndLocationPermissions;
import static com.example.foxizz.navigation.demo.Tools.isAirplaneModeOn;
import static com.example.foxizz.navigation.demo.Tools.isNetworkConnected;

public class MyNavigateHelper {

    private MainActivity mainActivity;
    public MyNavigateHelper(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private WalkNaviLaunchParam walkParam;
    private BikeNaviLaunchParam bikeParam;

    //开始导航
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void startNavigate() {
        if(!isNetworkConnected(mainActivity)) {//没有开网络
            Toast.makeText(mainActivity, mainActivity.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if(isAirplaneModeOn(mainActivity)) {//开启了飞行模式
            Toast.makeText(mainActivity, mainActivity.getString(R.string.close_airplane_mode), Toast.LENGTH_SHORT).show();
            return;
        }

        if(ifHaveReadWriteAndLocationPermissions(mainActivity)) {//权限不足
            mainActivity.requestPermission();
            return;
        }

        if(mainActivity.latLng == null) {//还没有得到定位
            Toast.makeText(mainActivity, mainActivity.getString(R.string.wait_for_location_result), Toast.LENGTH_SHORT).show();
        } else {
            return;
        }

        switch(mainActivity.routePlanSelect) {
            //驾车导航
            case 0:

                break;

            //步行导航
            case 1:
                initWalkNavigateHelper();
                break;

            //骑行导航
            case 2:
                initBikeNavigateHelper();
                break;

            //公交导航
            case 3:

                break;
        }
    }

    //初始化步行导航引擎
    private void initWalkNavigateHelper() {
        //获取步行导航控制类
        //步行引擎初始化
        WalkNavigateHelper.getInstance().initNaviEngine(mainActivity, new IWEngineInitListener() {
            @Override
            public void engineInitSuccess() {
                //Toast.makeText(mainActivity, "步行引擎初始化成功", Toast.LENGTH_SHORT).show();

                //获取定位点和目标点坐标
                WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
                walkStartNode.setLocation(mainActivity.latLng);
                WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
                walkEndNode.setLocation(mainActivity.searchList.get(0).getLatLng());

                walkParam = new WalkNaviLaunchParam()
                        .startNodeInfo(walkStartNode)
                        .endNodeInfo(walkEndNode);

                walkParam.extraNaviMode(0);//普通步行导航

                routeWalkPlanWithParam();//开始步行导航
            }

            @Override
            public void engineInitFail() {
                Toast.makeText(mainActivity, mainActivity.getString(R.string.walk_navigate_init_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //初始化步行路线规划
    private void routeWalkPlanWithParam() {
        WalkNavigateHelper.getInstance().routePlanWithRouteNode(walkParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                //Toast.makeText(mainActivity, "开始步行路线规划", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRoutePlanSuccess() {
                //Toast.makeText(mainActivity, "步行路线规划成功", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mainActivity, WNaviGuideActivity.class);
                mainActivity.startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
                Toast.makeText(mainActivity, mainActivity.getString(R.string.walk_route_plan_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //初始化骑行导航引擎
    private void initBikeNavigateHelper() {
        //获取骑行导航控制类
        //骑行引擎初始化
        BikeNavigateHelper.getInstance().initNaviEngine(mainActivity, new IBEngineInitListener() {
            @Override
            public void engineInitSuccess() {
                //Toast.makeText(mainActivity, "骑行引擎初始化成功", Toast.LENGTH_SHORT).show();

                //获取定位点和目标点坐标
                BikeRouteNodeInfo bikeStartNode = new BikeRouteNodeInfo();
                bikeStartNode.setLocation(mainActivity.latLng);
                BikeRouteNodeInfo bikeEndNode = new BikeRouteNodeInfo();
                bikeEndNode.setLocation(mainActivity.searchList.get(0).getLatLng());

                bikeParam = new BikeNaviLaunchParam()
                        .startNodeInfo(bikeStartNode)
                        .endNodeInfo(bikeEndNode);

                routeBikePlanWithParam();//开始骑行导航
            }

            @Override
            public void engineInitFail() {
                Toast.makeText(mainActivity, mainActivity.getString(R.string.bike_navigate_init_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void routeBikePlanWithParam() {
        BikeNavigateHelper.getInstance().routePlanWithRouteNode(bikeParam, new IBRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                //Toast.makeText(mainActivity, "开始骑行路线规划", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRoutePlanSuccess() {
                //Toast.makeText(mainActivity, "骑行路线规划成功", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mainActivity, BNaviGuideActivity.class);
                mainActivity.startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError bikeRoutePlanError) {
                Toast.makeText(mainActivity, mainActivity.getString(R.string.bike_route_plan_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
