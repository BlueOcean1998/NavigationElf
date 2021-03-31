package com.navigation.foxizz.activity.fragment

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cn.zerokirby.api.data.AvatarDataHelper
import cn.zerokirby.api.data.UserDataHelper
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.LoginRegisterActivity
import com.navigation.foxizz.activity.MainActivity
import com.navigation.foxizz.activity.SettingsActivity
import com.navigation.foxizz.data.Constants
import com.navigation.foxizz.receiver.LocalReceiver
import com.navigation.foxizz.util.showToast
import kotlinx.android.synthetic.main.fragment_user.*

/**
 * 用户页
 */
class UserFragment : Fragment(R.layout.fragment_user) {
    private lateinit var preferenceScreen: PreferenceScreen
    private lateinit var localReceiver: LocalReceiver //设置接收器
    private lateinit var localBroadcastManager: LocalBroadcastManager //本地广播管理器

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLocalReceiver() //初始化本地广播接收器
        initView() //初始化控件

        //初始化PreferenceScreen
        preferenceScreen = PreferenceScreen()
        requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_user_preferences, preferenceScreen)
                .commit()
    }

    override fun onResume() {
        super.onResume()
        val userId = UserDataHelper.loginUserId
        val user = UserDataHelper.getUser(userId)

        //设置用户名
        if ("0" != userId) tv_user_name.text = user.username

        //设置是否显示退出登录
        val preference = preferenceScreen.findPreference<Preference>(Constants.KEY_LOGOUT)
        preference?.isVisible = "0" != userId
    }

    override fun onDestroy() {
        super.onDestroy()
        localBroadcastManager.unregisterReceiver(localReceiver) //释放设置接收器实例
    }

    //初始化本地广播接收器
    private fun initLocalReceiver() {
        localReceiver = LocalReceiver(requireActivity())
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.LOGIN_BROADCAST)
        localBroadcastManager = LocalBroadcastManager.getInstance(requireActivity())
        localBroadcastManager.registerReceiver(localReceiver, intentFilter)
    }

    //初始化用户布局
    private fun initView() {
        //设置头像
        val userId = UserDataHelper.loginUserId
        if (userId != "0") {
            val avatarBitmap = AvatarDataHelper.getBitmapAvatar(userId)
            iv_avatar_image.setImageBitmap(avatarBitmap)
        }

        fl_avatar_layout.setOnClickListener {
            if ("0" == UserDataHelper.loginUserId)
                LoginRegisterActivity.startActivity(requireActivity())
            else AvatarDataHelper.checkPermissionsAndOpenAlbum(requireActivity())
        }

        ll_user_info_layout.setOnClickListener {
            if ("0" == UserDataHelper.loginUserId)
                LoginRegisterActivity.startActivity(requireActivity())
        }
    }

    //PreferenceScreen
    class PreferenceScreen : PreferenceFragmentCompat() {
        //创建PreferenceScreen
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_user, rootKey)
        }

        //设置PreferenceScreen的点击事件
        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            val browser = Intent("android.intent.action.VIEW")
            when (preference.key) {
                Constants.KEY_TO_SETTINGS -> {
                    val mainActivity = requireActivity() as MainActivity
                    val mainFragment = mainActivity.mainFragment
                    SettingsActivity.startActivity(mainActivity, mainFragment.mBaiduLocation.mCity)
                }
                Constants.KEY_CHECK_UPDATE -> {
                }
                Constants.KEY_SOUND_CODE ->
                    startActivity(browser.setData(Uri.parse(
                            "https://" + getString(R.string.sound_code_url))))
                Constants.KEY_CONTACT_ME ->
                    startActivity(browser.setData(Uri.parse(
                            "mailto:" + getString(R.string.contact_me_url))))
                Constants.KEY_LOGOUT ->
                    showLogoutDialog(requireActivity() as MainActivity, preference)
            }
            return super.onPreferenceTreeClick(preference)
        }

        companion object {
            //弹出退出登录提示对话框
            private fun showLogoutDialog(mainActivity: MainActivity, preference: Preference) {
                val builder = AlertDialog.Builder(mainActivity)
                builder.setTitle(R.string.hint)
                builder.setMessage(R.string.sure_to_logout)
                builder.setPositiveButton(R.string.confirm) { _, _ ->
                    R.string.logged_out.showToast()
                    preference.isVisible = false
                    UserDataHelper.logout() //退出登录

                    //还原用户名和头像为默认
                    val userFragment = mainActivity.userFragment
                    userFragment.tv_user_name.setText(R.string.to_login)
                    userFragment.iv_avatar_image.setImageResource(R.drawable.dolphizz_sketch)
                }
                builder.setNegativeButton(R.string.cancel) { _, _ ->
                    //do nothing
                }
                builder.show()
            }
        }
    }
}