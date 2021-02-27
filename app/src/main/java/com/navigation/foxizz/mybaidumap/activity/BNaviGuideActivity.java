/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.navigation.foxizz.mybaidumap.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBNaviStatusListener;
import com.baidu.mapapi.bikenavi.adapter.IBRouteGuidanceListener;
import com.baidu.mapapi.bikenavi.adapter.IBTTSPlayer;
import com.baidu.mapapi.bikenavi.model.BikeRouteDetailInfo;
import com.baidu.mapapi.walknavi.model.RouteGuideKind;
import com.baidu.tts.client.SpeechSynthesizer;
import com.navigation.foxizz.util.SettingUtil;

/**
 * 骑行导航诱导活动
 * 不能继承AppCompatActivity
 */
public class BNaviGuideActivity extends Activity {

    private final static String TAG = BNaviGuideActivity.class.getSimpleName();

    private BikeNavigateHelper mNaviHelper;

    /**
     * 启动骑行导航诱导活动
     *
     * @param context 上下文
     */
    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, BNaviGuideActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNaviHelper = BikeNavigateHelper.getInstance();

        View view = mNaviHelper.onCreate(this);
        if (view != null) {
            setContentView(view);
        }

        mNaviHelper.setBikeNaviStatusListener(new IBNaviStatusListener() {
            @Override
            public void onNaviExit() {
                Log.d(TAG, "onNaviExit");
            }
        });

        mNaviHelper.setTTsPlayer(new IBTTSPlayer() {
            @Override
            public int playTTSText(String s, boolean b) {
                Log.d("tts", s);
                SpeechSynthesizer.getInstance().speak(s);//语音播报
                return 0;
            }
        });

        mNaviHelper.startBikeNavi(this);

        mNaviHelper.setRouteGuidanceListener(this, new IBRouteGuidanceListener() {
            @Override
            public void onRouteGuideIconUpdate(Drawable icon) {

            }

            @Override
            public void onRouteGuideKind(RouteGuideKind routeGuideKind) {

            }

            @Override
            public void onRoadGuideTextUpdate(CharSequence charSequence, CharSequence charSequence1) {

            }

            @Override
            public void onRemainDistanceUpdate(CharSequence charSequence) {

            }

            @Override
            public void onRemainTimeUpdate(CharSequence charSequence) {

            }

            @Override
            public void onGpsStatusChange(CharSequence charSequence, Drawable drawable) {

            }

            @Override
            public void onRouteFarAway(CharSequence charSequence, Drawable drawable) {

            }

            @Override
            public void onRoutePlanYawing(CharSequence charSequence, Drawable drawable) {

            }

            @Override
            public void onReRouteComplete() {

            }

            @Override
            public void onArriveDest() {

            }

            @Override
            public void onVibrate() {

            }

            @Override
            public void onGetRouteDetailInfo(BikeRouteDetailInfo bikeRouteDetailInfo) {

            }
        });

        //初始化设置
        SettingUtil.initSettings(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNaviHelper.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNaviHelper.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNaviHelper.quit();
    }

}
