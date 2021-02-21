package cn.zerokirby.api;

import android.app.Application;

import cn.zerokirby.api.data.DatabaseHelper;
import cn.zerokirby.api.data.UserDataHelper;

/**
 * Api name: ZerokirbyApi
 * Author: Zerokirby & Foxizz
 * Accomplish date: 2021-02-04
 * Last modify date: 2021-02-22
 */
public class ZerokirbyApi {

    private static Application application;

    public static Application getApplication() {
        return application;
    }

    public static void initialize(Application application) {
        ZerokirbyApi.application = application;
        DatabaseHelper.initDatabaseHelper();
        UserDataHelper.initPhoneInfo();
    }

}
