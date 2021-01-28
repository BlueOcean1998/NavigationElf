package com.navigation.foxizz.activity.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    //UserFragment实例
    @SuppressLint("StaticFieldLeak")
    private static UserFragment instance;
    public static UserFragment getInstance() {
        return instance;
    }

    public static String userId = "0";
    public FrameLayout avatarLayout;//头像布局
    public ImageView avatarImage;//用户头像
    public LinearLayout userInfoLayout;//信息布局
    public TextView userName;//用户名
    public TextView userEmail;//用户email

    private PreferenceScreen preferenceScreen;

    private LocalReceiver localReceiver;//设置接收器
    private LocalBroadcastManager localBroadcastManager;//本地广播管理器

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        instance = this;//获取UserFragment实例

        initLocalBroadcast();//初始化本地广播接收器

        initView(view);//初始化控件

        preferenceScreen = new PreferenceScreen();

        //初始化PreferenceScreen
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.user_preferences, preferenceScreen)
                .commit();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        User user = UserDataHelper.getUser();
        userId = user.getUserId();

        //设置用户名
        if (!userId.equals("0")) userName.setText(user.getUsername());

        //设置是否显示退出登录
        Preference preference = preferenceScreen.findPreference(Constants.KEY_LOGOUT);
        if (preference != null) preference.setVisible(!userId.equals("0"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);//释放设置接收器实例
        instance = null;//释放UserFragment实例
    }

    //初始化本地广播接收器
    private void initLocalBroadcast() {
        localReceiver = new LocalReceiver(requireActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.LOGIN_BROADCAST);
        localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    //初始化用户布局
    private void initView(View view) {
        avatarLayout = view.findViewById(R.id.avatar_layout);
        avatarImage = view.findViewById(R.id.avatar_image);

        userInfoLayout = view.findViewById(R.id.user_info_layout);
        userName = view.findViewById(R.id.user_name);
        userEmail = view.findViewById(R.id.user_email);

        //设置头像
        Bitmap avatarBitmap = AvatarDataHelper.getBitmapAvatar();
        if (avatarBitmap != null) avatarImage.setImageBitmap(avatarBitmap);

        avatarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId.equals("0"))
                    startActivity(new Intent(getContext(), LoginRegisterActivity.class));
                else AvatarDataHelper.checkAvatarPermission(requireActivity());
            }
        });

        userInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId.equals("0"))
                    startActivity(new Intent(getContext(), LoginRegisterActivity.class));
            }
        });
    }

    //PreferenceScreen
    public static class PreferenceScreen extends PreferenceFragmentCompat {
        //创建PreferenceScreen
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.user_preferences, rootKey);
        }

        //设置PreferenceScreen的点击事件
        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            Intent browser = new Intent("android.intent.action.VIEW");
            switch (preference.getKey()) {
                case Constants.KEY_TO_SETTINGS:
                    Intent intent = new Intent(getContext(), SettingsActivity.class);

                    //寻找mainFragment
                    MainFragment mainFragment = ((MainActivity) requireActivity()).getMainFragment();
                    //传递mCity
                    if (mainFragment != null && mainFragment.mCity != null)
                        intent.putExtra(Constants.MY_CITY, mainFragment.mCity);

                    startActivity(intent);
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
                    showLogoutDialog(requireContext(), preference);
                    break;
                default:
                    break;
            }
            return super.onPreferenceTreeClick(preference);
        }

        //弹出退出登录提示对话框
        private static void showLogoutDialog(final Context context, final Preference preference) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.hint);
            builder.setMessage(R.string.sure_to_logout);

            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    userId = "0";

                    ToastUtil.showToast(R.string.logged_out);
                    preference.setVisible(false);

                    UserDataHelper.logout();//数据库中的user_id和last_sync_time归零

                    AvatarDataHelper.saveAvatar("".getBytes());//清空数据库中的头像
                    //还原用户名和头像为默认
                    if (context instanceof MainActivity) {
                        UserFragment userFragment = ((MainActivity) context).getUserFragment();
                        userFragment.userName.setText(R.string.to_login);
                        userFragment.avatarImage.setImageResource(R.drawable.foxizz_sketch);
                    }
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
