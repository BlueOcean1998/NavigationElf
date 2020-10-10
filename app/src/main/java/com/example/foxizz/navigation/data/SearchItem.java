package com.example.foxizz.navigation.data;

import androidx.annotation.NonNull;

import com.baidu.mapapi.model.LatLng;

/**
 * 搜索到的目标信息类
 */
public class SearchItem {

    private String uid;//唯一地址标识
    private LatLng latLng;//坐标
    private String targetName;//目标名
    private String address;//目标地址
    private Double distance;//与目标的距离

    public SearchItem(String uid, LatLng latLng, String targetName, String address, Double distance) {
        super();
        this.uid = uid;
        this.latLng = latLng;
        this.targetName = targetName;
        this.address = address;
        this.distance = distance;
    }

    public SearchItem() {
        super();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    @NonNull
    @Override
    public String toString() {
        return "SearchItem{" +
                "uid:'" + uid + '\'' +
                ", 坐标:" + latLng +
                ", 目标名:'" + targetName + '\'' +
                ", 目标地址:'" + address + '\'' +
                ", 距离:" + distance + '\'' +
                '}';
    }

}
