package com.navigation.foxizz.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import cn.zerokirby.api.data.AvatarDataHelper
import cn.zerokirby.api.data.UserDataHelper
import cn.zerokirby.api.util.UriUtil
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.fragment.MainFragment
import com.navigation.foxizz.activity.fragment.UserFragment
import com.navigation.foxizz.data.Constants
import com.navigation.foxizz.data.SearchDataHelper
import com.navigation.foxizz.util.SettingUtil
import com.navigation.foxizz.util.ThreadUtil
import com.navigation.foxizz.util.showToast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_user.*

/**
 * 主页
 */
class MainActivity : BaseActivity() {
    private lateinit var flPage: Fragment
    lateinit var mainFragment: MainFragment
    lateinit var userFragment: UserFragment

    private var exitTime: Long = 0 //实现再按一次退出程序时，用于保存系统时间
    private var isKeyDownFirst = false //是否有先监听到按下，确保在第三方应用使用onKeyDown返回时，不会连续返回2次

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    //初始化碎片
    private fun initFragments() {
        mainFragment = MainFragment()
        flPage = mainFragment
        userFragment = UserFragment()
        supportFragmentManager.beginTransaction()
                .add(R.id.fl_page, mainFragment)
                .add(R.id.fl_page, userFragment)
                .hide(userFragment).commit()
    }

    //切换碎片
    private fun replaceFragment(fragment: Fragment) {
        if (flPage != fragment) { //与显示的碎片不同才切换
            supportFragmentManager.beginTransaction().hide(flPage).show(fragment).commit()
            flPage = fragment
        }
    }

    //初始化控件
    private fun initView() {
        bt_main.setTextColor(getColor(R.color.skyblue))
        bt_user.setTextColor(getColor(R.color.black))
    }

    //监听权限申请
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.GET_LOCATION ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mainFragment.mBaiduLocation.requestLocationTime = 0 //请求定位的次数归零
                    mainFragment.mBaiduLocation.refreshSearchList = true //刷新搜索列表
                    mainFragment.mBaiduLocation.initLocationOption() //初始化定位
                } else R.string.get_location_permission_fail.showToast()
            Constants.CHOOSE_PHOTO ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    AvatarDataHelper.openAlbum(this)
                else R.string.get_choose_photo_permission_fail.showToast()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data) //开启Activity并返回结果
        when (requestCode) {
            Constants.CHOOSE_PHOTO -> if (resultCode == RESULT_OK && data != null)
                Constants.avatarUri = AvatarDataHelper.cropImage(this, data)
            Constants.PHOTO_REQUEST_CUT -> if (resultCode == RESULT_OK) {
                val avatarPath = UriUtil.getPath(Constants.avatarUri)
                AvatarDataHelper.showAvatarAndSave(
                        userFragment.iv_avatar_image, avatarPath, UserDataHelper.loginUserId)
                ThreadUtil.execute {
                    AvatarDataHelper.uploadAvatar(UriUtil.getPath(Constants.avatarUri))
                    R.string.upload_avatar_successfully.showToast()
                }
            }
        }
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
        if (flPage == mainFragment) { //mainFragment
            //如果是返回键且有先监听到按下
            if (keyCode == KeyEvent.KEYCODE_BACK && isKeyDownFirst) {
                isKeyDownFirst = false
                if (!mainFragment.searchLayoutFlag) { //如果搜索布局没有展开
                    mainFragment.backToUpperStory() //返回上一层
                    return false
                }
                if (!mainFragment.isHistorySearchResult) { //如果不是搜索历史记录
                    mainFragment.recycler_search_result.stopScroll() //停止信息列表滑动
                    mainFragment.isHistorySearchResult = true //现在是搜索历史记录了
                    SearchDataHelper.initSearchData(mainFragment) //初始化搜索记录
                }
                //如果焦点在searchEdit上或searchEdit有内容
                if (this.window.decorView.findFocus() == mainFragment.et_search
                        || mainFragment.et_search.text.toString().isNotEmpty()) {
                    mainFragment.et_search.clearFocus() //使搜索输入框失去焦点
                    mainFragment.et_search.setText("")
                    return false
                }
                if (mainFragment.searchExpandFlag) { //如果搜索抽屉展开
                    mainFragment.searchExpandFlag = false //设置搜索抽屉为收起
                    mainFragment.expandSearchDrawer(false) //收起搜索抽屉
                    return false
                }
                if (System.currentTimeMillis() - exitTime > 2000) { //弹出再按一次退出提示
                    R.string.exit_app.showToast()
                    exitTime = System.currentTimeMillis()
                    return false
                }
            }

            //如果是Enter键
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                mainFragment.mBaiduSearch.startSearch() //开始搜索
                mainFragment.et_search.requestFocus() //搜索框重新获得焦点
                mainFragment.takeBackKeyboard() //收回键盘
                return false
            }
        } else if (flPage == userFragment) { //userFragment
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