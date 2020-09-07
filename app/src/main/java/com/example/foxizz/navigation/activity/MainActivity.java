package com.example.foxizz.navigation.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.fragment.MainFragment;
import com.example.foxizz.navigation.activity.fragment.UserFragment;

/**
 * app_name: NavigationElf
 * author: Foxizz
 * accomplish_date: 2020-04-30
 * last_modify_date: 2020-09-07
 */
public class MainActivity extends AppCompatActivity {

    private Fragment fragmentLayout;
    private MainFragment mainFragment;
    private UserFragment userFragment;

    private Button mainButton;
    private Button userButton;

    private long exitTime = 0;//实现再按一次退出程序时，用于保存系统时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化碎片
        startFragmentAdd(new MainFragment());

        //初始化控件
        mainButton = findViewById(R.id.main_button);
        userButton = findViewById(R.id.user_button);

        //设置首页按钮的点击事件
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换碎片
                startFragmentAdd(new MainFragment());
                mainButton.setBackgroundResource(R.drawable.button_background_colorful);
                userButton.setBackgroundResource(R.drawable.background_null);
            }
        });

        //设置我的按钮的点击事件
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换碎片
                startFragmentAdd(new UserFragment());
                mainButton.setBackgroundResource(R.drawable.background_null);
                userButton.setBackgroundResource(R.drawable.button_background_colorful);
            }
        });
    }

    //监听按键抬起事件
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //如果是返回键
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            //如果焦点在searchEdit上
            if(MainActivity.this.getWindow().getDecorView().findFocus() == mainFragment.searchEdit) {
                mainFragment.searchEdit.clearFocus();//使搜索输入框失去焦点
                return true;//只收回键盘
            }
            if(mainFragment.canBack()) {//如果可以返回
                mainFragment.backToUpperStory();//返回上一层
                return true;
            }
            if(!mainFragment.isHistorySearchResult) {//如果不是搜索历史记录
                mainFragment.searchResult.stopScroll();//停止信息列表滑动
                mainFragment.dbHelper.initSearchData();//初始化搜索记录
                mainFragment.isHistorySearchResult = true;//现在是搜索历史记录了
                return true;
            }
            if(mainFragment.searchExpandFlag) {//收起搜索抽屉
                mainFragment.expandSearchDrawer(false);
                mainFragment.searchExpandFlag = false;
                return true;
            }
            if((System.currentTimeMillis() - exitTime) > 2000) {//弹出再按一次退出提示
                Toast.makeText(this, getString(R.string.exit_app), Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
                return true;
            }
        }

        //如果是Enter键
        if(keyCode == KeyEvent.KEYCODE_ENTER) {
            mainFragment.startPoiSearch();//开始POI搜索
            mainFragment.searchEdit.requestFocus();//搜索框重新获得焦点
            if(mainFragment.imm != null) mainFragment.imm.hideSoftInputFromWindow(
                    getWindow().getDecorView().getWindowToken(), 0
            );//收回键盘
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    //初始化、切换碎片
    private void startFragmentAdd(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(fragmentLayout == null) {
            fragmentTransaction.add(R.id.fragment_layout, fragment).commit();
            fragmentLayout = fragment;
            if(fragmentLayout instanceof MainFragment) mainFragment = (MainFragment) fragmentLayout;
            if(fragmentLayout instanceof UserFragment) userFragment = (UserFragment) fragmentLayout;
        }
        if(fragmentLayout != fragment) {
            //先判断是否被add过
            if(!fragment.isAdded()) {
                //隐藏当前的fragment，add下一个到Activity中
                fragmentTransaction.hide(fragmentLayout).add(R.id.fragment_layout, fragment).commit();
            } else {
                //隐藏当前的fragment，显示下一个
                fragmentTransaction.hide(fragmentLayout).show(fragment).commit();
            }
            fragmentLayout = fragment;
        }
    }

}
