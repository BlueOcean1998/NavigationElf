package com.example.foxizz.navigation.schemedata;

import androidx.annotation.NonNull;

import com.baidu.mapapi.search.route.MassTransitRouteLine;

/**
 * 路线规划信息类
 */
public class SchemeItem {
    private MassTransitRouteLine routeLine;//路线
    private String simpleInfo;//简要信息
    private String detailInfo;//详细信息
    private Boolean expandFlag;//伸展状态

    public SchemeItem(MassTransitRouteLine routeLine, String simpleInfo, String detailInfo) {
        this.routeLine = routeLine;
        this.simpleInfo = simpleInfo;
        this.detailInfo = detailInfo;
        expandFlag = false;
    }
    public SchemeItem() {
        super();
        expandFlag = false;
    }

    public MassTransitRouteLine getRouteLine() {
        return routeLine;
    }
    public void setRouteLine(MassTransitRouteLine routeLine) {
        this.routeLine = routeLine;
    }

    public String getSimpleInfo() {
        return simpleInfo;
    }
    public void setSimpleInfo(String simpleInfo) {
        this.simpleInfo = simpleInfo;
    }

    public String getDetailInfo() {
        return detailInfo;
    }
    public void setDetailInfo(String detailInfo) {
        this.detailInfo = detailInfo;
    }

    public Boolean getExpandFlag() {
        return expandFlag;
    }
    public void setExpandFlag(Boolean expandFlag) {
        this.expandFlag = expandFlag;
    }

    @NonNull
    @Override
    public String toString() {
        return "SchemeItem{" +
                "路线:'" + routeLine +
                ", 简要信息:'" + simpleInfo + '\'' +
                ", 详细信息:'" + detailInfo + '\'' +
                ", 伸展状态:'" + expandFlag + '\'' +
                '}';
    }

}
