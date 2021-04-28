package base.foxizz

import android.app.Application

class BaseApplication : Application() {
    companion object {
        /**
         * 获取Application
         *
         * @return application
         */
        lateinit var baseApplication: BaseApplication
    }

    override fun onCreate() {
        super.onCreate()
        baseApplication = this
    }
}