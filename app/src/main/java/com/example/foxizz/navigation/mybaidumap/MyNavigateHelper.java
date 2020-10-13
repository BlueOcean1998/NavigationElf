package com.example.foxizz.navigation.mybaidumap;

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
import com.example.foxizz.navigation.activity.WNaviGuideActivity;
import com.example.foxizz.navigation.activity.fragment.MainFragment;
import com.example.foxizz.navigation.util.Tools;

import java.util.ArrayList;
import java.util.List;

import static com.example.foxizz.navigation.mybaidumap.MyApplication.getContext;
import static com.example.foxizz.navigation.util.Tools.haveReadWriteAndLocationPermissions;
import static com.example.foxizz.navigation.util.Tools.isAirplaneModeOn;
import static com.example.foxizz.navigation.util.Tools.isNetworkConnected;

/**
 * 导航模块
 */
public class MyNavigateHelper {

    private static boolean enableDriveNavigate = false;//是否可以进行驾车导航
    private ProgressDialog progressDialog;

    private final MainFragment mainFragment;
    public MyNavigateHelper(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    /**
     * 初始化驾车导航引擎
     */
    public void initDriveNavigateHelper() {
        BaiduNaviManagerFactory.getBaiduNaviManager().init(getContext(),
                Tools.getSDCardDir(),
                Tools.getAppFolderName(),
                new IBaiduNaviManager.INaviInitListener() {
                    @Override
                    public void onAuthResult(int status, final String msg) {
                        if (status != 0) {
                            mainFragment.requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), R.string.key_checkout_fail + msg, Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getContext(), R.string.drive_navigate_init_fail + errCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });

        initProgressDialog();
    }

    //初始化提示弹窗
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(mainFragment.requireActivity());
        progressDialog.setTitle(R.string.hint);
        progressDialog.setMessage(mainFragment.getString(R.string.route_plan_please_wait));
        progressDialog.setCancelable(false);
    }

    //初始化语音合成模块
    private void initTTS() {
        BaiduNaviManagerFactory.getTTSManager().initTTS(new BNTTsInitConfig.Builder()
                .context(getContext())
                .sdcardRootPath(Tools.getSDCardDir())
                .appFolderName(Tools.getAppFolderName())
                .appId(mainFragment.getString(R.string.app_id))
                .appKey(mainFragment.getString(R.string.api_key))
                .secretKey(mainFragment.getString(R.string.secret_key))
                .build()
        );
    }

    /**
     * 初始化步行导航引擎
     */
    public void initWalkNavigateHelper() {
        //步行引擎初始化
        WalkNavigateHelper.getInstance().initNaviEngine(
                mainFragment.requireActivity(), new IWEngineInitListener() {
                    @Override
                    public void engineInitSuccess() {
                        routeWalkPlanWithParam();
                    }

                    @Override
                    public void engineInitFail() {
                        Toast.makeText(getContext(), R.string.walk_navigate_init_fail, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 初始化骑行导航引擎
     */
    public void initBikeNavigateHelper() {
        //骑行引擎初始化
        BikeNavigateHelper.getInstance().initNaviEngine(
                mainFragment.requireActivity(), new IBEngineInitListener() {
                    @Override
                    public void engineInitSuccess() {
                        routeBikePlanWithParam();
                    }

                    @Override
                    public void engineInitFail() {
                        Toast.makeText(getContext(), R.string.bike_navigate_init_fail, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 开始导航
     */
    public void startNavigate() {
        if (!isNetworkConnected()) {//没有网络连接
            Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isAirplaneModeOn()) {//没有关飞行模式
            Toast.makeText(getContext(), R.string.close_airplane_mode, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!haveReadWriteAndLocationPermissions()) {//权限不足
            mainFragment.requestPermission();//申请权限，获得权限后定位
            return;
        }

        if (mainFragment.latLng == null) {//还没有得到定位
            Toast.makeText(getContext(), R.string.wait_for_location_result, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mainFragment.endLocation == null) {
            Toast.makeText(getContext(), R.string.end_location_is_null, Toast.LENGTH_SHORT).show();
            return;
        }

        switch (mainFragment.routePlanSelect) {
            //驾车导航
            case MainFragment.DRIVING:
                routeDrivePlanWithParam();//开始驾车导航
                break;

            //步行导航，公交导航
            case MainFragment.WALKING:
            case MainFragment.TRANSIT:
                initWalkNavigateHelper();//开始步行导航
                break;

            //骑行导航
            case MainFragment.BIKING:
                initBikeNavigateHelper();//开始骑行导航
                break;
        }
    }

    //初始化驾车路线规划
    private void routeDrivePlanWithParam() {
        if (!enableDriveNavigate) return;

        //设置驾车导航的起点和终点
        BNRoutePlanNode startNode = new BNRoutePlanNode.Builder()
                .latitude(mainFragment.latLng.latitude)
                .longitude(mainFragment.latLng.longitude)
                .build();

        BNRoutePlanNode endNode = new BNRoutePlanNode.Builder()
                .latitude(mainFragment.endLocation.latitude)
                .longitude(mainFragment.endLocation.longitude)
                .build();

        List<BNRoutePlanNode> mBNRoutePlanNodes = new ArrayList<>();
        mBNRoutePlanNodes.add(startNode);
        mBNRoutePlanNodes.add(endNode);

        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                mBNRoutePlanNodes,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                null,
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                progressDialog.show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                progressDialog.dismiss();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), R.string.drive_route_plan_fail, Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                mainFragment.startActivity(
                                        new Intent(getContext(), DNaviGuideActivity.class)
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
        walkStartNode.setLocation(mainFragment.latLng);

        //设置步行导航的终点
        if (mainFragment.routePlanSelect == MainFragment.WALKING) {
            walkEndNode.setLocation(mainFragment.endLocation);

            //计算公交导航的步行导航的终点
        } else if (mainFragment.routePlanSelect == MainFragment.TRANSIT) {
            if (mainFragment.busStationLocations.get(0) == null) {
                Toast.makeText(getContext(), R.string.wait_for_route_plan_result, Toast.LENGTH_SHORT).show();
                return;
            }

            //设置目的地
            double minDistance = DistanceUtil.getDistance(
                    mainFragment.latLng, mainFragment.endLocation
            );
            walkEndNode.setLocation(mainFragment.endLocation);
            for (int i = 0; i < mainFragment.busStationLocations.size(); i++) {
                double busStationDistance = DistanceUtil.getDistance(
                        mainFragment.latLng, mainFragment.busStationLocations.get(i)
                );
                if (busStationDistance < minDistance) {
                    minDistance = busStationDistance;
                    //最近的站点距离大于100m则将目的地设置为最近的站点
                    if (minDistance > 100) {
                        walkEndNode.setLocation(mainFragment.busStationLocations.get(i));
                        //否则设置为最近的站点的下一个站点
                    } else if (i != mainFragment.busStationLocations.size() - 1) {
                        walkEndNode.setLocation(mainFragment.busStationLocations.get(i + 1));
                    }
                }
            }
        }

        WalkNaviLaunchParam walkParam = new WalkNaviLaunchParam()
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
                mainFragment.startActivity(new Intent(getContext(), WNaviGuideActivity.class));
            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), R.string.walk_route_plan_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //初始化骑行路线规划
    private void routeBikePlanWithParam() {
        //获取定位点和目标点坐标
        BikeRouteNodeInfo bikeStartNode = new BikeRouteNodeInfo();
        bikeStartNode.setLocation(mainFragment.latLng);
        BikeRouteNodeInfo bikeEndNode = new BikeRouteNodeInfo();
        bikeEndNode.setLocation(mainFragment.searchList.get(0).getLatLng());

        BikeNaviLaunchParam bikeParam = new BikeNaviLaunchParam()
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
                mainFragment.startActivity(new Intent(getContext(), BNaviGuideActivity.class));
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError bikeRoutePlanError) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), R.string.bike_route_plan_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
