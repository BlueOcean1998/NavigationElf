package cn.zerokirby.api.data;

/**
 * 常量类
 */
public class Constants {

    public final static String LOCAL_DATABASE = "ZerokirbyAPI.db";//本地数据库

    public final static String ROOT_URL = "https://zerokirby.cn:8443/progress_note_server/";//天天服务器根目录
    public final static String LOGIN_URL = ROOT_URL + "LoginServlet";//登录服务程序
    public final static String REGISTER_URL = ROOT_URL + "RegisterServlet";//注册服务程序
    public final static String DOWNLOAD_AVATAR_URL = ROOT_URL + "DownloadAvatarServlet";//下载头像服务程序
    public final static String UPLOAD_AVATAR_URL = ROOT_URL + "UploadAvatarServlet";//上传头像服务程序

    public final static int CHOOSE_PHOTO = 1;//选择图片
    public final static int PHOTO_REQUEST_CUT = 2;//请求裁剪图片

}
