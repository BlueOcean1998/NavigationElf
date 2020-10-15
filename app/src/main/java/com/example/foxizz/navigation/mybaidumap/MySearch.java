package com.example.foxizz.navigation.mybaidumap;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

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
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.fragment.MainFragment;
import com.example.foxizz.navigation.data.SearchItem;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.foxizz.navigation.mybaidumap.MyApplication.getContext;
import static com.example.foxizz.navigation.util.Tools.isEffectiveDate;

/**
 * 搜索模块
 */
@SuppressLint("Registered")
public class MySearch {

    public final static int PAGE_CAPACITY = 16;//每页的容量
    public final static int TO_NEARBY_SEARCH_MIN_NUM = 64;//触发周边搜索需要的最小目标点数量
    public final static int NEARBY_SEARCH_DISTANCE = 10000;//周边搜索的距离

    public final static int CITY_SEARCH = 0;//城市内搜索
    public final static int OTHER_CITY_SEARCH = 1;//其它城市搜索，使用城市内搜索不到内容时启用
    public final static int NEARBY_SEARCH = 2;//周边搜索，使用城市内搜索到的内容过多时启用
    public final static int CONSTRAINT_CITY_SEARCH = 3;//强制城市内搜索，使用城市内搜索不会再自动转为周边搜索
    public final static int DETAIL_SEARCH = 4;//直接详细信息搜索，一般直接用uid搜索
    public final static int DETAIL_SEARCH_ALL = 5;//详细搜索全部，用于数据库录入
    public int poiSearchType;//使用的搜索类型

    public boolean isSearching;//是否正在搜索
    private int currentPage;//当前页数
    public boolean isFirstDetailSearch;//是否是第一次详细信息搜索
    public final Set<String> uidList = new HashSet<>();//uid集合

    private final MainFragment mainFragment;
    public MySearch(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    /**
     * 开始POI搜索
     *
     * @param currentPage 第几页
     */
    public void startPoiSearch(int currentPage) {
        isSearching = true;//正在搜索
        this.currentPage = currentPage;//当前页
        uidList.clear();//清空uid集合

        if (currentPage == 0) {
            //开始Sug搜索
            if (mainFragment.searchCityList != null && mainFragment.searchCityList.size() > 0) {
                mainFragment.mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                        .city(mainFragment.searchCityList.get(0))
                        .keyword(mainFragment.searchContent));
            }
        } else {
            //开始城市内搜索
            mainFragment.mPoiSearch.searchInCity(new PoiCitySearchOption()
                    .city(mainFragment.searchCityList.get(0))
                    .keyword(mainFragment.searchContent)
                    .pageNum(currentPage)
                    .pageCapacity(PAGE_CAPACITY));
        }
    }

    /*
     * 初始化搜索
     */
    public void initSearch() {
        initSugSearch();
        initPoiSearch();
    }

    //初始化Sug搜索
    private void initSugSearch() {
        //获取Sug搜索实例
        mainFragment.mSuggestionSearch = SuggestionSearch.newInstance();

        OnGetSuggestionResultListener suggestionResultListener = new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {
                //将Sug获取到的uid录入uid列表
                List<SuggestionResult.SuggestionInfo> suggestionInfoList =
                        suggestionResult.getAllSuggestions();
                if (suggestionInfoList != null && suggestionInfoList.size() > 0) {
                    for (SuggestionResult.SuggestionInfo suggestionInfo
                            : suggestionResult.getAllSuggestions()) {
                        uidList.add(suggestionInfo.getUid());
                    }
                }

                if (uidList.size() == 0) {
                    Toast.makeText(getContext(), R.string.find_nothing, Toast.LENGTH_LONG).show();
                } else {
                    //要进行详细搜索的所有内容
                    for (String uid : uidList) {
                        //uid的集合，最多可以传入10个uid，多个uid之间用英文逗号分隔。
                        mainFragment.mPoiSearch.searchPoiDetail(//开始详细信息搜索
                                (new PoiDetailSearchOption()).poiUids(uid));
                    }
                }

                //开始城市内搜索
                mainFragment.mPoiSearch.searchInCity(new PoiCitySearchOption()
                        .city(mainFragment.searchCityList.get(0))
                        .keyword(mainFragment.searchContent)
                        .pageCapacity(PAGE_CAPACITY));
            }
        };

        mainFragment.mSuggestionSearch.setOnGetSuggestionResultListener(suggestionResultListener);
    }

    //初始化POI搜索
    private void initPoiSearch() {
        //获取POI搜索实例
        mainFragment.mPoiSearch = PoiSearch.newInstance();

        OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (currentPage == 0) {
                    //POI信息加载完成
                    mainFragment.searchLoading.setVisibility(View.GONE);
                    mainFragment.searchResult.setVisibility(View.VISIBLE);
                }

                if (poiResult == null//没有找到检索结果
                        || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    //城市内搜索不到内容时切换到别的城市继续搜索
                    if (poiSearchType == CITY_SEARCH) {
                        if (poiResult != null && poiResult.getSuggestCityList() != null) {
                            poiSearchType = OTHER_CITY_SEARCH;

                            for (CityInfo cityInfo : poiResult.getSuggestCityList()) {
                                //开始别的城市内搜索
                                mainFragment.mPoiSearch.searchInCity(new PoiCitySearchOption()
                                        .city(cityInfo.city)
                                        .keyword(mainFragment.searchContent)
                                        .pageCapacity(PAGE_CAPACITY));
                            }
                        }
                        return;
                    }

                    //周边搜索不到内容时切换回城市内搜索
                    if (poiSearchType == NEARBY_SEARCH) {
                        poiSearchType = CONSTRAINT_CITY_SEARCH;//设置搜索类型为强制城市内搜索

                        //开始城市内搜索
                        mainFragment.mPoiSearch.searchInCity(new PoiCitySearchOption()
                                .city(mainFragment.searchCityList.get(0))
                                .keyword(mainFragment.searchContent)
                                .pageCapacity(PAGE_CAPACITY));
                    } else if (uidList.size() == 0) {
                        Toast.makeText(getContext(), R.string.find_nothing, Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {//检索结果正常返回
                    //如果目标数量小于预设值或搜索类型为其它城市搜索或周边搜索或强制城市内搜索
                    if ((poiResult.getTotalPoiNum() < TO_NEARBY_SEARCH_MIN_NUM
                            || poiSearchType == OTHER_CITY_SEARCH
                            || poiSearchType == NEARBY_SEARCH
                            || poiSearchType == CONSTRAINT_CITY_SEARCH)) {

                        mainFragment.totalPage = poiResult.getTotalPageNum();
                        mainFragment.currentPage = poiResult.getCurrentPageNum();

                        /*
                        PoiOverlay poiOverlay = new PoiOverlay(mainFragment.mBaiduMap);
                        mainFragment.mBaiduMap.setOnMarkerClickListener(poiOverlay);
                        poiOverlay.setData(poiResult);//设置POI数据
                        poiOverlay.addToMap();//将所有的overlay添加到地图上
                        poiOverlay.zoomToSpan();//移动地图到目标点上
                        */

                        //新建searchItems，用于保存本次的搜索结果
                        List<SearchItem> searchItems = new ArrayList<>();

                        //将POI获取到的信息录入搜索结果列表
                        for (PoiInfo poiInfo : poiResult.getAllPoi()) {
                            if (!uidList.contains(poiInfo.getUid())//uid不重复且类型不是下面那些
                                    && poiInfo.getType() != PoiInfo.POITYPE.BUS_STATION
                                    && poiInfo.getType() != PoiInfo.POITYPE.BUS_LINE
                                    && poiInfo.getType() != PoiInfo.POITYPE.SUBWAY_STATION
                                    && poiInfo.getType() != PoiInfo.POITYPE.SUBWAY_LINE
                                    && isUidNotInSearList(poiInfo.getUid())) {

                                SearchItem searchItem = new SearchItem();

                                searchItem.setUid(poiInfo.getUid());//获取并设置Uid
                                searchItem.setTargetName(poiInfo.getName());//获取并设置目标名
                                searchItem.setAddress(poiInfo.getAddress());//获取并设置目标地址

                                LatLng tLatLng = poiInfo.getLocation();//获取目标坐标
                                searchItem.setLatLng(tLatLng);//设置目标坐标

                                //设置定位点到目标点的距离（单位：m，结果除以1000转化为km，保留两位小数）
                                searchItem.setDistance(BigDecimal.valueOf
                                        (DistanceUtil.getDistance(mainFragment.latLng, tLatLng) / 1000)
                                        .setScale(2, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue());

                                //添加搜索到的不同uid的内容添加到searchItems
                                searchItems.add(searchItem);
                            }
                        }

                        //周边搜索按距离升序排序
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                                && poiSearchType == NEARBY_SEARCH) {
                            searchItems.sort(new Comparator<SearchItem>() {
                                @Override
                                public int compare(SearchItem o1, SearchItem o2) {
                                    return o1.getDistance().compareTo(o2.getDistance());
                                }
                            });
                        }

                        mainFragment.searchList.addAll(searchItems);//将所有searchItem添加到searchList中

                        if (mainFragment.currentPage == 0) {//第0页全部排序
                            //周边搜索按距离升序排序
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                                    && poiSearchType == NEARBY_SEARCH) {
                                mainFragment.searchList.sort(new Comparator<SearchItem>() {
                                    @Override
                                    public int compare(SearchItem o1, SearchItem o2) {
                                        return o1.getDistance().compareTo(o2.getDistance());
                                    }
                                });
                            }
                        }

                        mainFragment.searchAdapter.notifyDataSetChanged();//通知searchAdapter更新
                        mainFragment.currentPage++;//当前页+1
                        isSearching = false;//搜索完成

                    } else {
                        poiSearchType = NEARBY_SEARCH;//设置搜索类型为周边搜索

                        //开始周边搜索
                        mainFragment.mPoiSearch.searchNearby(new PoiNearbySearchOption()
                                .location(mainFragment.latLng)
                                .radius(NEARBY_SEARCH_DISTANCE)
                                .keyword(mainFragment.searchContent)
                                .pageNum(currentPage)
                                .pageCapacity(PAGE_CAPACITY));
                    }
                }
            }

            @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailResult) {
                if (isFirstDetailSearch) {//如果是第一次详细信息搜索
                    isFirstDetailSearch = false;

                    //详细信息加载完成
                    mainFragment.infoLoading.setVisibility(View.GONE);
                    mainFragment.searchInfoScroll.setVisibility(View.VISIBLE);

                    if (poiDetailResult == null//没有找到检索结果
                            || poiDetailResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND)
                        return;
                }

                if (poiDetailResult.error == SearchResult.ERRORNO.NO_ERROR) {//检索结果正常返回
                    //直接的详细信息搜索
                    if (poiSearchType == DETAIL_SEARCH || poiSearchType == DETAIL_SEARCH_ALL) {
                        //由于一般只传入一个uid，列表里往往只有一个搜索结果，即使这里用了循环语句
                        for (PoiDetailInfo detailInfo : poiDetailResult.getPoiDetailInfoList()) {
                            //将结果保存到数据库
                            if (poiSearchType == DETAIL_SEARCH)
                                mainFragment.searchDataHelper.insertOrUpdateSearchData(detailInfo);

                            //更新搜索结果列表
                            SearchItem searchItem = new SearchItem();

                            //设置详细信息内容、更新搜索结果列表
                            //获取并设置目标uid
                            searchItem.setUid(detailInfo.getUid());

                            //获取并设置目标名
                            mainFragment.infoTargetName.setText(detailInfo.getName());
                            searchItem.setTargetName(detailInfo.getName());

                            //获取并设置目标地址
                            mainFragment.infoAddress.setText(detailInfo.getAddress());
                            searchItem.setAddress(detailInfo.getAddress());

                            LatLng tLatLng = detailInfo.getLocation();//获取目标坐标
                            searchItem.setLatLng(tLatLng);//设置目标坐标

                            //获取定位点到目标点的距离（单位：m，结果除以1000转化为km）
                            double distance = (DistanceUtil.getDistance(mainFragment.latLng, tLatLng) / 1000);
                            //保留两位小数
                            BigDecimal bd = new BigDecimal(distance);
                            distance = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            mainFragment.infoDistance.setText(distance + "km");
                            searchItem.setDistance(distance);

                            //寻找搜索列表中Uid相同的item
                            for (int i = 0; i < mainFragment.searchList.size(); i++) {
                                if (mainFragment.searchList.get(i).getUid().equals(detailInfo.getUid())) {
                                    if (poiSearchType == DETAIL_SEARCH) {
                                        mainFragment.searchList.remove(i);//移除原本位置的item
                                        mainFragment.searchList.add(0, searchItem);//将其添加到头部
                                    } else if (poiSearchType == DETAIL_SEARCH_ALL) {
                                        mainFragment.searchList.set(i, searchItem);//直接修改原位置的item
                                    }
                                    break;
                                }
                            }

                            //其它信息
                            StringBuilder otherInfo = new StringBuilder();

                            //获取联系方式
                            if (detailInfo.getTelephone() != null && !detailInfo.getTelephone().isEmpty()) {
                                try {
                                    otherInfo.append(getContext().getString(R.string.phone_number)).append(detailInfo.getTelephone()).append("\n");
                                } catch (Exception ignored) {

                                }
                            }

                            //获取营业时间
                            if (detailInfo.getShopHours() != null && !detailInfo.getShopHours().isEmpty()) {
                                otherInfo.append(getContext().getString(R.string.shop_time)).append(detailInfo.getShopHours());
                                try {
                                    boolean flag = false;

                                    DateFormat sdf = new SimpleDateFormat("HH:mm");
                                    Date nowTime = sdf.parse(sdf.format(new Date()));
                                    String[] shopHours = detailInfo.getShopHours().split(",");
                                    for (String shopHour : shopHours) {
                                        String[] time = shopHour.split("-");
                                        Date startTime = sdf.parse(time[0]);
                                        Date endTime = sdf.parse(time[1]);
                                        if (isEffectiveDate(nowTime, startTime, endTime)) {
                                            flag = true;
                                        }
                                    }

                                    if (flag)
                                        otherInfo.append(getContext().getString(R.string.shopping));
                                    else otherInfo.append(getContext().getString(R.string.relaxing));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                otherInfo.append("\n");
                            }

                            if (detailInfo.getPrice() != 0) {//获取平均消费
                                otherInfo.append(getContext().getString(R.string.price)).append(detailInfo.getPrice()).append("元\n");
                            }

                            mainFragment.infoOthers.setText(otherInfo.toString());//设置其它信息
                        }

                    } else {//间接的详细信息搜索
                        //由于一般只传入一个uid，列表里往往只有一个搜索结果，即使这里用了循环语句
                        for (PoiDetailInfo detailInfo : poiDetailResult.getPoiDetailInfoList()) {
                            if (isUidNotInSearList(detailInfo.getUid())) {
                                SearchItem searchItem = new SearchItem();

                                searchItem.setUid(detailInfo.getUid());//获取并设置Uid
                                searchItem.setTargetName(detailInfo.getName());//获取并设置目标名
                                searchItem.setAddress(detailInfo.getAddress());//获取并设置目标地址

                                LatLng tLatLng = detailInfo.getLocation();//获取目标坐标
                                searchItem.setLatLng(tLatLng);//设置目标坐标

                                //设置定位点到目标点的距离（单位：m，结果除以1000转化为km，保留两位小数）
                                searchItem.setDistance(BigDecimal.valueOf
                                        (DistanceUtil.getDistance(mainFragment.latLng, tLatLng) / 1000)
                                        .setScale(2, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue());

                                //添加搜索到的不同uid的内容添加到searchItems
                                mainFragment.searchList.add(searchItem);
                            }
                        }

                        //按距离升序排序
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            mainFragment.searchList.sort(new Comparator<SearchItem>() {
                                @Override
                                public int compare(SearchItem o1, SearchItem o2) {
                                    return o1.getDistance().compareTo(o2.getDistance());
                                }
                            });
                        }
                    }

                    mainFragment.searchAdapter.notifyDataSetChanged();//通知searchAdapter更新
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

        mainFragment.mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
    }

    //判断Uid是否不存在于搜索列表中
    private boolean isUidNotInSearList(String uid) {
        for (SearchItem searchItem : mainFragment.searchList) {
            if (uid.equals(searchItem.getUid())) {
                return false;
            }
        }
        return true;
    }

}
