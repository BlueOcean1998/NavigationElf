package com.navigation.foxizz.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.navigation.foxizz.R;
import com.navigation.foxizz.data.Constants;
import com.navigation.foxizz.data.SPHelper;
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

    //LoginActivity实例
    @SuppressLint("StaticFieldLeak")
    private static LoginRegisterActivity instance;
    public static LoginRegisterActivity getInstance() {
        return instance;
    }

    private ImageButton backButton;//返回按钮
    private TextView pageTitle;//标题，登录或注册
    private EditText usernameEdit;//用户名输入框
    private EditText passwordEdit;//密码输入框
    private ImageButton watchPasswordButton;//显示密码按钮
    private EditText verifyEdit;//验证码输入框
    private ImageView verifyImage;//验证码图片
    private CheckBox rememberUsername;//记住用户名
    private CheckBox rememberPassword;//记住密码
    private AppCompatButton loginRegisterButton;//登录或注册按钮
    private TextView registerLoginHint;//注册或登录提示
    private TextView registerLoginLink;//注册或登录按钮
    private ProgressBar loadingProgress;//加载进度条

    private boolean isLogin = true;//是否是登录页

    private String username = "";//用户名
    private String password = "";//密码

    private boolean isWatchPassword = false;//是否显示密码
    private String verifyCode;//验证码
    private String verify;//输入框里的验证码

    private static LocalBroadcastManager localBroadcastManager;//本地广播管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        instance = this;//获取LoginActivity实例

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        initView();//初始化控件
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetUsernameAndPassword();//重新设置账号密码
    }

    @Override
    protected void onPause() {
        super.onPause();
        rememberUsernameAndPassword();//重设是否记住账号密码
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;//释放LoginActivity实例
    }

    //初始化控件
    private void initView() {
        backButton = findViewById(R.id.back_button);
        pageTitle = findViewById(R.id.page_title);
        usernameEdit = findViewById(R.id.username_edit);
        passwordEdit = findViewById(R.id.password_edit);
        watchPasswordButton = findViewById(R.id.watch_password_button);
        verifyEdit = findViewById(R.id.verify_edit);
        verifyImage = findViewById(R.id.verify_image);
        rememberUsername = findViewById(R.id.remember_username);
        rememberPassword = findViewById(R.id.remember_password);
        loginRegisterButton = findViewById(R.id.login_register_button);
        registerLoginHint = findViewById(R.id.register_login_hint);
        registerLoginLink = findViewById(R.id.register_login_link);
        loadingProgress = findViewById(R.id.loading_progress);

        //设置密码输入框的类型
        passwordEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        //返回按钮的点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!showReturnHintDialog()) finish();
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
                username = usernameEdit.getText().toString();
                password = passwordEdit.getText().toString();
                verify = verifyEdit.getText().toString();

                loginRegisterButton.setEnabled(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)
                        && !TextUtils.isEmpty(verify)
                        && verifyEdit.getText().toString().equalsIgnoreCase(verifyCode));
            }
        };
        usernameEdit.addTextChangedListener(textWatcher);
        passwordEdit.addTextChangedListener(textWatcher);
        verifyEdit.addTextChangedListener(textWatcher);

        //显示密码按钮的点击事件
        watchPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWatchPassword) {
                    isWatchPassword = false;
                    watchPasswordButton.setImageResource(R.drawable.ic_eye_black_30dp);
                    passwordEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    isWatchPassword = true;
                    watchPasswordButton.setImageResource(R.drawable.ic_eye_blue_30dp);
                    passwordEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                passwordEdit.setSelection(password.length());//移动焦点到末尾
            }
        });

        resetVerify();//生成新验证码
        verifyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetVerify();//生成新验证码
            }
        });

        //记住账号按钮的点击事件
        rememberUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rememberUsername.isChecked()) {
                    rememberPassword.setEnabled(true);
                } else {
                    rememberPassword.setEnabled(false);
                    rememberPassword.setChecked(false);
                }
            }
        });

        //登录或注册按钮的点击事件
        loginRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgress.setVisibility(View.VISIBLE);//显示进度条

                new Thread(new Runnable() {
                    @Override
                    public void run() {
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
                                ToastUtil.showToast(finalToastMessage, Toast.LENGTH_SHORT);//弹出提示信息
                                loadingProgress.setVisibility(View.GONE);//隐藏进度条
                            }
                        });

                        //若登录成功
                        if (status == 1) {
                            UserDataHelper.initUserInfo(jsonObject, username, password, isLogin);//初始化用户信息
                            AvatarDataHelper.downloadAvatar();//下载头像
                            //发送本地广播通知更新头像
                            localBroadcastManager.sendBroadcast(new Intent(Constants.LOGIN_BROADCAST)
                                    .putExtra(Constants.LOGIN_TYPE, Constants.SET_AVATAR));
                            finish();//关闭页面
                        }
                    }
                }).start();
            }
        });

        //注册或登录链接的点击事件
        registerLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLogin) {
                    isLogin = false;
                    pageTitle.setText(R.string.register);//设置页面标题为注册
                    loginRegisterButton.setText(R.string.register);//设置登录或注册按钮为注册
                    registerLoginHint.setText(R.string.login_hint);//设置登录提示
                    registerLoginLink.setText(R.string.login_link);//设置登录链接
                } else {
                    isLogin = true;
                    pageTitle.setText(R.string.login);//设置页面标题为登录
                    loginRegisterButton.setText(R.string.login);//设置登录或注册按钮为登录
                    registerLoginHint.setText(R.string.register_hint);//设置注册提示
                    registerLoginLink.setText(R.string.register_link);//设置注册链接
                }
            }
        });
    }

    //重新设置账号密码
    private void resetUsernameAndPassword() {
        User user = null;
        if (SPHelper.getBoolean(Constants.REMEMBER_USERNAME, false)) {
            user = UserDataHelper.getUser();
            username = user.getUsername();
            usernameEdit.setText(username);
            rememberUsername.setChecked(true);
            rememberPassword.setEnabled(true);
        } else {
            rememberPassword.setEnabled(false);
        }

        if (SPHelper.getBoolean(Constants.REMEMBER_PASSWORD, false)) {
            if (user == null) user = UserDataHelper.getUser();
            password = user.getPassword();
            passwordEdit.setText(password);
            rememberPassword.setChecked(true);
        }
    }

    //重新生成验证码
    private void resetVerify() {
        Bitmap verifyBit = CodeUtil.createBitmap();//生成新验证码
        verifyImage.setImageBitmap(verifyBit);//设置新生成的验证码图片
        verifyCode = CodeUtil.getCode();//获取新生成的验证码
        loginRegisterButton.setEnabled(false);//重新生成后不可直接登录
    }

    //重设是否记住账号密码
    private void rememberUsernameAndPassword() {
        SPHelper.putBoolean(Constants.REMEMBER_USERNAME, rememberUsername.isChecked());
        SPHelper.putBoolean(Constants.REMEMBER_PASSWORD, rememberPassword.isChecked());
        if (!rememberUsername.isChecked()) {
            UserDataHelper.updateUser("username", "");
        }
        if (!rememberPassword.isChecked()) {
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
            if (showReturnHintDialog()) return true;
        }
        return super.onKeyUp(keyCode, event);
    }

}
