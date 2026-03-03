package me.fjq.security;

import lombok.extern.slf4j.Slf4j;
import me.fjq.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录尝试服务
 * <p>
 * 实现登录失败次数限制和账户锁定功能：
 * - 连续失败 5 次后锁定账户
 * - 锁定时间 30 分钟
 * - 基于 IP + 用户名组合限制
 * </p>
 *
 * @author fjq
 */
@Slf4j
@Service
public class LoginAttemptService {

    /**
     * 最大尝试次数
     */
    private static final int MAX_ATTEMPTS = 5;

    /**
     * 锁定时间（分钟）
     */
    private static final long LOCK_TIME_MINUTES = 30;

    /**
     * 尝试次数缓存 Key 前缀
     */
    private static final String ATTEMPT_PREFIX = "login:attempt:";

    /**
     * 锁定状态缓存 Key 前缀
     */
    private static final String LOCK_PREFIX = "login:lock:";

    private final RedisUtils redisUtils;

    public LoginAttemptService(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    /**
     * 记录登录失败
     *
     * @param username 用户名
     * @param ip       客户端 IP
     */
    public void loginFailed(String username, String ip) {
        String key = getKey(ATTEMPT_PREFIX, username, ip);
        Long attempts = redisUtils.incr(key, 1);

        // 首次失败时设置过期时间
        if (attempts != null && attempts == 1) {
            redisUtils.expire(key, LOCK_TIME_MINUTES, TimeUnit.MINUTES);
        }

        log.warn("登录失败 - 用户: {}, IP: {}, 已失败 {} 次", username, ip, attempts);

        // 达到最大尝试次数，锁定账户
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            lockAccount(username, ip);
            log.warn("账户已锁定 - 用户: {}, IP: {}, 锁定时间: {} 分钟", username, ip, LOCK_TIME_MINUTES);
        }
    }

    /**
     * 锁定账户
     *
     * @param username 用户名
     * @param ip       客户端 IP
     */
    private void lockAccount(String username, String ip) {
        String lockKey = getKey(LOCK_PREFIX, username, ip);
        redisUtils.set(lockKey, "1", LOCK_TIME_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 检查账户是否被锁定
     *
     * @param username 用户名
     * @param ip       客户端 IP
     * @return true 表示已锁定
     */
    public boolean isLocked(String username, String ip) {
        String lockKey = getKey(LOCK_PREFIX, username, ip);
        return redisUtils.hasKey(lockKey);
    }

    /**
     * 登录成功，清除失败记录
     *
     * @param username 用户名
     * @param ip       客户端 IP
     */
    public void loginSucceeded(String username, String ip) {
        String key = getKey(ATTEMPT_PREFIX, username, ip);
        redisUtils.del(key);
        log.info("登录成功，清除失败记录 - 用户: {}, IP: {}", username, ip);
    }

    /**
     * 获取剩余尝试次数
     *
     * @param username 用户名
     * @param ip       客户端 IP
     * @return 剩余尝试次数
     */
    public int getRemainingAttempts(String username, String ip) {
        String key = getKey(ATTEMPT_PREFIX, username, ip);
        Object attempts = redisUtils.get(key);

        if (attempts == null) {
            return MAX_ATTEMPTS;
        }

        int failedAttempts = Integer.parseInt(attempts.toString());
        return Math.max(0, MAX_ATTEMPTS - failedAttempts);
    }

    /**
     * 获取锁定剩余时间（秒）
     *
     * @param username 用户名
     * @param ip       客户端 IP
     * @return 锁定剩余时间（秒），未锁定返回 0
     */
    public long getLockRemainingTime(String username, String ip) {
        String lockKey = getKey(LOCK_PREFIX, username, ip);
        Long ttl = redisUtils.getExpire(lockKey);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * 生成缓存 Key
     */
    private String getKey(String prefix, String username, String ip) {
        return prefix + username + ":" + ip;
    }
}
