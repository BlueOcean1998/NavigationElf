package com.navigation.foxizz.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.navigation.foxizz.R;
import com.navigation.foxizz.data.Constants;
import com.navigation.foxizz.util.SPUtil;
import com.navigation.foxizz.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;

import cn.zerokirby.api.data.User;
import cn.zerokirby.api.data.UserDataHelper;
import cn.zerokirby.api.util.CodeUtil;
import cn.zerokirby.api.util.SystemUtil;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 登录页
 */
public class LoginRegisterActivity extends AppCompatActivity {

    private final static int LOGIN = 1;
    private final static int REGISTER = 2;
    //LoginActivity实例
    @SuppressLint("StaticFieldLeak")
    private static LoginRegisterActivity instance;
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

    private boolean isLoginPage = true;//是否是登录页
    private int status = 0;//登录状态
    private int userId = 0;//用户id
    private String username = "";//用户名
    private String password = "";//密码
    private boolean isWatchPassword = false;//是否显示密码
    private Bitmap verifyBit;//验证码图片
    private String verifyCode;//验证码
    private String verify;//输入框里的验证码
    private Handler loginRegisterHandler;//处理登录结果的handler
    private String toastMessage;//登录提示信息
    private String responseData;//登录返回的json字符串
    private long registerTime;//注册时间
    private long syncTime;//同步时间

    public static LoginRegisterActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        instance = this;//获取LoginActivity实例

        initView();//初始化控件

        //处理登录或注册结果
        loginRegisterHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                ToastUtil.showToast(toastMessage, Toast.LENGTH_SHORT);//弹出解析到的内容
                loadingProgress.setVisibility(View.GONE);//隐藏进度条
                if (status == 1) {
                    resetRemember();//重设记住的账号密码

                    User user = new User();
                    user.setUserId(userId);
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setLastUse(System.currentTimeMillis());

                    if (msg.what == LOGIN) {//登录
                        user.setRegisterTime(registerTime);
                        user.setLastSync(syncTime);
                    } else if (msg.what == REGISTER) {//注册
                        user.setRegisterTime(System.currentTimeMillis());
                    }

                    UserDataHelper.updateLoginStatus(user, false);//更新用户数据库
                    finish();//关闭页面
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetUsernameAndPassword();//重新设置账号密码
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
                if (showReturnHintDialog()) finish();
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
                sendRequestWithOkHttpLogin(username, password);
                loadingProgress.setVisibility(View.VISIBLE);//显示进度条
            }
        });

        //注册或登录链接的点击事件
        registerLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoginPage) {
                    isLoginPage = false;
                    pageTitle.setText(R.string.register);//设置页面标题为注册
                    loginRegisterButton.setText(R.string.register);//设置登录或注册按钮为注册
                    registerLoginHint.setText(R.string.login_hint);//设置登录提示
                    registerLoginLink.setText(R.string.login_link);//设置登录链接
                } else {
                    isLoginPage = true;
                    pageTitle.setText(R.string.login);//设置页面标题为登录
                    loginRegisterButton.setText(R.string.login);//设置登录或注册按钮为登录
                    registerLoginHint.setText(R.string.register_hint);//设置注册提示
                    registerLoginLink.setText(R.string.register_link);//设置注册链接
                }
            }
        });
    }

    //重设记住的账号密码
    private void resetRemember() {
        if (rememberUsername.isChecked()) {
            SPUtil.putString(Constants.REMEMBER_USERNAME, username);
        } else {
            SPUtil.putString(Constants.REMEMBER_USERNAME, "");
        }
        if (rememberPassword.isChecked()) {
            SPUtil.putString(Constants.REMEMBER_PASSWORD, password);
        } else {
            SPUtil.putString(Constants.REMEMBER_PASSWORD, "");
        }
    }

    //重新设置账号密码
    private void resetUsernameAndPassword() {
        username = SPUtil.getString(Constants.REMEMBER_USERNAME, "");
        usernameEdit.setText(username);
        password = SPUtil.getString(Constants.REMEMBER_PASSWORD, "");
        passwordEdit.setText(password);
        if (!TextUtils.isEmpty(username)) {
            rememberUsername.setChecked(true);
            rememberPassword.setEnabled(true);
        } else {
            rememberPassword.setEnabled(false);
        }
        if (!TextUtils.isEmpty(password)) {
            rememberPassword.setChecked(true);
        }
    }

    //重新生成验证码
    private void resetVerify() {
        verifyBit = CodeUtil.getInstance().createBitmap();//生成新验证码
        verifyImage.setImageBitmap(verifyBit);//设置新生成的验证码图片
        verifyCode = CodeUtil.getInstance().getCode();//获取新生成的验证码
        loginRegisterButton.setEnabled(false);//重新生成后不可直接登录
    }

    //提交账号信息
    private void sendRequestWithOkHttpLogin(final String username, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();//利用OkHttp发送HTTP请求调用服务端登录servlet
                    RequestBody requestBody = new FormBody.Builder().add("username", username).add("password", password)
                            .add("language", SystemUtil.getSystemLanguage()).add("version", SystemUtil.getSystemVersion())
                            .add("display", SystemUtil.getSystemDisplay()).add("model", SystemUtil.getSystemModel())
                            .add("brand", SystemUtil.getDeviceBrand()).build();

                    Request request;
                    if (isLoginPage) {//登录页
                        request = new Request.Builder().url(
                                "https://zerokirby.cn:8443/progress_note_server/LoginServlet")
                                .post(requestBody).build();
                    } else {//注册页
                        request = new Request.Builder().url(
                                "https://zerokirby.cn:8443/progress_note_server/RegisterServlet")
                                .post(requestBody).build();
                    }

                    Response response = client.newCall(request).execute();
                    responseData = Objects.requireNonNull(response.body()).string();
                    parseJSONWithJSONObject(responseData);//处理JSON

                    //获取头像
                    if (status == 1) {//登陆成功的情况下才处理头像
                        client = new OkHttpClient();
                        requestBody = new FormBody.Builder().add("userId", String.valueOf(userId)).build();
                        request = new Request.Builder().url("https://zerokirby.cn:8443/progress_note_server/DownloadAvatarServlet").post(requestBody).build();
                        response = client.newCall(request).execute();
                        InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();

                        //将用户ID、用户名、密码存储到本地
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];//缓冲区大小
                        int n;
                        while (-1 != (n = inputStream.read(buffer))) {
                            output.write(buffer, 0, n);
                        }
                        inputStream.close();
                        output.close();
                        byte[] bytes = output.toByteArray();

                        UserDataHelper.saveUserNameAndPassword(bytes);
                    }

                    //通知loginRegisterHandler登录或注册操作完成
                    Message message = new Message();
                    if (isLoginPage) {//登录页
                        message.what = LOGIN;
                    } else {//注册页
                        message.what = REGISTER;
                    }
                    loginRegisterHandler.sendMessage(message);
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //处理返回的json字符串
    private void parseJSONWithJSONObject(String jsonData) {//处理JSON
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            status = jsonObject.getInt("Status");//取出Status字段
            if (isLoginPage) {//登录页
                switch (status) {
                    case 1:
                        registerTime = jsonObject.getLong("RegisterTime");
                        syncTime = jsonObject.getLong("SyncTime");
                        userId = Integer.parseInt(jsonObject.getString("Id"));//取出ID字段
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
                }
            } else {//注册页
                if (status == 1) {
                    userId = Integer.parseInt(jsonObject.getString("Id"));//取出ID字段
                    toastMessage = getResources().getString(R.string.register_successfully);
                } else {
                    toastMessage = getResources().getString(R.string.username_already_exists);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //显示返回提示对话框
    private boolean showReturnHintDialog() {
        if (isLoginPage) {//如果是登录页
            //如果账号或密码修改过
            if (!username.equals(SPUtil.getString(Constants.REMEMBER_USERNAME, ""))
                    || !password.equals(SPUtil.getString(Constants.REMEMBER_PASSWORD, ""))) {
                showReturnHintDialog(getString(R.string.login_page_return_hint));
                return false;//不退出
            }
        } else {//如果是注册页
            //如果账号或密码不为空
            if (!TextUtils.isEmpty(username) || !TextUtils.isEmpty(password)) {
                showReturnHintDialog(getString(R.string.register_page_return_hint));
                return false;//不退出
            }
        }
        return true;//退出
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
            if (!showReturnHintDialog()) return true;
        }
        return super.onKeyUp(keyCode, event);
    }

}
