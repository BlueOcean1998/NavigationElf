package com.example.foxizz.navigation.activity;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.fragment.MainFragment;
import com.example.foxizz.navigation.activity.fragment.UserFragment;

import static com.example.foxizz.navigation.mybaidumap.MyApplication.getContext;

/**
 * app_name: NavigationElf
 * author: Foxizz
 * accomplish_date: 2020-04-30
 * last_modify_date: 2020-11-02
 */
public class MainActivity extends BaseActivity {

    //MainActivity实例
    @SuppressLint("StaticFieldLeak")
    private static MainActivity instance;
    public static MainActivity getInstance() {
        return instance;
    }

    private FragmentManager fragmentManager;
    private Fragment fragmentLayout;
    private MainFragment mainFragment;
    private UserFragment userFragment;
    private Button mainButton;
    private Button userButton;
    private long exitTime = 0;//实现再按一次退出程序时，用于保存系统时间

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

        instance = this;//获取MainActivity实例

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;////释放MainActivity实例
    }

    //初始化碎片
    private void initFragments() {
        fragmentManager = getSupportFragmentManager();

        fragmentLayout = mainFragment = new MainFragment();
        userFragment = new UserFragment();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_layout, userFragment).hide(userFragment)
                .add(R.id.fragment_layout, mainFragment).commit();
    }

    //切换碎片
    private void replaceFragment(Fragment fragment) {
        if (fragmentLayout != fragment) {//与显示的碎片不同才切换
            fragmentManager.beginTransaction().hide(fragmentLayout).show(fragment).commit();
            fragmentLayout = fragment;
        }
    }

    //初始化控件
    private void initView() {
        mainButton = findViewById(R.id.main_button);
        userButton = findViewById(R.id.user_button);
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
                mainFragment.myLocation.refreshSearchList = true;//刷新搜索列表
                mainFragment.myLocation.initLocationOption();//初始化定位
            } else
                Toast.makeText(getContext(), R.string.get_permission_fail, Toast.LENGTH_SHORT).show();
        }
    }

    //监听按键抬起事件
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //mainFragment
        if (fragmentLayout == mainFragment) {
            //如果是返回键
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mainFragment.canBack()) {//如果可以返回
                    mainFragment.backToUpperStory();//返回上一层
                    return true;
                }
                if (!mainFragment.isHistorySearchResult) {//如果不是搜索历史记录
                    mainFragment.searchResult.stopScroll();//停止信息列表滑动
                    mainFragment.searchDataHelper.initSearchData(mainFragment);//初始化搜索记录
                    mainFragment.isHistorySearchResult = true;//现在是搜索历史记录了
                }
                //如果焦点在searchEdit上或searchEdit有内容
                if (MainActivity.this.getWindow().getDecorView().findFocus() == mainFragment.searchEdit
                        || !mainFragment.searchEdit.getText().toString().isEmpty()) {
                    mainFragment.searchEdit.clearFocus();//使搜索输入框失去焦点
                    mainFragment.searchEdit.setText("");
                    return true;
                }
                if (mainFragment.searchExpandFlag) {//收起搜索抽屉
                    mainFragment.expandSearchDrawer(false);
                    mainFragment.searchExpandFlag = false;
                    return true;
                }
                if ((System.currentTimeMillis() - exitTime) > 2000) {//弹出再按一次退出提示
                    Toast.makeText(getContext(), getString(R.string.exit_app), Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                    return true;
                }
            }

            //如果是Enter键
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                mainFragment.startSearch();//开始搜索
                mainFragment.searchEdit.requestFocus();//搜索框重新获得焦点
                mainFragment.takeBackKeyboard();//收回键盘
                return true;
            }
            //userFragment
        } else if (fragmentLayout == userFragment) {
            //回到首页
            replaceFragment(mainFragment);
            mainButton.setTextColor(getResources().getColor(R.color.skyblue));
            userButton.setTextColor(getResources().getColor(R.color.black));
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

}
