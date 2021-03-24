package com.navigation.foxizz.mybaidumap

import android.annotation.SuppressLint
import android.os.Build
import android.view.*
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.poi.*
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener
import com.baidu.mapapi.search.sug.SuggestionSearch
import com.baidu.mapapi.search.sug.SuggestionSearchOption
import com.baidu.mapapi.utils.DistanceUtil
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.fragment.MainFragment
import com.navigation.foxizz.data.*
import com.navigation.foxizz.util.*
import java.math.BigDecimal
import java.util.*

/**
 * 搜索模块
 */
class BaiduSearch(private val mainFragment: MainFragment) {
    companion object {
        const val TO_NEARBY_SEARCH_MIN_NUM = 64 //触发周边搜索需要的最小目标点数量
        const val NEARBY_SEARCH_DISTANCE = 5 * 1000 //周边搜索的距离

        const val CITY_SEARCH = 0 //城市内搜索
        const val OTHER_CITY_SEARCH = 1 //其它城市搜索，使用城市内搜索不到内容时启用
        const val NEARBY_SEARCH = 2 //周边搜索，使用城市内搜索到的内容过多时启用
        const val CONSTRAINT_CITY_SEARCH = 3 //强制城市内搜索，使用城市内搜索不会再自动转为周边搜索
        const val DETAIL_SEARCH = 4 //直接详细信息搜索，一般直接用uid搜索
        const val DETAIL_SEARCH_ALL = 5 //详细搜索全部，用于数据库录入

        const val PAGE_CAPACITY = 16 //每页的容量
    }

    lateinit var mSuggestionSearch: SuggestionSearch //Sug搜索
    lateinit var mPoiSearch: PoiSearch //POI搜索

    val searchCityList = ArrayList<String>() //要进行POI搜索的城市列表
    var searchContent = "" //搜索内容

    var searchType = 0//使用的搜索类型
    var isSearching = false//是否正在搜索
    var isFirstDetailSearch = false//是否是第一次详细信息搜索
    val uidList: MutableSet<String> = HashSet() //uid集合

    var mTotalPage = 0 //总页数
    var mCurrentPage = 0 //当前页

    /**
     * 开始搜索
     */
    fun startSearch() {
        if (isSearching) {
            R.string.wait_for_search_result.showToast()
            return
        }
        if (!NetworkUtil.isNetworkConnected) { //没有网络连接
            R.string.network_error.showToast()
            return
        }
        if (NetworkUtil.isAirplaneModeOn) { //没有关飞行模式
            R.string.close_airplane_mode.showToast()
            return
        }
        if (!mainFragment.searchExpandFlag) return  //如果搜索布局没有展开则不进行搜索
        searchContent = mainFragment.etSearch.text.toString()
        if (searchContent.isEmpty()) { //如果搜索内容为空
            if (!mainFragment.isHistorySearchResult) { //如果不是搜索历史记录
                mainFragment.isHistorySearchResult = true //现在是搜索历史记录了
                SearchDataHelper.initSearchData(mainFragment) //初始化搜索记录
            }
            return
        }
        ThreadUtil.execute {
            var searchCity = mainFragment.mBaiduLocation.mCity
            //如果存储的城市不为空，则换用存储的城市
            val saveCity = SPHelper.getString(Constants.DESTINATION_CITY, "")
            if (saveCity.isNotEmpty()) searchCity = saveCity
            if (searchCity.isEmpty()) {
                mainFragment.requestPermission() //申请权限，获得权限后定位
                return@execute
            }

            //如果是省份，则搜索城市列表设置为省份内所有的城市，否则设置为单个城市
            searchCityList.clear()
            if (CityUtil.checkProvinceName(searchCity)) {
                searchCityList.addAll(CityUtil.getCityList(searchCity))
            } else {
                searchCityList.add(searchCity)
            }
            mainFragment.requireActivity().runOnUiThread {
                if (!mainFragment.searchExpandFlag) { //如果搜索抽屉收起
                    mainFragment.searchExpandFlag = true //设置搜索抽屉为展开
                    mainFragment.expandSearchDrawer(true) //展开搜索抽屉
                }
                mainFragment.takeBackKeyboard() //收回键盘
                mainFragment.mBaiduMap.clear() //清空地图上的所有标记点和绘制的路线
                mainFragment.mRecyclerSearchResult.stopScroll() //停止信息列表滑动
                mainFragment.searchList.clear() //清空searchList
                mainFragment.mSearchAdapter.updateList() //通知adapter更新

                //滚动到顶部
                mainFragment.mRecyclerSearchResult.stopScroll()
                mainFragment.mRecyclerSearchResult.scrollToPosition(0)
                //加载搜索信息
                mainFragment.llSearchLoading.visibility = View.VISIBLE
                mainFragment.mRecyclerSearchResult.visibility = View.GONE
            }
            mainFragment.isHistorySearchResult = false //已经不是搜索历史记录了
            mCurrentPage = 0 //页数归零
            if (mainFragment.mSharedPreferences.getBoolean(Constants.KEY_INTELLIGENT_SEARCH, true))
                mainFragment.mBaiduSearch.searchType = CITY_SEARCH //设置搜索类型为城市内搜索
            else mainFragment.mBaiduSearch.searchType = CONSTRAINT_CITY_SEARCH //设置搜索类型为强制城市内搜索
            startSearch(mCurrentPage) //开始搜索
        }
    }

    /**
     * 开始搜索
     *
     * @param currentPage 第几页
     */
    @Synchronized
    fun startSearch(currentPage: Int) {
        ThreadUtil.execute {
            isSearching = true
            mCurrentPage = currentPage //当前页
            uidList.clear() //清空uid集合
            if (searchType == CITY_SEARCH || searchType == CONSTRAINT_CITY_SEARCH) {
                if (currentPage == 0) {
                    //开始Sug搜索
                    if (searchCityList.isNotEmpty()) {
                        mSuggestionSearch.requestSuggestion(SuggestionSearchOption()
                                .city(searchCityList[0])
                                .keyword(searchContent))
                    }
                } else {
                    if (searchCityList.isNotEmpty()) {
                        //开始POI城市内搜索
                        mPoiSearch.searchInCity(PoiCitySearchOption()
                                .city(searchCityList[0])
                                .keyword(searchContent)
                                .pageNum(currentPage)
                                .pageCapacity(PAGE_CAPACITY))
                    }
                }
            } else if (searchType == NEARBY_SEARCH) {
                //开始周边搜索
                mPoiSearch.searchNearby(PoiNearbySearchOption()
                        .location(mainFragment.mBaiduLocation.mLatLng)
                        .radius(NEARBY_SEARCH_DISTANCE)
                        .keyword(searchContent)
                        .pageNum(currentPage)
                        .pageCapacity(PAGE_CAPACITY))
            }
        }
    }

    /*
     * 初始化搜索
     */
    fun initSearch() {
        initSugSearch()
        initPoiSearch()
    }

    //初始化Sug搜索
    private fun initSugSearch() {
        //获取Sug搜索实例
        mSuggestionSearch = SuggestionSearch.newInstance()
        val suggestionResultListener = OnGetSuggestionResultListener { suggestionResult ->
            ThreadUtil.execute { //将Sug获取到的uid录入uid列表
                val suggestionInfoList = suggestionResult.allSuggestions
                if (suggestionInfoList != null && suggestionInfoList.size > 0) {
                    for (suggestionInfo in suggestionResult.allSuggestions) {
                        uidList.add(suggestionInfo.getUid())
                    }
                }
                if (uidList.size == 0) {
                    mainFragment.isHistorySearchResult = true //现在是搜索历史记录了
                    SearchDataHelper.initSearchData(mainFragment) //初始化搜索记录
                    R.string.find_nothing.showToast()
                } else {
                    //要进行详细搜索的所有内容
                    for (uid in uidList) {
                        //uid的集合，最多可以传入10个uid，多个uid之间用英文逗号分隔。
                        mPoiSearch.searchPoiDetail( //开始详细信息搜索
                                PoiDetailSearchOption().poiUids(uid))
                    }
                }

                //开始城市内搜索
                mPoiSearch.searchInCity(PoiCitySearchOption()
                        .city(searchCityList[0])
                        .keyword(searchContent)
                        .pageCapacity(PAGE_CAPACITY))
            }
        }
        mSuggestionSearch.setOnGetSuggestionResultListener(suggestionResultListener)
    }

    //初始化POI搜索
    private fun initPoiSearch() {
        //获取POI搜索实例
        mPoiSearch = PoiSearch.newInstance()
        val poiSearchResultListener: OnGetPoiSearchResultListener = object : OnGetPoiSearchResultListener {
            override fun onGetPoiResult(poiResult: PoiResult) {
                if (mCurrentPage == 0) {
                    //POI信息加载完成
                    mainFragment.llSearchLoading.visibility = View.GONE
                    mainFragment.mRecyclerSearchResult.visibility = View.VISIBLE
                }
                if (poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    //城市内搜索不到内容时切换到别的城市继续搜索
                    if (searchType == CITY_SEARCH) {
                        if (poiResult.suggestCityList != null && poiResult.suggestCityList.size > 0) {
                            searchType = OTHER_CITY_SEARCH
                            for (cityInfo in poiResult.suggestCityList) {
                                //开始别的城市内搜索
                                mPoiSearch.searchInCity(PoiCitySearchOption()
                                        .city(cityInfo.city)
                                        .keyword(searchContent)
                                        .pageCapacity(PAGE_CAPACITY))
                            }
                        } else {
                            isSearching = false
                        }
                        return
                    }

                    //周边搜索不到内容时切换回城市内搜索
                    if (searchType == NEARBY_SEARCH) {
                        searchType = CONSTRAINT_CITY_SEARCH //设置搜索类型为强制城市内搜索

                        //开始城市内搜索
                        mPoiSearch.searchInCity(PoiCitySearchOption()
                                .city(searchCityList[0])
                                .keyword(searchContent)
                                .pageNum(mCurrentPage)
                                .pageCapacity(PAGE_CAPACITY))
                    } else if (uidList.size == 0) {
                        isSearching = false
                        mainFragment.isHistorySearchResult = true //现在是搜索历史记录了
                        SearchDataHelper.initSearchData(mainFragment) //初始化搜索记录
                        R.string.find_nothing.showToast()
                    }
                    return
                }
                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) { //检索结果正常返回
                    //如果目标数量小于预设值或搜索类型为其它城市搜索或周边搜索或强制城市内搜索
                    if (poiResult.totalPoiNum < TO_NEARBY_SEARCH_MIN_NUM || searchType == OTHER_CITY_SEARCH || searchType == NEARBY_SEARCH || searchType == CONSTRAINT_CITY_SEARCH) {
                        mTotalPage = poiResult.totalPageNum
                        mCurrentPage = poiResult.currentPageNum

                        /*
                        val poiOverlay = PoiOverlay(mainFragment.mBaiduMap)
                        mainFragment.mBaiduMap.setOnMarkerClickListener(poiOverlay)
                        poiOverlay.setData(poiResult) //设置POI数据
                        poiOverlay.addToMap() //将所有的overlay添加到地图上
                        poiOverlay.zoomToSpan() //移动地图到目标点上
                        */

                        //新建searchItems，用于保存本次的搜索结果
                        val searchItems = ArrayList<SearchItem>()

                        //将POI获取到的信息录入搜索结果列表
                        for (poiInfo in poiResult.allPoi) {
                            if (!uidList.contains(poiInfo.getUid()) //uid不重复且类型不是下面那些
                                    && poiInfo.getType() != PoiInfo.POITYPE.BUS_STATION && poiInfo.getType() != PoiInfo.POITYPE.BUS_LINE && poiInfo.getType() != PoiInfo.POITYPE.SUBWAY_STATION && poiInfo.getType() != PoiInfo.POITYPE.SUBWAY_LINE && isUidNotInSearList(poiInfo.getUid())) {
                                val searchItem = SearchItem()
                                searchItem.uid = poiInfo.getUid() //获取并设置Uid
                                searchItem.targetName = poiInfo.getName() //获取并设置目标名
                                searchItem.address = poiInfo.getAddress() //获取并设置目标地址
                                val tLatLng = poiInfo.getLocation() //获取目标坐标
                                searchItem.latLng = tLatLng //设置目标坐标

                                //设置定位点到目标点的距离（单位：m，结果除以1000转化为km，保留两位小数）
                                searchItem.distance = BigDecimal.valueOf(DistanceUtil.getDistance(mainFragment.mBaiduLocation.mLatLng, tLatLng) / 1000)
                                        .setScale(2, BigDecimal.ROUND_HALF_UP)
                                        .toDouble()

                                //添加搜索到的不同uid的内容添加到searchItems
                                searchItems.add(searchItem)
                            }
                        }

                        //周边搜索按距离升序排序
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                                && searchType == NEARBY_SEARCH) {
                            searchItems.sortWith { o1, o2 ->
                                o1.distance.compareTo(o2.distance)
                            }
                        }
                        mainFragment.searchList.addAll(searchItems) //将所有searchItem添加到searchList中
                        if (mCurrentPage == 0) { //第0页全部排序
                            //周边搜索按距离升序排序
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                                    && searchType == NEARBY_SEARCH) {
                                mainFragment.searchList.sortWith { o1, o2 ->
                                    o1.distance.compareTo(o2.distance)
                                }
                            }
                        }
                        mainFragment.mSearchAdapter.updateList() //通知adapter更新
                        mCurrentPage++ //当前页+1
                        isSearching = false
                    } else {
                        searchType = NEARBY_SEARCH //设置搜索类型为周边搜索

                        //开始周边搜索
                        mPoiSearch.searchNearby(PoiNearbySearchOption()
                                .location(mainFragment.mBaiduLocation.mLatLng)
                                .radius(NEARBY_SEARCH_DISTANCE)
                                .keyword(searchContent)
                                .pageNum(mCurrentPage)
                                .pageCapacity(PAGE_CAPACITY))
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onGetPoiDetailResult(poiDetailResult: PoiDetailSearchResult) {
                if (isFirstDetailSearch) { //如果是第一次详细信息搜索
                    isFirstDetailSearch = false

                    //详细信息加载完成
                    mainFragment.llSearchInfoLoading.visibility = View.GONE
                    mainFragment.svSearchInfo.visibility = View.VISIBLE
                    if (poiDetailResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) return
                }
                if (poiDetailResult.error == SearchResult.ERRORNO.NO_ERROR) { //检索结果正常返回
                    //直接的详细信息搜索
                    if (searchType == DETAIL_SEARCH || searchType == DETAIL_SEARCH_ALL) {
                        //由于一般只传入一个uid，列表里往往只有一个搜索结果，即使这里用了循环语句
                        for (detailInfo in poiDetailResult.poiDetailInfoList) {
                            //将结果保存到数据库
                            if (searchType == DETAIL_SEARCH) SearchDataHelper.insertOrUpdateSearchData(detailInfo)

                            //更新搜索结果列表
                            val searchItem = SearchItem()
                            searchItem.uid = detailInfo.uid //获取并设置目标uid
                            searchItem.targetName = detailInfo.name //获取并设置目标名
                            searchItem.address = detailInfo.address //获取并设置目标地址
                            val latLng = detailInfo.location //获取目标坐标
                            searchItem.latLng = latLng //设置目标坐标

                            //获取定位点到目标点的距离（单位：m，结果除以1000转化为km）
                            var distance = DistanceUtil.getDistance(mainFragment.mBaiduLocation.mLatLng, latLng) / 1000
                            //保留两位小数
                            val bd = BigDecimal(distance)
                            distance = bd.setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
                            searchItem.distance = distance

                            //寻找搜索列表中Uid相同的item
                            for (i in mainFragment.searchList.indices) {
                                if (mainFragment.searchList[i].uid == detailInfo.uid) {
                                    if (searchType == DETAIL_SEARCH) {
                                        mainFragment.searchList.removeAt(i) //移除原本位置的item
                                        mainFragment.searchList.add(0, searchItem) //将其添加到头部
                                    } else if (searchType == DETAIL_SEARCH_ALL) {
                                        mainFragment.searchList[i] = searchItem //直接修改原位置的item
                                    }
                                    break
                                }
                            }
                            if (searchType == DETAIL_SEARCH_ALL) return  //详细搜索全部不更新详细信息布局

                            //获取其它信息
                            val otherInfo = StringBuilder()

                            //获取联系方式
                            if (detailInfo.telephone.isNotEmpty()) {
                                try {
                                    otherInfo.append(mainFragment.getString(R.string.phone_number)).append(detailInfo.telephone).append("\n")
                                } catch (ignored: Exception) {
                                }
                            }

                            //获取营业时间
                            if (detailInfo.getShopHours().isNotEmpty()) {
                                otherInfo.append(mainFragment.getString(R.string.shop_time)).append(detailInfo.getShopHours())
                                var isInShopHour = 0
                                try {
                                    val nowTime = TimeUtil.parse(TimeUtil.format(Date(),
                                            TimeUtil.FORMATION_Hm), TimeUtil.FORMATION_Hm)
                                    val shopHours = detailInfo.getShopHours() //去除中文和头尾的空格
                                            .replace("[\\u4e00-\\u9fa5]", "").trim()
                                            .split(",")
                                    for (shopHour in shopHours) {
                                        val time = shopHour.split("-")
                                        val startTime = TimeUtil.parse(time[0], TimeUtil.FORMATION_Hm)
                                        val endTime = TimeUtil.parse(time[1], TimeUtil.FORMATION_Hm)
                                        if (TimeUtil.isEffectiveDate(nowTime, startTime, endTime)) {
                                            isInShopHour = 1
                                        }
                                    }
                                } catch (ignored: Exception) {
                                    isInShopHour = -1 //未知
                                }
                                if (isInShopHour == 1)
                                    otherInfo.append(" ").append(mainFragment.getString(R.string.shopping))
                                else if (isInShopHour == 0)
                                    otherInfo.append(" ").append(mainFragment.getString(R.string.relaxing))
                                otherInfo.append("\n")
                            }
                            if (detailInfo.getPrice() != 0.0) { //获取平均消费
                                otherInfo.append(mainFragment.getString(R.string.price)).append(detailInfo.getPrice()).append("元\n")
                            }

                            //更新详细信息布局
                            mainFragment.tvSearchTargetName.text = detailInfo.name
                            mainFragment.tvSearchAddress.text = detailInfo.address
                            mainFragment.tvSearchDistance.text = distance.toString() + "km"
                            mainFragment.tvSearchOthers.text = otherInfo.toString()
                        }
                    } else { //间接的详细信息搜索
                        //由于一般只传入一个uid，列表里往往只有一个搜索结果，即使这里用了循环语句
                        for (detailInfo in poiDetailResult.poiDetailInfoList) {
                            if (isUidNotInSearList(detailInfo.uid)) {
                                val searchItem = SearchItem()
                                searchItem.uid = detailInfo.uid //获取并设置Uid
                                searchItem.targetName = detailInfo.name //获取并设置目标名
                                searchItem.address = detailInfo.address //获取并设置目标地址
                                val tLatLng = detailInfo.location //获取目标坐标
                                searchItem.latLng = tLatLng //设置目标坐标

                                //设置定位点到目标点的距离（单位：m，结果除以1000转化为km，保留两位小数）
                                searchItem.distance = BigDecimal.valueOf(DistanceUtil
                                        .getDistance(mainFragment.mBaiduLocation.mLatLng, tLatLng) / 1000)
                                        .setScale(2, BigDecimal.ROUND_HALF_UP)
                                        .toDouble()

                                //添加搜索到的不同uid的内容添加到searchItems
                                mainFragment.searchList.add(searchItem)
                            }
                        }

                        //按距离升序排序
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            mainFragment.searchList.sortWith { o1, o2 ->
                                o1.distance.compareTo(o2.distance)
                            }
                        }
                    }
                    mainFragment.mSearchAdapter.updateList() //通知adapter更新
                }
            }

            override fun onGetPoiIndoorResult(poiIndoorResult: PoiIndoorResult) {}

            //已弃用的方法，但仍需实现
            override fun onGetPoiDetailResult(poiDetailResult: PoiDetailResult) {}
        }
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener)
    }

    //判断Uid是否不存在于搜索列表中
    private fun isUidNotInSearList(uid: String): Boolean {
        for (searchItem in mainFragment.searchList) {
            if (uid == searchItem.uid) {
                return false
            }
        }
        return true
    }
}