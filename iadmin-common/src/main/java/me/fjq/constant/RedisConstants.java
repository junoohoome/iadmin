package me.fjq.constant;

/**
 * Redis 常量
 *
 * @author fjq
 * @since 2025-02-05
 */
public class RedisConstants {

    /**
     * 在线用户 Token 前缀
     * 格式: online:token:{token}
     */
    public static final String ONLINE_TOKEN_KEY = "online:token:";

    /**
     * 在线用户集合
     * 格式: online:users
     */
    public static final String ONLINE_USERS_KEY = "online:users";

    /**
     * Token 黑名单前缀
     * 格式: blacklisted:token:{token}
     */
    public static final String BLACKLISTED_TOKEN_KEY = "blacklisted:token:";

    /**
     * 登录用户 Redis Key
     * 格式: login_tokens:{username}
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 验证码 Redis Key
     * 格式: captcha_codes:{uuid}
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 密码重置码 Redis Key
     * 格式: password_reset_codes:{uuid}
     */
    public static final String PASSWORD_RESET_CODE_KEY = "password_reset_codes:";

    /**
     * 缓存名称前缀
     */
    public static final String CACHE_NAME_PREFIX = "cache:";

    /**
     * 在线用户 Token 过期时间（秒）
     * 默认 30 分钟
     */
    public static final long ONLINE_TOKEN_EXPIRE_SECONDS = 30 * 60;

    /**
     * Token 黑名单过期时间（秒）
     * 默认与 Token 过期时间一致
     */
    public static final long BLACKLISTED_TOKEN_EXPIRE_SECONDS = 2 * 60 * 60; // 2小时

    private RedisConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
