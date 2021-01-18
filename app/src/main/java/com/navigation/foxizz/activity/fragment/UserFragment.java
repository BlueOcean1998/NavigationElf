package com.navigation.foxizz.activity.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.navigation.foxizz.R;
import com.navigation.foxizz.activity.LoginRegisterActivity;
import com.navigation.foxizz.activity.MainActivity;
import com.navigation.foxizz.activity.SettingsActivity;
import com.navigation.foxizz.util.ToastUtil;
import com.navigation.foxizz.view.AdaptationTextView;

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

    public static String  userId = "0";
    private FrameLayout portraitLayout;//头像布局
    private ImageView userPortrait;//用户头像
    private LinearLayout userInfoLayout;//信息布局
    private AdaptationTextView userName;//用户名
    private AdaptationTextView userEmail;//用户email

    private PreferenceScreen preferenceScreen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        instance = this;//获取UserFragment实例

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
        userId = UserDataHelper.getUserInfo().getUserId();
        Preference preference = preferenceScreen.findPreference("logout");
        if (preference != null) {
            preference.setVisible(!userId.equals("0"));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;//释放UserFragment实例
    }

    //初始化用户布局
    private void initView(View view) {
        portraitLayout = view.findViewById(R.id.portrait_layout);
        userPortrait = view.findViewById(R.id.user_portrait);

        userInfoLayout = view.findViewById(R.id.user_info_layout);
        userName = view.findViewById(R.id.user_name);
        userEmail = view.findViewById(R.id.user_email);

        portraitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId.equals("0")) {
                    startActivity(new Intent(getContext(), LoginRegisterActivity.class));
                } else {
                    //TODO
                    ToastUtil.showToast("你已登录");
                }
            }
        });

        userInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId.equals("0")) {
                    startActivity(new Intent(getContext(), LoginRegisterActivity.class));
                } else {
                    //TODO
                    ToastUtil.showToast("你已登录");
                }
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
                case "to_settings":
                    Intent intent = new Intent(getContext(), SettingsActivity.class);

                    //寻找mainFragment
                    MainFragment mainFragment = ((MainActivity) requireActivity()).getMainFragment();
                    //传递mCity
                    if (mainFragment != null && mainFragment.mCity != null)
                        intent.putExtra("mCity", mainFragment.mCity);

                    startActivity(intent);
                    break;
                case "check_update":

                    break;
                case "sound_code":
                    startActivity(browser.setData(Uri.parse("https://github.com/BlueEra/Navigation")));
                    break;
                case "contact_me":
                    startActivity(browser.setData(Uri.parse("mailto:2872545042@qq.com")));
                    break;
                case "logout":
                    showLogoutDialog(requireContext(), preference);
                    break;
                default:
                    break;
            }
            return super.onPreferenceTreeClick(preference);
        }

        //弹出退出登录提示对话框
        private static void showLogoutDialog(Context context, final Preference preference) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.hint);
            builder.setMessage(R.string.sure_to_logout);

            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UserDataHelper.logout();
                    ToastUtil.showToast(R.string.logged_out);
                    userId = "0";
                    preference.setVisible(false);
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
