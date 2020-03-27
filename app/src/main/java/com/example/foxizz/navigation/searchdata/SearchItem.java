package com.example.foxizz.navigation.searchdata;

import com.baidu.mapapi.model.LatLng;

public class SearchItem {
    private LatLng latLng;//坐标
    private Double latitude;//纬度
    private Double longitude;//经度
    private String targetName;//目标名
    private String address;//目标地址
    private Double distance;//与目标的距离

    public SearchItem(LatLng latLng, String targetName, String address, Double distance) {
        super();
        this.latLng = latLng;
        this.targetName = targetName;
        this.address = address;
        this.distance = distance;
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

}
