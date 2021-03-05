package com.navigation.foxizz.mybaidumap;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
import com.navigation.foxizz.R;
import com.navigation.foxizz.activity.fragment.MainFragment;
import com.navigation.foxizz.mybaidumap.activity.BNaviGuideActivity;
import com.navigation.foxizz.mybaidumap.activity.DNaviGuideActivity;
import com.navigation.foxizz.mybaidumap.activity.WNaviGuideActivity;
import com.navigation.foxizz.util.AppUtil;
import com.navigation.foxizz.util.NetworkUtil;
import com.navigation.foxizz.util.SettingUtil;
import com.navigation.foxizz.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import static com.navigation.foxizz.BaseApplication.getBaseApplication;

/**
 * 导航模块
 */
public class MyNavigation {

    private final MainFragment mainFragment;
    public MyNavigation(MainFragment mainFragment) {
        this.mainFragment = mainFragment;

        initProgressDialog();//初始化加载弹窗

        //初始化驾车导航引擎
        if (NetworkUtil.isNetworkConnected()) {//有网络连接
            initDriveNavigateHelper();
        }
    }

    private ProgressDialog loadingProgress;//加载弹窗
    private static boolean hasInitDriveNavigate = false;//驾车导航是否已初始化
    private static boolean enableDriveNavigate = false;//是否可以进行驾车导航

    /**
     * 初始化驾车导航引擎
     */
    public void initDriveNavigateHelper() {
        if (!hasInitDriveNavigate) {
            BaiduNaviManagerFactory.getBaiduNaviManager().init(getBaseApplication(),
                    AppUtil.getSDCardDir(),
                    AppUtil.getAppFolderName(),
                    new IBaiduNaviManager.INaviInitListener() {
                        @Override
                        public void onAuthResult(int status, final String msg) {
                            if (status != 0) {
                                ToastUtil.showToast(mainFragment.getString(R.string.key_checkout_fail) + msg);
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

                            hasInitDriveNavigate = true;
                        }

                        @Override
                        public void initFailed(int errCode) {
                            ToastUtil.showToast(R.string.drive_navigate_init_fail + errCode);
                        }
                    });
        }
    }

    //初始化加载弹窗
    private void initProgressDialog() {
        loadingProgress = new ProgressDialog(mainFragment.requireActivity());
        loadingProgress.setTitle(R.string.hint);
        loadingProgress.setMessage(mainFragment.getString(R.string.route_plan_please_wait));
        loadingProgress.setCancelable(false);
    }

    //初始化语音合成模块
    private void initTTS() {
        BaiduNaviManagerFactory.getTTSManager().initTTS(new BNTTsInitConfig.Builder()
                .context(getBaseApplication())
                .sdcardRootPath(AppUtil.getSDCardDir())
                .appFolderName(AppUtil.getAppFolderName())
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
                        ToastUtil.showToast(R.string.walk_navigate_init_fail);
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
                        ToastUtil.showToast(R.string.bike_navigate_init_fail);
                    }
                });
    }

    /**
     * 开始导航
     */
    public void startNavigate() {
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

        if (mainFragment.mLatLng == null) {//还没有得到定位
            ToastUtil.showToast(R.string.wait_for_location_result);
            return;
        }

        if (mainFragment.mEndLocation == null) {
            ToastUtil.showToast(R.string.end_location_is_null);
            return;
        }

        switch (mainFragment.mRoutePlanSelect) {
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

            default:
                break;
        }
    }

    //初始化驾车路线规划
    private void routeDrivePlanWithParam() {
        if (!enableDriveNavigate) return;

        //设置驾车导航的起点和终点
        BNRoutePlanNode startNode = new BNRoutePlanNode.Builder()
                .latitude(mainFragment.mLatLng.latitude)
                .longitude(mainFragment.mLatLng.longitude)
                .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
                .build();

        BNRoutePlanNode endNode = new BNRoutePlanNode.Builder()
                .latitude(mainFragment.mEndLocation.latitude)
                .longitude(mainFragment.mEndLocation.longitude)
                .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
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
                                loadingProgress.show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                loadingProgress.dismiss();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                loadingProgress.dismiss();
                                ToastUtil.showToast(R.string.drive_route_plan_fail);
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                DNaviGuideActivity.startActivity(mainFragment.requireActivity());
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
        walkStartNode.setLocation(mainFragment.mLatLng);

        //设置步行导航的终点
        if (mainFragment.mRoutePlanSelect == MainFragment.WALKING) {
            walkEndNode.setLocation(mainFragment.mEndLocation);

            //计算公交导航的步行导航的终点
        } else if (mainFragment.mRoutePlanSelect == MainFragment.TRANSIT) {
            if (mainFragment.mBusStationLocations.size() == 0) {
                ToastUtil.showToast(R.string.wait_for_route_plan_result);
                return;
            }

            //设置目的地
            double minDistance = DistanceUtil.getDistance(
                    mainFragment.mLatLng, mainFragment.mEndLocation
            );
            walkEndNode.setLocation(mainFragment.mEndLocation);
            for (int i = 0; i < mainFragment.mBusStationLocations.size(); i++) {
                double busStationDistance = DistanceUtil.getDistance(
                        mainFragment.mLatLng, mainFragment.mBusStationLocations.get(i)
                );
                if (busStationDistance < minDistance) {
                    minDistance = busStationDistance;
                    //最近的站点距离大于100m则将目的地设置为最近的站点
                    if (minDistance > 100) {
                        walkEndNode.setLocation(mainFragment.mBusStationLocations.get(i));
                        //否则设置为最近的站点的下一个站点
                    } else if (i != mainFragment.mBusStationLocations.size() - 1) {
                        walkEndNode.setLocation(mainFragment.mBusStationLocations.get(i + 1));
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
                loadingProgress.show();
            }

            @Override
            public void onRoutePlanSuccess() {
                loadingProgress.dismiss();
                WNaviGuideActivity.startActivity(mainFragment.requireActivity());
            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
                loadingProgress.dismiss();
                ToastUtil.showToast(R.string.walk_route_plan_fail);
            }
        });
    }

    //初始化骑行路线规划
    private void routeBikePlanWithParam() {
        //获取定位点和目标点坐标
        BikeRouteNodeInfo bikeStartNode = new BikeRouteNodeInfo();
        bikeStartNode.setLocation(mainFragment.mLatLng);
        BikeRouteNodeInfo bikeEndNode = new BikeRouteNodeInfo();
        bikeEndNode.setLocation(mainFragment.searchList.get(0).getLatLng());

        BikeNaviLaunchParam bikeParam = new BikeNaviLaunchParam()
                .startNodeInfo(bikeStartNode)
                .endNodeInfo(bikeEndNode);

        BikeNavigateHelper.getInstance().routePlanWithRouteNode(bikeParam, new IBRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                loadingProgress.show();
            }

            @Override
            public void onRoutePlanSuccess() {
                loadingProgress.dismiss();
                BNaviGuideActivity.startActivity(mainFragment.requireActivity());
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError bikeRoutePlanError) {
                loadingProgress.dismiss();
                ToastUtil.showToast(R.string.bike_route_plan_fail);
            }
        });
    }

}
