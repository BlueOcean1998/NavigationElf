package com.navigation.foxizz.activity.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.navigation.foxizz.R;
import com.navigation.foxizz.activity.LoginRegisterActivity;
import com.navigation.foxizz.activity.MainActivity;
import com.navigation.foxizz.activity.SettingsActivity;
import com.navigation.foxizz.data.Constants;
import com.navigation.foxizz.receiver.LocalReceiver;
import com.navigation.foxizz.util.ToastUtil;

import cn.zerokirby.api.data.AvatarDataHelper;
import cn.zerokirby.api.data.User;
import cn.zerokirby.api.data.UserDataHelper;

/**
 * 用户页
 */
public class UserFragment extends Fragment {

    public FrameLayout flAvatarLayout;//头像布局
    public ImageView ivAvatar;//用户头像
    public LinearLayout llUserInfoLayout;//信息布局
    public TextView tvUserName;//用户名
    public TextView tvUserEmail;//用户email

    private PreferenceScreen preferenceScreen;

    private LocalReceiver localReceiver;//设置接收器
    private LocalBroadcastManager localBroadcastManager;//本地广播管理器

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        initLocalBroadcast();//初始化本地广播接收器

        initView(view);//初始化控件

        preferenceScreen = new PreferenceScreen();

        //初始化PreferenceScreen
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_user_preferences, preferenceScreen)
                .commit();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String  userId = UserDataHelper.getLoginUserId();
        User user = UserDataHelper.getUser(userId);

        //设置用户名
        if (!userId.equals("0")) tvUserName.setText(user.getUsername());

        //设置是否显示退出登录
        Preference preference = preferenceScreen.findPreference(Constants.KEY_LOGOUT);
        if (preference != null) preference.setVisible(!userId.equals("0"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);//释放设置接收器实例
    }

    //初始化本地广播接收器
    private void initLocalBroadcast() {
        localReceiver = new LocalReceiver(requireActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.LOGIN_BROADCAST);
        localBroadcastManager = LocalBroadcastManager.getInstance(requireActivity());
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    //初始化用户布局
    private void initView(View view) {
        flAvatarLayout = view.findViewById(R.id.fl_avatar_layout);
        ivAvatar = view.findViewById(R.id.iv_avatar_image);

        llUserInfoLayout = view.findViewById(R.id.ll_user_info_layout);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);

        //设置头像
        String userId = UserDataHelper.getLoginUserId();
        Bitmap avatarBitmap = AvatarDataHelper.getBitmapAvatar(userId);
        if (avatarBitmap != null) ivAvatar.setImageBitmap(avatarBitmap);

        flAvatarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserDataHelper.getLoginUserId().equals("0"))
                    LoginRegisterActivity.startActivity(requireActivity());
                else AvatarDataHelper.checkAvatarPermission(requireActivity());
            }
        });

        llUserInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserDataHelper.getLoginUserId().equals("0"))
                    LoginRegisterActivity.startActivity(requireActivity());
            }
        });
    }

    //PreferenceScreen
    public static class PreferenceScreen extends PreferenceFragmentCompat {
        //创建PreferenceScreen
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_user, rootKey);
        }

        //设置PreferenceScreen的点击事件
        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            Intent browser = new Intent("android.intent.action.VIEW");
            switch (preference.getKey()) {
                case Constants.KEY_TO_SETTINGS:
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    MainFragment mainFragment = mainActivity.getMainFragment();
                    SettingsActivity.startActivity(mainActivity, mainFragment.mCity);
                    break;
                case Constants.KEY_CHECK_UPDATE:
                    //TODO
                    break;
                case Constants.KEY_SOUND_CODE:
                    startActivity(browser.setData(Uri.parse("https://" + getString(R.string.sound_code_url))));
                    break;
                case Constants.KEY_CONTACT_ME:
                    startActivity(browser.setData(Uri.parse("mailto:" + getString(R.string.contact_me_url))));
                    break;
                case Constants.KEY_LOGOUT:
                    showLogoutDialog((MainActivity) requireActivity(), preference);
                    break;
                default:
                    break;
            }
            return super.onPreferenceTreeClick(preference);
        }

        //弹出退出登录提示对话框
        private static void showLogoutDialog(final MainActivity mainActivity, final Preference preference) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
            builder.setTitle(R.string.hint);
            builder.setMessage(R.string.sure_to_logout);

            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ToastUtil.showToast(R.string.logged_out);
                    preference.setVisible(false);

                    UserDataHelper.logout();//退出登录

                    //还原用户名和头像为默认
                    UserFragment userFragment = mainActivity.getUserFragment();
                    userFragment.tvUserName.setText(R.string.to_login);
                    userFragment.ivAvatar.setImageResource(R.drawable.dolphizz_sketch);
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    //do nothing
                }
            });

            builder.show();
        }
    }

}
