package com.example.foxizz.navigation.util;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener;
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener;
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.bikenavi.params.BikeRouteNodeInfo;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.baidu.navisdk.adapter.struct.BNTTsInitConfig;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.BNaviGuideActivity;
import com.example.foxizz.navigation.activity.DNaviGuideActivity;
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.activity.WNaviGuideActivity;
import com.example.foxizz.navigation.demo.Tools;

import java.util.ArrayList;
import java.util.List;

import static com.example.foxizz.navigation.demo.Tools.haveReadWriteAndLocationPermissions;
import static com.example.foxizz.navigation.demo.Tools.isAirplaneModeOn;
import static com.example.foxizz.navigation.demo.Tools.isNetworkConnected;

public class MyNavigateHelper {

    private MainActivity mainActivity;
    public MyNavigateHelper(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private WalkNaviLaunchParam walkParam;
    private BikeNaviLaunchParam bikeParam;

    private static boolean enableDriveNavigate = false;//是否可以进行驾车导航
    private ProgressDialog progressDialog;

    //初始化驾车导航引擎
    public void initDriveNavigateHelper() {
        BaiduNaviManagerFactory.getBaiduNaviManager().init(mainActivity,
                Tools.getSDCardDir(),
                Tools.getAppFolderName(mainActivity),
            new IBaiduNaviManager.INaviInitListener() {
                @Override
                public void onAuthResult(int status, final String msg) {
                    if(status != 0) {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mainActivity, "key校验失败, " + msg,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                @Override
                public void initStart() {

                }

                @Override
                public void initSuccess() {
                    enableDriveNavigate = true;

                    //初始化语音合成模块
                    initTTS();
                }

                @Override
                public void initFailed(int errCode) {
                    Toast.makeText(mainActivity, R.string.drive_navigate_init_fail + errCode,
                            Toast.LENGTH_SHORT).show();
                }
            });

        initProgressDialog();
    }

    //初始化提示弹窗
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(mainActivity);
        progressDialog.setTitle(mainActivity.getString(R.string.hint));
        progressDialog.setMessage(mainActivity.getString(R.string.route_plan_please_wait));
        progressDialog.setCancelable(false);
    }

    //初始化语音合成模块
    private void initTTS() {
        BaiduNaviManagerFactory.getTTSManager().initTTS(new BNTTsInitConfig.Builder()
                .context(mainActivity)
                .sdcardRootPath(Tools.getSDCardDir())
                .appFolderName(Tools.getAppFolderName(mainActivity))
                .appId(mainActivity.getString(R.string.app_id))
                .appKey(mainActivity.getString(R.string.api_key))
                .secretKey(mainActivity.getString(R.string.secret_key))
                .build()
        );
    }

    //初始化步行导航引擎
    public void initWalkNavigateHelper() {
        //步行引擎初始化
        WalkNavigateHelper.getInstance().initNaviEngine(mainActivity, new IWEngineInitListener() {
            @Override
            public void engineInitSuccess() {
                routeWalkPlanWithParam();
            }

            @Override
            public void engineInitFail() {
                Toast.makeText(mainActivity, mainActivity.getString(R.string.walk_navigate_init_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //初始化骑行导航引擎
    public void initBikeNavigateHelper() {
        //骑行引擎初始化
        BikeNavigateHelper.getInstance().initNaviEngine(mainActivity, new IBEngineInitListener() {
            @Override
            public void engineInitSuccess() {
                routeBikePlanWithParam();
            }

            @Override
            public void engineInitFail() {
                Toast.makeText(mainActivity, mainActivity.getString(R.string.bike_navigate_init_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //开始导航
    public void startNavigate() {
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

        switch(mainActivity.routePlanSelect) {
            //驾车导航
            case 0:
                routeDrivePlanWithParam();//开始驾车导航
                break;

            //步行导航，公交导航
            case 1:case 3:
                initWalkNavigateHelper();//开始步行导航
                break;

            //骑行导航
            case 2:
                initBikeNavigateHelper();//开始骑行导航
                break;
        }
    }

    //初始化驾车路线规划
    private void routeDrivePlanWithParam() {
        if(!enableDriveNavigate) return;

        //设置驾车导航的起点和终点
        BNRoutePlanNode startNode = new BNRoutePlanNode.Builder()
                .latitude(mainActivity.latLng.latitude)
                .longitude(mainActivity.latLng.longitude)
                .build();

        BNRoutePlanNode endNode = new BNRoutePlanNode.Builder()
                .latitude(mainActivity.endLocation.latitude)
                .longitude(mainActivity.endLocation.longitude)
                .build();

        List<BNRoutePlanNode> list = new ArrayList<>();
        list.add(startNode);
        list.add(endNode);

        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                null,
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch(msg.what) {
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                progressDialog.show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                progressDialog.dismiss();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                progressDialog.dismiss();
                                Toast.makeText(mainActivity, R.string.drive_route_plan_fail,
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                mainActivity.startActivity(
                                        new Intent(mainActivity, DNaviGuideActivity.class)
                                );
                                break;
                            default:
                                break;
                        }
                    }
                }
        );
    }

    //初始化步行路线规划
    private void routeWalkPlanWithParam() {
        WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
        WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();

        //设置起点
        walkStartNode.setLocation(mainActivity.latLng);

        //设置步行导航的终点
        if(mainActivity.routePlanSelect == MainActivity.WALKING) {
            walkEndNode.setLocation(mainActivity.endLocation);

            //计算公交导航的步行导航的终点
        } else if(mainActivity.routePlanSelect == MainActivity.TRANSIT) {
            if(mainActivity.busStationLocations.get(0) == null) {
                Toast.makeText(mainActivity, mainActivity.getString(R.string.wait_for_route_plan_result), Toast.LENGTH_SHORT).show();
                return;
            }

            //设置目的地
            double minDistance = DistanceUtil.getDistance(
                    mainActivity.latLng, mainActivity.endLocation
            );
            walkEndNode.setLocation(mainActivity.endLocation);
            for(int i = 0; i < mainActivity.busStationLocations.size(); i++) {
                double busStationDistance = DistanceUtil.getDistance(
                        mainActivity.latLng, mainActivity.busStationLocations.get(i)
                );
                if(busStationDistance < minDistance) {
                    minDistance = busStationDistance;
                    //最近的站点距离大于100m则将目的地设置为最近的站点
                    if(minDistance > 100) {
                        walkEndNode.setLocation(mainActivity.busStationLocations.get(i));
                        //否则设置为最近的站点的下一个站点
                    } else if(i != mainActivity.busStationLocations.size() - 1) {
                        walkEndNode.setLocation(mainActivity.busStationLocations.get(i + 1));
                    }
                }
            }
        }

        walkParam = new WalkNaviLaunchParam()
                .startNodeInfo(walkStartNode)
                .endNodeInfo(walkEndNode);

        walkParam.extraNaviMode(0);//普通步行导航

        WalkNavigateHelper.getInstance().routePlanWithRouteNode(walkParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                progressDialog.show();
            }

            @Override
            public void onRoutePlanSuccess() {
                progressDialog.dismiss();
                mainActivity.startActivity(new Intent(mainActivity, WNaviGuideActivity.class));
            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
                progressDialog.dismiss();
                Toast.makeText(mainActivity, mainActivity.getString(R.string.walk_route_plan_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //初始化骑行路线规划
    private void routeBikePlanWithParam() {
        //获取定位点和目标点坐标
        BikeRouteNodeInfo bikeStartNode = new BikeRouteNodeInfo();
        bikeStartNode.setLocation(mainActivity.latLng);
        BikeRouteNodeInfo bikeEndNode = new BikeRouteNodeInfo();
        bikeEndNode.setLocation(mainActivity.searchList.get(0).getLatLng());

        bikeParam = new BikeNaviLaunchParam()
                .startNodeInfo(bikeStartNode)
                .endNodeInfo(bikeEndNode);

        BikeNavigateHelper.getInstance().routePlanWithRouteNode(bikeParam, new IBRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                progressDialog.show();
            }

            @Override
            public void onRoutePlanSuccess() {
                progressDialog.dismiss();
                mainActivity.startActivity(new Intent(mainActivity, BNaviGuideActivity.class));
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError bikeRoutePlanError) {
                progressDialog.dismiss();
                Toast.makeText(mainActivity, mainActivity.getString(R.string.bike_route_plan_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
