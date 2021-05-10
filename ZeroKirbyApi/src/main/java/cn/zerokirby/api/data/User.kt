package cn.zerokirby.api.data

/**
 * 用户类
 */
data class User(
    var userId: String = "", //id
    var username: String = "", //用户名
    var password: String = "", //密码
    var language: String = "", //语言
    var version: String = "", //版本
    var display: String = "", //显示信息
    var model: String = "", //型号
    var brand: String = "", //品牌
    var registerTime: Long = 0L, //注册时间
    var lastUse: Long = 0L, //上次使用时间
    var lastSync: Long = 0L, //上次同步时间
)