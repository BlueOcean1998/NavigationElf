package base.foxizz

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import java.util.*
import kotlin.reflect.KClass

/**
 * 基础活动
 * 管理除3个百度导航诱导活动外的所有活动
 */
open class BaseActivity : FragmentActivity() {
    companion object {
        private val ACTIVITIES = ArrayList<BaseActivity>()

        /**
         * 获取指定活动
         *
         * @param baseActivityClass 基础活动类
         * @return 基础活动
         */
        fun findActivity(baseActivityClass: KClass<out BaseActivity>): BaseActivity? {
            for (activity in ACTIVITIES) {
                if (activity::class == baseActivityClass) {
                    return activity
                }
            }
            return null
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