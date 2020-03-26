package com.example.foxizz.navigation.demo;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiSearch;

public class MyPoiOverlay extends PoiOverlay {

    private PoiSearch poiSearch;
    public MyPoiOverlay(BaiduMap arg0, PoiSearch poiSearch) {
        super(arg0);
        this.poiSearch = poiSearch;
    }

    @Override
    public boolean onPoiClick(int arg0) {
        super.onPoiClick(arg0);
        PoiInfo poiInfo = getPoiResult().getAllPoi().get(arg0);
        // 检索poi详细信息
        poiSearch.searchPoiDetail(new PoiDetailSearchOption()
                .poiUid(poiInfo.uid));
        return true;
    }
}
