package base.foxizz

import android.app.Application

/**
 * 基础应用
 */
class BaseApplication : Application() {
    companion object {
        lateinit var baseApplication: BaseApplication
    }

    override fun onCreate() {
        super.onCreate()
        baseApplication = this
    }
}