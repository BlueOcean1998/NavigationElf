package com.navigation.foxizz.activity.fragment

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.baidu.mapapi.map.*
import com.baidu.tts.client.SpeechSynthesizer
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.SettingsActivity
import com.navigation.foxizz.activity.adapter.SchemeAdapter
import com.navigation.foxizz.activity.adapter.SearchAdapter
import com.navigation.foxizz.data.*
import com.navigation.foxizz.mybaidumap.*
import com.navigation.foxizz.receiver.LocalReceiver
import com.navigation.foxizz.receiver.SystemReceiver
import com.navigation.foxizz.util.LayoutUtil
import com.navigation.foxizz.util.SettingUtil
import com.navigation.foxizz.util.showToast
import java.util.*

/**
 * 地图页
 */
class MainFragment : Fragment() {
    /*
     * 地图控件
     */
    private lateinit var mMapView: MapView
    lateinit var mBaiduMap: BaiduMap
    private lateinit var mUiSettings: UiSettings

    /*
     * 方向传感器
     */
    lateinit var mOrientationListener: MyOrientationListener

    /*
     * 定位相关
     */
    lateinit var mBaiduLocation: BaiduLocation

    /*
     * 搜索相关
     */
    lateinit var mBaiduSearch: BaiduSearch
    lateinit var llSearchLayout: LinearLayout //搜索布局
    lateinit var etSearch: EditText //搜索输入框
    lateinit var ibEmpty: ImageButton //清空按钮
    private lateinit var btSearch: Button //搜索按钮
    private lateinit var ibSearchExpand: ImageButton //搜索结果伸缩按钮
    var searchExpandFlag = false //搜索抽屉展开状态
    private lateinit var llSearchDrawer: LinearLayout //搜索抽屉
    lateinit var llSearchLoading: LinearLayout //搜索加载
    lateinit var mRecyclerSearchResult: RecyclerView //搜索结果
    val searchList = ArrayList<SearchItem>() //搜索列表
    private lateinit var mSearchLayoutManager: StaggeredGridLayoutManager //搜索布局管理器
    lateinit var mSearchAdapter: SearchAdapter //搜索适配器
    lateinit var llSearchInfoLayout: LinearLayout //详细信息布局
    lateinit var llSearchInfoLoading: LinearLayout //信息加载
    lateinit var svSearchInfo: ScrollView //详细信息布局的拖动条
    lateinit var tvSearchTargetName: TextView //目标名
    lateinit var tvSearchAddress: TextView //目标地址
    lateinit var tvSearchDistance: TextView //与目标的距离
    lateinit var tvSearchOthers: TextView //目标的其它信息（联系方式，营业时间等）
    var isHistorySearchResult = true //是否是搜索历史记录

    /*
     * 路线规划相关
     */
    lateinit var mBaiduRoutePlan: BaiduRoutePlan
    lateinit var llSelectLayout: LinearLayout //选择布局
    var mRoutePlanSelect = 0
    private lateinit var btSelect1: Button //选择驾车
    private lateinit var btSelect2: Button //选择步行
    private lateinit var btSelect3: Button //选择骑行
    private lateinit var btSelect4: Button //选择公交

    /**
     * 方案布局
     */
    var schemeExpandFlag = 0 //方案布局展开状态（0：未展开，1：方案列表，2：单个方案）
    lateinit var llSchemeDrawer: LinearLayout //方案抽屉
    lateinit var llSchemeLoading: LinearLayout //方案加载
    lateinit var recyclerSchemeResult: RecyclerView //方案结果
    lateinit var llSchemeInfoLayout: LinearLayout //方案信息布局
    private lateinit var svSchemeInfo: ScrollView //方案信息的拖动条
    lateinit var tvSchemeInfo: TextView //方案信息
    var mSchemeList = ArrayList<SchemeItem>() //方案列表
    lateinit var mSchemeLayoutManager: StaggeredGridLayoutManager //方案布局管理器
    lateinit var mSchemeAdapter: SchemeAdapter //方案适配器

    /*
     * 导航相关
     */
    lateinit var mBaiduNavigation: BaiduNavigation
    lateinit var llStartLayout: LinearLayout //开始导航布局
    private lateinit var btBack: Button //返回按钮
    var infoFlag = 0 //信息显示状态（0：路线，1：详细信息，2：交通选择）
    lateinit var btMiddle: Button //路线规划、详细信息切换按钮
    private lateinit var btStart: Button //开始导航按钮

    /*
     * 控制相关
     */
    private var mBodyLength = 0 //屏幕的长
    private var mBodyShort = 0 //屏幕的宽
    private lateinit var imm: InputMethodManager //键盘
    private lateinit var ibSettings: ImageButton //设置
    private lateinit var ibRefresh: ImageButton //刷新
    private lateinit var ibLocation: ImageButton //定位

    /*
     * 设置相关
     */
    private lateinit var mSystemReceiver: SystemReceiver //系统广播接收器
    private lateinit var mLocalReceiver: LocalReceiver //本地广播接收器
    private lateinit var mLocalBroadcastManager: LocalBroadcastManager //本地广播管理器

    /*
     * 数据相关
     */
    lateinit var mSharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        //获取默认设置
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        initMap(view) //初始化地图控件
        initSettings() //初始化偏好设置
        initSystemReceiver() //初始化系统广播接收器
        initLocalReceiver() //初始化本地广播接收器
        initView(view) //初始化控件
        initMyDirectionSensor() //初始化方向传感器

        mBaiduLocation = BaiduLocation(this) //初始化定位模块
        mBaiduSearch = BaiduSearch(this) //初始化搜索模块
        mBaiduSearch.initSearch() //初始化搜索目标信息
        mBaiduRoutePlan = BaiduRoutePlan(this) //初始化路线规划模块
        mBaiduRoutePlan.initRoutePlanSearch() //初始化路线规划
        mBaiduNavigation = BaiduNavigation(this) //初始化导航模块

        requestPermission() //申请权限，获得权限后定位

        return view
    }

    //管理地图的生命周期
    override fun onStart() {
        super.onStart()
        mBaiduMap.isMyLocationEnabled = true //开启定位
        mOrientationListener.start() //开启方向传感
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
        calculateWidthAndHeightOfScreen() //计算屏幕宽高，用于设置控件的高度
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(mSystemReceiver) //释放系统广播接收器实例
        mLocalBroadcastManager.unregisterReceiver(mLocalReceiver) //释放本地广播接收器实例

        mBaiduMap.isMyLocationEnabled = false //关闭定位
        mOrientationListener.stop() //停止方向传感

        //停止定位服务
        if (mBaiduLocation.mLocationClient.isStarted) {
            mBaiduLocation.mLocationClient.stop()
        }

        //释放地图、Sug搜索、POI搜索、路线规划、语音合成实例
        mMapView.onDestroy()
        mBaiduSearch.mSuggestionSearch.destroy()
        mBaiduSearch.mPoiSearch.destroy()
        mBaiduRoutePlan.mRoutePlanSearch.destroy()
        SpeechSynthesizer.getInstance().release()
    }

    //初始化地图控件
    private fun initMap(view: View) {
        //获取地图控件引用
        mMapView = view.findViewById(R.id.map_view)
        mBaiduMap = mMapView.map
        mUiSettings = mBaiduMap.uiSettings

        //配置定位图层显示方式，使用默认的定位图标，设置精确度圆的填充色和边框色
        //LocationMode定位模式有三种：普通模式，跟随模式，罗盘模式，在这使用普通模式
        val myLocationConfiguration = MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null, -0x55401001, -0x55603001)
        mBaiduMap.setMyLocationConfiguration(myLocationConfiguration)

        //mMapView.removeViewAt(1);//去除百度水印
        mMapView.scaleControlPosition = Point() //改变比例尺位置
        mMapView.zoomControlsPosition = Point() //改变缩放按钮位置

        //设置缩放等级
        val builder = MapStatus.Builder()
        builder.zoom(18.0f)
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()))

        //移动视角到最近的一条搜索记录
        SearchDataHelper.moveToLastSearchRecordLocation(this)

        /*
        //下载离线地图
        val mCity = SPHelper.getString(Constants.MY_CITY, "")
        if (mCity.isNotEmpty()) {
            //启动下载离线地图服务
            OfflineMapService.startService(requireActivity(), mCity)
        }
        */
    }

    /**
     * 设置地图类型
     */
    fun setMapType() {
        when (SPHelper.getString(Constants.MAP_TYPE, Constants.STANDARD_MAP)) {
            Constants.STANDARD_MAP -> {
                if (mBaiduMap.mapType != BaiduMap.MAP_TYPE_NORMAL)
                    mBaiduMap.mapType = BaiduMap.MAP_TYPE_NORMAL
                mBaiduMap.isTrafficEnabled = false
            }
            Constants.SATELLITE_MAP -> {
                if (mBaiduMap.mapType != BaiduMap.MAP_TYPE_SATELLITE)
                    mBaiduMap.mapType = BaiduMap.MAP_TYPE_SATELLITE
                mBaiduMap.isTrafficEnabled = false
            }
            Constants.TRAFFIC_MAP -> {
                if (mBaiduMap.mapType != BaiduMap.MAP_TYPE_NORMAL)
                    mBaiduMap.mapType = BaiduMap.MAP_TYPE_NORMAL
                mBaiduMap.isTrafficEnabled = true
            }
        }
    }

    /**
     * 设置是否启用3D视角
     */
    fun setAngle3D() {
        mUiSettings.isOverlookingGesturesEnabled =
                mSharedPreferences.getBoolean(Constants.KEY_ANGLE_3D, false)
    }

    /**
     * 设置是否允许地图旋转
     */
    fun setMapRotation() {
        mUiSettings.isRotateGesturesEnabled =
                mSharedPreferences.getBoolean(Constants.KEY_MAP_ROTATION, false)
    }

    /**
     * 设置是否显示比例尺
     */
    fun setScaleControl() {
        mMapView.showScaleControl(
                mSharedPreferences.getBoolean(Constants.KEY_SCALE_CONTROL, true))
    }

    /**
     * 设置是否显示缩放按钮
     */
    fun setZoomControls() {
        mMapView.showZoomControls(
                mSharedPreferences.getBoolean(Constants.KEY_ZOOM_CONTROLS, false))
    }

    /**
     * 设置是否显示指南针
     */
    fun setCompass() {
        mUiSettings.isCompassEnabled =
                mSharedPreferences.getBoolean(Constants.KEY_COMPASS, true)
    }

    //初始化偏好设置
    private fun initSettings() {
        //获取键盘对象
        imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setMapType()
        SettingUtil.initSettings(requireActivity())
        setAngle3D()
        setMapRotation()
        setScaleControl()
        setZoomControls()
        setCompass()
    }

    //初始化系统广播接收器
    private fun initSystemReceiver() {
        mSystemReceiver = SystemReceiver(requireActivity())
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.CONNECTIVITY_CHANGE)
        requireActivity().registerReceiver(mSystemReceiver, intentFilter)
    }

    //初始化本地广播接收器
    private fun initLocalReceiver() {
        mLocalReceiver = LocalReceiver(requireActivity())
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.SETTINGS_BROADCAST)
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(requireActivity())
        mLocalBroadcastManager.registerReceiver(mLocalReceiver, intentFilter)
    }

    //初始化自定义控件
    private fun initView(view: View) {
        ibSettings = view.findViewById(R.id.ib_settings)
        ibRefresh = view.findViewById(R.id.ib_refresh)
        ibLocation = view.findViewById(R.id.ib_location)
        llSelectLayout = view.findViewById(R.id.ll_select_layout)
        btSelect1 = view.findViewById(R.id.bt_select_1)
        btSelect2 = view.findViewById(R.id.bt_select_2)
        btSelect3 = view.findViewById(R.id.bt_select_3)
        btSelect4 = view.findViewById(R.id.bt_select_4)
        llSearchLayout = view.findViewById(R.id.ll_search_layout)
        etSearch = view.findViewById(R.id.et_search)
        ibEmpty = view.findViewById(R.id.ib_empty)
        btSearch = view.findViewById(R.id.bt_search)
        ibSearchExpand = view.findViewById(R.id.ib_search_expand)
        llSearchDrawer = view.findViewById(R.id.ll_search_drawer)
        llSearchLoading = view.findViewById(R.id.include_search_loading)
        mRecyclerSearchResult = view.findViewById(R.id.recycler_search_result)
        llSearchInfoLayout = view.findViewById(R.id.ll_search_info_layout)
        llSearchInfoLoading = view.findViewById(R.id.include_search_info_loading)
        svSearchInfo = view.findViewById(R.id.sv_search_info)
        tvSearchTargetName = view.findViewById(R.id.tv_search_target_name)
        tvSearchAddress = view.findViewById(R.id.tv_search_address)
        tvSearchDistance = view.findViewById(R.id.tv_search_distance)
        tvSearchOthers = view.findViewById(R.id.tv_search_others)
        llSchemeDrawer = view.findViewById(R.id.ll_scheme_drawer)
        llSchemeLoading = view.findViewById(R.id.include_scheme_loading)
        recyclerSchemeResult = view.findViewById(R.id.recycler_scheme_result)
        llSchemeInfoLayout = view.findViewById(R.id.ll_scheme_info_layout)
        svSchemeInfo = view.findViewById(R.id.sv_scheme_info)
        tvSchemeInfo = view.findViewById(R.id.tv_scheme_info)
        llStartLayout = view.findViewById(R.id.ll_start_layout)
        btBack = view.findViewById(R.id.bt_return)
        btMiddle = view.findViewById(R.id.bt_middle)
        btStart = view.findViewById(R.id.bt_start)

        //设置选项布局、搜索结果抽屉、详细信息、方案抽屉、方案信息布局、开始导航布局初始高度为0
        LayoutUtil.setViewHeight(llSelectLayout, 0)
        LayoutUtil.setViewHeight(llSearchDrawer, 0)
        LayoutUtil.setViewHeight(llSearchInfoLayout, 0)
        LayoutUtil.setViewHeight(llSchemeDrawer, 0)
        LayoutUtil.setViewHeight(llSchemeInfoLayout, 0)
        LayoutUtil.setViewHeight(llStartLayout, 0)

        //设置搜索、搜索信息、方案加载不可见
        llSearchLoading.visibility = View.GONE
        llSearchInfoLoading.visibility = View.GONE
        llSchemeLoading.visibility = View.GONE

        mSearchAdapter = SearchAdapter(this) //初始化搜索适配器
        mSearchLayoutManager = StaggeredGridLayoutManager(
                1, StaggeredGridLayoutManager.VERTICAL
        ) //搜索布局行数为1
        mRecyclerSearchResult.adapter = mSearchAdapter //设置搜索适配器
        mRecyclerSearchResult.layoutManager = mSearchLayoutManager //设置搜索布局
        mSchemeAdapter = SchemeAdapter(this) //初始化方案适配器
        mSchemeLayoutManager = StaggeredGridLayoutManager(
                1, StaggeredGridLayoutManager.VERTICAL
        ) //方案布局行数为1
        recyclerSchemeResult.adapter = mSchemeAdapter //设置方案适配器
        recyclerSchemeResult.layoutManager = mSchemeLayoutManager //设置方案布局

        //设置按钮的点击事件
        ibSettings.setOnClickListener {
            SettingsActivity.startActivity(requireActivity(), mBaiduLocation.mCity)
        }

        //刷新按钮的点击事件
        ibRefresh.setOnClickListener {
            requireActivity().finish() //关闭当前活动
            startActivity(requireActivity().intent) //重启当前活动
        }

        //定位按钮的点击事件
        ibLocation.setOnClickListener {
            requestPermission() //申请权限，获得权限后定位
        }

        //驾车按钮的点击事件
        btSelect1.setOnClickListener {
            btSelect1.setBackgroundResource(R.drawable.bt_bg_black_gray)
            btSelect2.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            btSelect3.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            btSelect4.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            mRoutePlanSelect = BaiduRoutePlan.DRIVING
            mBaiduRoutePlan.startRoutePlanSearch() //开始路线规划
        }

        //步行按钮的点击事件
        btSelect2.setOnClickListener {
            btSelect1.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            btSelect2.setBackgroundResource(R.drawable.bt_bg_black_gray)
            btSelect3.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            btSelect4.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            mRoutePlanSelect = BaiduRoutePlan.WALKING
            mBaiduRoutePlan.startRoutePlanSearch() //开始路线规划
        }

        //骑行按钮的点击事件
        btSelect3.setOnClickListener {
            btSelect1.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            btSelect2.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            btSelect3.setBackgroundResource(R.drawable.bt_bg_black_gray)
            btSelect4.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            mRoutePlanSelect = BaiduRoutePlan.BIKING
            mBaiduRoutePlan.startRoutePlanSearch() //开始路线规划
        }

        //公交按钮的点击事件
        btSelect4.setOnClickListener {
            btSelect1.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            btSelect2.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            btSelect3.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            btSelect4.setBackgroundResource(R.drawable.bt_bg_black_gray)
            mRoutePlanSelect = BaiduRoutePlan.TRANSIT
            mBaiduRoutePlan.startRoutePlanSearch() //开始路线规划
        }

        //输入框获取焦点时
        etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                //如果搜索抽屉收起且有搜索历史记录
                if (!searchExpandFlag && SearchDataHelper.isHasSearchData) {
                    searchExpandFlag = true //设置搜索抽屉为展开
                    expandSearchDrawer(true) //展开搜索抽屉
                }
            }
        }

        //监听输入框内容改变
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                //根据是否有内容判断显示和隐藏清空按钮
                if (etSearch.text.toString().isNotEmpty())
                    ibEmpty.visibility = View.VISIBLE
                else ibEmpty.visibility = View.INVISIBLE
            }
        })

        //清空按钮的点击事件
        ibEmpty.setOnClickListener {
            ibEmpty.visibility = View.INVISIBLE //隐藏清空按钮
            etSearch.setText("") //清空搜索内容
            if (!isHistorySearchResult) { //如果不是搜索历史记录
                mRecyclerSearchResult.stopScroll() //停止信息列表滑动
                isHistorySearchResult = true //现在是搜索历史记录了
                SearchDataHelper.initSearchData(this) //初始化搜索记录
            }
        }

        //搜索按钮的点击事件
        btSearch.setOnClickListener {
            mBaiduSearch.startSearch() //开始搜索
        }

        //伸缩按钮的点击事件
        ibSearchExpand.setOnClickListener {
            searchExpandFlag = !searchExpandFlag //反转搜索抽屉状态
            expandSearchDrawer(searchExpandFlag) //展开或收起搜索抽屉
        }

        //返回按钮的点击事件
        btBack.setOnClickListener {
            backToUpperStory() //返回上一层
        }

        //路线规划、详细信息、交通选择切换按钮的点击事件
        btMiddle.setOnClickListener {
            if (schemeExpandFlag != 0) {//如果方案布局已展开
                LayoutUtil.expandLayout(llSelectLayout, true) //展开选择布局
                if (schemeExpandFlag == 1)//如果方案布局为方案列表
                    LayoutUtil.expandLayout(llSchemeDrawer, false) //收起方案抽屉
                if (schemeExpandFlag == 2) //如果方案布局为单个方案
                    LayoutUtil.expandLayout(llSchemeInfoLayout, false) //收起方案信息布局
                schemeExpandFlag = 0 //设置方案布局状态为收起
                infoFlag = 1 //设置信息状态为交通选择
                btMiddle.setText(R.string.middle_button2) //设置按钮为详细信息
                return@setOnClickListener
            }
            if (infoFlag == 0) { //如果显示为详细信息
                infoFlag = 1 //设置信息状态为详细信息
                btMiddle.setText(R.string.middle_button2) //设置按钮为详细信息
                LayoutUtil.expandLayout(llSelectLayout, true) //展开选择布局
                LayoutUtil.expandLayout(llSearchInfoLayout, false) //收起详细信息布局
                mBaiduRoutePlan.startRoutePlanSearch() //开始路线规划
            } else { //如果显示为详细信息或交通选择
                infoFlag = 0 //设置信息状态为路线
                btMiddle.setText(R.string.middle_button1) //设置按钮为路线
                LayoutUtil.expandLayout(llSelectLayout, false) //收起选择布局
                LayoutUtil.expandLayout(llSearchInfoLayout, true) //展开详细信息布局
            }
        }

        //开始导航按钮的点击事件
        btStart.setOnClickListener {
            //交通选择
            if (mRoutePlanSelect == 0) {
                if (infoFlag == 0) btMiddle.callOnClick()
                else R.string.please_select_transportation.showToast()
                return@setOnClickListener
            }
            mBaiduNavigation.startNavigate() //开始导航
        }
    }

    //初始化方向传感器
    private fun initMyDirectionSensor() {
        //方向传感器
        mOrientationListener = MyOrientationListener()
        mOrientationListener.setOnOrientationListener(object : MyOrientationListener.OnOrientationListener {
            override fun onOrientationChanged(x: Float) {
                //更新方向
                mOrientationListener.mLastX = x
                mBaiduLocation.mLocData = MyLocationData.Builder()
                        .accuracy(mBaiduLocation.mRadius)
                        .direction(mOrientationListener.mLastX) //此处设置开发者获取到的方向信息，顺时针0-360
                        .latitude(mBaiduLocation.mLatitude)
                        .longitude(mBaiduLocation.mLongitude).build()
                mBaiduMap.setMyLocationData(mBaiduLocation.mLocData) //设置定位数据
            }
        })
    }

    /**
     * 申请权限，获得权限后定位
     */
    fun requestPermission() {
        val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE, //读写手机存储
                Manifest.permission.ACCESS_COARSE_LOCATION //定位
        )
        //将没有获得的权限加入申请列表
        val toApplyPermissions = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(requireActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED)
                toApplyPermissions.add(permission)
        }

        //若权限都已获取，则开始定位，反之则申请权限
        if (toApplyPermissions.isEmpty()) {
            mBaiduLocation.requestLocationTime = 0 //请求定位的次数归零
            mBaiduLocation.refreshSearchList = true //刷新搜索列表
            mBaiduLocation.initLocationOption() //初始化定位
        } else
            ActivityCompat.requestPermissions(requireActivity(),
                    toApplyPermissions.toTypedArray(), 0)
    }

    /**
     * 计算屏幕的宽高，并据此设置控件的高度
     */
    private fun calculateWidthAndHeightOfScreen() {
        //计算屏幕宽高，用于设置控件的高度
        val defaultDisplay = requireActivity().windowManager.defaultDisplay
        val point = Point()
        defaultDisplay.getSize(point)
        val configuration = this.resources.configuration //获取设置的配置信息
        val ori = configuration.orientation //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) { //横屏时
            mBodyLength = point.x
            mBodyShort = point.y
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) { //竖屏时
            mBodyLength = point.y
            mBodyShort = point.x
        }

        //设置搜索抽屉的结果列表、详细信息布局的拖动布局、路线方案抽屉的结果列表、路线方案信息的拖动布局的高度
        LayoutUtil.setViewHeight(mRecyclerSearchResult, mBodyLength / 2)
        LayoutUtil.setViewHeight(svSearchInfo, mBodyLength / 4)
        LayoutUtil.setViewHeight(recyclerSchemeResult, 2 * mBodyLength / 5)
        LayoutUtil.setViewHeight(svSchemeInfo, mBodyLength / 4)
    }

    /**
     * 伸缩搜索抽屉
     *
     * @param flag 伸或缩
     */
    fun expandSearchDrawer(flag: Boolean) {
        LayoutUtil.expandLayout(llSearchDrawer, flag)
        if (flag) LayoutUtil.rotateExpandIcon(ibSearchExpand, 0f, 180f) //旋转伸展按钮
        else LayoutUtil.rotateExpandIcon(ibSearchExpand, 180f, 0f) //旋转伸展按钮
    }

    /*
     * 收回键盘
     */
    fun takeBackKeyboard() {
        imm.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
    }

    /**
     * 返回上一层
     */
    fun backToUpperStory() {
        if (schemeExpandFlag == 2) { //如果方案布局为单个方案
            schemeExpandFlag = 1 //设置方案布局状态为方案列表
            LayoutUtil.expandLayout(llSchemeDrawer, true) //展开方案抽屉
            LayoutUtil.expandLayout(llSchemeInfoLayout, false) //收起方案信息布局
        } else {
            LayoutUtil.expandLayout(llSelectLayout, false) //收起选择布局
            LayoutUtil.expandLayout(llSearchLayout, true) //展开搜索布局
            if (!searchExpandFlag) { //如果搜索抽屉收起
                searchExpandFlag = true //设置搜索抽屉为收起
                expandSearchDrawer(true) //展开搜索抽屉
            }
            LayoutUtil.expandLayout(llSearchInfoLayout, false) //收起详细信息布局
            LayoutUtil.expandLayout(llStartLayout, false) //收起开始导航布局
            if (schemeExpandFlag == 1) { //如果方案布局为方案列表
                schemeExpandFlag = 0 //设置方案布局为收起
                LayoutUtil.expandLayout(llSchemeDrawer, false) //收起方案抽屉
            }
        }
    }

    /**
     * 判断是否可以返回上一层
     */
    fun canBack(): Boolean {
        return !(llSelectLayout.height == 0
                && llSearchLayout.height != 0
                && llSearchInfoLayout.height == 0
                && llStartLayout.height == 0)
    }
}