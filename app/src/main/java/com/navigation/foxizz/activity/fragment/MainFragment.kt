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
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.baidu.mapapi.map.*
import com.baidu.tts.client.SpeechSynthesizer
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.SettingsActivity
import com.navigation.foxizz.activity.adapter.SchemeAdapter
import com.navigation.foxizz.activity.adapter.SearchAdapter
import com.navigation.foxizz.data.*
import com.navigation.foxizz.dsp
import com.navigation.foxizz.imm
import com.navigation.foxizz.lbm
import com.navigation.foxizz.mybaidumap.*
import com.navigation.foxizz.receiver.LocalReceiver
import com.navigation.foxizz.receiver.SystemReceiver
import com.navigation.foxizz.util.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.util.*

/**
 * 地图页
 */
class MainFragment : Fragment(R.layout.fragment_main) {
    //地图控件
    lateinit var mBaiduMap: BaiduMap

    //方向传感器
    val mOrientationListener = MyOrientationListener()

    //定位相关
    lateinit var mBaiduLocation: BaiduLocation

    //搜索相关
    lateinit var mBaiduSearch: BaiduSearch
    var searchLayoutFlag = true //搜索布局展开状态
    var searchExpandFlag = false //搜索抽屉展开状态
    var isHistorySearchResult = true //是否是搜索历史记录
    private var mSearchLayoutManager = StaggeredGridLayoutManager(
            1, StaggeredGridLayoutManager.VERTICAL) //搜索布局管理器
    lateinit var mSearchAdapter: SearchAdapter //搜索适配器

    //路线规划相关
    lateinit var mBaiduRoutePlan: BaiduRoutePlan
    var mRoutePlanSelect = 0 //交通工具选择
    var schemeExpandFlag = 0 //方案布局展开状态（0：未展开，1：方案列表，2：单个方案）
    var mSchemeLayoutManager = StaggeredGridLayoutManager(
            1, StaggeredGridLayoutManager.VERTICAL) //方案布局管理器
    lateinit var mSchemeAdapter: SchemeAdapter //方案适配器

    //导航相关
    lateinit var mBaiduNavigation: BaiduNavigation
    var infoFlag = 0 //信息显示状态（0：路线，1：详细信息，2：交通选择）

    //控制相关
    private var mBodyLength = 0 //屏幕的长
    private var mBodyShort = 0 //屏幕的宽

    //设置相关
    private lateinit var mSystemReceiver: SystemReceiver //系统广播接收器
    private lateinit var mLocalReceiver: LocalReceiver //本地广播接收器

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMap() //初始化地图控件
        initSettings() //初始化偏好设置
        initSystemReceiver() //初始化系统广播接收器
        initLocalReceiver() //初始化本地广播接收器
        initView() //初始化控件
        initMyDirectionSensor() //初始化方向传感器

        mBaiduLocation = BaiduLocation(this) //初始化定位模块
        mBaiduSearch = BaiduSearch(this) //初始化搜索模块
        mBaiduRoutePlan = BaiduRoutePlan(this) //初始化路线规划模块
        mBaiduNavigation = BaiduNavigation(this) //初始化导航模块

        requestPermission() //申请权限，获得权限后定位
    }

    //管理地图的生命周期
    override fun onStart() {
        super.onStart()
        mBaiduMap.isMyLocationEnabled = true //开启定位
        mOrientationListener.start() //开启方向传感
    }

    override fun onResume() {
        super.onResume()
        map_view.onResume()
        calculateWidthAndHeightOfScreen() //计算屏幕宽高，用于设置控件的高度
    }

    override fun onPause() {
        super.onPause()
        map_view.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        requireActivity().unregisterReceiver(mSystemReceiver) //释放系统广播接收器实例
        lbm.unregisterReceiver(mLocalReceiver) //释放本地广播接收器实例

        mBaiduMap.isMyLocationEnabled = false //关闭定位
        mOrientationListener.stop() //停止方向传感

        //停止定位服务
        if (mBaiduLocation.mLocationClient.isStarted) {
            mBaiduLocation.mLocationClient.stop()
        }

        //释放地图、Sug搜索、POI搜索、路线规划、语音合成实例
        map_view?.onDestroy()
        mBaiduSearch.mSuggestionSearch.destroy()
        mBaiduSearch.mPoiSearch.destroy()
        mBaiduRoutePlan.mRoutePlanSearch.destroy()
        SpeechSynthesizer.getInstance().release()
    }

    //初始化地图控件
    private fun initMap() {
        //获取地图控件引用
        mBaiduMap = map_view.map

        //配置定位图层显示方式，使用默认的定位图标，设置精确度圆的填充色和边框色
        //LocationMode定位模式有三种：普通模式，跟随模式，罗盘模式，在这使用普通模式
        val myLocationConfiguration = MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null, -0x55401001, -0x55603001)
        mBaiduMap.setMyLocationConfiguration(myLocationConfiguration)

        //map_view.removeViewAt(1);//去除百度水印
        map_view.scaleControlPosition = Point() //改变比例尺位置
        map_view.zoomControlsPosition = Point() //改变缩放按钮位置

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

    //初始化偏好设置
    private fun initSettings() {
        setMapType()
        setAngle3D()
        setMapRotation()
        setScaleControl()
        setZoomControls()
        setCompass()
    }

    /**
     * 设置地图类型
     */
    fun setMapType() {
        when (SPUtil.getString(Constants.MAP_TYPE, Constants.STANDARD_MAP)) {
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
        mBaiduMap.uiSettings.isOverlookingGesturesEnabled =
                dsp.getBoolean(Constants.KEY_ANGLE_3D, false)
    }

    /**
     * 设置是否允许地图旋转
     */
    fun setMapRotation() {
        mBaiduMap.uiSettings.isRotateGesturesEnabled =
                dsp.getBoolean(Constants.KEY_MAP_ROTATION, false)
    }

    /**
     * 设置是否显示比例尺
     */
    fun setScaleControl() {
        map_view.showScaleControl(
                dsp.getBoolean(Constants.KEY_SCALE_CONTROL, true))
    }

    /**
     * 设置是否显示缩放按钮
     */
    fun setZoomControls() {
        map_view.showZoomControls(
                dsp.getBoolean(Constants.KEY_ZOOM_CONTROLS, false))
    }

    /**
     * 设置是否显示指南针
     */
    fun setCompass() {
        mBaiduMap.uiSettings.isCompassEnabled =
                dsp.getBoolean(Constants.KEY_COMPASS, true)
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
        lbm.registerReceiver(mLocalReceiver, intentFilter)
    }

    //初始化自定义控件
    private fun initView() {
        //设置选项布局、搜索结果抽屉、详细信息、方案抽屉、方案信息布局、开始导航布局初始高度为0
        ll_select_layout.setHeight(0)
        ll_search_drawer.setHeight(0)
        ll_search_info_layout.setHeight(0)
        ll_scheme_drawer.setHeight(0)
        ll_scheme_info_layout.setHeight(0)
        ll_start_layout.setHeight(0)

        //设置搜索、搜索信息、方案加载不可见
        include_search_loading.visibility = View.GONE
        include_search_info_loading.visibility = View.GONE
        include_scheme_loading.visibility = View.GONE

        mSearchAdapter = SearchAdapter(this) //初始化搜索适配器
        recycler_search_result.adapter = mSearchAdapter //设置搜索适配器
        recycler_search_result.layoutManager = mSearchLayoutManager //设置搜索布局

        mSchemeAdapter = SchemeAdapter(this) //初始化方案适配器
        recycler_scheme_result.adapter = mSchemeAdapter //设置方案适配器
        recycler_scheme_result.layoutManager = mSchemeLayoutManager //设置方案布局

        //设置按钮的点击事件
        ib_settings.setOnClickListener {
            SettingsActivity.startActivity(requireActivity(), mBaiduLocation.mCity)
        }

        //刷新按钮的点击事件
        ib_refresh.setOnClickListener {
            requireActivity().finish() //关闭当前活动
            startActivity(requireActivity().intent) //重启当前活动
        }

        //定位按钮的点击事件
        ib_location.setOnClickListener {
            requestPermission() //申请权限，获得权限后定位
        }

        //驾车按钮的点击事件
        bt_select_1.setOnClickListener {
            bt_select_1.setBackgroundResource(R.drawable.bt_bg_black_gray)
            bt_select_2.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            bt_select_3.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            bt_select_4.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            mRoutePlanSelect = BaiduRoutePlan.DRIVING
            mBaiduRoutePlan.startRoutePlanSearch() //开始路线规划
        }

        //步行按钮的点击事件
        bt_select_2.setOnClickListener {
            bt_select_1.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            bt_select_2.setBackgroundResource(R.drawable.bt_bg_black_gray)
            bt_select_3.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            bt_select_4.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            mRoutePlanSelect = BaiduRoutePlan.WALKING
            mBaiduRoutePlan.startRoutePlanSearch() //开始路线规划
        }

        //骑行按钮的点击事件
        bt_select_3.setOnClickListener {
            bt_select_1.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            bt_select_2.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            bt_select_3.setBackgroundResource(R.drawable.bt_bg_black_gray)
            bt_select_4.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            mRoutePlanSelect = BaiduRoutePlan.BIKING
            mBaiduRoutePlan.startRoutePlanSearch() //开始路线规划
        }

        //公交按钮的点击事件
        bt_select_4.setOnClickListener {
            bt_select_1.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            bt_select_2.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            bt_select_3.setBackgroundResource(R.drawable.bt_bg_alpha_black)
            bt_select_4.setBackgroundResource(R.drawable.bt_bg_black_gray)
            mRoutePlanSelect = BaiduRoutePlan.TRANSIT
            mBaiduRoutePlan.startRoutePlanSearch() //开始路线规划
        }

        //输入框获取焦点时
        et_search.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                //如果搜索抽屉收起且有搜索历史记录
                if (!searchExpandFlag && SearchDataHelper.isHasSearchData) {
                    searchExpandFlag = true //设置搜索抽屉为展开
                    expandSearchDrawer(true) //展开搜索抽屉
                }
            }
        }

        //监听输入框内容改变
        et_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                //根据是否有内容判断显示和隐藏清空按钮
                if (et_search.text.toString().isNotEmpty())
                    ib_empty.visibility = View.VISIBLE
                else ib_empty.visibility = View.INVISIBLE
            }
        })

        //清空按钮的点击事件
        ib_empty.setOnClickListener {
            ib_empty.visibility = View.INVISIBLE //隐藏清空按钮
            et_search.setText("") //清空搜索内容
            if (!isHistorySearchResult) { //如果不是搜索历史记录
                recycler_search_result.stopScroll() //停止信息列表滑动
                isHistorySearchResult = true //现在是搜索历史记录了
                SearchDataHelper.initSearchData(this) //初始化搜索记录
            }
        }

        //搜索按钮的点击事件
        bt_search.setOnClickListener {
            mBaiduSearch.startSearch() //开始搜索
        }

        //伸缩按钮的点击事件
        ib_search_expand.setOnClickListener {
            searchExpandFlag = !searchExpandFlag //反转搜索抽屉状态
            expandSearchDrawer(searchExpandFlag) //展开或收起搜索抽屉
        }

        //返回按钮的点击事件
        bt_return.setOnClickListener {
            backToUpperStory() //返回上一层
        }

        //路线规划、详细信息、交通选择切换按钮的点击事件
        bt_middle.setOnClickListener {
            if (schemeExpandFlag != 0) {//如果方案布局已展开
                ll_select_layout.expandLayout(true) //展开选择布局
                if (schemeExpandFlag == 1)//如果方案布局为方案列表
                    ll_scheme_drawer.expandLayout(false) //收起方案抽屉
                if (schemeExpandFlag == 2) //如果方案布局为单个方案
                    ll_scheme_info_layout.expandLayout(false) //收起方案信息布局
                schemeExpandFlag = 0 //设置方案布局状态为收起
                infoFlag = 1 //设置信息状态为交通选择
                bt_middle.setText(R.string.middle_button2) //设置按钮为详细信息
                return@setOnClickListener
            }
            if (infoFlag == 0) { //如果显示为详细信息
                infoFlag = 1 //设置信息状态为详细信息
                bt_middle.setText(R.string.middle_button2) //设置按钮为详细信息
                ll_select_layout.expandLayout(true) //展开选择布局
                ll_search_info_layout.expandLayout(false) //收起详细信息布局
                mBaiduRoutePlan.startRoutePlanSearch() //开始路线规划
            } else { //如果显示为详细信息或交通选择
                infoFlag = 0 //设置信息状态为路线
                bt_middle.setText(R.string.middle_button1) //设置按钮为路线
                ll_select_layout.expandLayout(false) //收起选择布局
                ll_search_info_layout.expandLayout(true) //展开详细信息布局
            }
        }

        //开始导航按钮的点击事件
        bt_start.setOnClickListener {
            //交通选择
            if (mRoutePlanSelect == 0) {
                if (infoFlag == 0) bt_middle.callOnClick()
                else R.string.please_select_transportation.showToast()
                return@setOnClickListener
            }
            mBaiduNavigation.startNavigate() //开始导航
        }
    }

    //初始化方向传感器
    private fun initMyDirectionSensor() {
        //方向传感器
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
        recycler_search_result.setHeight(mBodyLength / 2)
        sv_search_info.setHeight(mBodyLength / 4)
        recycler_scheme_result.setHeight(2 * mBodyLength / 5)
        sv_scheme_info.setHeight(mBodyLength / 4)
    }

    /**
     * 伸缩搜索抽屉
     *
     * @param flag 伸或缩
     */
    fun expandSearchDrawer(flag: Boolean) {
        ll_search_drawer.expandLayout(flag)
        if (flag) ib_search_expand.rotateExpandIcon(0f, 180f) //旋转伸展按钮
        else ib_search_expand.rotateExpandIcon(180f, 0f) //旋转伸展按钮
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
            ll_scheme_drawer.expandLayout(true) //展开方案抽屉
            ll_scheme_info_layout.expandLayout(false) //收起方案信息布局
        } else {
            ll_select_layout.expandLayout(false) //收起选择布局
            searchLayoutFlag = true //设置搜索布局为展开
            ll_search_layout.expandLayout(true) //展开搜索布局
            if (!searchExpandFlag) { //如果搜索抽屉收起
                searchExpandFlag = true //设置搜索抽屉为收起
                expandSearchDrawer(true) //展开搜索抽屉
            }
            ll_search_info_layout.expandLayout(false) //收起详细信息布局
            ll_start_layout.expandLayout(false) //收起开始导航布局
            if (schemeExpandFlag == 1) { //如果方案布局为方案列表
                schemeExpandFlag = 0 //设置方案布局为收起
                ll_scheme_drawer.expandLayout(false) //收起方案抽屉
            }
        }
    }
}