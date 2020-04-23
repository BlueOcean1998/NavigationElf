package com.example.foxizz.navigation.util;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.baidu.location.Poi;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.searchdata.SearchItem;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.foxizz.navigation.demo.Tools.isEffectiveDate;

/**
 * 搜索模块
 */
@SuppressLint("Registered")
public class MyPoiSearch {

    private final static int MAX_SEARCH_NUM = 5;//搜索的最大页数
    private final static int TO_NEARBY_SEARCH_MIN_NUM = 100;//触发周边搜索需要的最小目标点数量
    private final static int NEARBY_SEARCH_DISTANCE = 5000;//周边搜索的距离

    private MainActivity mainActivity;
    public MyPoiSearch(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public int poiSearchType;//使用的搜索类型
    public final static int CITY_SEARCH = 0;//城市内搜索
    public final static int OTHER_CITY_SEARCH = 1;//其它城市搜索，使用城市内搜索不到内容时启用
    public final static int NEARBY_SEARCH = 2;//周边搜索，使用城市内搜索到的内容过多时启用
    public final static int CONSTRAINT_CITY_SEARCH = 3;//强制城市内搜索，使用城市内搜索不会再自动转为周边搜索
    public final static int DETAIL_SEARCH = 4;//直接详细信息搜索，一般直接用uid搜索
    public final static int DETAIL_SEARCH_ALL = 5;//详细搜索全部，用于数据库录入

    //初始化搜索目标信息
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void initSearch() {
        //获取Poi搜索实例
        mainActivity.mPoiSearch = PoiSearch.newInstance();

        OnGetPoiSearchResultListener listener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                mainActivity.mBaiduMap.clear();//清空地图上的所有标记点和绘制的路线
                mainActivity.searchList.clear();//清空searchList
                mainActivity.searchAdapter.notifyDataSetChanged();//通知adapter更新
                mainActivity.isHistorySearchResult = false;//已经不是搜索历史记录了

                if(poiResult == null//没有找到检索结果
                        || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    //城市内搜索不到内容时切换到别的城市继续搜索
                    if(poiSearchType == CITY_SEARCH) {
                        if(poiResult != null && poiResult.getSuggestCityList() != null) {
                            poiSearchType = OTHER_CITY_SEARCH;

                            for(CityInfo cityInfo : poiResult.getSuggestCityList()) {
                                //开始别的城市内搜索
                                mainActivity.mPoiSearch.searchInCity(new PoiCitySearchOption()
                                        .city(cityInfo.city)
                                        .keyword(mainActivity.searchContent));
                            }
                        }
                        return;
                    }

                    //周边搜索不到内容时切换回城市内搜索
                    if(poiSearchType == NEARBY_SEARCH) {
                        poiSearchType = CONSTRAINT_CITY_SEARCH;//设置搜索类型为强制城市内搜索

                        //开始城市内搜索
                        mainActivity.mPoiSearch.searchInCity(new PoiCitySearchOption()
                                .city(mainActivity.mCity)
                                .keyword(mainActivity.searchContent));
                    } else {
                        Toast.makeText(mainActivity, mainActivity.getString(R.string.find_nothing), Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                if(poiResult.error == SearchResult.ERRORNO.NO_ERROR) {//检索结果正常返回
                    if((poiResult.getTotalPoiNum() < TO_NEARBY_SEARCH_MIN_NUM
                            || poiSearchType == OTHER_CITY_SEARCH
                            || poiSearchType == NEARBY_SEARCH
                            || poiSearchType == CONSTRAINT_CITY_SEARCH)) {

                        /*这些是测试时给程序员看的
                        Toast.makeText(mainActivity,
                                "总共查到" + poiResult.getTotalPoiNum() + "个兴趣点, 分为"
                                        + poiResult.getTotalPageNum() + "页", Toast.LENGTH_SHORT).show();

                        PoiOverlay poiOverlay = new PoiOverlay(mainActivity.mBaiduMap);
                        mainActivity.mBaiduMap.setOnMarkerClickListener(poiOverlay);
                        poiOverlay.setData(poiResult);//设置POI数据
                        poiOverlay.addToMap();//将所有的overlay添加到地图上
                        poiOverlay.zoomToSpan();//移动地图到目标点上
                        */

                        //详细搜索所有页的所有内容，超过最大页数则只搜索最大页数内容
                        int searchPageNum = MAX_SEARCH_NUM;
                        if(poiResult.getTotalPageNum() < searchPageNum) {
                            searchPageNum = poiResult.getTotalPageNum();
                        }

                        for(int i = 0; i < searchPageNum; i++) {
                            poiResult.setCurrentPageNum(i);//下一页

                            for(PoiInfo info: poiResult.getAllPoi()) {
                                //检索到的Poi类型不是下面那些
                                if(info.getType() != PoiInfo.POITYPE.BUS_STATION
                                        && info.getType() != PoiInfo.POITYPE.BUS_LINE
                                        && info.getType() != PoiInfo.POITYPE.SUBWAY_STATION
                                        && info.getType() != PoiInfo.POITYPE.SUBWAY_LINE) {
                                    //uid的集合，最多可以传入10个uid，多个uid之间用英文逗号分隔。
                                    mainActivity.mPoiSearch.searchPoiDetail(//进行详细信息搜索
                                            (new PoiDetailSearchOption()).poiUids(info.getUid()));
                                }
                            }
                        }

                    //如果目标数量小于预设值或搜索类型为周边搜索或搜索类型为强制城市内搜索
                    } else {
                        poiSearchType = NEARBY_SEARCH;//设置搜索类型为周边搜索

                        //开始周边搜索
                        mainActivity.mPoiSearch.searchNearby(new PoiNearbySearchOption()
                                .location(mainActivity.latLng)
                                .radius(NEARBY_SEARCH_DISTANCE)
                                .keyword(mainActivity.searchContent));
                    }
                }
            }

            @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailResult) {
                if(poiDetailResult == null//没有找到检索结果
                        || poiDetailResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.find_nothing), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(poiDetailResult.error == SearchResult.ERRORNO.NO_ERROR) {//检索结果正常返回
                    //直接的详细信息搜索
                    if(poiSearchType == DETAIL_SEARCH || poiSearchType == DETAIL_SEARCH_ALL) {
                        for(PoiDetailInfo info: poiDetailResult.getPoiDetailInfoList()) {
                            //将结果保存到数据库
                            if(poiSearchType ==DETAIL_SEARCH)
                                insertOrUpdateSearchDatabase(info);

                            //更新搜索结果列表
                            SearchItem searchItem = new SearchItem();

                            //设置详细信息内容、更新搜索结果列表
                            //获取并设置目标uid
                            searchItem.setUid(info.getUid());

                            //获取并设置目标名
                            mainActivity.infoTargetName.setText(info.getName());
                            searchItem.setTargetName(info.getName());

                            //获取并设置目标地址
                            mainActivity.infoAddress.setText(info.getAddress());
                            searchItem.setAddress(info.getAddress());

                            LatLng tLatLng = info.getLocation();//获取目标坐标
                            searchItem.setLatLng(tLatLng);//设置目标坐标

                            //获取定位点到目标点的距离（单位：m，结果除以1000转化为km）
                            double distance = (DistanceUtil.getDistance(mainActivity.latLng, tLatLng) / 1000);
                            //保留两位小数
                            BigDecimal bd = new BigDecimal(distance);
                            distance = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            mainActivity.infoDistance.setText(distance + "km");
                            searchItem.setDistance(distance);

                            //寻找搜索列表中Uid相同的item
                            for(int i = 0; i < mainActivity.searchList.size(); i++) {
                                if(mainActivity.searchList.get(i).getUid().equals(info.getUid())) {
                                    if(poiSearchType == DETAIL_SEARCH) {
                                        mainActivity.searchList.remove(i);//移除原本位置的item
                                        mainActivity.searchList.add(0, searchItem);//将其添加到头部
                                    } else if(poiSearchType == DETAIL_SEARCH_ALL) {
                                        mainActivity.searchList.set(i, searchItem);//直接修改原位置的item
                                    }
                                    break;
                                }
                            }

                            //其它信息
                            String otherInfo = "";

                            if(info.getTelephone() != null && !info.getTelephone().isEmpty()) {//获取联系方式
                                otherInfo += mainActivity.getString(R.string.phone_number) + info.getTelephone() + "\n";
                            }

                            if(info.getShopHours() != null && !info.getShopHours().isEmpty()) {//获取营业时间
                                otherInfo += mainActivity.getString(R.string.shop_time) + info.getShopHours();
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

                                    if(flag) otherInfo += mainActivity.getString(R.string.shopping);
                                    else otherInfo += mainActivity.getString(R.string.relaxing);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                otherInfo += "\n";
                            }

                            if(info.getPrice() != 0) {//获取平均消费
                                otherInfo += mainActivity.getString(R.string.price) + info.getPrice() + "元\n";
                            }

                            mainActivity.infoOthers.setText(otherInfo);//设置其它信息
                        }

                    } else {//间接的详细信息搜索
                        //由于一般传入的是uid，详细检索结果往往只有一个
                        for(PoiDetailInfo info: poiDetailResult.getPoiDetailInfoList()) {
                            //设置搜索结果列表
                            SearchItem searchItem = new SearchItem();

                            searchItem.setUid(info.getUid());//获取并设置Uid
                            searchItem.setTargetName(info.getName());//获取并设置目标名
                            searchItem.setAddress(info.getAddress());//获取并设置目标地址

                            LatLng tLatLng = info.getLocation();//获取目标坐标
                            searchItem.setLatLng(tLatLng);//设置目标坐标

                            //获取定位点到目标点的距离（单位：m，结果除以1000转化为km）
                            double distance = (DistanceUtil.getDistance(mainActivity.latLng, tLatLng) / 1000);
                            //保留两位小数
                            BigDecimal bd = new BigDecimal(distance);
                            distance = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            searchItem.setDistance(distance);//设置定位点到目标点的距离

                            mainActivity.searchList.add(searchItem);//添加搜到的内容到searchList
                        }

                        /*经过测试，随机排序更为合理
                        if(poiSearchType == NEARBY_SEARCH) {//如果是使用周边搜索
                            //按距离升序排序
                            mainActivity.searchList.sort(new Comparator<SearchItem>() {
                                @Override
                                public int compare(SearchItem o1, SearchItem o2) {
                                    return o1.getDistance().compareTo(o2.getDistance());
                                }
                            });
                        }
                        */

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

    //将详细搜索结果录入数据库或更新数据库中这条记录的内容
    private void insertOrUpdateSearchDatabase(PoiDetailInfo info) {
        Cursor cursor = mainActivity.dbHelper.getSearchData(info.getUid());
        if(cursor != null) {
            if(cursor.getCount() > 0) mainActivity.dbHelper.updateSearchData(info);//有则更新
            else mainActivity.dbHelper.insertSearchData(info);//没有则添加

            cursor.close();
        }
    }

}
