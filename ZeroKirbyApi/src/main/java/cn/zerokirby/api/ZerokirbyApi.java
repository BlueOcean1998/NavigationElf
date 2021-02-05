package cn.zerokirby.api;

import android.app.Application;

import cn.zerokirby.api.data.DatabaseHelper;
import cn.zerokirby.api.data.UserDataHelper;

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
