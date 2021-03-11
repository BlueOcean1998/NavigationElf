package com.navigation.foxizz.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.navigation.foxizz.R;
import com.navigation.foxizz.activity.fragment.MainFragment;
import com.navigation.foxizz.activity.fragment.UserFragment;
import com.navigation.foxizz.data.Constants;
import com.navigation.foxizz.data.SearchDataHelper;
import com.navigation.foxizz.util.ThreadUtil;
import com.navigation.foxizz.util.ToastUtil;

import cn.zerokirby.api.data.AvatarDataHelper;
import cn.zerokirby.api.data.UserDataHelper;
import cn.zerokirby.api.util.UriUtil;

/**
 * 主页
 */
public class MainActivity extends BaseActivity {

    private Fragment flPage;
    private MainFragment mainFragment;
    private UserFragment userFragment;
    private Button mainButton;
    private Button userButton;

    private FragmentManager fragmentManager;

    private long exitTime = 0;//实现再按一次退出程序时，用于保存系统时间
    private boolean isKeyDownFirst = false;//是否有先监听到按下，确保在第三方应用使用onKeyDown返回时，不会连续返回2次

    public MainFragment getMainFragment() {
        return mainFragment;
    }

    public UserFragment getUserFragment() {
        return userFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFragments();//初始化碎片

        initView();//初始化控件

        //设置首页按钮的点击事件
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(mainFragment);//切换碎片

                mainButton.setTextColor(getResources().getColor(R.color.skyblue));
                userButton.setTextColor(getResources().getColor(R.color.black));

                mainFragment.takeBackKeyboard();//收回键盘
            }
        });

        //设置我的按钮的点击事件
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(userFragment);//切换碎片

                mainButton.setTextColor(getResources().getColor(R.color.black));
                userButton.setTextColor(getResources().getColor(R.color.skyblue));

                mainFragment.takeBackKeyboard();//收回键盘
            }
        });
    }

    //初始化碎片
    private void initFragments() {
        fragmentManager = getSupportFragmentManager();

        flPage = mainFragment = new MainFragment();
        userFragment = new UserFragment();

        fragmentManager.beginTransaction()
                .add(R.id.fl_page, userFragment).hide(userFragment)
                .add(R.id.fl_page, mainFragment).commit();
    }

    //切换碎片
    private void replaceFragment(Fragment fragment) {
        if (flPage != fragment) {//与显示的碎片不同才切换
            fragmentManager.beginTransaction().hide(flPage).show(fragment).commit();
            flPage = fragment;
        }
    }

    //初始化控件
    private void initView() {
        mainButton = findViewById(R.id.bt_main);
        userButton = findViewById(R.id.bt_user);
        mainButton.setTextColor(getResources().getColor(R.color.skyblue));
        userButton.setTextColor(getResources().getColor(R.color.black));
    }

    //监听权限申请
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mainFragment.myLocation.initLocationOption();//初始化定位
            } else
                ToastUtil.showToast(R.string.get_permission_fail);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {//开启Activity并返回结果
        switch (requestCode) {
            case Constants.CHOOSE_PHOTO:
                if (resultCode == RESULT_OK && intent != null) {
                    Constants.avatarUri = AvatarDataHelper.startPhotoZoom(this, intent);
                }
                break;
            case Constants.PHOTO_REQUEST_CUT:
                if (resultCode == RESULT_OK) {
                    AvatarDataHelper.showAvatarAndSave(
                            userFragment.ivAvatar, UriUtil.getPath(Constants.avatarUri),
                            UserDataHelper.getLoginUserId()
                    );
                    ThreadUtil.execute(new Runnable() {
                        @Override
                        public void run() {
                            AvatarDataHelper.uploadAvatar(UriUtil.getPath(Constants.avatarUri));
                            ToastUtil.showToast(R.string.upload_avatar_successfully);
                        }
                    });
                }
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isKeyDownFirst = true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //监听按键抬起事件
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //mainFragment
        if (flPage == mainFragment) {
            //如果是返回键且有先监听到按下
            if (keyCode == KeyEvent.KEYCODE_BACK && isKeyDownFirst) {
                isKeyDownFirst = false;
                if (mainFragment.canBack()) {//如果可以返回
                    mainFragment.backToUpperStory();//返回上一层
                    return false;
                }
                if (!mainFragment.isHistorySearchResult) {//如果不是搜索历史记录
                    mainFragment.mRecyclerSearchResult.stopScroll();//停止信息列表滑动
                    SearchDataHelper.initSearchData(mainFragment);//初始化搜索记录
                    mainFragment.isHistorySearchResult = true;//现在是搜索历史记录了
                }
                //如果焦点在searchEdit上或searchEdit有内容
                if (MainActivity.this.getWindow().getDecorView().findFocus() == mainFragment.etSearch
                        || !TextUtils.isEmpty(mainFragment.etSearch.getText().toString())) {
                    mainFragment.etSearch.clearFocus();//使搜索输入框失去焦点
                    mainFragment.etSearch.setText("");
                    return false;
                }
                if (mainFragment.llSearchDrawer.getHeight() != 0) {//如果搜索抽屉展开
                    mainFragment.expandSearchDrawer(false);//收起搜索抽屉
                    return false;
                }
                if ((System.currentTimeMillis() - exitTime) > 2000) {//弹出再按一次退出提示
                    ToastUtil.showToast(R.string.exit_app);
                    exitTime = System.currentTimeMillis();
                    return false;
                }
            }

            //如果是Enter键
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                mainFragment.mySearch.startSearch();//开始搜索
                mainFragment.etSearch.requestFocus();//搜索框重新获得焦点
                mainFragment.takeBackKeyboard();//收回键盘
                return false;
            }
        } else if (flPage == userFragment) {//userFragment
            //如果是返回键且有先监听到按下
            if (keyCode == KeyEvent.KEYCODE_BACK  && isKeyDownFirst) {
                isKeyDownFirst = false;
                //回到首页
                replaceFragment(mainFragment);
                mainButton.setTextColor(getResources().getColor(R.color.skyblue));
                userButton.setTextColor(getResources().getColor(R.color.black));
            }
            return false;
        }
        return super.onKeyUp(keyCode, event);
    }

}
