package com.example.foxizz.navigation.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.foxizz.navigation.R;

/**
 * 登录页
 */
public class LoginActivity extends AppCompatActivity {

    //LoginActivity实例
    @SuppressLint("StaticFieldLeak")
    private static LoginActivity instance;
    public static LoginActivity getInstance() {
        return instance;
    }

    private ImageButton backButton;
    private TextView title;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private ImageButton watchPasswordButton;
    private EditText verificationCodeEdit;
    private ImageView codeImage;
    private CheckBox usernameCheckbox;
    private CheckBox passwordCheckbox;
    private AppCompatButton loginButton;
    private TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        instance = this;//获取LoginActivity实例

        initLoginLayout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;//释放LoginActivity实例
    }

    private void initLoginLayout() {
        backButton = findViewById(R.id.back_button);
        title= findViewById(R.id.title);
        usernameEdit= findViewById(R.id.username_edit);
        passwordEdit= findViewById(R.id.password_edit);
        watchPasswordButton= findViewById(R.id.watch_password_button);
        verificationCodeEdit= findViewById(R.id.verification_code_edit);
        codeImage= findViewById(R.id.code_image);
        usernameCheckbox= findViewById(R.id.username_checkbox);
        passwordCheckbox= findViewById(R.id.password_checkbox);
        loginButton= findViewById(R.id.login_button);
        registerLink= findViewById(R.id.register_link);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
