package com.example.foxizz.navigation.searchdata;

import com.baidu.mapapi.model.LatLng;

public class SearchItem {
    private LatLng latLng;//坐标
    private String targetName;//目标名
    private String address;//目标地址
    private Double distance;//与目标的距离
    private String otherInfo;//其它信息

    public SearchItem(LatLng latLng, String targetName, String address, Double distance, String otherInfo) {
        super();
        this.latLng = latLng;
        this.targetName = targetName;
        this.address = address;
        this.distance = distance;
        this.otherInfo = otherInfo;
    }
    public SearchItem() {
        super();
    }

    public LatLng getLatLng() {
        return latLng;
    }
    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getTargetName() {
        return targetName;
    }
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public Double getDistance() {
        return distance;
    }
    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getOtherInfo() {
        return otherInfo;
    }
    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }

    @Override
    public String toString() {
        return "SearchItem{" + "坐标:'" + latLng +
                ", 目标名:'" + targetName + '\'' +
                ", 目标地址:'" + address + '\'' +
                ", 距离:'" + distance + '\'' +
                ", 其它信息:'" + otherInfo + '\'' + '}';
    }

}
