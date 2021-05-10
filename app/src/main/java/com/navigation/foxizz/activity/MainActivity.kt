package com.navigation.foxizz.activity

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import base.foxizz.BaseActivity
import base.foxizz.util.SettingUtil
import base.foxizz.util.showToast
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.fragment.MainFragment
import com.navigation.foxizz.activity.fragment.UserFragment
import com.navigation.foxizz.data.SearchDataHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * 主页
 */
class MainActivity : BaseActivity(R.layout.activity_main) {
    private lateinit var mFlPage: Fragment
    lateinit var mainFragment: MainFragment
    lateinit var userFragment: UserFragment

    //是否有先监听到按下，确保在第三方应用使用onKeyDown返回时，不会连续返回2次
    private var isKeyDownFirst = false
    private var exitTime = 0L //实现再按一次退出程序时，用于保存系统时间

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBaiduMapSDK() //初始化百度地图SDK
        SettingUtil.initSettings(this) //初始化设置
        initFragments() //初始化碎片
        initView() //初始化控件

        //设置首页按钮的点击事件
        bt_main.setOnClickListener {
            replaceFragment(mainFragment) //切换碎片
            bt_main.setTextColor(getColor(R.color.skyblue))
            bt_user.setTextColor(getColor(R.color.black))
            mainFragment.takeBackKeyboard() //收回键盘
        }

        //设置我的按钮的点击事件
        bt_user.setOnClickListener {
            replaceFragment(userFragment) //切换碎片
            bt_main.setTextColor(getColor(R.color.black))
            bt_user.setTextColor(getColor(R.color.skyblue))
            mainFragment.takeBackKeyboard() //收回键盘
        }
    }

    //初始化百度地图SDK
    private fun initBaiduMapSDK() {
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(this.applicationContext)
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL)
    }

    //初始化碎片
    private fun initFragments() {
        mainFragment = MainFragment()
        mFlPage = mainFragment
        userFragment = UserFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.fl_page, mainFragment)
            .add(R.id.fl_page, userFragment)
            .hide(userFragment).commit()
    }

    //切换碎片
    private fun replaceFragment(fragment: Fragment) {
        if (mFlPage != fragment) { //与显示的碎片不同才切换
            supportFragmentManager.beginTransaction().hide(mFlPage).show(fragment).commit()
            mFlPage = fragment
        }
    }

    //初始化控件
    private fun initView() {
        bt_main.setTextColor(getColor(R.color.skyblue))
        bt_user.setTextColor(getColor(R.color.black))
    }

    //监听按键按下事件
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isKeyDownFirst = true
        }
        return super.onKeyDown(keyCode, event)
    }

    //监听按键抬起事件
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (mFlPage == mainFragment) {
            mainFragment.run {
                //如果是返回键且有先监听到按下
                if (keyCode == KeyEvent.KEYCODE_BACK && isKeyDownFirst) {
                    isKeyDownFirst = false
                    if (!searchLayoutFlag) { //如果搜索布局没有展开
                        backToUpperStory() //返回上一层
                        return false
                    }
                    if (!isHistorySearchResult) { //如果不是搜索历史记录
                        recycler_search_result.stopScroll() //停止信息列表滑动
                        isHistorySearchResult = true //现在是搜索历史记录了
                        SearchDataHelper.initSearchData(this) //初始化搜索记录
                    }
                    //如果焦点在searchEdit上或searchEdit有内容
                    if (window.decorView.findFocus() == et_search
                        || et_search.text.toString().isNotEmpty()
                    ) {
                        et_search.clearFocus() //使搜索输入框失去焦点
                        et_search.setText("")
                        return false
                    }
                    if (searchExpandFlag) { //如果搜索抽屉展开
                        searchExpandFlag = false //设置搜索抽屉为收起
                        expandSearchDrawer(false) //收起搜索抽屉
                        return false
                    }
                    if (System.currentTimeMillis() - exitTime > 2000) { //弹出再按一次退出提示
                        showToast(R.string.exit_app)
                        exitTime = System.currentTimeMillis()
                        return false
                    }
                }

                //如果是Enter键
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    mBaiduSearch.startSearch() //开始搜索
                    et_search.requestFocus() //搜索框重新获得焦点
                    takeBackKeyboard() //收回键盘
                    return false
                }
            }
        } else if (mFlPage == userFragment) {
            //如果是返回键且有先监听到按下
            if (keyCode == KeyEvent.KEYCODE_BACK && isKeyDownFirst) {
                isKeyDownFirst = false
                //回到首页
                replaceFragment(mainFragment)
                bt_main.setTextColor(getColor(R.color.skyblue))
                bt_user.setTextColor(getColor(R.color.black))
            }
            return false
        }
        return super.onKeyUp(keyCode, event)
    }
}