package com.navigation.foxizz.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.navigation.foxizz.R
import kotlinx.android.synthetic.main.view_title_layout.view.*

class TitleLayout(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    init {
        inflate(context, R.layout.view_title_layout, this)

        ib_back.setOnClickListener {
            (context as? Activity)?.finish()
        }

        //获取自定义属性
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleLayout)
        //默认启用返回按钮
        setBackButtonEnable(typedArray.getBoolean(
            R.styleable.TitleLayout_back_enable, true))
        setTitleTextContent(typedArray.getString(
            R.styleable.TitleLayout_title) ?: "")
        //默认不启用菜单按钮
        setMenuButtonEnable(typedArray.getBoolean(
            R.styleable.TitleLayout_menu_enable, false))
        typedArray.recycle()
    }

    /**
     * 设置返回按钮是否启用
     * @param enable 是否启用
     */
    fun setBackButtonEnable(enable: Boolean) {
        ib_back.visibility = if (enable) VISIBLE else GONE
    }

    /**
     * 设置标题内容
     * @param content 内容
     */
    private fun setTitleTextContent(content: String) {
        tv_title.text = content
    }

    /**
     * 设置菜单按钮是否启用
     * @param enable 是否启用
     */
    fun setMenuButtonEnable(enable: Boolean) {
        ib_menu.visibility = if (enable) VISIBLE else GONE
    }

    /**
     * 设置返回按钮点击事件
     * @param backOnClickListener 返回按钮点击事件
     */
    fun setBackOnClickListener(backOnClickListener: OnClickListener) =
        ib_menu.setOnClickListener(backOnClickListener)

    /**
     * 设置菜单按钮点击事件
     * @param menuOnClickListener 菜单按钮点击事件
     */
    fun setMenuOnClickListener(menuOnClickListener: OnClickListener) =
        ib_menu.setOnClickListener(menuOnClickListener)
}