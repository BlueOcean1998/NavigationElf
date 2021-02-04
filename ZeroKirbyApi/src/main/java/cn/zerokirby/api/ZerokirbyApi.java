package cn.zerokirby.api;

import android.app.Application;

import cn.zerokirby.api.data.AvatarDataHelper;
import cn.zerokirby.api.data.UserDataHelper;

public class ZerokirbyApi {

    private static Application application;

    public static Application getApplication() {
        return application;
    }

    public static void initialize(Application application) {
        ZerokirbyApi.application = application;
        UserDataHelper.initUserDataHelper();
        AvatarDataHelper.initAvatarDataHelper();
    }

}
