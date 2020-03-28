package com.example.foxizz.navigation.searchdata;

import com.baidu.mapapi.model.LatLng;

public class SearchItem {
    private LatLng latLng;//坐标
    private Double latitude;//纬度
    private Double longitude;//经度
    private String targetName;//目标名
    private String address;//目标地址
    private Double distance;//与目标的距离
    private String telephone;//联系方式
    private String shopTime;//营业时间

    public SearchItem(Double latitude, Double longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
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

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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

    public String getTelephone() {
        return telephone;
    }
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getShopTime() {
        return shopTime;
    }
    public void setShopTime(String shopTime) {
        this.shopTime = shopTime;
    }

    @Override
    public String toString() {
        return "SearchItem{" +
                "坐标:'" + latLng +
                ", 纬度:'" + latitude +
                ", 经度:'" + longitude +
                ", 目标名:'" + targetName + '\'' +
                ", 目标地址:'" + address + '\'' +
                ", 距离:" + distance +
                ", 联系方式:'" + telephone + '\'' +
                ", 营业时间:'" + shopTime + '\'' +
                '}';
    }

    //只打印其它信息
    public String otherInfoToString() {
        return "SearchItem{" +
                "联系方式:'" + telephone + '\'' +
                ", 营业时间:'" + shopTime + '\'' +
                '}';
    }

}
