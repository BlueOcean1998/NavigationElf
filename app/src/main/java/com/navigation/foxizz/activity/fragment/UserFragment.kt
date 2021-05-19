package com.navigation.foxizz.activity.fragment

import Constants
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import base.foxizz.BaseFragment
import base.foxizz.util.ThreadUtil
import base.foxizz.util.UriUtil
import base.foxizz.util.showToast
import cn.zerokirby.api.data.AvatarDataHelper
import cn.zerokirby.api.data.UserDataHelper
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.LoginRegisterActivity
import com.navigation.foxizz.activity.MainActivity
import com.navigation.foxizz.activity.SettingsActivity
import com.navigation.foxizz.receiver.LocalReceiver
import kotlinx.android.synthetic.main.fragment_user.*

/**
 * 用户页
 */
class UserFragment : BaseFragment(R.layout.fragment_user) {
    private var mPreferenceScreen = PreferenceScreen()
    private lateinit var mLocalReceiver: LocalReceiver //设置接收器
    private lateinit var mLocalBroadcastManager: LocalBroadcastManager //本地广播管理器

    private lateinit var avatarUri: Uri //头像文件路径uri
    private var choosePhotoLauncher = //调用系统相册选择图片后的操作
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Activity.RESULT_OK == it.resultCode) {
                it.data?.run { avatarUri = AvatarDataHelper.cropImage(this, cutPhotoLauncher) }
            }
        }
    private var cutPhotoLauncher = //调用系统方法裁剪图片后的操作
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Activity.RESULT_OK == it.resultCode) {
                val avatarPath = UriUtil.getPath(avatarUri)
                AvatarDataHelper.showAvatarAndSave(
                    iv_avatar_image, avatarPath, UserDataHelper.loginUserId
                )
                ThreadUtil.execute {
                    AvatarDataHelper.uploadAvatar(UriUtil.getPath(avatarUri))
                    showToast(R.string.upload_avatar_successfully)
                }
            }
        }

    override fun initView() {
        initLocalReceiver() //初始化本地广播接收器

        //设置头像
        val userId = UserDataHelper.loginUserId
        if (userId != "0") {
            val avatarBitmap = AvatarDataHelper.getBitmapAvatar(userId)
            iv_avatar_image.setImageBitmap(avatarBitmap)
        }

        //点击头像登录或修改头像
        fl_avatar_layout.setOnClickListener {
            if ("0" == UserDataHelper.loginUserId)
                LoginRegisterActivity.startActivity(baseActivity)
            else AvatarDataHelper.openAlbum(choosePhotoLauncher)
        }

        //点击用户信息登录
        ll_user_info_layout.setOnClickListener {
            if ("0" == UserDataHelper.loginUserId)
                LoginRegisterActivity.startActivity(baseActivity)
        }

        //初始化PreferenceScreen
        baseActivity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.fl_user_preferences, mPreferenceScreen)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        val userId = UserDataHelper.loginUserId
        val user = UserDataHelper.getUser(userId)

        //设置用户名
        if ("0" != userId) tv_user_name.text = user.username

        //设置是否显示退出登录
        val preference = mPreferenceScreen.findPreference<Preference>(Constants.KEY_LOGOUT)
        preference?.isVisible = "0" != userId
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mLocalBroadcastManager.unregisterReceiver(mLocalReceiver) //释放设置接收器实例
    }

    //初始化本地广播接收器
    private fun initLocalReceiver() {
        mLocalReceiver = LocalReceiver(baseActivity)
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.LOGIN_BROADCAST)
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(baseActivity)
        mLocalBroadcastManager.registerReceiver(mLocalReceiver, intentFilter)
    }

    //PreferenceScreen
    class PreferenceScreen : PreferenceFragmentCompat() {
        //创建PreferenceScreen
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_user, rootKey)
        }

        //设置PreferenceScreen的点击事件
        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            val browser = Intent(Intent.ACTION_VIEW)
            when (preference.key) {
                Constants.KEY_TO_SETTINGS -> {
                    val mainActivity = requireActivity() as? MainActivity
                    mainActivity?.run {
                        SettingsActivity.startActivity(
                            this, mainFragment.mBaiduLocation.mCity
                        )
                    }
                }
                Constants.KEY_CHECK_UPDATE -> {
                }
                Constants.KEY_SOUND_CODE ->
                    startActivity(
                        browser.setData(
                            Uri.parse("https://${getString(R.string.sound_code_url)}")
                        )
                    )
                Constants.KEY_CONTACT_ME ->
                    startActivity(
                        browser.setData(
                            Uri.parse("mailto:${getString(R.string.contact_me_url)}")
                        )
                    )
                Constants.KEY_LOGOUT -> {
                    val mainActivity = requireActivity() as? MainActivity
                    mainActivity?.let { showLogoutDialog(it, preference) }
                }
            }
            return super.onPreferenceTreeClick(preference)
        }

        companion object {
            //弹出退出登录提示对话框
            private fun showLogoutDialog(mainActivity: MainActivity, preference: Preference) {
                AlertDialog.Builder(mainActivity)
                    .setTitle(R.string.hint)
                    .setMessage(R.string.sure_to_logout)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        showToast(R.string.logged_out)
                        preference.isVisible = false
                        UserDataHelper.logout() //退出登录

                        //还原用户名和头像为默认
                        val userFragment = mainActivity.userFragment
                        userFragment.tv_user_name.setText(R.string.to_login)
                        userFragment.iv_avatar_image.setImageResource(R.drawable.dolphizz_sketch)
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        //do nothing
                    }
                    .show()
            }
        }
    }
}