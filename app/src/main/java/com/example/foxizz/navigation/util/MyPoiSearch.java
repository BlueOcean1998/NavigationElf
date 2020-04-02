package com.example.foxizz.navigation.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.overlayutil.PoiOverlay;
import com.example.foxizz.navigation.searchdata.SearchItem;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import static com.example.foxizz.navigation.demo.Tools.isEffectiveDate;

/**
 * 搜索模块
 */
@SuppressLint("Registered")
public class MyPoiSearch {

    private MainActivity mainActivity;
    public MyPoiSearch(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    //初始化搜索目标信息
    public void initSearch() {
        mainActivity.mPoiSearch = PoiSearch.newInstance();

        OnGetPoiSearchResultListener listener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if(poiResult == null
                        || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {//没有找到检索结果
                    Toast.makeText(mainActivity,
                            "未找到结果", Toast.LENGTH_LONG).show();
                    return;
                }

                if(poiResult.error == SearchResult.ERRORNO.NO_ERROR) {//检索结果正常返回
                    //如果搜索到的目标数量小于50或使用的是周边搜索
                    if(poiResult.getTotalPoiNum() < 50 || MainActivity.poiSearchType == MainActivity.NEARBY_SEARCH) {
                        /*
                        Toast.makeText(MainActivity.this,
                                "总共查到" + poiResult.getTotalPoiNum() + "个兴趣点, 分为"
                                        + poiResult.getTotalPageNum() + "页", Toast.LENGTH_SHORT).show();
                        */

                        mainActivity.mBaiduMap.clear();//清空地图上的所有标记点和绘制的路线
                        mainActivity.searchList.clear();//清空searchList

                        PoiOverlay poiOverlay = new PoiOverlay(mainActivity.mBaiduMap);
                        mainActivity.mBaiduMap.setOnMarkerClickListener(poiOverlay);
                        poiOverlay.setData(poiResult);//设置POI数据
                        poiOverlay.addToMap();//将所有的overlay添加到地图上
                        poiOverlay.zoomToSpan();

                        //详细搜索所有页的所有内容，超过5页则只搜索5页内容
                        int searchNum = 5;
                        if(poiResult.getTotalPageNum() < searchNum) {
                            searchNum = poiResult.getTotalPageNum();
                        }
                        for(int i = 0; i < searchNum; i++) {
                            for(PoiInfo info: poiResult.getAllPoi()) {
                                //uid的集合，最多可以传入10个uid，多个uid之间用英文逗号分隔。
                                mainActivity.mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUids(info.getUid()));
                            }

                            poiResult.setCurrentPageNum(i);
                        }

                        MainActivity.poiSearchType = MainActivity.CITY_SEARCH;//还原搜索类型为城市内搜索

                    } else {//如果搜索到的目标数量不小于50则用周边搜索周围5km内的目标
                        mainActivity.mPoiSearch.searchNearby(new PoiNearbySearchOption()
                                .location(MainActivity.latLng)
                                .radius(5000)
                                .keyword(MainActivity.searchContent));

                        MainActivity.poiSearchType = MainActivity.NEARBY_SEARCH;//设置搜索类型为周边搜索
                    }
                }
            }

            @SuppressLint("SimpleDateFormat")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailResult) {
                if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(mainActivity,
                            "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                } else {//正常返回结果的时候，此处可以获得很多相关信息
                    SearchItem searchItem = new SearchItem();
                    //详细检索结果往往只有一个
                    for(PoiDetailInfo info: poiDetailResult.getPoiDetailInfoList()) {
                        searchItem.setTargetName(info.getName());//获取并设置目标名
                        searchItem.setAddress(info.getAddress());//获取并设置目标地址

                        LatLng tLatLng = info.getLocation();//获取目标坐标
                        searchItem.setLatLng(tLatLng);//设置目标坐标

                        //获取定位点到目标点的距离（单位：m，结果除以1000转化为km）
                        double distance = (DistanceUtil.getDistance(MainActivity.latLng, tLatLng) / 1000);
                        //保留两位小数
                        BigDecimal bd = new BigDecimal(distance);
                        distance = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        searchItem.setDistance(distance);


                        String otherInfo = "";

                        if(!info.getTelephone().equals("")) {
                            otherInfo += "联系方式：" + info.getTelephone() + "\n";
                        }

                        if(!info.getShopHours().equals("")) {
                            otherInfo += "营业时间：" + info.getShopHours();
                            try {
                                boolean flag = false;

                                DateFormat sdf = new SimpleDateFormat("HH:mm");
                                Date nowTime = sdf.parse(sdf.format(new Date()));
                                String[] shopHours = info.getShopHours().split(",");
                                for(String shopHour: shopHours) {
                                    String[] time = shopHour.split("-");
                                    Date startTime = sdf.parse(time[0]);
                                    Date endTime = sdf.parse(time[1]);
                                    if(isEffectiveDate(nowTime, startTime, endTime)) {
                                        flag = true;
                                    }
                                }

                                if(flag) otherInfo += " 营业中";
                                else otherInfo += " 休息中";
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            otherInfo += "\n";
                        }

                        if(info.getPrice() != 0) {
                            otherInfo += "平均消费：" + info.getPrice() + "\n";
                        }

                        searchItem.setOtherInfo(otherInfo);

                        mainActivity.searchList.add(searchItem);//添加搜到的内容到searchList
                    }

                    //这里的poiSearchType因为线程不同步问题始终不会等于NEARBY_SEARCH，待解决，（影响不大）
                    if(MainActivity.poiSearchType == MainActivity.NEARBY_SEARCH) {//如果是使用周边搜索
                        //按距离升序排序
                        mainActivity.searchList.sort(new Comparator<SearchItem>() {
                            @Override
                            public int compare(SearchItem o1, SearchItem o2) {
                                return o1.getDistance().compareTo(o2.getDistance());
                            }
                        });
                    }

                    mainActivity.searchAdapter.notifyDataSetChanged();//通知searchAdapter更新
                    mainActivity.searchResult.scrollToPosition(0);//移动回头部
                }
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }

            //已弃用的方法，但仍需实现
            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }
        };

        mainActivity.mPoiSearch.setOnGetPoiSearchResultListener(listener);
    }

}
