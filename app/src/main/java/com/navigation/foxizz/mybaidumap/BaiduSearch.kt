package com.navigation.foxizz.mybaidumap

import Constants
import android.annotation.SuppressLint
import android.os.Build
import android.view.*
import base.foxizz.dsp
import base.foxizz.getString
import base.foxizz.mlh
import base.foxizz.util.*
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
import kotlinx.android.synthetic.main.fragment_main.*
import java.math.BigDecimal
import java.util.*

/**
 * 搜索模块
 *
 * @param mainFragment 地图页
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

    val mSearchList = ArrayList<SearchItem>() //搜索列表
    val mSearchCityList = ArrayList<String>() //要进行POI搜索的城市列表
    var mSearchContent = "" //搜索内容
    var mSearchType = 0//使用的搜索类型

    var isSearching = false//是否正在搜索
    var isFirstDetailSearch = false//是否是第一次详细信息搜索
    val uidList = HashSet<String>() //uid集合

    var mTotalPage = 0 //总页数
    var mCurrentPage = 0 //当前页

    init {
        initSugSearch()
        initPoiSearch()
    }

    /**
     * 开始搜索
     */
    fun startSearch() {
        if (isSearching) {
            showToast(R.string.wait_for_search_result)
            return
        }
        if (!NetworkUtil.isNetworkConnected) { //没有网络连接
            showToast(R.string.network_error)
            return
        }
        if (NetworkUtil.isAirplaneModeEnable) { //没有关飞行模式
            showToast(R.string.close_airplane_mode)
            return
        }
        mainFragment.run {
            if (!searchLayoutFlag) return //如果搜索布局没有展开则不进行搜索
            mSearchContent = et_search.text.toString()
            if (mSearchContent.isEmpty()) { //如果搜索内容为空
                if (!isHistorySearchResult) { //如果不是搜索历史记录
                    isHistorySearchResult = true //现在是搜索历史记录了
                    SearchDataHelper.initSearchData(this) //初始化搜索记录
                }
                return
            }
            ThreadUtil.execute {
                var searchCity = mBaiduLocation.mCity
                //如果存储的城市不为空，则换用存储的城市
                val saveCity = SPUtil.getString(Constants.DESTINATION_CITY)
                if (saveCity.isNotEmpty()) searchCity = saveCity
                if (searchCity.isEmpty()) {
                    checkPermissionAndLocate() //申请权限，获得权限后定位
                    return@execute
                }

                //如果是省份，则搜索城市列表设置为省份内所有的城市，否则设置为单个城市
                mSearchCityList.clear()
                if (CityUtil.isProvinceName(searchCity))
                    mSearchCityList.addAll(CityUtil.getCityList(searchCity))
                else mSearchCityList.add(searchCity)

                mlh.post {
                    if (!searchExpandFlag) { //如果搜索抽屉收起
                        searchExpandFlag = true //设置搜索抽屉为展开
                        expandSearchDrawer(true) //展开搜索抽屉
                    }
                    takeBackKeyboard() //收回键盘
                    mBaiduMap.clear() //清空地图上的所有标记点和绘制的路线
                    recycler_search_result.stopScroll() //停止信息列表滑动
                    mSearchList.clear() //清空searchList
                    mSearchAdapter.updateList() //通知adapter更新

                    //滚动到顶部
                    recycler_search_result.stopScroll()
                    recycler_search_result.scrollToPosition(0)
                    //加载搜索信息
                    include_search_loading.visibility = View.VISIBLE
                    recycler_search_result.visibility = View.GONE
                }

                isHistorySearchResult = false //已经不是搜索历史记录了
                mCurrentPage = 0 //页数归零
                if (dsp.getBoolean(Constants.KEY_INTELLIGENT_SEARCH, true))
                    mBaiduSearch.mSearchType = CITY_SEARCH //设置搜索类型为城市内搜索
                else mBaiduSearch.mSearchType = CONSTRAINT_CITY_SEARCH //设置搜索类型为强制城市内搜索
                startSearch(mCurrentPage) //开始搜索
            }
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
            if (mSearchType == CITY_SEARCH || mSearchType == CONSTRAINT_CITY_SEARCH) {
                if (currentPage == 0) {
                    //开始Sug搜索
                    if (mSearchCityList.isNotEmpty()) {
                        mSuggestionSearch.requestSuggestion(
                            SuggestionSearchOption()
                                .city(mSearchCityList[0])
                                .keyword(mSearchContent)
                        )
                    }
                } else {
                    if (mSearchCityList.isNotEmpty()) {
                        //开始POI城市内搜索
                        mPoiSearch.searchInCity(
                            PoiCitySearchOption()
                                .city(mSearchCityList[0])
                                .keyword(mSearchContent)
                                .pageNum(currentPage)
                                .pageCapacity(PAGE_CAPACITY)
                        )
                    }
                }
            } else if (mSearchType == NEARBY_SEARCH) {
                //开始周边搜索
                mPoiSearch.searchNearby(
                    PoiNearbySearchOption()
                        .location(mainFragment.mBaiduLocation.mLatLng)
                        .radius(NEARBY_SEARCH_DISTANCE)
                        .keyword(mSearchContent)
                        .pageNum(currentPage)
                        .pageCapacity(PAGE_CAPACITY)
                )
            }
        }
    }

    //初始化Sug搜索
    private fun initSugSearch() {
        //获取Sug搜索实例
        mSuggestionSearch = SuggestionSearch.newInstance()
        val suggestionResultListener = OnGetSuggestionResultListener { suggestionResult ->
            ThreadUtil.execute {
                //将Sug获取到的uid录入uid列表
                val suggestionInfoList = suggestionResult.allSuggestions
                if (suggestionInfoList != null && suggestionInfoList.size > 0) {
                    suggestionResult.allSuggestions.forEach {
                        uidList.add(it.getUid())
                    }
                }
                if (uidList.size == 0) {
                    mainFragment.isHistorySearchResult = true //现在是搜索历史记录了
                    SearchDataHelper.initSearchData(mainFragment) //初始化搜索记录
                    showToast(R.string.find_nothing)
                } else {
                    //要进行详细搜索的所有内容
                    uidList.forEach {
                        //uid的集合，最多可以传入10个uid，多个uid之间用英文逗号分隔。
                        mPoiSearch.searchPoiDetail(PoiDetailSearchOption().poiUids(it))
                    }
                }

                //开始城市内搜索
                mPoiSearch.searchInCity(
                    PoiCitySearchOption()
                        .city(mSearchCityList[0])
                        .keyword(mSearchContent)
                        .pageCapacity(PAGE_CAPACITY)
                )
            }
        }
        mSuggestionSearch.setOnGetSuggestionResultListener(suggestionResultListener)
    }

    //初始化POI搜索
    private fun initPoiSearch() {
        //获取POI搜索实例
        mPoiSearch = PoiSearch.newInstance()
        val poiSearchResultListener = object : OnGetPoiSearchResultListener {
            override fun onGetPoiResult(poiResult: PoiResult) {
                if (mCurrentPage == 0) {
                    //POI信息加载完成
                    mainFragment.include_search_loading.visibility = View.GONE
                    mainFragment.recycler_search_result.visibility = View.VISIBLE
                }
                if (poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    //城市内搜索不到内容时切换到别的城市继续搜索
                    if (mSearchType == CITY_SEARCH) {
                        if (poiResult.suggestCityList != null
                            && poiResult.suggestCityList.size > 0
                        ) {
                            mSearchType = OTHER_CITY_SEARCH
                            poiResult.suggestCityList.forEach {
                                //开始别的城市内搜索
                                mPoiSearch.searchInCity(
                                    PoiCitySearchOption()
                                        .city(it.city)
                                        .keyword(mSearchContent)
                                        .pageCapacity(PAGE_CAPACITY)
                                )
                            }
                        } else isSearching = false
                        return
                    }

                    //周边搜索不到内容时切换回城市内搜索
                    if (mSearchType == NEARBY_SEARCH) {
                        mSearchType = CONSTRAINT_CITY_SEARCH //设置搜索类型为强制城市内搜索

                        //开始城市内搜索
                        mPoiSearch.searchInCity(
                            PoiCitySearchOption()
                                .city(mSearchCityList[0])
                                .keyword(mSearchContent)
                                .pageNum(mCurrentPage)
                                .pageCapacity(PAGE_CAPACITY)
                        )
                    } else if (uidList.size == 0) {
                        isSearching = false
                        mainFragment.isHistorySearchResult = true //现在是搜索历史记录了
                        SearchDataHelper.initSearchData(mainFragment) //初始化搜索记录
                        showToast(R.string.find_nothing)
                    }
                    return
                }
                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) { //检索结果正常返回
                    //如果目标数量小于预设值或搜索类型为其它城市搜索或周边搜索或强制城市内搜索
                    if (poiResult.totalPoiNum < TO_NEARBY_SEARCH_MIN_NUM
                        || mSearchType == OTHER_CITY_SEARCH
                        || mSearchType == NEARBY_SEARCH
                        || mSearchType == CONSTRAINT_CITY_SEARCH
                    ) {
                        mTotalPage = poiResult.totalPageNum
                        mCurrentPage = poiResult.currentPageNum

                        /*
                        PoiOverlay(mainFragment.mBaiduMap).run {
                            mainFragment.mBaiduMap.setOnMarkerClickListener(this)
                            setData(poiResult) //设置POI数据
                            addToMap() //将所有的overlay添加到地图上
                            zoomToSpan() //移动地图到目标点上
                        }
                        */

                        //新建searchItems，用于保存本次的搜索结果
                        val searchItems = ArrayList<SearchItem>()

                        //将POI获取到的信息录入搜索结果列表
                        poiResult.allPoi.forEach {
                            if (!uidList.contains(it.uid) //uid不重复且类型不是下面那些
                                && it.type != PoiInfo.POITYPE.BUS_STATION
                                && it.type != PoiInfo.POITYPE.BUS_LINE
                                && it.type != PoiInfo.POITYPE.SUBWAY_STATION
                                && it.type != PoiInfo.POITYPE.SUBWAY_LINE
                                && isUidNotInSearList(it.uid)
                            ) {
                                //添加搜索到的不同uid的内容添加到searchItems
                                searchItems.add(SearchItem().apply {
                                    uid = it.uid //获取并设置Uid
                                    val tLatLng = it.location //获取目标坐标
                                    latLng = tLatLng //设置目标坐标
                                    targetName = it.name //获取并设置目标名
                                    address = it.address //获取并设置目标地址

                                    //设置定位点到目标点的距离（单位：m，结果除以1000转化为km，保留两位小数）
                                    distance = BigDecimal
                                        .valueOf(
                                            DistanceUtil.getDistance(
                                                mainFragment.mBaiduLocation.mLatLng,
                                                tLatLng
                                            ) / 1000
                                        )
                                        .setScale(2, BigDecimal.ROUND_HALF_UP)
                                        .toDouble()
                                })
                            }
                        }

                        //周边搜索按距离升序排序
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                            && mSearchType == NEARBY_SEARCH
                        ) {
                            searchItems.sortWith { o1, o2 ->
                                o1.distance.compareTo(o2.distance)
                            }
                        }
                        mSearchList.addAll(searchItems) //将所有searchItem添加到searchList中
                        if (mCurrentPage == 0) { //第0页全部排序
                            //周边搜索按距离升序排序
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                                && mSearchType == NEARBY_SEARCH
                            ) {
                                mSearchList.sortWith { o1, o2 ->
                                    o1.distance.compareTo(o2.distance)
                                }
                            }
                        }
                        mainFragment.mSearchAdapter.updateList() //通知adapter更新
                        mCurrentPage++ //当前页+1
                        isSearching = false
                    } else {
                        mSearchType = NEARBY_SEARCH //设置搜索类型为周边搜索

                        //开始周边搜索
                        mPoiSearch.searchNearby(
                            PoiNearbySearchOption()
                                .location(mainFragment.mBaiduLocation.mLatLng)
                                .radius(NEARBY_SEARCH_DISTANCE)
                                .keyword(mSearchContent)
                                .pageNum(mCurrentPage)
                                .pageCapacity(PAGE_CAPACITY)
                        )
                    }
                }
            }

            override fun onGetPoiDetailResult(poiDetailResult: PoiDetailSearchResult) {
                if (isFirstDetailSearch) { //如果是第一次详细信息搜索
                    isFirstDetailSearch = false

                    //详细信息加载完成
                    mainFragment.include_search_info_loading.visibility = View.GONE
                    mainFragment.sv_search_info.visibility = View.VISIBLE
                    if (poiDetailResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) return
                }
                if (poiDetailResult.error == SearchResult.ERRORNO.NO_ERROR) { //检索结果正常返回
                    //直接的详细信息搜索
                    if (mSearchType == DETAIL_SEARCH || mSearchType == DETAIL_SEARCH_ALL) {
                        //由于一般只传入一个uid，列表里往往只有一个搜索结果，即使这里用了循环语句
                        poiDetailResult.poiDetailInfoList.forEach { detailInfo ->
                            //将结果保存到数据库
                            if (mSearchType == DETAIL_SEARCH)
                                SearchDataHelper.insertOrUpdateSearchData(detailInfo)

                            val latLng = detailInfo.location //获取目标坐标

                            //获取定位点到目标点的距离（单位：m，结果除以1000转化为km）
                            var distance = DistanceUtil.getDistance(
                                mainFragment.mBaiduLocation.mLatLng, latLng
                            ) / 1000
                            //保留两位小数
                            val bd = BigDecimal(distance)
                            distance = bd.setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()

                            //更新搜索结果列表
                            SearchItem().let {
                                it.uid = detailInfo.uid //获取并设置目标uid
                                it.targetName = detailInfo.name //获取并设置目标名
                                it.address = detailInfo.address //获取并设置目标地址
                                it.latLng = latLng //设置目标坐标
                                it.distance = distance

                                //寻找搜索列表中Uid相同的item
                                for (i in mSearchList.indices) {
                                    if (mSearchList[i].uid == detailInfo.uid) {
                                        if (mSearchType == DETAIL_SEARCH) {
                                            mSearchList.removeAt(i) //移除原本位置的item
                                            mSearchList.add(0, it) //将其添加到头部
                                        } else if (mSearchType == DETAIL_SEARCH_ALL) {
                                            mSearchList[i] = it //直接修改原位置的item
                                        }
                                        break
                                    }
                                }
                            }

                            if (mSearchType == DETAIL_SEARCH_ALL) return //详细搜索全部不更新详细信息布局

                            //获取其它信息
                            val otherInfo = StringBuilder()
                            otherInfo.run {
                                //获取联系方式
                                if (detailInfo.telephone.isNotEmpty())
                                    append(
                                        getString(R.string.phone_number), detailInfo.telephone, "\n"
                                    )

                                //获取营业时间
                                if (detailInfo.getShopHours().isNotEmpty()) {
                                    append(getString(R.string.shop_time), detailInfo.getShopHours())
                                    var isInShopHour = 0
                                    try {
                                        TimeUtil.run {
                                            val nowTime = parse(
                                                format(Date(), FORMATION_Hm),
                                                FORMATION_Hm
                                            )
                                            //去除中文和头尾的空格
                                            val shopHours = detailInfo.getShopHours()
                                                .removeChinese().trim().split(",")
                                            shopHours.forEach {
                                                val time = it.split("-")
                                                val startTime = parse(time[0], FORMATION_Hm)
                                                val endTime = parse(time[1], FORMATION_Hm)
                                                if (isInTime(nowTime, startTime, endTime))
                                                    isInShopHour = 1
                                            }
                                        }
                                    } catch (e: Exception) {
                                        isInShopHour = -1 //未知
                                    }
                                    if (isInShopHour == 1)
                                        append(" ", getString(R.string.shopping))
                                    else if (isInShopHour == 0)
                                        append(" ", getString(R.string.relaxing))
                                    append("\n")
                                }

                                if (detailInfo.getPrice() != 0.0) //获取平均消费
                                    append(getString(R.string.price), detailInfo.getPrice(), "元\n")
                            }

                            //更新详细信息布局
                            mainFragment.run {
                                tv_search_target_name.text = detailInfo.name
                                tv_search_address.text = detailInfo.address
                                @SuppressLint("SetTextI18n")
                                tv_search_distance.text = "${distance}km"
                                tv_search_others.text = otherInfo
                            }
                        }
                    } else { //间接的详细信息搜索
                        //由于一般只传入一个uid，列表里往往只有一个搜索结果，即使这里用了循环语句
                        poiDetailResult.poiDetailInfoList.forEach { detailInfo ->
                            if (isUidNotInSearList(detailInfo.uid)) {
                                SearchItem().let {
                                    it.uid = detailInfo.uid //获取并设置Uid
                                    it.targetName = detailInfo.name //获取并设置目标名
                                    it.address = detailInfo.address //获取并设置目标地址
                                    val tLatLng = detailInfo.location //获取目标坐标
                                    it.latLng = tLatLng //设置目标坐标

                                    //设置定位点到目标点的距离（单位：m，结果除以1000转化为km，保留两位小数）
                                    it.distance = BigDecimal
                                        .valueOf(
                                            DistanceUtil.getDistance(
                                                mainFragment.mBaiduLocation.mLatLng,
                                                tLatLng
                                            ) / 1000
                                        )
                                        .setScale(2, BigDecimal.ROUND_HALF_UP)
                                        .toDouble()

                                    //添加搜索到的不同uid的内容添加到searchItems
                                    mSearchList.add(it)
                                }
                            }
                        }

                        //按距离升序排序
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            mSearchList.sortWith { o1, o2 ->
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
        mSearchList.forEach { if (uid == it.uid) return false }
        return true
    }
}