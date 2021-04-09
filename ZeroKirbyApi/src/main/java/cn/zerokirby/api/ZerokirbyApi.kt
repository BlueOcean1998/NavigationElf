package cn.zerokirby.api

import android.app.Application
import cn.zerokirby.api.data.UserDataHelper

/**
 * Api name: ZerokirbyApi
 * Author: Zerokirby & Foxizz
 * Accomplish date: 2021-02-04
 * Last modify date: 2021-04-08
 */
object ZerokirbyApi {
    lateinit var application: Application

    fun initialize(application: Application) {
        ZerokirbyApi.application = application

        UserDataHelper.initPhoneInfo()
    }
}