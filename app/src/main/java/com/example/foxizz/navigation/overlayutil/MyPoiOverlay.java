package com.example.foxizz.navigation.overlayutil;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiSearch;

public class MyPoiOverlay extends PoiOverlay {

    private PoiSearch poiSearch;
    public MyPoiOverlay(BaiduMap baiduMap, PoiSearch poiSearch) {
        super(baiduMap);
        this.poiSearch = poiSearch;
    }

    @Override
    public boolean onPoiClick(int index) {
        super.onPoiClick(index);
        PoiInfo poiInfo = getPoiResult().getAllPoi().get(index);
        //检索poi详细信息
        poiSearch.searchPoiDetail(new PoiDetailSearchOption()
                .poiUid(poiInfo.uid));
        return true;
    }
}
