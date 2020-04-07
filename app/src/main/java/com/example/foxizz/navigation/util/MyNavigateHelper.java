package com.example.foxizz.navigation.util;

import android.content.Intent;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.activity.WNaviGuideActivity;

public class MyNavigateHelper {

    private MainActivity mainActivity;
    public MyNavigateHelper(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    //初始化导航引擎
    public void initNavigateHelper() {
        //获取导航控制类
        //引擎初始化
        WalkNavigateHelper.getInstance().initNaviEngine(mainActivity, new IWEngineInitListener() {

            @Override
            public void engineInitSuccess() {
                //Toast.makeText(mainActivity, "引擎初始化成功", Toast.LENGTH_LONG).show();

                startNavigate();//开始导航
            }

            @Override
            public void engineInitFail() {
                //引擎初始化失败的回调
                Toast.makeText(mainActivity, mainActivity.getString(R.string.walk_navigate_init_fail), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void routeWalkPlanWithParam() {
        WalkNavigateHelper.getInstance().routePlanWithRouteNode(mainActivity.walkParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                //Toast.makeText(mainActivity, "开始路线规划", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRoutePlanSuccess() {
                //Toast.makeText(mainActivity, "路线规划成功", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(mainActivity, WNaviGuideActivity.class);
                mainActivity.startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
                Toast.makeText(mainActivity, mainActivity.getString(R.string.walk_route_plan_fail), Toast.LENGTH_LONG).show();
            }

        });
    }

    //开始导航
    public void startNavigate() {
        //获取定位点和目标点
        LatLng startNode = mainActivity.latLng;
        LatLng endNode = mainActivity.searchList.get(mainActivity.searchItemSelect).getLatLng();

        switch(mainActivity.routePlanSelect) {
            //驾车导航
            case 0:

                break;

            //步行导航
            case 1:
                WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
                walkStartNode.setLocation(startNode);
                WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
                walkEndNode.setLocation(endNode);

                mainActivity.walkParam = new WalkNaviLaunchParam()
                        .startNodeInfo(walkStartNode)
                        .endNodeInfo(walkEndNode);

                mainActivity.walkParam.extraNaviMode(0);//普通步行导航

                routeWalkPlanWithParam();//开始普通步行导航
                break;

            //公交导航
            case 2:

                break;
        }
    }

}
