package com.example.foxizz.navigation.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.foxizz.navigation.R;

public class TitleLayout extends LinearLayout {

    public ImageButton backButton;//返回
    public TextView titleText;//标题
    public ImageButton menuButton;//菜单

    public TitleLayout(Context context) {
        super(context);
    }

    public TitleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTitle(context, attrs);
    }

    /**
     * 设置返回按钮是否启用
     * @param enable 是否启用
     */
    public void setBackButtonEnable(boolean enable) {
        if (enable) backButton.setVisibility(VISIBLE);
        else backButton.setVisibility(GONE);
    }

    /**
     * 设置标题内容
     * @param content 内容
     */
    public void setTitleTextContent(String content) {
        titleText.setText(content);
    }

    /**
     * 设置菜单按钮是否启用
     * @param enable 是否启用
     */
    public void setMenuButtonEnable(boolean enable) {
        if (enable) menuButton.setVisibility(VISIBLE);
        else menuButton.setVisibility(GONE);
    }

    /**
     * 设置返回按钮点击事件
     * @param backOnClickListener 返回按钮点击事件
     */
    public void setBackOnClickListener(OnClickListener backOnClickListener) {
        backButton.setOnClickListener(backOnClickListener);
    }

    /**
     * 设置菜单按钮点击事件
     * @param menuOnClickListener 菜单按钮点击事件
     */
    public void setMenuOnClickListener(OnClickListener menuOnClickListener) {
        menuButton.setOnClickListener(menuOnClickListener);
    }

    //初始化标题
    private void initTitle(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.title_layout, this);
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        menuButton = findViewById(R.id.menu_button);

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) getContext()).finish();
            }
        });

        //获取自定义属性
        @SuppressLint({"Recycle", "CustomViewStyleable"})
        TypedArray typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.TitleLayout);
        if (typedArray != null) {
            setBackButtonEnable(typedArray.getBoolean(//默认启用返回按钮
                    R.styleable.TitleLayout_back_enable, true));
            setTitleTextContent(typedArray.getString(
                    R.styleable.TitleLayout_title));
            setMenuButtonEnable(typedArray.getBoolean(//默认不启用菜单按钮
                    R.styleable.TitleLayout_menu_enable, false));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
}
