package com.navigation.foxizz.activity

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

/**
 * 基础活动
 * 管理除3个百度导航诱导活动外的所有活动
 */
open class BaseActivity : AppCompatActivity() {
    companion object {
        private val ACTIVITIES = ArrayList<Activity>()

        /**
         * 获取指定活动
         * 若该活动不在返回栈中，则返回MainActivity
         *
         * @param cls 活动类
         * @return 活动
         */
        fun findActivity(cls: Class<*>): Activity {
            for (activity in ACTIVITIES) {
                if (activity.javaClass == cls) {
                    return activity
                }
            }
            return ACTIVITIES[0]
        }

        /**
         * 退出程序
         */
        fun finishAll() {
            for (activity in ACTIVITIES) {
                if (!activity.isFinishing) {
                    activity.finish()
                }
            }
            ACTIVITIES.clear()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ACTIVITIES.add(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ACTIVITIES.remove(this)
    }
}