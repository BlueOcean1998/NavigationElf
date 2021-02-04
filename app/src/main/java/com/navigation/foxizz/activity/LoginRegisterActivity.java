package com.navigation.foxizz.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.navigation.foxizz.R;
import com.navigation.foxizz.data.Constants;
import com.navigation.foxizz.data.SPHelper;
import com.navigation.foxizz.util.SettingUtil;
import com.navigation.foxizz.util.ToastUtil;

import org.json.JSONObject;

import cn.zerokirby.api.data.AvatarDataHelper;
import cn.zerokirby.api.data.User;
import cn.zerokirby.api.data.UserDataHelper;
import cn.zerokirby.api.util.CodeUtil;

/**
 * 登录页
 */
public class LoginRegisterActivity extends AppCompatActivity {

    private RelativeLayout rlLoginRegister;//登录注册页
    private ImageButton tbBack;//返回按钮
    private TextView tvPageTitle;//标题，登录或注册
    private EditText etUsername;//用户名输入框
    private EditText etPassword;//密码输入框
    private ImageButton ibWatchPassword;//显示密码按钮
    private EditText ETVerify;//验证码输入框
    private ImageView tvVerify;//验证码图片
    private CheckBox cbRememberUsername;//记住用户名
    private CheckBox cbRememberPassword;//记住密码
    private AppCompatButton appCompatBtLoginRegister;//登录或注册按钮
    private TextView tvRegisterLoginHint;//注册或登录提示
    private TextView tvRegisterLoginLink;//注册或登录按钮
    private ProgressBar pbLoadingProgress;//加载进度条

    private boolean isLogin = true;//是否是登录页
    private boolean isSending = false;//是否正在登录或注册
    private boolean isWatchPassword = false;//是否显示密码

    private String username = "";//用户名
    private String password = "";//密码
    private String verify = "";//输入框里的验证码
    private String verifyCode;//验证码

    private CodeUtil codeUtil;//验证码生成工具

    private static LocalBroadcastManager localBroadcastManager;//本地广播管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        codeUtil = new CodeUtil();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        initView();//初始化控件

        //恢复输入框中的信息
        if (savedInstanceState != null) {
            username = savedInstanceState.getString("username");
            password = savedInstanceState.getString("password");
            verify = savedInstanceState.getString("verify");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetEdit();//重设输入框填入的信息
    }

    @Override
    protected void onPause() {
        super.onPause();
        rememberUsernameAndPassword();//重设是否记住账号密码
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //活动被回收时保存输入框中的信息
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", username);
        outState.putString("password", password);
        outState.putString("verify", verify);
    }

    //初始化控件
    private void initView() {
        rlLoginRegister = findViewById(R.id.rl_activity_login_register);
        tbBack = findViewById(R.id.ib_back);
        tvPageTitle = findViewById(R.id.tv_page_title);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        ibWatchPassword = findViewById(R.id.ib_watch_password);
        ETVerify = findViewById(R.id.et_verify);
        tvVerify = findViewById(R.id.ib_verify_image);
        cbRememberUsername = findViewById(R.id.cb_remember_username);
        cbRememberPassword = findViewById(R.id.cb_remember_password);
        appCompatBtLoginRegister = findViewById(R.id.app_compat_bt_login_register);
        tvRegisterLoginHint = findViewById(R.id.tv_register_login_hint);
        tvRegisterLoginLink = findViewById(R.id.tv_register_login_link);
        pbLoadingProgress = findViewById(R.id.pb_loading);

        //手机模式只允许竖屏，平板模式只允许横屏，且根据不同的模式设置不同的背景图
        if (SettingUtil.isMobile()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            rlLoginRegister.setBackgroundResource(R.drawable.beach);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            rlLoginRegister.setBackgroundResource(R.drawable.foxizz_on_the_beach);
        }

        //设置密码输入框的类型
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        setUsernameAndPassword();//设置账号密码

        //返回按钮的点击事件
        tbBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSending && !showReturnHintDialog()) finish();
            }
        });

        //监听用户输入变化，账号密码都不为空且验证码正确时，登录或注册按钮可点击
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                username = etUsername.getText().toString();
                password = etPassword.getText().toString();
                verify = ETVerify.getText().toString();

                appCompatBtLoginRegister.setEnabled(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)
                        && !TextUtils.isEmpty(verify)
                        && ETVerify.getText().toString().equalsIgnoreCase(verifyCode));
            }
        };
        etUsername.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        ETVerify.addTextChangedListener(textWatcher);

        //显示密码按钮的点击事件
        ibWatchPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWatchPassword) {
                    isWatchPassword = false;
                    ibWatchPassword.setImageResource(R.drawable.ic_eye_black_30dp);
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    isWatchPassword = true;
                    ibWatchPassword.setImageResource(R.drawable.ic_eye_blue_30dp);
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                etPassword.setSelection(password.length());//移动焦点到末尾
            }
        });

        resetVerify();//生成新验证码
        tvVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetVerify();//生成新验证码
            }
        });

        //记住账号按钮的点击事件
        cbRememberUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbRememberUsername.isChecked()) {
                    cbRememberPassword.setEnabled(true);
                } else {
                    cbRememberPassword.setEnabled(false);
                    cbRememberPassword.setChecked(false);
                }
            }
        });

        //登录或注册按钮的点击事件
        appCompatBtLoginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbLoadingProgress.setVisibility(View.VISIBLE);//显示进度条
                appCompatBtLoginRegister.setEnabled(false);//登录或注册时不可点击

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        isSending = true;

                        //提交账号信息，获取返回结果。该操作会阻塞线程，需在子线程进行
                        JSONObject jsonObject =  UserDataHelper.sendRequestWithOkHttp(username, password, isLogin);

                        //获取登录或注册结果
                        int status = 2;
                        try {
                            if (jsonObject != null) status = jsonObject.getInt("Status");
                        } catch (Exception ignored) {

                        }

                        //生成登录或注册结果提示信息
                        String toastMessage = "";
                        if (isLogin) {//登录页
                            switch (status) {
                                case 2:
                                    toastMessage = getResources().getString(R.string.server_error);
                                    break;
                                case 1:
                                    toastMessage = getString(R.string.login_successfully);
                                    break;
                                case 0:
                                    toastMessage = getString(R.string.wrong_username_or_password);
                                    break;
                                case -1:
                                    toastMessage = getString(R.string.username_banned);
                                    break;
                                case -2:
                                    toastMessage = getString(R.string.username_not_exists);
                                    break;
                                default:
                                    break;
                            }
                        } else {//注册页
                            switch (status) {
                                case 2:
                                    toastMessage = getResources().getString(R.string.server_error);
                                    break;
                                case 1:
                                    toastMessage = getResources().getString(R.string.register_successfully);
                                    break;
                                default:
                                    toastMessage = getResources().getString(R.string.username_already_exists);
                                    break;
                            }
                        }

                        //返回UI线程进行UI操作（主线程）
                        final String finalToastMessage = toastMessage;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isSending = false;
                                ToastUtil.showToast(finalToastMessage, Toast.LENGTH_SHORT);//弹出提示信息
                                pbLoadingProgress.setVisibility(View.GONE);//隐藏进度条
                                appCompatBtLoginRegister.setEnabled(true);//登录或注册完毕后可点击
                            }
                        });

                        //若登录成功
                        if (status == 1) {
                            UserDataHelper.initUserInfo(jsonObject, username, password, isLogin);//初始化用户信息
                            //发送本地广播通知更新用户名
                            localBroadcastManager.sendBroadcast(new Intent(Constants.LOGIN_BROADCAST)
                                    .putExtra(Constants.LOGIN_TYPE, Constants.SET_USERNAME));

                            finish();//关闭页面

                            AvatarDataHelper.downloadAvatar(UserDataHelper.getUser().getUserId());//下载头像
                            //发送本地广播通知更新头像
                            localBroadcastManager.sendBroadcast(new Intent(Constants.LOGIN_BROADCAST)
                                    .putExtra(Constants.LOGIN_TYPE, Constants.SET_AVATAR));
                        }
                    }
                }).start();
            }
        });

        //注册或登录链接的点击事件
        tvRegisterLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSending) {
                    if (isLogin) {
                        isLogin = false;
                        tvPageTitle.setText(R.string.register);//设置页面标题为注册
                        appCompatBtLoginRegister.setText(R.string.register);//设置登录或注册按钮为注册
                        tvRegisterLoginHint.setText(R.string.login_hint);//设置登录提示
                        tvRegisterLoginLink.setText(R.string.login_link);//设置登录链接
                    } else {
                        isLogin = true;
                        tvPageTitle.setText(R.string.login);//设置页面标题为登录
                        appCompatBtLoginRegister.setText(R.string.login);//设置登录或注册按钮为登录
                        tvRegisterLoginHint.setText(R.string.register_hint);//设置注册提示
                        tvRegisterLoginLink.setText(R.string.register_link);//设置注册链接
                    }
                }
            }
        });
    }

    //设置账号密码
    private void setUsernameAndPassword() {
        User user = null;
        if (SPHelper.getBoolean(Constants.REMEMBER_USERNAME, false)) {
            user = UserDataHelper.getUser();
            username = user.getUsername();
            etUsername.setText(username);
            cbRememberUsername.setChecked(true);
            cbRememberPassword.setEnabled(true);
        } else {
            cbRememberPassword.setEnabled(false);
        }

        if (SPHelper.getBoolean(Constants.REMEMBER_PASSWORD, false)) {
            if (user == null) user = UserDataHelper.getUser();
            password = user.getPassword();
            etPassword.setText(password);
            cbRememberPassword.setChecked(true);
        }
    }

    //重设输入框填入的信息
    private void resetEdit() {
        etUsername.setText(username);
        etPassword.setText(password);
        ETVerify.setText(verify);
    }

    //重新生成验证码
    private void resetVerify() {
        Bitmap verifyBit = codeUtil.createBitmap();//生成新验证码
        tvVerify.setImageBitmap(verifyBit);//设置新生成的验证码图片
        verifyCode = codeUtil.getCode();//获取新生成的验证码
        appCompatBtLoginRegister.setEnabled(false);//重新生成后不可直接登录
    }

    //重设是否记住账号密码
    private void rememberUsernameAndPassword() {
        SPHelper.putBoolean(Constants.REMEMBER_USERNAME, cbRememberUsername.isChecked());
        SPHelper.putBoolean(Constants.REMEMBER_PASSWORD, cbRememberPassword.isChecked());
        if (!cbRememberUsername.isChecked()) {
            UserDataHelper.updateUser("username", "");
        }
        if (!cbRememberPassword.isChecked()) {
            UserDataHelper.updateUser("password", "");
        }
    }

    //显示返回提示对话框
    private boolean showReturnHintDialog() {
        if (isLogin) {//如果是登录页
            User user = UserDataHelper.getUser();
            //如果账号或密码修改过
            if ((!username.equalsIgnoreCase(user.getUsername()) || !password.equals(user.getPassword()))) {
                showReturnHintDialog(getString(R.string.login_page_return_hint));
                return true;//显示
            }
        } else {//如果是注册页
            //如果账号或密码不为空
            if (!TextUtils.isEmpty(username) || !TextUtils.isEmpty(password)) {
                showReturnHintDialog(getString(R.string.register_page_return_hint));
                return true;//显示
            }
        }
        return false;//不显示
    }

    //设置返回提示对话框并弹出
    private void showReturnHintDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.hint);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();//退出登录页
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

    //监听按键抬起事件
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //如果是返回键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //信息有修改或正在连接服务器时不可返回
            if (isSending || showReturnHintDialog()) return false;
        }
        return super.onKeyUp(keyCode, event);
    }

}
