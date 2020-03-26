package com.example.foxizz.navigation.util;

public class SearchItem {
    private String targetName;
    private String address;
    private Double distance;

    public SearchItem(String targetName, String address, Double distance) {
        super();
        this.targetName = targetName;
        this.address = address;
        this.distance = distance;
    }
    public SearchItem() {
        super();
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
