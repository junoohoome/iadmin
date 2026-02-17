package me.fjq.constant;

/**
 * @author fjq
 */
public class Constants {

    /**
     * 通用成功标识
     */
    public static final String SUCCESS = "0";

    /**
     * 通用失败标识
     */
    public static final String FAIL = "1";
    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 当前记录起始索引
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * 每页显示记录数
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 超级管理员ID
     */
    public static final String SYS_ADMIN_ID = "sysadmin";
    /**
     * 超级管理员角色
     */
    public static final String SYS_ADMIN_ROLE = "superadmin";
    /**
     * 超级管理员权限
     */
    public static final String SYS_ADMIN_PERMISSION = "admin";

    /**
     * 顶级ID
     */
    public static final String PID = "0";

    /**
     * 用于IP定位转换
     */
    public static final String REGION = "内网IP|内网IP";

    /**
     * 验证码 CODE_KEY
     */
    public static final String CODE_KEY = "code_key";

    /**
     * 验证码有效时间/分钟
     */
    public static final Long CODE_EXPIRE_TIME = 3L;

    /**
     * 参数错误提示
     */
    public static final String INVALID_PARAMS = "参数有误";

    /**
     * 验证码不存在或已过期
     */
    public static final String CAPTCHA_NOT_EXIST = "验证码不存在或已过期";

    /**
     * 验证码错误
     */
    public static final String CAPTCHA_ERROR = "验证码错误";

    /**
     * 用户权限缓存 Key 前缀
     */
    public static final String USER_PERMISSIONS_KEY = "user:permissions:";

    /**
     * 用户权限缓存过期时间/分钟
     */
    public static final Long USER_PERMISSIONS_EXPIRE_TIME = 30L;
}
